package com.ticketbooking.booking.infrastructure.client.seat;

import java.util.List;
import java.util.UUID;

/**
 * Interface defining synchronous REST operations against Seat Service.
 */
public interface SeatServiceClient {

    /**
     * Read-only verification of active seat lock.
     *
     * @param showId Show identifier.
     * @param showSeatIds List of show seat IDs.
     * @param lockToken Unique lock token.
     * @param userId Owning user ID.
     * @return Verification result containing seat pricing and details.
     */
    SeatVerificationResult verifySeats(UUID showId, List<UUID> showSeatIds, UUID lockToken, UUID userId);

    /**
     * Transitions locked show seats to BOOKED status post-payment.
     *
     * @param showId Show identifier.
     * @param showSeatIds List of show seat IDs.
     * @param lockToken Unique lock token.
     * @param userId Owning user ID.
     */
    void confirmSeats(UUID showId, List<UUID> showSeatIds, UUID lockToken, UUID userId);

    /**
     * Transitions locked show seats back to AVAILABLE status on payment failure/timeout.
     *
     * @param showId Show identifier.
     * @param showSeatIds List of show seat IDs.
     * @param lockToken Unique lock token.
     * @param userId Owning user ID.
     */
    void releaseSeats(UUID showId, List<UUID> showSeatIds, UUID lockToken, UUID userId);

    /**
     * Transitions booked show seats back to AVAILABLE status on user cancellation of a confirmed booking.
     *
     * @param showId Show identifier.
     * @param showSeatIds List of show seat IDs.
     * @param lockToken Unique lock token.
     * @param userId Owning user ID.
     */
    void unbookSeats(UUID showId, List<UUID> showSeatIds, UUID lockToken, UUID userId);
}
