package com.zlatko.packageselfservicebackend.model.exceptions;

public class PackageNotFoundException extends RuntimeException {
    public PackageNotFoundException(String packageId, String senderId) {
        super("Package with ID '%s' not found, for sender '%s'.".formatted(packageId, senderId));
    }
}
