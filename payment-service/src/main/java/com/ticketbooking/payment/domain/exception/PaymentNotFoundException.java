package com.ticketbooking.payment.domain.exception;

import com.ticketbooking.common.exception.ResourceNotFoundException;
import java.util.UUID;

/**
 * Thrown when a requested payment aggregate cannot be found.
 */
public class PaymentNotFoundException extends ResourceNotFoundException {

    public PaymentNotFoundException(UUID paymentId) {
        super("PAYMENT_NOT_FOUND", "Payment with ID " + paymentId + " was not found");
    }

    public PaymentNotFoundException(String message) {
        super("PAYMENT_NOT_FOUND", message);
    }
}
