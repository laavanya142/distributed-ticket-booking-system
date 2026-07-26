package com.ticketbooking.event.domain.exception;

import com.ticketbooking.common.exception.ResourceNotFoundException;
import java.util.UUID;

/**
 * Thrown when a requested venue cannot be found by ID.
 */
public class VenueNotFoundException extends ResourceNotFoundException {
    public VenueNotFoundException(UUID id) {
        super("VENUE_NOT_FOUND", "Venue", id);
    }
}
