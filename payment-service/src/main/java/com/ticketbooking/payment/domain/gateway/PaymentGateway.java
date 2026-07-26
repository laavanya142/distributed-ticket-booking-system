package com.ticketbooking.payment.domain.gateway;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Interface contract for external payment gateway provider interactions.
 */
public interface PaymentGateway {

    /**
     * Charges a specified payment method synchronously.
     *
     * @param paymentId Unique payment entity reference ID.
     * @param amount Charge amount.
     * @param currency Currency ISO code.
     * @param paymentMethodId Payment method reference.
     * @return PaymentGatewayResult detailing charge success or failure.
     */
    PaymentGatewayResult processCharge(UUID paymentId, BigDecimal amount, String currency, UUID paymentMethodId);

    /**
     * Refunds an existing captured transaction synchronously.
     *
     * @param paymentId Unique payment entity reference ID.
     * @param providerTransactionId Original provider capture transaction ID.
     * @param amount Refund amount.
     * @param currency Currency ISO code.
     * @return RefundGatewayResult detailing refund outcome.
     */
    RefundGatewayResult processRefund(UUID paymentId, String providerTransactionId, BigDecimal amount, String currency);
}
