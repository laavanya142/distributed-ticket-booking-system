package com.ticketbooking.common.exception;

import lombok.Getter;

/**
 * Base runtime exception for domain business rule violations.
 */
@Getter
public class DomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;

    /**
     * Constructs a new DomainException with code and message.
     *
     * @param errorCode The domain specific error identifier.
     * @param message The human readable explanation.
     */
    public DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new DomainException with code, message, and cause.
     *
     * @param errorCode The domain specific error identifier.
     * @param message The human readable explanation.
     * @param cause The underlying cause.
     */
    public DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
