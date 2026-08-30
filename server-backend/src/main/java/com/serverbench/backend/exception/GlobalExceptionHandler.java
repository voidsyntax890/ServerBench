package com.serverbench.backend.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ================================================================
    // VALIDATION ERRORS
    // ================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationError(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(
                        error ->
                                fieldErrors.put(
                                        error.getField(),
                                        error.getDefaultMessage()
                                )
                );

        String requestId =
                UUID.randomUUID().toString();

        ApiError error =
                new ApiError(
                        "VALIDATION_ERROR",
                        "The experiment configuration is invalid.",
                        requestId,
                        LocalDateTime.now(),
                        fieldErrors
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // ================================================================
    // ILLEGAL ARGUMENT
    // ================================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException exception
    ) {

        String requestId =
                UUID.randomUUID().toString();

        ApiError error =
                new ApiError(
                        "INVALID_REQUEST",
                        exception.getMessage(),
                        requestId,
                        LocalDateTime.now(),
                        Map.of()
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // ================================================================
    // ILLEGAL STATE
    // ================================================================

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(
            IllegalStateException exception
    ) {

        String requestId =
                UUID.randomUUID().toString();

        ApiError error =
                new ApiError(
                        "INVALID_OPERATION",
                        exception.getMessage(),
                        requestId,
                        LocalDateTime.now(),
                        Map.of()
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    // ================================================================
    // UNEXPECTED ERROR
    // ================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedError(
            Exception exception
    ) {


        String requestId =
                UUID.randomUUID().toString();

        ApiError error =
                new ApiError(
                        "INTERNAL_ERROR",
                        "An unexpected error occurred.",
                        requestId,
                        LocalDateTime.now(),
                        Map.of()
                );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}