package com.ticketbooking.booking.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a requested booking entity cannot be found.
 */
public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(UUID bookingId) {
        super(String.format("Booking with ID %s was not found", bookingId));
    }
}
