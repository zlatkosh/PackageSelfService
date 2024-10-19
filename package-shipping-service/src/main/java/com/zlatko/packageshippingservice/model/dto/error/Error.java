package com.zlatko.packageshippingservice.model.dto.error;

import java.util.List;

public record Error (int status, String message, List<ValidationError> errors) {
}
