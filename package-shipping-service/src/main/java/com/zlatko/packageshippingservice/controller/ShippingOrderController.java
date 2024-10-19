package com.zlatko.packageshippingservice.controller;

import com.zlatko.packageshippingservice.model.dto.ShippingOrder;
import com.zlatko.packageshippingservice.model.dto.ShippingOrderDetails;
import com.zlatko.packageshippingservice.model.enums.OrderStatus;
import com.zlatko.packageshippingservice.service.ShippingOrderService;
import com.zlatko.packageshippingservice.utils.GlobalConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/shippingOrders")
@RequiredArgsConstructor
@Slf4j
public class ShippingOrderController {
    private final ShippingOrderService shippingOrderService;

    @PostMapping
    public ResponseEntity<Void> createShippingOrder(@Valid @RequestBody ShippingOrder shippingOrderDTO, HttpServletRequest request) {

        UUID createdOrderId = shippingOrderService.createShippingOrder(shippingOrderDTO);
        log.trace("Created shipping order with ID: {}", createdOrderId);

        URI location = getOrderUri(request.getRequestURL().toString(), createdOrderId);

        return ResponseEntity.created(location) // Sets the 201 Created status and Location header
                .build(); // Returning an empty body with just the headers
    }

    /**
     * Construct the URI for the created order.
     *
     * @param requestUrl The URL of the request
     * @param createdOrderId The ID of the created order
     * @return The URI of the created order
     */
    private URI getOrderUri(String requestUrl, UUID createdOrderId) {
        // Construct the absolute URI for the created resource
        URI location = URI.create("%s/%s".formatted(requestUrl, createdOrderId)); // Append the new order ID

        log.trace("Setting Location header to: {}", location);
        return location;
    }


    @GetMapping
    public ResponseEntity<List<ShippingOrderDetails>> listOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int limit) {

        // Call the service to fetch orders, passing the parameters
        List<ShippingOrderDetails> orders = shippingOrderService.listShippingOrders(status, offset, limit);

        log.trace("Returning shipping orders {}", orders);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ShippingOrderDetails> getOrderDetails(
            @Pattern(regexp = GlobalConstants.UUID_REGEX_PATTERN, message = "Invalid orderId format.")
            @PathVariable String orderId) {
        Optional<ShippingOrderDetails> orderDetails = shippingOrderService.getOrderDetails(orderId);
        log.trace("Returning shipping order details {}", orderDetails);
        return orderDetails
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
