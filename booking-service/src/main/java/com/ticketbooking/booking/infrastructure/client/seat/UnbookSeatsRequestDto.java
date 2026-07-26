package com.ticketbooking.booking.infrastructure.client.seat;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload sent to Seat Service for unbooking seats on user cancellation of a confirmed booking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnbookSeatsRequestDto {

    private List<UUID> showSeatIds;
    private UUID lockToken;
    private UUID userId;
}
