package com.ticketbooking.payment.interfaces.rest;

import com.ticketbooking.common.constants.HeaderConstants;
import com.ticketbooking.common.dto.ErrorResponse;
import com.ticketbooking.payment.domain.exception.DuplicatePaymentException;
import com.ticketbooking.payment.domain.exception.InvalidPaymentStateException;
import com.ticketbooking.payment.domain.exception.PaymentNotFoundException;
import com.ticketbooking.payment.domain.exception.PaymentProcessingException;
import com.ticketbooking.payment.domain.exception.RefundFailedException;
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
 * Service-specific exception handler mapping domain and validation exceptions to standard ErrorResponse payloads.
 */
@Slf4j
@RestControllerAdvice
@Order(1)
public class PaymentExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(PaymentNotFoundException ex, HttpServletRequest request) {
        log.warn("Payment not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("PAYMENT_NOT_FOUND", ex.getMessage(), getCorrelationId(request)));
    }

    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicatePaymentException ex, HttpServletRequest request) {
        log.warn("Duplicate payment conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("DUPLICATE_PAYMENT", ex.getMessage(), getCorrelationId(request)));
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handlePaymentProcessingFailed(
            PaymentProcessingException ex, HttpServletRequest request) {
        log.warn("Payment processing failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(ErrorResponse.of("PAYMENT_FAILED", ex.getMessage(), getCorrelationId(request)));
    }

    @ExceptionHandler(RefundFailedException.class)
    public ResponseEntity<ErrorResponse> handleRefundFailed(RefundFailedException ex, HttpServletRequest request) {
        log.warn("Refund operation failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("REFUND_FAILED", ex.getMessage(), getCorrelationId(request)));
    }

    @ExceptionHandler(InvalidPaymentStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(
            InvalidPaymentStateException ex, HttpServletRequest request) {
        log.warn("Invalid payment state transition: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("INVALID_PAYMENT_STATE", ex.getMessage(), getCorrelationId(request)));
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
        log.error("Unexpected error during payment processing", ex);
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
