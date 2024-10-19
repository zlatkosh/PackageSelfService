package com.zlatko.packageselfservicebackend.model.dtos;

import com.zlatko.packageselfservicebackend.model.dtos.annotations.SenderIsNotRecipient;
import com.zlatko.packageselfservicebackend.utils.GlobalConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// Custom validation annotation, makes sure that the sender and recipient are not the same person
@SenderIsNotRecipient
public record Package(
        @NotBlank(message = "Package name is required.") String packageName,
        @NotNull(message = "Package weight is required.")
        @Min(value = 1, message = "Package weight must be greater than 0.") int weightInGrams,
        @Pattern(regexp = GlobalConstants.UUID_REGEX_PATTERN, message = "Invalid recipientId format.")
        @NotBlank(message = "Recipient ID is required.") String recipientId,
        @Pattern(regexp = GlobalConstants.UUID_REGEX_PATTERN, message = "Invalid senderId format.")
        @NotBlank(message = "Sender ID is required.") String senderId) {
}
