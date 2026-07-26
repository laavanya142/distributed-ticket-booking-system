package com.ticketbooking.notification.interfaces.rest;

import com.ticketbooking.common.constants.HeaderConstants;
import com.ticketbooking.common.dto.ErrorResponse;
import com.ticketbooking.notification.domain.exception.DuplicateNotificationException;
import com.ticketbooking.notification.domain.exception.NotificationNotFoundException;
import com.ticketbooking.notification.domain.exception.NotificationProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler mapping notification domain and validation exceptions to standard ErrorResponse payloads.
 */
@Slf4j
@RestControllerAdvice
@Order(1)
public class NotificationExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotificationNotFoundException ex, HttpServletRequest request) {
        log.warn("Notification not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("NOTIFICATION_NOT_FOUND", ex.getMessage(), getCorrelationId(request)));
    }

    @ExceptionHandler(DuplicateNotificationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateNotificationException ex, HttpServletRequest request) {
        log.warn("Duplicate notification event: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("DUPLICATE_NOTIFICATION", ex.getMessage(), getCorrelationId(request)));
    }

    @ExceptionHandler(NotificationProcessingException.class)
    public ResponseEntity<ErrorResponse> handleProcessingFailed(
            NotificationProcessingException ex, HttpServletRequest request) {
        log.warn("Notification processing failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("NOTIFICATION_PROCESSING_FAILED", ex.getMessage(), getCorrelationId(request)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed for request: {}", request.getRequestURI());
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        "VALIDATION_ERROR", "Request validation failed", details, getCorrelationId(request)));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_ERROR", ex.getMessage(), getCorrelationId(request)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error in Notification Service", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        "INTERNAL_SERVER_ERROR", "An unexpected error occurred", getCorrelationId(request)));
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
