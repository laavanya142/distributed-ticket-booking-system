package com.ticketbooking.seat.domain.exception;

import com.ticketbooking.common.exception.DomainException;
import java.util.UUID;

/**
 * Thrown when a lock release or validation fails due to invalid lockToken ownership.
 */
public class InvalidSeatLockException extends DomainException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs InvalidSeatLockException for an invalid or mismatched lock token.
     *
     * @param lockToken The unauthorized or invalid lock token.
     */
    public InvalidSeatLockException(UUID lockToken) {
        super("INVALID_SEAT_LOCK", String.format("Invalid or unauthorized lock token: %s", lockToken));
    }
}
