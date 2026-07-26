package com.ticketbooking.notification.domain.exception;

import com.ticketbooking.common.exception.ResourceNotFoundException;
import java.util.UUID;

/**
 * Thrown when a requested notification cannot be found.
 */
public class NotificationNotFoundException extends ResourceNotFoundException {

    public NotificationNotFoundException(UUID notificationId) {
        super("NOTIFICATION_NOT_FOUND", "Notification with ID " + notificationId + " was not found");
    }

    public NotificationNotFoundException(String message) {
        super("NOTIFICATION_NOT_FOUND", message);
    }
}
