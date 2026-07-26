package com.ticketbooking.booking.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a user attempts to access or mutate a booking owned by another user.
 */
public class BookingAccessDeniedException extends RuntimeException {

    public BookingAccessDeniedException(UUID bookingId, UUID userId) {
        super(String.format("User %s is not authorized to access booking %s", userId, bookingId));
    }
}
