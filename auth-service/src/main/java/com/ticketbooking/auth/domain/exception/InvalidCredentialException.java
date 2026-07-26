package com.ticketbooking.auth.domain.exception;

import com.ticketbooking.common.exception.DomainException;

/**
 * Thrown when authentication fails due to incorrect email or password.
 */
public class InvalidCredentialException extends DomainException {
    public InvalidCredentialException(String message) {
        super("INVALID_CREDENTIALS", message);
    }
}
