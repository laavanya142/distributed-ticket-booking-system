package com.ticketbooking.booking.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload representing a seat item inside a booking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response details for a seat item within a booking")
public class BookingItemResponse {

    @Schema(description = "Item ID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID id;

    @Schema(description = "Show seat ID reference", example = "e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b")
    private UUID showSeatId;

    @Schema(description = "Seat label", example = "A-12")
    private String seatLabel;

    @Schema(description = "Seat category", example = "PREMIUM")
    private String category;

    @Schema(description = "Price frozen at booking time", example = "120.00")
    private BigDecimal priceAtBooking;
}
