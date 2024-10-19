package com.zlatko.packageselfservicebackend.model.exceptions;

public class DuplicatePackageNameException extends RuntimeException {
    public DuplicatePackageNameException(String packageName) {
        super("Package name '%s' already exists. Please provide a unique name.".formatted(packageName));
    }
}