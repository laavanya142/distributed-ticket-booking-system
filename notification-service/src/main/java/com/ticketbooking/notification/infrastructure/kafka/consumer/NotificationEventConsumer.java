package com.ticketbooking.notification.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbooking.notification.application.model.ProcessNotificationCommand;
import com.ticketbooking.notification.application.service.NotificationService;
import com.ticketbooking.notification.domain.entity.NotificationChannel;
import com.ticketbooking.notification.domain.exception.DuplicateNotificationException;
import com.ticketbooking.notification.infrastructure.kafka.dto.NotificationEventDto;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Event consumer listening to booking and payment domain Kafka events and initiating notification dispatch.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {
                "ticket.booking.confirmed",
                "ticket.booking.cancelled",
                "ticket.payment.captured",
                "ticket.payment.refunded"
            },
            groupId = "${spring.kafka.consumer.group-id:notification-service-group}")
    public void consumeNotificationEvent(String messagePayload, Acknowledgment ack) {
        log.info("Received Kafka notification event payload: {}", messagePayload);

        NotificationEventDto eventDto;
        try {
            eventDto = parsePayload(messagePayload);
        } catch (Exception ex) {
            log.error(
                    "Failed to parse Kafka notification message: {}. Payload: {}", ex.getMessage(), messagePayload, ex);
            if (ack != null) {
                ack.acknowledge();
            }
            return;
        }

        ProcessNotificationCommand command = ProcessNotificationCommand.builder()
                .eventId(eventDto.getEventId() != null ? eventDto.getEventId() : UUID.randomUUID())
                .aggregateId(eventDto.getAggregateId() != null ? eventDto.getAggregateId() : UUID.randomUUID())
                .aggregateType(eventDto.getAggregateType() != null ? eventDto.getAggregateType() : "Unknown")
                .eventType(eventDto.getEventType() != null ? eventDto.getEventType() : "ticket.notification")
                .userId(eventDto.getUserId() != null ? eventDto.getUserId() : UUID.randomUUID())
                .channel(eventDto.getChannel() != null ? eventDto.getChannel() : NotificationChannel.EMAIL)
                .recipient(eventDto.getRecipient() != null ? eventDto.getRecipient() : "user@example.com")
                .subject(eventDto.getSubject() != null ? eventDto.getSubject() : "Ticket Booking Update")
                .title(eventDto.getTitle() != null ? eventDto.getTitle() : "Notification")
                .message(
                        eventDto.getMessage() != null
                                ? eventDto.getMessage()
                                : "Your ticket booking status has updated.")
                .build();

        try {
            notificationService.processNotification(command);
            log.info("Successfully processed event ID {} for user ID {}", command.getEventId(), command.getUserId());
        } catch (DuplicateNotificationException ex) {
            log.warn("Ignored duplicate notification event ID {}: {}", command.getEventId(), ex.getMessage());
        } catch (Exception ex) {
            log.error("Error processing notification event ID {}: {}", command.getEventId(), ex.getMessage(), ex);
            throw ex;
        } finally {
            if (ack != null) {
                ack.acknowledge();
            }
        }
    }

    private NotificationEventDto parsePayload(String rawPayload) throws Exception {
        if (rawPayload.trim().startsWith("{")) {
            Map<String, Object> map = objectMapper.readValue(rawPayload, Map.class);
            NotificationEventDto dto = new NotificationEventDto();
            if (map.containsKey("eventId")) {
                dto.setEventId(UUID.fromString((String) map.get("eventId")));
            }
            if (map.containsKey("bookingId")) {
                dto.setAggregateId(UUID.fromString((String) map.get("bookingId")));
            }
            if (map.containsKey("paymentId")) {
                dto.setAggregateId(UUID.fromString((String) map.get("paymentId")));
            }
            if (map.containsKey("userId")) {
                dto.setUserId(UUID.fromString((String) map.get("userId")));
            }
            if (map.containsKey("recipient")) {
                dto.setRecipient((String) map.get("recipient"));
            }
            if (map.containsKey("subject")) {
                dto.setSubject((String) map.get("subject"));
            }
            if (map.containsKey("title")) {
                dto.setTitle((String) map.get("title"));
            }
            if (map.containsKey("message")) {
                dto.setMessage((String) map.get("message"));
            }
            if (map.containsKey("channel")) {
                dto.setChannel(NotificationChannel.valueOf((String) map.get("channel")));
            }
            if (map.containsKey("eventType")) {
                dto.setEventType((String) map.get("eventType"));
            }
            if (map.containsKey("status")) {
                dto.setMessage("Status update: " + map.get("status"));
            }
            return dto;
        }
        return objectMapper.readValue(rawPayload, NotificationEventDto.class);
    }
}
