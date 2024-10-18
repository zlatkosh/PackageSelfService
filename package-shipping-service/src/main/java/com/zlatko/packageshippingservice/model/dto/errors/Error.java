package com.zlatko.packageshippingservice.model.dto.errors;

import java.util.List;

public record Error (int status, String message, List<ValidationError> errors) {
}
