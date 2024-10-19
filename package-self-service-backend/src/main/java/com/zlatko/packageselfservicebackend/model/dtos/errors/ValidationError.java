package com.zlatko.packageselfservicebackend.model.dtos.errors;

public record ValidationError(String field, String message) {
}
