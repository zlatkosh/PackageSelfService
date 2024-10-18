package com.zlatko.packageshippingservice.model.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ShippingOrder(
        @NotBlank(message = "Package name is required.") String packageName,
        @NotBlank(message = "Postal code is required.") String postalCode,
        @NotBlank(message = "Street name is required.") String streetName,
        @NotBlank(message = "Receiver name is required.") String receiverName,
        @Pattern(regexp = "^(S|M|L|XL)$", message = "Package size must be one of: S, M, L, XL.")
        @NotBlank(message = "Package size is required.")  String packageSize
) {}