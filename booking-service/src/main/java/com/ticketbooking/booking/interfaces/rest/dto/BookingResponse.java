package com.ticketbooking.booking.interfaces.rest.dto;

import com.ticketbooking.booking.domain.entity.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload representing full booking details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full booking details response payload")
public class BookingResponse {

    @Schema(description = "Booking ID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID id;

    @Schema(description = "Owning User ID", example = "b1a2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID userId;

    @Schema(description = "Show ID", example = "c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")
    private UUID showId;

    @Schema(description = "Current lifecycle status", example = "CONFIRMED")
    private BookingStatus status;

    @Schema(description = "Total price calculated server-side", example = "240.00")
    private BigDecimal totalAmount;

    @Schema(description = "Currency ISO code", example = "USD")
    private String currency;

    @Schema(description = "Payment ID reference if charged", example = "d1e2f3a4-b5c6-7d8e-9f0a-1b2c3d4e5f6a")
    private UUID paymentId;

    @Schema(description = "Issued confirmation code (BK-YYYYMMDD-XXXXXX)", example = "BK-20260727-A3K9PZ")
    private String confirmationCode;

    @Schema(description = "Expiration timestamp of the checkout window")
    private Instant expiresAt;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;

    @Schema(description = "List of booked seat items")
    private List<BookingItemResponse> items;
}
