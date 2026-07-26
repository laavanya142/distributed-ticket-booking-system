package com.ticketbooking.notification.application.model;

import com.ticketbooking.notification.domain.entity.NotificationChannel;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command object encapsulating data required to dispatch a multi-channel notification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessNotificationCommand {

    private UUID eventId;
    private UUID aggregateId;
    private String aggregateType;
    private String eventType;
    private UUID userId;
    private NotificationChannel channel;
    private String recipient;
    private String subject;
    private String title;
    private String message;
}
