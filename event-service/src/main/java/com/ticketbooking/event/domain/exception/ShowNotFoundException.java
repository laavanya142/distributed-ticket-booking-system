package com.ticketbooking.event.domain.exception;

import com.ticketbooking.common.exception.ResourceNotFoundException;
import java.util.UUID;

/**
 * Thrown when a requested show cannot be found by ID.
 */
public class ShowNotFoundException extends ResourceNotFoundException {
    public ShowNotFoundException(UUID id) {
        super("SHOW_NOT_FOUND", "Show", id);
    }
}
