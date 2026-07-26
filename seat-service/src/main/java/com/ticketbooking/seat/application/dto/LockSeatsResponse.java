package com.ticketbooking.seat.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for successful seat lock operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockSeatsResponse {

    private UUID showId;
    private UUID lockToken;
    private List<UUID> lockedSeatIds;
    private Instant expiresAt;
    private long ttlSeconds;
}
