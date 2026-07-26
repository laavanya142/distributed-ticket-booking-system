package com.ticketbooking.notification.infrastructure.kafka.dto;

import com.ticketbooking.notification.domain.entity.NotificationChannel;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event DTO representing incoming Kafka domain notification events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventDto {

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
    private Instant timestamp;
}
