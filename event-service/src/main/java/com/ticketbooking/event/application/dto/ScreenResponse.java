package com.ticketbooking.event.application.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Immutable read-only response representation of a screen auditorium.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenResponse {
    private UUID id;
    private UUID venueId;
    private String name;
    private Integer capacity;
    private Instant createdAt;
}
