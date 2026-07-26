package com.ticketbooking.payment.infrastructure.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ticketbooking.payment.domain.exception.PaymentProcessingException;
import com.ticketbooking.payment.domain.gateway.PaymentGatewayResult;
import com.ticketbooking.payment.domain.gateway.RefundGatewayResult;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FakePaymentGatewayTest {

    private FakePaymentGateway fakePaymentGateway;

    @BeforeEach
    void setUp() {
        fakePaymentGateway = new FakePaymentGateway();
    }

    @Test
    @DisplayName("Successful Charge: Returns provider transaction ID and CAPTURED status")
    void testSuccessfulCharge() {
        UUID paymentId = UUID.randomUUID();
        UUID methodId = UUID.randomUUID();

        PaymentGatewayResult result =
                fakePaymentGateway.processCharge(paymentId, new BigDecimal("100.00"), "USD", methodId);

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getStatus()).isEqualTo("CAPTURED");
        assertThat(result.getProviderTransactionId()).startsWith("TX-");
    }

    @Test
    @DisplayName("Declined Payment: Returns failure for FAIL_PAYMENT_METHOD_ID or FAIL_AMOUNT")
    void testDeclinedPayment() {
        UUID paymentId = UUID.randomUUID();

        PaymentGatewayResult resultByMethod = fakePaymentGateway.processCharge(
                paymentId, new BigDecimal("100.00"), "USD", FakePaymentGateway.FAIL_PAYMENT_METHOD_ID);

        assertThat(resultByMethod.isSuccessful()).isFalse();
        assertThat(resultByMethod.getFailureReason()).contains("CARD_DECLINED");

        PaymentGatewayResult resultByAmount =
                fakePaymentGateway.processCharge(paymentId, FakePaymentGateway.FAIL_AMOUNT, "USD", UUID.randomUUID());

        assertThat(resultByAmount.isSuccessful()).isFalse();
        assertThat(resultByAmount.getFailureReason()).contains("CARD_DECLINED");
    }

    @Test
    @DisplayName("Gateway Timeout: Throws PaymentProcessingException for TIMEOUT_PAYMENT_METHOD_ID")
    void testGatewayTimeout() {
        UUID paymentId = UUID.randomUUID();

        assertThatThrownBy(() -> fakePaymentGateway.processCharge(
                        paymentId, new BigDecimal("100.00"), "USD", FakePaymentGateway.TIMEOUT_PAYMENT_METHOD_ID))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("Gateway network timeout");
    }

    @Test
    @DisplayName("Successful Refund: Returns refund transaction ID")
    void testSuccessfulRefund() {
        UUID paymentId = UUID.randomUUID();
        String providerTxId = "TX-12345678";

        RefundGatewayResult result =
                fakePaymentGateway.processRefund(paymentId, providerTxId, new BigDecimal("100.00"), "USD");

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getRefundTransactionId()).startsWith("RF-");
    }

    @Test
    @DisplayName("Refund Failure: Returns failure reason for FAIL_AMOUNT")
    void testRefundFailure() {
        UUID paymentId = UUID.randomUUID();
        String providerTxId = "TX-12345678";

        RefundGatewayResult result =
                fakePaymentGateway.processRefund(paymentId, providerTxId, FakePaymentGateway.FAIL_AMOUNT, "USD");

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getFailureReason()).contains("REFUND_DECLINED");
    }
}
