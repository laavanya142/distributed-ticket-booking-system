package com.ticketbooking.common.dto;

import java.time.Instant;

/**
 * Standard API response wrapper for consistent JSON payloads across all microservices.
 *
 * @param <T> The payload data type.
 */
public record ApiResponse<T>(boolean success, String message, T data, Instant timestamp, String correlationId) {
    /**
     * Creates a successful API response with data.
     *
     * @param data The payload data.
     * @param correlationId The current trace correlation ID.
     * @param <T> The data type.
     * @return A successful ApiResponse.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, Instant.now(), null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now(), null);
    }

    public static <T> ApiResponse<T> success(T data, String correlationId) {
        return new ApiResponse<>(true, "Success", data, Instant.now(), correlationId);
    }

    public static <T> ApiResponse<T> success(String message, T data, String correlationId) {
        return new ApiResponse<>(true, message, data, Instant.now(), correlationId);
    }
}
