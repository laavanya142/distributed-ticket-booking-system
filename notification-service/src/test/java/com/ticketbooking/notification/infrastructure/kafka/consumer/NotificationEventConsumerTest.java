package com.ticketbooking.notification.infrastructure.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

import com.ticketbooking.notification.application.model.ProcessNotificationCommand;
import com.ticketbooking.notification.application.service.NotificationService;
import com.ticketbooking.notification.domain.entity.Notification;
import com.ticketbooking.notification.domain.entity.NotificationStatus;
import com.ticketbooking.notification.domain.exception.DuplicateNotificationException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NotificationEventConsumerTest {

    @Test
    @DisplayName("Consume JSON Payload: Deserializes event and calls NotificationService")
    void testConsumeEvent_successfulInvocation() {
        NotificationService mockService = org.mockito.Mockito.mock(NotificationService.class);
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        NotificationEventConsumer consumer = new NotificationEventConsumer(mockService, objectMapper);

        UUID eventId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String jsonPayload = String.format(
                """
                {
                    "eventId": "%s",
                    "bookingId": "%s",
                    "userId": "%s",
                    "eventType": "booking.confirmed",
                    "channel": "EMAIL",
                    "recipient": "test@example.com",
                    "subject": "Confirmed",
                    "message": "Your booking is confirmed"
                }
                """,
                eventId, bookingId, userId);

        given(mockService.processNotification(any(ProcessNotificationCommand.class)))
                .willReturn(Notification.builder()
                        .id(UUID.randomUUID())
                        .status(NotificationStatus.SENT)
                        .build());

        consumer.consumeNotificationEvent(jsonPayload, null);

        ArgumentCaptor<ProcessNotificationCommand> captor = ArgumentCaptor.forClass(ProcessNotificationCommand.class);
        org.mockito.Mockito.verify(mockService).processNotification(captor.capture());

        ProcessNotificationCommand command = captor.getValue();
        assertThat(command.getEventId()).isEqualTo(eventId);
        assertThat(command.getAggregateId()).isEqualTo(bookingId);
        assertThat(command.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Duplicate Event ID: Catches DuplicateNotificationException and ignores without error")
    void testConsumeEvent_duplicateEventId_ignored() {
        NotificationService mockService = org.mockito.Mockito.mock(NotificationService.class);
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        NotificationEventConsumer consumer = new NotificationEventConsumer(mockService, objectMapper);

        UUID eventId = UUID.randomUUID();
        String jsonPayload = String.format("{\"eventId\":\"%s\",\"eventType\":\"booking.confirmed\"}", eventId);

        doThrow(new DuplicateNotificationException(eventId))
                .when(mockService)
                .processNotification(any(ProcessNotificationCommand.class));

        // Should complete cleanly without throwing exception
        consumer.consumeNotificationEvent(jsonPayload, null);
    }
}
