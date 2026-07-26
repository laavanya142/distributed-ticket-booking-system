package com.ticketbooking.event.domain.exception;

import com.ticketbooking.common.exception.ResourceNotFoundException;
import java.util.UUID;

/**
 * Thrown when a requested screen cannot be found by ID.
 */
public class ScreenNotFoundException extends ResourceNotFoundException {
    public ScreenNotFoundException(UUID id) {
        super("SCREEN_NOT_FOUND", "Screen", id);
    }
}
