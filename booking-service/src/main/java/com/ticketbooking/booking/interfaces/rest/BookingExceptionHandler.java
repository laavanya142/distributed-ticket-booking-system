package com.ticketbooking.booking.interfaces.rest;

import com.ticketbooking.booking.domain.exception.BookingAccessDeniedException;
import com.ticketbooking.booking.domain.exception.BookingNotFoundException;
import com.ticketbooking.booking.domain.exception.BookingStateTransitionException;
import com.ticketbooking.booking.domain.exception.DuplicateBookingException;
import com.ticketbooking.booking.domain.exception.PaymentFailedException;
import com.ticketbooking.booking.domain.exception.SeatVerificationFailedException;
import com.ticketbooking.common.constants.HeaderConstants;
import com.ticketbooking.common.dto.ErrorResponse;
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
public class BookingExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(BookingNotFoundException ex, HttpServletRequest request) {
        log.warn("Booking not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("BOOKING_NOT_FOUND", ex.getMessage(), getCorrelationId(request)));
    }

    @ExceptionHandler(BookingAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            BookingAccessDeniedException ex, HttpServletRequest request) {
        log.warn("Booking access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("ACCESS_DENIED", ex.getMessage(), getCorrelationId(request)));
    }

    @ExceptionHandler(DuplicateBookingException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateBooking(
            DuplicateBookingException ex, HttpServletRequest request) {
        log.warn("Duplicate active booking conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("DUPLICATE_ACTIVE_BOOKING", ex.getMessage(), getCorrelationId(request)));
    }

    @ExceptionHandler(SeatVerificationFailedException.class)
    public ResponseEntity<ErrorResponse> handleSeatVerificationFailed(
            SeatVerificationFailedException ex, HttpServletRequest request) {
        log.warn("Seat verification failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("SEATS_UNAVAILABLE", ex.getMessage(), getCorrelationId(request)));
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentFailed(PaymentFailedException ex, HttpServletRequest request) {
        log.warn("Payment charge failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(ErrorResponse.of("PAYMENT_FAILED", ex.getMessage(), getCorrelationId(request)));
    }

    @ExceptionHandler(BookingStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleStateTransition(
            BookingStateTransitionException ex, HttpServletRequest request) {
        log.warn("Invalid booking state transition: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("INVALID_BOOKING_STATE", ex.getMessage(), getCorrelationId(request)));
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
        log.error("Unexpected error during booking request processing", ex);
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
