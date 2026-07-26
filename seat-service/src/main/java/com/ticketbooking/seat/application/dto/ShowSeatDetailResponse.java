package com.ticketbooking.seat.application.dto;

import com.ticketbooking.seat.domain.entity.SeatCategory;
import com.ticketbooking.seat.domain.entity.ShowSeatStatus;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for individual seat details in a show seat map.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeatDetailResponse {

    private UUID showSeatId;
    private UUID seatId;
    private String rowNumber;
    private Integer seatNumber;
    private SeatCategory category;
    private BigDecimal price;
    private ShowSeatStatus status;
}
