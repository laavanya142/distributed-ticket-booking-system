package com.ticketbooking.analytics.interfaces.rest;

import com.ticketbooking.common.constants.HeaderConstants;
import com.ticketbooking.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Service-specific exception handler mapping unhandled exceptions to standard ErrorResponse payloads.
 */
@Slf4j
@RestControllerAdvice
@Order(1)
public class AnalyticsExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error in Analytics Service", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred in analytics service",
                        getCorrelationId(request)));
    }

    private String getCorrelationId(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HeaderConstants.CORRELATION_ID))
                .or(() -> Optional.ofNullable(MDC.get("correlationId")))
                .orElse("unknown-trace");
    }
}
