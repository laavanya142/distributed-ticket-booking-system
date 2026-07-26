package com.ticketbooking.booking.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when seat lock verification fails during checkout.
 */
public class SeatVerificationFailedException extends RuntimeException {

    public SeatVerificationFailedException(UUID showId) {
        super(String.format(
                "Seat lock verification failed for show %s. Seats may be unlocked or owned by another user.", showId));
    }
}
