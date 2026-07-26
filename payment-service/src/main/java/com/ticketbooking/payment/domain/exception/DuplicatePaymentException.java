package com.ticketbooking.payment.domain.exception;

import com.ticketbooking.common.exception.DomainException;
import java.util.UUID;

/**
 * Thrown when attempting to initiate a duplicate payment for an already captured booking.
 */
public class DuplicatePaymentException extends DomainException {

    public DuplicatePaymentException(UUID bookingId) {
        super("DUPLICATE_PAYMENT", "A payment has already been successfully captured for booking ID " + bookingId);
    }
}
