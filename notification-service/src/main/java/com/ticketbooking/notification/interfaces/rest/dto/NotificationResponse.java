package com.ticketbooking.notification.interfaces.rest.dto;

import com.ticketbooking.notification.domain.entity.NotificationChannel;
import com.ticketbooking.notification.domain.entity.NotificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload representing notification audit details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response details for a notification dispatch record")
public class NotificationResponse {

    @Schema(description = "Notification ID", example = "e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b")
    private UUID id;

    @Schema(description = "Source event ID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID eventId;

    @Schema(description = "Aggregate ID reference", example = "b1a2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID aggregateId;

    @Schema(description = "Aggregate type", example = "Booking")
    private String aggregateType;

    @Schema(description = "Event type", example = "booking.confirmed")
    private String eventType;

    @Schema(description = "Recipient user ID", example = "c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")
    private UUID userId;

    @Schema(description = "Notification delivery channel", example = "EMAIL")
    private NotificationChannel channel;

    @Schema(description = "Notification dispatch status", example = "SENT")
    private NotificationStatus status;

    @Schema(description = "Target recipient address/phone/token", example = "user@example.com")
    private String recipient;

    @Schema(description = "Email subject if applicable", example = "Booking Confirmed!")
    private String subject;

    @Schema(description = "Push notification title if applicable", example = "Ticket Booking")
    private String title;

    @Schema(description = "Message body content", example = "Your booking for show SHOW-101 has been confirmed.")
    private String message;

    @Schema(description = "Provider message transaction reference", example = "MSG-98765432")
    private String providerMessageId;

    @Schema(description = "Reason for failure if status is FAILED", example = "RECIPIENT_UNREACHABLE")
    private String failureReason;

    @Schema(description = "Notification creation timestamp")
    private Instant createdAt;

    @Schema(description = "Notification dispatch timestamp")
    private Instant sentAt;

    @Schema(description = "Notification update timestamp")
    private Instant updatedAt;
}
