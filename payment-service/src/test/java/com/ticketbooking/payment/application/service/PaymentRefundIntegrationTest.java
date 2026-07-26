package com.ticketbooking.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ticketbooking.payment.application.model.RefundPaymentCommand;
import com.ticketbooking.payment.domain.entity.OutboxEvent;
import com.ticketbooking.payment.domain.entity.Payment;
import com.ticketbooking.payment.domain.entity.PaymentStatus;
import com.ticketbooking.payment.domain.exception.InvalidPaymentStateException;
import com.ticketbooking.payment.domain.exception.PaymentNotFoundException;
import com.ticketbooking.payment.domain.exception.RefundFailedException;
import com.ticketbooking.payment.domain.repository.OutboxEventRepository;
import com.ticketbooking.payment.domain.repository.PaymentRepository;
import com.ticketbooking.payment.infrastructure.gateway.FakePaymentGateway;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class PaymentRefundIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    @DisplayName("Successful Refund: Updates status to REFUNDED and emits payment.refunded outbox event")
    void refund_successfulRefund() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Payment payment = paymentRepository.save(Payment.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(PaymentStatus.CAPTURED)
                .paymentMethodId(UUID.randomUUID())
                .providerTransactionId("TX-ORIGINAL")
                .build());

        RefundPaymentCommand command = RefundPaymentCommand.builder()
                .paymentId(payment.getId())
                .bookingId(bookingId)
                .userId(userId)
                .amount(payment.getAmount())
                .currency("USD")
                .reason("USER_CANCELLED")
                .idempotencyKey("idem-refund-success")
                .build();

        Payment refunded = paymentService.refund(command);

        assertThat(refunded.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(refunded.getProviderTransactionId()).startsWith("RF-");

        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents).extracting(OutboxEvent::getEventType).contains("payment.refunded");
    }

    @Test
    @DisplayName("Payment Not Found: Throws PaymentNotFoundException for invalid payment ID")
    void refund_paymentNotFound_throwsException() {
        UUID missingPaymentId = UUID.randomUUID();

        RefundPaymentCommand command = RefundPaymentCommand.builder()
                .paymentId(missingPaymentId)
                .bookingId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .reason("USER_CANCELLED")
                .build();

        assertThatThrownBy(() -> paymentService.refund(command)).isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    @DisplayName("Invalid Payment State: Throws InvalidPaymentStateException if payment is not CAPTURED")
    void refund_invalidState_throwsException() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Payment payment = paymentRepository.save(Payment.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(PaymentStatus.FAILED) // Uncaptured state
                .paymentMethodId(UUID.randomUUID())
                .build());

        RefundPaymentCommand command = RefundPaymentCommand.builder()
                .paymentId(payment.getId())
                .bookingId(bookingId)
                .userId(userId)
                .amount(payment.getAmount())
                .currency("USD")
                .reason("USER_CANCELLED")
                .build();

        assertThatThrownBy(() -> paymentService.refund(command)).isInstanceOf(InvalidPaymentStateException.class);
    }

    @Test
    @DisplayName(
            "Refund Gateway Failure: Updates status to REFUND_FAILED, emits payment.refund_failed outbox, and throws RefundFailedException")
    void refund_gatewayFailure_marksRefundFailedAndEmitsOutbox() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Payment payment = paymentRepository.save(Payment.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(FakePaymentGateway.FAIL_AMOUNT)
                .currency("USD")
                .status(PaymentStatus.CAPTURED)
                .paymentMethodId(UUID.randomUUID())
                .providerTransactionId("TX-12345")
                .build());

        RefundPaymentCommand command = RefundPaymentCommand.builder()
                .paymentId(payment.getId())
                .bookingId(bookingId)
                .userId(userId)
                .amount(payment.getAmount())
                .currency("USD")
                .reason("USER_CANCELLED")
                .build();

        assertThatThrownBy(() -> paymentService.refund(command)).isInstanceOf(RefundFailedException.class);

        Payment updated = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.REFUND_FAILED);

        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents).extracting(OutboxEvent::getEventType).contains("payment.refund_failed");
    }
}
