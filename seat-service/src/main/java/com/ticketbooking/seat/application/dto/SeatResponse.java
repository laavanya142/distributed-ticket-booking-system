package com.ticketbooking.seat.application.dto;

import com.ticketbooking.seat.domain.entity.SeatCategory;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for physical seat master data details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {

    private UUID id;
    private UUID screenId;
    private String rowNumber;
    private Integer seatNumber;
    private SeatCategory category;
    private boolean active;
}
