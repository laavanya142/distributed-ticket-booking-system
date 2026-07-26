package com.ticketbooking.notification.application.service;

import com.ticketbooking.notification.application.model.ProcessNotificationCommand;
import com.ticketbooking.notification.domain.entity.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for processing event-driven notifications and querying notification history.
 */
public interface NotificationService {

    /**
     * Processes an event-driven notification dispatch with deduplication and provider invocation.
     *
     * @param command Notification dispatch command details.
     * @return Dispatched Notification entity.
     */
    Notification processNotification(ProcessNotificationCommand command);

    /**
     * Returns a paginated list of notification history for a user.
     *
     * @param userId User identifier.
     * @param pageable Pagination parameters.
     * @return Page of Notification aggregates.
     */
    Page<Notification> findByUser(UUID userId, Pageable pageable);

    /**
     * Returns all notifications for a user.
     *
     * @param userId User identifier.
     * @return List of Notification aggregates.
     */
    List<Notification> findByUser(UUID userId);

    /**
     * Finds a notification by ID.
     *
     * @param notificationId Notification identifier.
     * @return Notification aggregate.
     */
    Notification findById(UUID notificationId);
}
