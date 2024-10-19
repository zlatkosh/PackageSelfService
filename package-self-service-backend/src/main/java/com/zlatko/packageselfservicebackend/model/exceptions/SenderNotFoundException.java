package com.zlatko.packageselfservicebackend.model.exceptions;

public class SenderNotFoundException extends RuntimeException {
    public SenderNotFoundException(String senderId) {
        super("Sender with ID '%s' not found.".formatted(senderId));
    }
}
