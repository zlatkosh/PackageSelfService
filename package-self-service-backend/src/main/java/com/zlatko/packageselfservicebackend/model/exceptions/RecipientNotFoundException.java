package com.zlatko.packageselfservicebackend.model.exceptions;

public class RecipientNotFoundException extends RuntimeException {
    public RecipientNotFoundException(String recipientId) {
        super("Recipient with ID '%s' not found.".formatted(recipientId));
    }
}
