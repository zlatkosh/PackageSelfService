package com.zlatko.packageshippingservice.model.dto;

import com.zlatko.packageshippingservice.model.enums.OrderStatus;
import com.zlatko.packageshippingservice.model.enums.PackageSize;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShippingOrderDetails(
        @NotBlank(message = "Package ID is required.") String packageId,
        @NotBlank(message = "Package name is required.") String packageName,
        @NotBlank(message = "Package size is required.") PackageSize packageSize,
        @NotBlank(message = "Postal code is required.") String postalCode,
        @NotBlank(message = "Street name is required.") String streetName,
        @NotBlank(message = "Receiver name is required.") String receiverName,
        @NotBlank(message = "Order status is required.") OrderStatus orderStatus,
        @NotBlank(message = "Expected delivery date is required") LocalDate expectedDeliveryDate,
        LocalDateTime actualDeliveryDateTime
) {}