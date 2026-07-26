package com.ticketbooking.seat.application.service;

import com.ticketbooking.seat.domain.entity.Seat;
import com.ticketbooking.seat.domain.entity.ShowSeat;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service interface defining seat inventory management and seat locking capabilities.
 */
public interface SeatService {

    /**
     * Batch creates physical screen seats for a venue screen.
     *
     * @param screenId Screen identifier.
     * @param seats List of physical seat configurations to create.
     * @return Created list of Seat entities.
     */
    List<Seat> createScreenSeats(UUID screenId, List<Seat> seats);

    /**
     * Eagerly pre-populates show seats for a newly scheduled show based on active screen seats.
     *
     * @param showId Show identifier.
     * @param screenId Screen identifier.
     * @param basePrice Base price for regular tier seats.
     * @return Number of ShowSeat records created.
     */
    int initializeShowSeats(UUID showId, UUID screenId, BigDecimal basePrice);

    /**
     * Retrieves the real-time seat map and status for a show.
     *
     * @param showId Show identifier.
     * @return List of show seats with physical seat attributes attached.
     */
    List<ShowSeat> getShowSeatMap(UUID showId);

    /**
     * Atomically locks a batch of show seats for checkout.
     *
     * @param showId Show identifier.
     * @param showSeatIds List of show seat IDs to lock.
     * @param lockToken Unique lock ownership token.
     * @param ttlSeconds Lock duration in seconds (0 for default).
     * @return List of successfully locked ShowSeat entities.
     */
    List<ShowSeat> lockSeats(UUID showId, List<UUID> showSeatIds, UUID lockToken, long ttlSeconds);

    /**
     * Explicitly releases a batch of locked show seats back to AVAILABLE status.
     *
     * @param showId Show identifier.
     * @param showSeatIds List of show seat IDs to release.
     * @param lockToken Lock ownership token verifying caller authority.
     */
    void releaseSeats(UUID showId, List<UUID> showSeatIds, UUID lockToken);
}
