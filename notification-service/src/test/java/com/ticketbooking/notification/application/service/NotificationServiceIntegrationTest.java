package com.ticketbooking.notification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ticketbooking.notification.application.model.ProcessNotificationCommand;
import com.ticketbooking.notification.domain.entity.Notification;
import com.ticketbooking.notification.domain.entity.NotificationChannel;
import com.ticketbooking.notification.domain.entity.NotificationStatus;
import com.ticketbooking.notification.domain.exception.NotificationProcessingException;
import com.ticketbooking.notification.domain.repository.NotificationRepository;
import com.ticketbooking.notification.infrastructure.provider.FakeNotificationProvider;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class NotificationServiceIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("booking.confirmed Event: Dispatches notification, sets status to SENT, and saves providerMessageId")
    void processNotification_bookingConfirmed_success() {
        UUID eventId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ProcessNotificationCommand command = ProcessNotificationCommand.builder()
                .eventId(eventId)
                .aggregateId(bookingId)
                .aggregateType("Booking")
                .eventType("booking.confirmed")
                .userId(userId)
                .channel(NotificationChannel.EMAIL)
                .recipient("user@example.com")
                .subject("Booking Confirmed!")
                .title("Ticket Booking")
                .message("Your booking for show SHOW-101 has been confirmed.")
                .build();

        Notification notification = notificationService.processNotification(command);

        assertThat(notification).isNotNull();
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getProviderMessageId()).startsWith("MSG-");
        assertThat(notification.getSentAt()).isNotNull();

        // Verify DB state
        Notification saved =
                notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(saved.getEventId()).isEqualTo(eventId);
    }

    @Test
    @DisplayName("booking.cancelled Event: Dispatches cancellation notification")
    void processNotification_bookingCancelled_success() {
        UUID eventId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ProcessNotificationCommand command = ProcessNotificationCommand.builder()
                .eventId(eventId)
                .aggregateId(bookingId)
                .aggregateType("Booking")
                .eventType("booking.cancelled")
                .userId(userId)
                .channel(NotificationChannel.SMS)
                .recipient("+1234567890")
                .subject("Booking Cancelled")
                .message("Your booking for show SHOW-101 has been cancelled.")
                .build();

        Notification notification = notificationService.processNotification(command);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getChannel()).isEqualTo(NotificationChannel.SMS);
    }

    @Test
    @DisplayName("payment.captured Event: Dispatches payment receipt notification")
    void processNotification_paymentCaptured_success() {
        UUID eventId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ProcessNotificationCommand command = ProcessNotificationCommand.builder()
                .eventId(eventId)
                .aggregateId(paymentId)
                .aggregateType("Payment")
                .eventType("payment.captured")
                .userId(userId)
                .channel(NotificationChannel.PUSH)
                .recipient("device-token-abc")
                .title("Payment Received")
                .message("Your payment of $120.00 was captured successfully.")
                .build();

        Notification notification = notificationService.processNotification(command);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getChannel()).isEqualTo(NotificationChannel.PUSH);
    }

    @Test
    @DisplayName("Duplicate Event ID: Returns existing notification without re-dispatching")
    void processNotification_duplicateEventId_returnsExisting() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ProcessNotificationCommand command = ProcessNotificationCommand.builder()
                .eventId(eventId)
                .aggregateId(UUID.randomUUID())
                .aggregateType("Booking")
                .eventType("booking.confirmed")
                .userId(userId)
                .channel(NotificationChannel.EMAIL)
                .recipient("user@example.com")
                .subject("Subject")
                .message("Message")
                .build();

        Notification firstCall = notificationService.processNotification(command);
        Notification secondCall = notificationService.processNotification(command);

        assertThat(secondCall.getId()).isEqualTo(firstCall.getId());
    }

    @Test
    @DisplayName("Provider Failure: Updates status to FAILED and throws NotificationProcessingException")
    void processNotification_providerFailure_marksFailed() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ProcessNotificationCommand command = ProcessNotificationCommand.builder()
                .eventId(eventId)
                .aggregateId(UUID.randomUUID())
                .aggregateType("Booking")
                .eventType("booking.confirmed")
                .userId(userId)
                .channel(NotificationChannel.EMAIL)
                .recipient(FakeNotificationProvider.FAIL_RECIPIENT)
                .subject("Subject")
                .message("Message")
                .build();

        assertThatThrownBy(() -> notificationService.processNotification(command))
                .isInstanceOf(NotificationProcessingException.class);

        Notification notification =
                notificationRepository.findByEventId(eventId).orElseThrow();
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getFailureReason()).isEqualTo("RECIPIENT_UNREACHABLE");
    }

    @Test
    @DisplayName("Notification History Retrieval: Queries user history with pagination")
    void findByUser_paginatedHistory() {
        UUID userId = UUID.randomUUID();

        ProcessNotificationCommand command1 = ProcessNotificationCommand.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .aggregateType("Booking")
                .eventType("booking.confirmed")
                .userId(userId)
                .channel(NotificationChannel.EMAIL)
                .recipient("user@example.com")
                .subject("1")
                .message("Msg 1")
                .build();

        ProcessNotificationCommand command2 = ProcessNotificationCommand.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .aggregateType("Payment")
                .eventType("payment.captured")
                .userId(userId)
                .channel(NotificationChannel.SMS)
                .recipient("+1234567890")
                .message("Msg 2")
                .build();

        notificationService.processNotification(command1);
        notificationService.processNotification(command2);

        Page<Notification> history = notificationService.findByUser(userId, PageRequest.of(0, 10));
        assertThat(history.getContent()).hasSize(2);
    }
}
