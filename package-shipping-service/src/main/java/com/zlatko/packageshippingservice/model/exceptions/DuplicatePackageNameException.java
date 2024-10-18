package com.zlatko.packageshippingservice.model.exceptions;

public class DuplicatePackageNameException extends RuntimeException {
    public DuplicatePackageNameException(String message) {
        super(message);
    }
}