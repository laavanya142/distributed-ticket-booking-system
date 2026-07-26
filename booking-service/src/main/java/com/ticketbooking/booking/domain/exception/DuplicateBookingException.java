package com.ticketbooking.booking.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a user attempts to create a duplicate active booking for the same show.
 */
public class DuplicateBookingException extends RuntimeException {

    public DuplicateBookingException(UUID userId, UUID showId) {
        super(String.format("User %s already has an active booking for show %s", userId, showId));
    }
}
