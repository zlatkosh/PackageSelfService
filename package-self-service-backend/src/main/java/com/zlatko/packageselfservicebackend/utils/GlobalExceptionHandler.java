package com.zlatko.packageselfservicebackend.utils;

import com.zlatko.packageselfservicebackend.model.dtos.errors.Error;
import com.zlatko.packageselfservicebackend.model.dtos.errors.ValidationError;
import com.zlatko.packageselfservicebackend.model.exceptions.DuplicatePackageNameException;
import com.zlatko.packageselfservicebackend.model.exceptions.PackageNotFoundException;
import com.zlatko.packageselfservicebackend.model.exceptions.RecipientNotFoundException;
import com.zlatko.packageselfservicebackend.model.exceptions.SenderNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles the DuplicatePackageNameException and returns a 409 Conflict response.
     *
     * @param ex The exception that was thrown
     * @return The response entity with the error message
     */
    @ExceptionHandler(DuplicatePackageNameException.class)
    public ResponseEntity<Error> handleDuplicatePackageName(
            DuplicatePackageNameException ex) {

        Error error = new Error(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                null
        );

        log.trace("Returning 409 Conflict response: {}", error);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    /**
     * Handles the MethodArgumentNotValidException and returns a 400 Bad Request response.
     * @param ex The exception that was thrown
     * @return The response entity with the error message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Error> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        List<ValidationError> validationErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        // Add global errors to the list of validation errors
        validationErrors.addAll(ex.getBindingResult().getGlobalErrors()
                .stream()
                .map(error -> new ValidationError(error.getObjectName(), error.getDefaultMessage()))
                .toList());

        Error error = new Error(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid input data",
                validationErrors
        );

        log.trace("Returning 400 Bad Request response: {}", error);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    /**
     * Handles the MethodArgumentNotValidException and returns a 400 Bad Request response.
     * @param ex The exception that was thrown
     * @return The response entity with the error message
     */
    @ExceptionHandler({RecipientNotFoundException.class, SenderNotFoundException.class, PackageNotFoundException.class})
    public ResponseEntity<Error> handleValidationExceptions(
            RuntimeException ex) {

        Error error = new Error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null
        );

        log.trace("Returning 400 Bad Request response: {}", error);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    /**
     * Handles all other exceptions and returns a 500 Internal Server Error response.
     * @param ex The exception that was thrown
     * @return The response entity with the error message
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Error> handleGenericException(
            Exception ex) {

        Error error = new Error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                null
        );

        log.error("Returning 500 Internal Server Error response: {}", error, ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
}