package com.ticketbooking.auth.domain.exception;

import com.ticketbooking.common.exception.DomainException;

/**
 * Thrown when a refresh token is expired, revoked, or invalid.
 */
public class TokenRefreshException extends DomainException {
    public TokenRefreshException(String message) {
        super("INVALID_REFRESH_TOKEN", message);
    }
}
