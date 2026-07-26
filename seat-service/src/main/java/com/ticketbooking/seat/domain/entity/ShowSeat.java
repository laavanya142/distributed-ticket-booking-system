package com.ticketbooking.seat.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
 * JPA entity representing per-show dynamic seat state, pricing, and locking.
 */
@Entity
@Table(
        name = "show_seats",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_show_seat",
                    columnNames = {"show_id", "seat_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "show_id", nullable = false)
    private UUID showId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false, foreignKey = @ForeignKey(name = "fk_show_seats_seat"))
    private Seat seat;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShowSeatStatus status = ShowSeatStatus.AVAILABLE;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @Column(name = "lock_token")
    private UUID lockToken;

    @Column(name = "locked_by_user_id")
    private UUID lockedByUserId;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
     * Attempts to place a temporary lock on this show seat.
     *
     * @param token Unique lock token (e.g. booking session ID).
     * @param userId Owning user identifier.
     * @param time Timestamp when lock was acquired.
     */
    public void lock(UUID token, UUID userId, Instant time) {
        this.status = ShowSeatStatus.LOCKED;
        this.lockToken = token;
        this.lockedByUserId = userId;
        this.lockedAt = time;
    }

    /**
     * Releases any active lock and restores the show seat to AVAILABLE status.
     */
    public void unlock() {
        this.status = ShowSeatStatus.AVAILABLE;
        this.lockToken = null;
        this.lockedByUserId = null;
        this.lockedAt = null;
    }

    /**
     * Permanently confirms the seat as BOOKED post-payment.
     */
    public void confirmBooking() {
        this.status = ShowSeatStatus.BOOKED;
        this.lockToken = null;
        this.lockedByUserId = null;
        this.lockedAt = null;
    }

    /**
     * Checks if a current lock has expired past the given TTL window.
     *
     * @param now Current timestamp.
     * @param ttlSeconds TTL window in seconds.
     * @return true if status is LOCKED and lockedAt + ttlSeconds is before now.
     */
    public boolean isLockExpired(Instant now, long ttlSeconds) {
        if (status != ShowSeatStatus.LOCKED || lockedAt == null) {
            return false;
        }
        return lockedAt.plusSeconds(ttlSeconds).isBefore(now);
    }
}
