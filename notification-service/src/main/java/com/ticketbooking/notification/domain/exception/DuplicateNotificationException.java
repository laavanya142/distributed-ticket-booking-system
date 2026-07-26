package com.ticketbooking.notification.domain.exception;

import com.ticketbooking.common.exception.DomainException;
import java.util.UUID;

/**
 * Thrown when attempting to process an eventId that has already been dispatched.
 */
public class DuplicateNotificationException extends DomainException {

    public DuplicateNotificationException(UUID eventId) {
        super("DUPLICATE_NOTIFICATION", "Notification for event ID " + eventId + " has already been processed");
    }
}
