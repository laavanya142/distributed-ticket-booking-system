package com.ticketbooking.booking.domain.exception;

import com.ticketbooking.booking.domain.entity.BookingStatus;

/**
 * Exception thrown when an invalid booking state transition is attempted.
 */
public class BookingStateTransitionException extends RuntimeException {

    public BookingStateTransitionException(BookingStatus currentStatus, BookingStatus targetStatus) {
        super(String.format("Illegal booking state transition from %s to %s", currentStatus, targetStatus));
    }
}
