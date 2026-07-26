package com.ticketbooking.payment.domain.entity;

import com.ticketbooking.payment.domain.exception.InvalidPaymentStateException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payment aggregate entity tracking payment capture and refund lifecycles.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "payment_method_id", nullable = false)
    private UUID paymentMethodId;

    @Column(name = "provider_transaction_id", length = 128)
    private String providerTransactionId;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = PaymentStatus.INITIATED;
        }
        if (currency == null) {
            currency = "USD";
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Marks payment as successfully captured.
     *
     * @param providerTxId Transaction ID issued by payment gateway.
     */
    public void capture(String providerTxId) {
        if (status != PaymentStatus.INITIATED && status != PaymentStatus.PROCESSING) {
            throw new InvalidPaymentStateException(id, status, PaymentStatus.CAPTURED);
        }
        this.status = PaymentStatus.CAPTURED;
        this.providerTransactionId = providerTxId;
        this.updatedAt = Instant.now();
    }

    /**
     * Marks payment as failed.
     *
     * @param reason Failure explanation.
     */
    public void fail(String reason) {
        if (status != PaymentStatus.INITIATED && status != PaymentStatus.PROCESSING) {
            throw new InvalidPaymentStateException(id, status, PaymentStatus.FAILED);
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    /**
     * Marks captured payment as successfully refunded.
     *
     * @param refundTxId Refund transaction ID from gateway.
     */
    public void refund(String refundTxId) {
        if (status != PaymentStatus.CAPTURED) {
            throw new InvalidPaymentStateException(id, status, PaymentStatus.REFUNDED);
        }
        this.status = PaymentStatus.REFUNDED;
        this.providerTransactionId = refundTxId;
        this.updatedAt = Instant.now();
    }

    /**
     * Marks refund operation as failed.
     *
     * @param reason Refund failure explanation.
     */
    public void failRefund(String reason) {
        if (status != PaymentStatus.CAPTURED) {
            throw new InvalidPaymentStateException(id, status, PaymentStatus.REFUND_FAILED);
        }
        this.status = PaymentStatus.REFUND_FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }
}
