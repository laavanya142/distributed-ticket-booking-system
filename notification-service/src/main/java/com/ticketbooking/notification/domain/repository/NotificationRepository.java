package com.ticketbooking.notification.domain.repository;

import com.ticketbooking.notification.domain.entity.Notification;
import com.ticketbooking.notification.domain.entity.NotificationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Notification persistence and history query operations.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Notification> findByUserId(UUID userId);

    List<Notification> findByStatus(NotificationStatus status);

    boolean existsByEventId(UUID eventId);

    Optional<Notification> findByEventId(UUID eventId);
}
