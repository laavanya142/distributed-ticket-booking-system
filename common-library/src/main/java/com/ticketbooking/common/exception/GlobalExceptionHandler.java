package com.ticketbooking.common.exception;

import com.ticketbooking.common.constants.HeaderConstants;
import com.ticketbooking.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for Servlet-based Spring Boot microservices.
 * Formats all unhandled exceptions into standardized ErrorResponse JSON payloads.
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {

    /**
     * Handles domain business rule exceptions.
     *
     * @param ex The DomainException.
     * @param request The HTTP request.
     * @return A 400 Bad Request or 404 Not Found response.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex, HttpServletRequest request) {
        log.warn("Domain exception occurred: [{}] {}", ex.getErrorCode(), ex.getMessage());
        HttpStatus status = (ex instanceof ResourceNotFoundException
                        || (ex.getErrorCode() != null && ex.getErrorCode().endsWith("_NOT_FOUND")))
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        String correlationId = getCorrelationId(request);
        return ResponseEntity.status(status).body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), correlationId));
    }

    /**
     * Handles Jakarta validation failures on request bodies.
     *
     * @param ex The MethodArgumentNotValidException.
     * @param request The HTTP request.
     * @return A 400 Bad Request response with detailed field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed for request: {}", request.getRequestURI());
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();
        String correlationId = getCorrelationId(request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_ERROR", "Request validation failed", details, correlationId));
    }

    /**
     * Handles all unexpected system or infrastructure exceptions.
     *
     * @param ex The Exception.
     * @param request The HTTP request.
     * @return A 500 Internal Server Error response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected exception occurred during request processing", ex);
        String correlationId = getCorrelationId(request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_SERVER_ERROR", "An unexpected error occurred", correlationId));
    }

    private String getCorrelationId(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HeaderConstants.CORRELATION_ID))
                .or(() -> Optional.ofNullable(MDC.get("correlationId")))
                .orElse("unknown-trace");
    }

    private String formatFieldError(FieldError error) {
        return String.format("%s: %s", error.getField(), error.getDefaultMessage());
    }
}
