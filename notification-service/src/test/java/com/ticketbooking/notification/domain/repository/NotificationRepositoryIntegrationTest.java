package com.ticketbooking.notification.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketbooking.notification.domain.entity.Notification;
import com.ticketbooking.notification.domain.entity.NotificationChannel;
import com.ticketbooking.notification.domain.entity.NotificationStatus;
import java.util.List;
import java.util.Optional;
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
class NotificationRepositoryIntegrationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("Query paginated notifications by user ID ordered by createdAt descending")
    void testFindByUserIdOrderByCreatedAtDesc() {
        UUID userId = UUID.randomUUID();

        notificationRepository.save(Notification.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .aggregateType("Booking")
                .eventType("booking.confirmed")
                .userId(userId)
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.SENT)
                .recipient("user@example.com")
                .message("Booking 1")
                .build());

        notificationRepository.save(Notification.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .aggregateType("Payment")
                .eventType("payment.captured")
                .userId(userId)
                .channel(NotificationChannel.SMS)
                .status(NotificationStatus.SENT)
                .recipient("+1234567890")
                .message("Payment 1")
                .build());

        Page<Notification> page =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Query notifications by status")
    void testFindByStatus() {
        UUID userId = UUID.randomUUID();

        notificationRepository.save(Notification.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .aggregateType("Booking")
                .eventType("booking.confirmed")
                .userId(userId)
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.SENT)
                .recipient("user@example.com")
                .message("Sent msg")
                .build());

        notificationRepository.save(Notification.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .aggregateType("Booking")
                .eventType("booking.failed")
                .userId(userId)
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.FAILED)
                .recipient("user@example.com")
                .message("Failed msg")
                .build());

        List<Notification> sent = notificationRepository.findByStatus(NotificationStatus.SENT);
        assertThat(sent).hasSize(1);
        assertThat(sent.get(0).getStatus()).isEqualTo(NotificationStatus.SENT);

        List<Notification> failed = notificationRepository.findByStatus(NotificationStatus.FAILED);
        assertThat(failed).hasSize(1);
        assertThat(failed.get(0).getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    @DisplayName("Check existence and lookup by event ID")
    void testExistsAndFindByEventId() {
        UUID eventId = UUID.randomUUID();

        notificationRepository.save(Notification.builder()
                .eventId(eventId)
                .aggregateId(UUID.randomUUID())
                .aggregateType("Booking")
                .eventType("booking.confirmed")
                .userId(UUID.randomUUID())
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.SENT)
                .recipient("user@example.com")
                .message("Message")
                .build());

        boolean exists = notificationRepository.existsByEventId(eventId);
        assertThat(exists).isTrue();

        Optional<Notification> found = notificationRepository.findByEventId(eventId);
        assertThat(found).isPresent();
        assertThat(found.get().getEventId()).isEqualTo(eventId);
    }
}
