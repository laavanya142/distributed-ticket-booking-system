package com.ticketbooking.event.domain.exception;

import com.ticketbooking.common.exception.ResourceNotFoundException;
import java.util.UUID;

/**
 * Thrown when a requested movie cannot be found by ID.
 */
public class MovieNotFoundException extends ResourceNotFoundException {
    public MovieNotFoundException(UUID id) {
        super("MOVIE_NOT_FOUND", "Movie", id);
    }
}
