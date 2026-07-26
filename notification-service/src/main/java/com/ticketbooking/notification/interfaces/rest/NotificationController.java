package com.ticketbooking.notification.interfaces.rest;

import com.ticketbooking.common.constants.HeaderConstants;
import com.ticketbooking.common.dto.ApiResponse;
import com.ticketbooking.notification.application.service.NotificationService;
import com.ticketbooking.notification.domain.entity.Notification;
import com.ticketbooking.notification.interfaces.rest.dto.NotificationResponse;
import com.ticketbooking.notification.interfaces.rest.mapper.NotificationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only REST controller exposing notification history queries for authenticated users.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Validated
@Slf4j
@RequiredArgsConstructor
@Tag(
        name = "Notification Query API",
        description = "Read-only APIs for querying user notification history and audit logs")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    /**
     * Retrieves paginated notification history for the authenticated user, ordered by creation time descending.
     *
     * @param userId Authenticated user ID injected by API Gateway.
     * @param pageable Pagination and sorting options.
     * @return ResponseEntity with Page of NotificationResponse payloads.
     */
    @GetMapping
    @Operation(
            summary = "Get user notifications",
            description = "Fetches a paginated history of notifications dispatched to the authenticated user")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Notification history retrieved successfully")
    })
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUserNotifications(
            @Parameter(hidden = true) @RequestHeader(value = HeaderConstants.USER_ID) UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Received request to fetch notification history for user ID: {}", userId);

        Page<Notification> page = notificationService.findByUser(userId, pageable);
        Page<NotificationResponse> dtoPage = page.map(notificationMapper::toNotificationResponse);

        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", dtoPage));
    }

    /**
     * Retrieves details of a specific notification by ID.
     *
     * @param notificationId Notification identifier.
     * @param userId Authenticated user ID.
     * @return ResponseEntity with NotificationResponse payload.
     */
    @GetMapping("/{notificationId}")
    @Operation(
            summary = "Get notification details",
            description = "Fetches audit details of a specific notification by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Notification details retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Notification not found")
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationDetails(
            @PathVariable UUID notificationId,
            @Parameter(hidden = true) @RequestHeader(value = HeaderConstants.USER_ID) UUID userId) {
        log.debug("Received request to fetch notification ID: {} for user ID: {}", notificationId, userId);

        Notification notification = notificationService.findById(notificationId);
        NotificationResponse response = notificationMapper.toNotificationResponse(notification);

        return ResponseEntity.ok(ApiResponse.success("Notification details retrieved successfully", response));
    }
}
