package com.ticketbooking.payment.domain.exception;

import com.ticketbooking.common.exception.DomainException;
import com.ticketbooking.payment.domain.entity.PaymentStatus;
import java.util.UUID;

/**
 * Thrown when an illegal lifecycle state transition is requested on a Payment.
 */
public class InvalidPaymentStateException extends DomainException {

    public InvalidPaymentStateException(UUID paymentId, PaymentStatus currentStatus, PaymentStatus targetStatus) {
        super(
                "INVALID_PAYMENT_STATE",
                String.format("Payment ID %s cannot transition from %s to %s", paymentId, currentStatus, targetStatus));
    }
}
