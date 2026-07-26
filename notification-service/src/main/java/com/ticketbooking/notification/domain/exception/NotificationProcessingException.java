package com.ticketbooking.notification.domain.exception;

import com.ticketbooking.common.exception.DomainException;

/**
 * Thrown when notification delivery fails at the provider level.
 */
public class NotificationProcessingException extends DomainException {

    public NotificationProcessingException(String message) {
        super("NOTIFICATION_PROCESSING_FAILED", "Notification processing failed: " + message);
    }
}
