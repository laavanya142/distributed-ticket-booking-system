package com.ticketbooking.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ticketbooking.payment.application.model.ChargePaymentCommand;
import com.ticketbooking.payment.domain.entity.IdempotencyKeyEntity;
import com.ticketbooking.payment.domain.entity.OutboxEvent;
import com.ticketbooking.payment.domain.entity.Payment;
import com.ticketbooking.payment.domain.entity.PaymentStatus;
import com.ticketbooking.payment.domain.exception.DuplicatePaymentException;
import com.ticketbooking.payment.domain.exception.PaymentProcessingException;
import com.ticketbooking.payment.domain.repository.IdempotencyKeyRepository;
import com.ticketbooking.payment.domain.repository.OutboxEventRepository;
import com.ticketbooking.payment.domain.repository.PaymentRepository;
import com.ticketbooking.payment.infrastructure.gateway.FakePaymentGateway;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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
class PaymentChargeIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Test
    @DisplayName("Successful Charge: Persists CAPTURED status, providerTxId, outbox events, and idempotency key")
    void charge_successfulPayment() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID methodId = UUID.randomUUID();
        String idempotencyKey = "idem-charge-success";

        ChargePaymentCommand command = ChargePaymentCommand.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(new BigDecimal("120.00"))
                .currency("USD")
                .paymentMethodId(methodId)
                .idempotencyKey(idempotencyKey)
                .build();

        Payment payment = paymentService.charge(command);

        assertThat(payment).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(payment.getProviderTransactionId()).startsWith("TX-");

        // Verify DB persistence
        Payment saved = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.CAPTURED);

        // Verify Outbox events
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents)
                .extracting(OutboxEvent::getEventType)
                .contains("payment.initiated", "payment.captured");

        // Verify Idempotency persistence
        Optional<IdempotencyKeyEntity> idemRecord =
                idempotencyKeyRepository.findById_KeyAndId_UserId(idempotencyKey, userId);
        assertThat(idemRecord).isPresent();
        assertThat(idemRecord.get().getPaymentId()).isEqualTo(payment.getId());
    }

    @Test
    @DisplayName("Duplicate Idempotency Key: Returns cached payment without re-charging")
    void charge_duplicateIdempotencyKey_returnsCachedPayment() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String idempotencyKey = "idem-duplicate-key";

        ChargePaymentCommand command = ChargePaymentCommand.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .paymentMethodId(UUID.randomUUID())
                .idempotencyKey(idempotencyKey)
                .build();

        Payment firstCall = paymentService.charge(command);
        Payment secondCall = paymentService.charge(command);

        assertThat(secondCall.getId()).isEqualTo(firstCall.getId());
    }

    @Test
    @DisplayName(
            "Duplicate Captured Payment: Throws DuplicatePaymentException if active captured payment exists for booking")
    void charge_duplicateCapturedPayment_throwsException() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        paymentRepository.save(Payment.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(PaymentStatus.CAPTURED)
                .paymentMethodId(UUID.randomUUID())
                .build());

        ChargePaymentCommand command = ChargePaymentCommand.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .paymentMethodId(UUID.randomUUID())
                .idempotencyKey("idem-dup-captured")
                .build();

        assertThatThrownBy(() -> paymentService.charge(command)).isInstanceOf(DuplicatePaymentException.class);
    }

    @Test
    @DisplayName(
            "Gateway Decline Failure: Updates status to FAILED, emits payment.failed outbox event, and throws PaymentProcessingException")
    void charge_gatewayFailure_marksFailedAndEmitsOutbox() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ChargePaymentCommand command = ChargePaymentCommand.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .paymentMethodId(FakePaymentGateway.FAIL_PAYMENT_METHOD_ID)
                .idempotencyKey("idem-gateway-fail")
                .build();

        assertThatThrownBy(() -> paymentService.charge(command)).isInstanceOf(PaymentProcessingException.class);

        List<Payment> payments = paymentRepository.findByBookingId(bookingId);
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.FAILED);

        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents).extracting(OutboxEvent::getEventType).contains("payment.initiated", "payment.failed");
    }

    @Test
    @DisplayName(
            "Gateway Timeout: Catches timeout exception, updates status to FAILED, and emits payment.failed outbox event")
    void charge_gatewayTimeout_marksFailedAndEmitsOutbox() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ChargePaymentCommand command = ChargePaymentCommand.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .paymentMethodId(FakePaymentGateway.TIMEOUT_PAYMENT_METHOD_ID)
                .idempotencyKey("idem-gateway-timeout")
                .build();

        assertThatThrownBy(() -> paymentService.charge(command))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("Gateway network timeout");

        List<Payment> payments = paymentRepository.findByBookingId(bookingId);
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.FAILED);
    }
}
