package com.ticketbooking.booking.infrastructure.client.payment;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Interface defining synchronous REST operations against Payment Service.
 */
public interface PaymentServiceClient {

    /**
     * Charges a payment for checkout.
     *
     * @param bookingId Booking identifier.
     * @param userId Owning user identifier.
     * @param paymentMethodId Payment method identifier.
     * @param amount Total amount to charge.
     * @param currency Currency code.
     * @param idempotencyKey Deduplication key.
     * @return Result of the charge operation.
     */
    PaymentChargeResult chargePayment(
            UUID bookingId,
            UUID userId,
            UUID paymentMethodId,
            BigDecimal amount,
            String currency,
            String idempotencyKey);

    /**
     * Refunds a previously captured payment.
     *
     * @param paymentId Payment reference identifier.
     * @param bookingId Booking identifier.
     * @param userId Owning user identifier.
     * @param reason Cancellation/refund reason.
     * @param idempotencyKey Deduplication key.
     * @return Result of the refund operation.
     */
    PaymentRefundResult refundPayment(
            UUID paymentId, UUID bookingId, UUID userId, String reason, String idempotencyKey);
}
