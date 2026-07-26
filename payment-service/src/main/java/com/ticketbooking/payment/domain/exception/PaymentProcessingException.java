package com.ticketbooking.payment.domain.exception;

import com.ticketbooking.common.exception.DomainException;

/**
 * Thrown when a payment charge fails downstream at the payment gateway.
 */
public class PaymentProcessingException extends DomainException {

    public PaymentProcessingException(String reason) {
        super("PAYMENT_FAILED", "Payment charge failed: " + reason);
    }
}
