package com.ticketbooking.common.dto;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response record for structured JSON error payloads across all services.
 *
 * @param success Always false for errors.
 * @param errorCode Domain or HTTP error code string.
 * @param message Human-readable error explanation.
 * @param details Optional list of detailed field errors or validation failures.
 * @param timestamp The exact time the error occurred.
 * @param correlationId The current trace correlation ID.
 */
public record ErrorResponse(
        boolean success,
        String errorCode,
        String message,
        List<String> details,
        Instant timestamp,
        String correlationId) {
    /**
     * Creates an ErrorResponse with default empty field details.
     *
     * @param errorCode The error code string.
     * @param message The error message.
     * @param correlationId The trace correlation ID.
     * @return A constructed ErrorResponse instance.
     */
    public static ErrorResponse of(String errorCode, String message, String correlationId) {
        return new ErrorResponse(false, errorCode, message, List.of(), Instant.now(), correlationId);
    }

    /**
     * Creates an ErrorResponse with detailed validation errors.
     *
     * @param errorCode The error code string.
     * @param message The error message.
     * @param details List of specific field errors.
     * @param correlationId The trace correlation ID.
     * @return A constructed ErrorResponse instance.
     */
    public static ErrorResponse of(String errorCode, String message, List<String> details, String correlationId) {
        return new ErrorResponse(false, errorCode, message, details, Instant.now(), correlationId);
    }
}
