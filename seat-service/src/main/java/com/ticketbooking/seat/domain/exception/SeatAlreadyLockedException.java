package com.ticketbooking.seat.domain.exception;

import com.ticketbooking.common.exception.DomainException;
import java.util.List;
import java.util.UUID;

/**
 * Thrown when one or more seats requested for locking are already locked or booked.
 */
public class SeatAlreadyLockedException extends DomainException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs SeatAlreadyLockedException for a show and set of conflicting seat IDs.
     *
     * @param showId The show identifier.
     * @param showSeatIds List of conflicting show seat identifiers.
     */
    public SeatAlreadyLockedException(UUID showId, List<UUID> showSeatIds) {
        super(
                "SEAT_ALREADY_LOCKED",
                String.format(
                        "One or more requested seats in show %s are already locked or booked: %s",
                        showId, showSeatIds));
    }
}
