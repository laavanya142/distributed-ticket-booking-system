package com.ticketbooking.auth.domain.exception;

import com.ticketbooking.common.exception.DomainException;

/**
 * Thrown during registration if the requested email is already registered.
 */
public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException(String email) {
        super("USER_ALREADY_EXISTS", "User account already exists with email: " + email);
    }
}
