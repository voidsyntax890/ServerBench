package com.serverbench.backend.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ApiError {

    private final String code;
    private final String message;
    private final String requestId;
    private final LocalDateTime timestamp;
    private final Map<String, String> fieldErrors;

    public ApiError(
            String code,
            String message,
            String requestId,
            LocalDateTime timestamp,
            Map<String, String> fieldErrors
    ) {

        this.code = code;
        this.message = message;
        this.requestId = requestId;
        this.timestamp = timestamp;
        this.fieldErrors = fieldErrors;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getRequestId() {
        return requestId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}