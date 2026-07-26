package com.ticketbooking.booking.infrastructure.client.seat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result payload from Seat Service seat lock verification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatVerificationResult {

    private boolean valid;
    private UUID lockToken;
    private List<SeatDetailDto> seats;
    private Instant expiresAt;
    private long ttlSeconds;
}
