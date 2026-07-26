package com.ticketbooking.seat.interfaces.rest;

import com.ticketbooking.common.dto.ErrorResponse;
import com.ticketbooking.seat.domain.exception.InvalidSeatLockException;
import com.ticketbooking.seat.domain.exception.SeatAlreadyLockedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Seat-service-specific exception handler that maps seat locking domain exceptions to HTTP 409 Conflict.
 * Declared with higher precedence than the common GlobalExceptionHandler so these handlers
 * are evaluated first and the common handler remains a catch-all fallback.
 */
@Slf4j
@RestControllerAdvice
@Order(1)
public class SeatExceptionHandler {

    /**
     * Maps SeatAlreadyLockedException to 409 Conflict.
     * Triggered when one or more requested seats are already locked or booked by another client.
     *
     * @param ex The SeatAlreadyLockedException.
     * @param request The HTTP request.
     * @return A 409 Conflict ErrorResponse.
     */
    @ExceptionHandler(SeatAlreadyLockedException.class)
    public ResponseEntity<ErrorResponse> handleSeatAlreadyLocked(
            SeatAlreadyLockedException ex, HttpServletRequest request) {
        log.warn("Seat lock conflict: [{}] {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), null));
    }

    /**
     * Maps InvalidSeatLockException to 409 Conflict.
     * Triggered when the provided lockToken does not match the stored ownership token.
     *
     * @param ex The InvalidSeatLockException.
     * @param request The HTTP request.
     * @return A 409 Conflict ErrorResponse.
     */
    @ExceptionHandler(InvalidSeatLockException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSeatLock(
            InvalidSeatLockException ex, HttpServletRequest request) {
        log.warn("Invalid seat lock token: [{}] {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), null));
    }

    /**
     * Maps IllegalArgumentException to 400 Bad Request.
     * Triggered by batch size violations (e.g. more than 10 seats in a single lock request).
     *
     * @param ex The IllegalArgumentException.
     * @param request The HTTP request.
     * @return A 400 Bad Request ErrorResponse.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument in seat request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INVALID_REQUEST", ex.getMessage(), null));
    }
}
