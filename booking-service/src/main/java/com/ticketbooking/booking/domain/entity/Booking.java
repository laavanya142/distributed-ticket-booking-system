package com.ticketbooking.booking.domain.entity;

import com.ticketbooking.booking.domain.exception.BookingStateTransitionException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing the Booking aggregate root.
 */
@Entity
@Table(
        name = "bookings",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_booking_idempotency",
                    columnNames = {"idempotency_key"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "show_id", nullable = false)
    private UUID showId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "lock_token", nullable = false)
    private UUID lockToken;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Builder.Default
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "confirmation_code", length = 20)
    private String confirmationCode;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Helper method to associate booking items with this aggregate root.
     *
     * @param item BookingItem to add.
     */
    public void addItem(BookingItem item) {
        items.add(item);
        item.setBooking(this);
    }

    /**
     * Enforces domain state machine transitions.
     *
     * @param targetStatus The desired target state.
     */
    public void transitionTo(BookingStatus targetStatus) {
        if (this.status == targetStatus) {
            return;
        }

        boolean valid =
                switch (this.status) {
                    case PENDING -> targetStatus == BookingStatus.AWAITING_PAYMENT
                            || targetStatus == BookingStatus.CANCELLED;
                    case AWAITING_PAYMENT -> targetStatus == BookingStatus.CONFIRMED
                            || targetStatus == BookingStatus.CANCELLED
                            || targetStatus == BookingStatus.EXPIRED;
                    case EXPIRED -> targetStatus == BookingStatus.CANCELLED;
                    case CONFIRMED -> targetStatus == BookingStatus.CANCELLED;
                    case CANCELLED -> false; // Terminal state
                };

        if (!valid) {
            throw new BookingStateTransitionException(this.status, targetStatus);
        }

        this.status = targetStatus;
    }
}
