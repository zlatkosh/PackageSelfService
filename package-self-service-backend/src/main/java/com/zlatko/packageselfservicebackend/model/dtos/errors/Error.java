package com.zlatko.packageselfservicebackend.model.dtos.errors;

import java.util.List;

public record Error (int status, String message, List<ValidationError> errors) {
}
