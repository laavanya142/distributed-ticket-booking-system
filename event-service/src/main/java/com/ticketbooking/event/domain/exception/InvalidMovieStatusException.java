package com.ticketbooking.event.domain.exception;

import com.ticketbooking.common.exception.DomainException;

/**
 * Thrown when trying to schedule a show for a movie that is ARCHIVED.
 */
public class InvalidMovieStatusException extends DomainException {
    public InvalidMovieStatusException(String message) {
        super("INVALID_MOVIE_STATUS", message);
    }
}
