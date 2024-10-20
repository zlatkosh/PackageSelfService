package com.zlatko.packageselfservicebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zlatko.packageselfservicebackend.controllers.PackageSelfServiceController;
import com.zlatko.packageselfservicebackend.model.dtos.Package;
import com.zlatko.packageselfservicebackend.model.dtos.PackageDetails;
import com.zlatko.packageselfservicebackend.model.dtos.RecipientDetails;
import com.zlatko.packageselfservicebackend.model.dtos.enums.PackageStatus;
import com.zlatko.packageselfservicebackend.model.exceptions.DuplicatePackageNameException;
import com.zlatko.packageselfservicebackend.services.PackageSelfServiceService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@WebMvcTest(PackageSelfServiceController.class)
class PackageSelfServiceControllerTest {
    private final static ObjectMapper jackson = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PackageSelfServiceService service;

    @Nested
    class SubmitPackageTests {

        @SneakyThrows
        @Test
        void should_return_201_when_package_is_submitted_successfully() {
            // Given
            Package packageDTO = initValidPackage();
            UUID submittedPackageId = UUID.randomUUID();
            when(service.submitPackage(any(Package.class))).thenReturn(submittedPackageId);

            // When + Then
            mockMvc.perform(post("/api/package-self-service")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jackson.writeValueAsString(packageDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "http://localhost/api/package-self-service/" + submittedPackageId));

            verify(service).submitPackage(any(Package.class));  // Verify service method was called
        }

        @SneakyThrows
        @Test
        void should_return_400_when_invalid_package_data_is_provided() {
            // Given
            String invalidPackageJson = "{}"; // Simulate invalid package data

            // When + Then
            mockMvc.perform(post("/api/package-self-service")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidPackageJson))
                    .andExpect(status().isBadRequest()); // Expect 400 Bad Request for invalid data
        }

        @SneakyThrows
        @Test
        void should_return_500_when_service_throws_runtime_exception() {
            // Given
            Package packageDTO = initValidPackage();
            when(service.submitPackage(any(Package.class))).thenThrow(new RuntimeException("Service error"));

            // When + Then
            mockMvc.perform(post("/api/package-self-service")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jackson.writeValueAsString(packageDTO)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

            verify(service).submitPackage(any(Package.class));  // Ensure service was called
        }

        @SneakyThrows
        @Test
        void should_return_409_when_service_throws_DuplicatePackageNameException() {
            // Given
            Package packageDTO = initValidPackage();
            when(service.submitPackage(any(Package.class))).thenThrow(new DuplicatePackageNameException(packageDTO.packageName()));

            // When + Then
            mockMvc.perform(post("/api/package-self-service")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jackson.writeValueAsString(packageDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                    .andExpect(jsonPath("$.message").value("Package name '%s' already exists. Please provide a unique name.".formatted(packageDTO.packageName())));

            verify(service).submitPackage(any(Package.class));  // Ensure service was called
        }
    }

    @Nested
    class GetPackageDetailsTests {

        @SneakyThrows
        @Test
        void should_return_200_and_package_details_when_valid_packageId_is_provided() {
            // Given
            String packageId = UUID.randomUUID().toString();
            String senderId = UUID.randomUUID().toString();
            PackageDetails packageDetails = initDummyPackageDetails(packageId);
            when(service.getPackageDetails(anyString(), anyString())).thenReturn(packageDetails);

            // When + Then
            mockMvc.perform(get("/api/package-self-service/{packageId}", packageId)
                            .param("senderId", senderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.packageId").value(packageId))
                    .andExpect(jsonPath("$.packageName").value("package1"))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));


            verify(service).getPackageDetails(packageId, senderId);
        }

        @SneakyThrows
        @Test
        void should_return_400_when_invalid_senderId_and_invalid_packageId_is_provided() {
            // Given
            String invalidSenderId = "invalid-sender-id";
            String invalidPackageId = "invalid-package-id";

            // When + Then
            mockMvc.perform(get("/api/package-self-service/{packageId}", invalidPackageId)
                            .param("senderId", invalidSenderId)) // Pass invalid senderId
                    .andExpect(status().isBadRequest());  // Expect 400 Bad Request
        }

        @SneakyThrows
        @Test
        void should_return_500_when_service_throws_runtime_exception() {
            // Given
            String senderId = UUID.randomUUID().toString();
            String packageId = UUID.randomUUID().toString();
            when(service.getPackageDetails(anyString(), anyString())).thenThrow(new RuntimeException("Service error"));

            // When + Then
            mockMvc.perform(get("/api/package-self-service/{packageId}", packageId)
                            .param("senderId", senderId))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

            verify(service).getPackageDetails(packageId, senderId);  // Verify service method was called
        }
    }

    @Nested
    class ListPackageDetailsTests {

        @SneakyThrows
        @Test
        void should_return_200_and_list_of_package_details_when_valid_senderId_is_provided() {
            // Given
            String senderId = UUID.randomUUID().toString();
            List<PackageDetails> packageDetailsList = List.of(initDummyPackageDetails(UUID.randomUUID().toString()));
            when(service.listPackageDetails(anyString(), any())).thenReturn(packageDetailsList);

            // When + Then
            mockMvc.perform(get("/api/package-self-service")
                            .param("senderId", senderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(packageDetailsList.size()));

            verify(service).listPackageDetails(senderId, Optional.empty());
        }

        @SneakyThrows
        @Test
        void should_filter_by_status_when_status_is_provided() {
            // Given
            String senderId = UUID.randomUUID().toString();
            Optional<PackageStatus> status = Optional.of(PackageStatus.DELIVERED);
            List<PackageDetails> filteredPackages = List.of(initDummyPackageDetails(UUID.randomUUID().toString()));
            when(service.listPackageDetails(anyString(), eq(status))).thenReturn(filteredPackages);

            // When + Then
            mockMvc.perform(get("/api/package-self-service")
                            .param("senderId", senderId)
                            .param("status", "DELIVERED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(filteredPackages.size()));

            verify(service).listPackageDetails(senderId, status);
        }

        @SneakyThrows
        @Test
        void should_return_500_when_service_throws_runtime_exception() {
            // Given
            String senderId = UUID.randomUUID().toString();
            when(service.listPackageDetails(anyString(), any())).thenThrow(new RuntimeException("Service error"));

            // When + Then
            mockMvc.perform(get("/api/package-self-service")
                            .param("senderId", senderId))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

            verify(service).listPackageDetails(senderId, Optional.empty());
        }
    }

    /**
     * Helper method for initializing a valid Package object
     * @return A valid Package object
     */
    private Package initValidPackage() {
        return new Package(
                "package1",
                1000,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
    }

    /**
     * Helper method for initializing a dummy PackageDetails object
     * @param packageId The package ID
     * @return A dummy PackageDetails object
     */
    private PackageDetails initDummyPackageDetails(String packageId) {
        return new PackageDetails(
                packageId,
                "package1",
                LocalDateTime.now(),
                PackageStatus.IN_PROGRESS,
                LocalDate.now(),
                LocalDateTime.now(),
                new RecipientDetails(
                        UUID.randomUUID().toString(),
                        "John Doe",
                        "123 Main St, 12345 - Springfield, IL - USA"
                )
        );
    }
}
