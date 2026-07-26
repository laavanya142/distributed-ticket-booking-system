package com.ticketbooking.seat.application.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for complete show seat map and availability statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeatMapResponse {

    private UUID showId;
    private int totalSeats;
    private int availableSeats;
    private int lockedSeats;
    private int bookedSeats;
    private List<ShowSeatDetailResponse> seats;
}
