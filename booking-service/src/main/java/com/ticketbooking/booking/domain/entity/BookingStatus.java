package com.ticketbooking.booking.domain.entity;

/**
 * Enumeration representing the lifecycle states of a booking.
 */
public enum BookingStatus {

    /**
     * Initial state when booking entity is created and seat lock is verified.
     */
    PENDING,

    /**
     * Intermediate state while synchronous payment REST call is in-flight.
     */
    AWAITING_PAYMENT,

    /**
     * Terminal success state after payment captured and seats confirmed.
     */
    CONFIRMED,

    /**
     * Terminal cancelled state after Saga rollback or user cancellation.
     */
    CANCELLED,

    /**
     * Intermediate expired state when lock TTL elapses before payment completes.
     */
    EXPIRED
}
