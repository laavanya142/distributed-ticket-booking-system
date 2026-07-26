package com.ticketbooking.booking.infrastructure.client.seat;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing a verified seat detail from Seat Service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDetailDto {

    private UUID showSeatId;
    private String seatLabel;
    private String category;
    private BigDecimal price;
}
