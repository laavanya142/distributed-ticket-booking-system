package com.ticketbooking.booking.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a payment charge or refund operation fails.
 */
public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(UUID bookingId, String reason) {
        super(String.format("Payment operation failed for booking %s: %s", bookingId, reason));
    }
}
