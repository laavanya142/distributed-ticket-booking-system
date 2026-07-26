package com.ticketbooking.booking.domain.repository;

import com.ticketbooking.booking.domain.entity.Booking;
import com.ticketbooking.booking.domain.entity.BookingStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Booking aggregate root persistence.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /**
     * Finds a booking by client idempotency key and user ID.
     *
     * @param idempotencyKey Client idempotency key.
     * @param userId Owning user identifier.
     * @return Optional containing matching Booking if found.
     */
    Optional<Booking> findByIdempotencyKeyAndUserId(String idempotencyKey, UUID userId);

    /**
     * Queries expired active bookings for background cleanup by the expiry sweeper.
     *
     * @param statuses Active statuses to query (e.g. PENDING, AWAITING_PAYMENT).
     * @param now Current timestamp threshold.
     * @return List of expired bookings.
     */
    @Query("SELECT b FROM Booking b WHERE b.status IN :statuses AND b.expiresAt < :now")
    List<Booking> findExpiredBookings(@Param("statuses") List<BookingStatus> statuses, @Param("now") Instant now);

    /**
     * Returns a paginated list of bookings owned by a user.
     *
     * @param userId Owning user identifier.
     * @param pageable Page request.
     * @return Page of user's bookings.
     */
    Page<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Checks if an active booking exists for a user and show in pending/in-flight status.
     *
     * @param userId User identifier.
     * @param showId Show identifier.
     * @param statuses Active statuses.
     * @return true if an active booking exists.
     */
    boolean existsByUserIdAndShowIdAndStatusIn(UUID userId, UUID showId, List<BookingStatus> statuses);
}
