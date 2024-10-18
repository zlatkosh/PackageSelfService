package com.zlatko.packageshippingservice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zlatko.packageshippingservice.controller.ShippingOrderController;
import com.zlatko.packageshippingservice.model.dto.ShippingOrder;
import com.zlatko.packageshippingservice.model.exceptions.DuplicatePackageNameException;
import com.zlatko.packageshippingservice.service.ShippingOrderService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShippingOrderController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShippingOrderService shippingOrderService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class HandleDuplicatePackageNameException {

        @Test
        @SneakyThrows
        void givenDuplicatePackageNameException_whenPostRequest_thenReturnsConflict() {
            // Given: A DuplicatePackageNameException is thrown by the service
            ShippingOrder shippingOrder = new ShippingOrder("Birthday Present", "1082PP", "Gustav Mahlerlaan 10", "Robert Swaak", "M");
            doThrow(new DuplicatePackageNameException("The selected packageName was already taken"))
                    .when(shippingOrderService)
                    .createShippingOrder(shippingOrder);

            // When: A POST request is made to /shippingOrders
            mockMvc.perform(post("/shippingOrders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(shippingOrder)))
                    // Then: Expect 409 Conflict
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status", is(409)))
                    .andExpect(jsonPath("$.message", is("The selected packageName was already taken")))
                    .andExpect(jsonPath("$.errors").doesNotExist());  // No validation errors
        }
    }

    @Nested
    class HandleValidationExceptions {

        @Test
        @SneakyThrows
        void givenInvalidRequest_whenPostRequest_thenReturnsBadRequest() {
            // Given: A POST request with invalid data (missing required fields)
            ShippingOrder invalidShippingOrder = new ShippingOrder("", "1234", "Gustav Mahlerlaan 10", "Robert Swaak", "M");

            // When: The request is made to /shippingOrders
            mockMvc.perform(post("/shippingOrders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidShippingOrder)))
                    // Then: Expect 400 Bad Request due to validation errors
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.message", is("Invalid input data")))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[0].field", is("packageName")))
                    .andExpect(jsonPath("$.errors[0].message", is("Package name is required.")));  // This depends on your validation message
        }
    }

    @Nested
    class HandleGenericException {

        @Test
        @SneakyThrows
        void givenRuntimeException_whenGetRequest_thenReturnsInternalServerError() {
            // Given: A GET request to /shippingOrders/{orderId} that triggers a RuntimeException
            String validUuid = "0f8f9e96-9ecb-431d-8f17-aedb8a9c15a5";

            // Mock the service to throw a RuntimeException
            doThrow(new RuntimeException("Unexpected error occurred"))
                    .when(shippingOrderService)
                    .getOrderDetails(validUuid);

            // When: The request is made
            mockMvc.perform(get("/shippingOrders/{orderId}", validUuid))
                    // Then: Expect 500 Internal Server Error
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status", is(500)))
                    .andExpect(jsonPath("$.message", is("An unexpected error occurred")))
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }
    }
}