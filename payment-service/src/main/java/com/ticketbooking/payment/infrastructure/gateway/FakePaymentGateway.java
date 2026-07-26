package com.ticketbooking.payment.infrastructure.gateway;

import com.ticketbooking.payment.domain.exception.PaymentProcessingException;
import com.ticketbooking.payment.domain.gateway.PaymentGateway;
import com.ticketbooking.payment.domain.gateway.PaymentGatewayResult;
import com.ticketbooking.payment.domain.gateway.RefundGatewayResult;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mock implementation of PaymentGateway supporting chaos injection and deterministic failure flags for testing.
 */
@Component
@Slf4j
public class FakePaymentGateway implements PaymentGateway {

    public static final UUID FAIL_PAYMENT_METHOD_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID TIMEOUT_PAYMENT_METHOD_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    public static final BigDecimal FAIL_AMOUNT = new BigDecimal("999.99");

    @Override
    public PaymentGatewayResult processCharge(
            UUID paymentId, BigDecimal amount, String currency, UUID paymentMethodId) {
        log.info("Simulating payment gateway charge for paymentId: {}, amount: {} {}", paymentId, amount, currency);

        if (TIMEOUT_PAYMENT_METHOD_ID.equals(paymentMethodId)) {
            log.warn("Chaos simulation triggered: Gateway timeout for paymentId: {}", paymentId);
            throw new PaymentProcessingException("Gateway network timeout");
        }

        if (FAIL_PAYMENT_METHOD_ID.equals(paymentMethodId) || FAIL_AMOUNT.compareTo(amount) == 0) {
            log.warn("Simulating payment charge failure for paymentId: {}", paymentId);
            return PaymentGatewayResult.builder()
                    .successful(false)
                    .failureReason("CARD_DECLINED_INSUFFICIENT_FUNDS")
                    .status("FAILED")
                    .build();
        }

        String providerTxId =
                "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Payment charge succeeded for paymentId: {}. Issued providerTxId: {}", paymentId, providerTxId);

        return PaymentGatewayResult.builder()
                .successful(true)
                .providerTransactionId(providerTxId)
                .status("CAPTURED")
                .build();
    }

    @Override
    public RefundGatewayResult processRefund(
            UUID paymentId, String providerTransactionId, BigDecimal amount, String currency) {
        log.info(
                "Simulating payment gateway refund for paymentId: {}, providerTxId: {}",
                paymentId,
                providerTransactionId);

        if (FAIL_AMOUNT.compareTo(amount) == 0) {
            log.warn("Simulating refund failure for paymentId: {}", paymentId);
            return RefundGatewayResult.builder()
                    .successful(false)
                    .failureReason("REFUND_DECLINED_BY_BANK")
                    .build();
        }

        String refundTxId = "RF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Payment refund succeeded for paymentId: {}. Issued refundTxId: {}", paymentId, refundTxId);

        return RefundGatewayResult.builder()
                .successful(true)
                .refundTransactionId(refundTxId)
                .build();
    }
}
