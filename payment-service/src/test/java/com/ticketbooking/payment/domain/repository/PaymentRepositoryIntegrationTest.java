package com.ticketbooking.payment.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ticketbooking.payment.domain.entity.IdempotencyKeyEntity;
import com.ticketbooking.payment.domain.entity.IdempotencyKeyId;
import com.ticketbooking.payment.domain.entity.Payment;
import com.ticketbooking.payment.domain.entity.PaymentStatus;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class PaymentRepositoryIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Query payments by booking ID")
    void testFindByBookingId() {
        UUID bookingId = UUID.randomUUID();

        paymentRepository.save(Payment.builder()
                .bookingId(bookingId)
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(PaymentStatus.CAPTURED)
                .paymentMethodId(UUID.randomUUID())
                .build());

        List<Payment> payments = paymentRepository.findByBookingId(bookingId);
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getBookingId()).isEqualTo(bookingId);
    }

    @Test
    @DisplayName("Query payment by booking ID and status")
    void testFindByBookingIdAndStatus() {
        UUID bookingId = UUID.randomUUID();

        paymentRepository.save(Payment.builder()
                .bookingId(bookingId)
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .status(PaymentStatus.CAPTURED)
                .paymentMethodId(UUID.randomUUID())
                .build());

        Optional<Payment> captured = paymentRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.CAPTURED);
        assertThat(captured).isPresent();

        Optional<Payment> failed = paymentRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.FAILED);
        assertThat(failed).isEmpty();
    }

    @Test
    @DisplayName("Check existence of payment by booking ID and status")
    void testExistsByBookingIdAndStatus() {
        UUID bookingId = UUID.randomUUID();

        paymentRepository.save(Payment.builder()
                .bookingId(bookingId)
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("200.00"))
                .currency("USD")
                .status(PaymentStatus.CAPTURED)
                .paymentMethodId(UUID.randomUUID())
                .build());

        boolean exists = paymentRepository.existsByBookingIdAndStatus(bookingId, PaymentStatus.CAPTURED);
        assertThat(exists).isTrue();

        boolean notExists = paymentRepository.existsByBookingIdAndStatus(bookingId, PaymentStatus.FAILED);
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Idempotency key lookup by composite key")
    void testIdempotencyLookup() {
        String key = "idem-pay-123";
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        idempotencyKeyRepository.save(IdempotencyKeyEntity.builder()
                .id(new IdempotencyKeyId(key, userId))
                .paymentId(paymentId)
                .statusCode(200)
                .responseBody("CAPTURED")
                .expiresAt(Instant.now().plusSeconds(86400))
                .build());

        Optional<IdempotencyKeyEntity> found = idempotencyKeyRepository.findById_KeyAndId_UserId(key, userId);
        assertThat(found).isPresent();
        assertThat(found.get().getPaymentId()).isEqualTo(paymentId);
    }

    @Test
    @DisplayName("Optimistic Locking: Version increments on update and detects concurrent modification")
    void testOptimisticLocking() {
        Payment payment = paymentRepository.saveAndFlush(Payment.builder()
                .bookingId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(PaymentStatus.INITIATED)
                .paymentMethodId(UUID.randomUUID())
                .build());

        Long initialVersion = payment.getVersion();

        payment.capture("TX-12345");
        Payment updated = paymentRepository.saveAndFlush(payment);

        assertThat(updated.getVersion()).isGreaterThan(initialVersion);

        // Detach and simulate stale update
        entityManager.detach(updated);

        Payment stalePayment = Payment.builder()
                .id(updated.getId())
                .bookingId(updated.getBookingId())
                .userId(updated.getUserId())
                .amount(updated.getAmount())
                .currency(updated.getCurrency())
                .status(PaymentStatus.REFUNDED)
                .paymentMethodId(updated.getPaymentMethodId())
                .version(initialVersion) // Stale version
                .build();

        assertThatThrownBy(() -> paymentRepository.saveAndFlush(stalePayment))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
