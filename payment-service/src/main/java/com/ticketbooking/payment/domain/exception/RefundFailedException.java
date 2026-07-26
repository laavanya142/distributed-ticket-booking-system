package com.ticketbooking.payment.domain.exception;

import com.ticketbooking.common.exception.DomainException;

/**
 * Thrown when a payment refund operation fails at the payment gateway.
 */
public class RefundFailedException extends DomainException {

    public RefundFailedException(String reason) {
        super("REFUND_FAILED", "Payment refund failed: " + reason);
    }
}
