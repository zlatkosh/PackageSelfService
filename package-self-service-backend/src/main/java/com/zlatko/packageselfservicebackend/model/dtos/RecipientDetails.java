package com.zlatko.packageselfservicebackend.model.dtos;

public record RecipientDetails(
        String recipientId,
        String recipientName,
        String recipientAddress
) {
}
