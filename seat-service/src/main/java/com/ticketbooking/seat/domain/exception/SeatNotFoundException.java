package com.ticketbooking.seat.domain.exception;

import com.ticketbooking.common.exception.ResourceNotFoundException;
import java.util.UUID;

/**
 * Thrown when a requested seat or show seat cannot be found.
 */
public class SeatNotFoundException extends ResourceNotFoundException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs SeatNotFoundException with seat identifier.
     *
     * @param id The missing seat or show seat identifier.
     */
    public SeatNotFoundException(UUID id) {
        super("SEAT_NOT_FOUND", "Seat", id);
    }
}
