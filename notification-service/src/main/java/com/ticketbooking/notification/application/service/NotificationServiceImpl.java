package com.ticketbooking.notification.application.service;

import com.ticketbooking.notification.application.model.ProcessNotificationCommand;
import com.ticketbooking.notification.domain.entity.Notification;
import com.ticketbooking.notification.domain.entity.NotificationStatus;
import com.ticketbooking.notification.domain.exception.DuplicateNotificationException;
import com.ticketbooking.notification.domain.exception.NotificationNotFoundException;
import com.ticketbooking.notification.domain.exception.NotificationProcessingException;
import com.ticketbooking.notification.domain.provider.NotificationProvider;
import com.ticketbooking.notification.domain.provider.NotificationProviderResult;
import com.ticketbooking.notification.domain.repository.NotificationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of NotificationService executing deduplication, persistence, and provider dispatching.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationProvider notificationProvider;

    @Override
    @Transactional
    public Notification processNotification(ProcessNotificationCommand command) {
        log.info(
                "Processing notification dispatch for event ID: {}, user ID: {}, channel: {}",
                command.getEventId(),
                command.getUserId(),
                command.getChannel());

        // Deduplication check
        if (notificationRepository.existsByEventId(command.getEventId())) {
            log.warn("Duplicate eventId '{}' detected. Skipping notification processing.", command.getEventId());
            Optional<Notification> existing = notificationRepository.findByEventId(command.getEventId());
            if (existing.isPresent()) {
                return existing.get();
            }
            throw new DuplicateNotificationException(command.getEventId());
        }

        // Step 1: Persist in PENDING state
        Notification notification = Notification.builder()
                .eventId(command.getEventId())
                .aggregateId(command.getAggregateId())
                .aggregateType(command.getAggregateType())
                .eventType(command.getEventType())
                .userId(command.getUserId())
                .channel(command.getChannel())
                .status(NotificationStatus.PENDING)
                .recipient(command.getRecipient())
                .subject(command.getSubject())
                .title(command.getTitle())
                .message(command.getMessage())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // Step 2: Dispatch to provider based on channel
        NotificationProviderResult providerResult;
        try {
            switch (command.getChannel()) {
                case EMAIL -> providerResult = notificationProvider.sendEmail(
                        command.getRecipient(), command.getSubject(), command.getMessage());
                case SMS -> providerResult = notificationProvider.sendSms(command.getRecipient(), command.getMessage());
                case PUSH -> providerResult =
                        notificationProvider.sendPush(command.getRecipient(), command.getTitle(), command.getMessage());
                default -> throw new IllegalArgumentException(
                        "Unsupported notification channel: " + command.getChannel());
            }
        } catch (Exception ex) {
            log.error(
                    "Exception occurred during provider dispatch for notification ID {}: {}",
                    savedNotification.getId(),
                    ex.getMessage());
            savedNotification.markFailed(ex.getMessage());
            notificationRepository.save(savedNotification);
            throw new NotificationProcessingException(ex.getMessage());
        }

        // Step 3: Update status based on result
        if (providerResult.isSuccessful()) {
            savedNotification.markSent(providerResult.getProviderMessageId());
            Notification updated = notificationRepository.save(savedNotification);
            log.info(
                    "Successfully dispatched notification ID: {} via channel {}",
                    updated.getId(),
                    updated.getChannel());
            return updated;
        } else {
            savedNotification.markFailed(providerResult.getFailureReason());
            notificationRepository.save(savedNotification);
            log.warn(
                    "Failed to dispatch notification ID: {} via channel {}: {}",
                    savedNotification.getId(),
                    savedNotification.getChannel(),
                    providerResult.getFailureReason());
            throw new NotificationProcessingException(providerResult.getFailureReason());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> findByUser(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByUser(UUID userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Notification findById(UUID notificationId) {
        return notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
    }
}
