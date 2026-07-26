package com.ticketbooking.booking.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating a new booking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a new booking")
public class CreateBookingRequest {

    @NotNull(message = "showId is required")
    @Schema(description = "UUID of the show being booked", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID showId;

    @NotEmpty(message = "showSeatIds cannot be empty")
    @Size(min = 1, max = 10, message = "Number of seats must be between 1 and 10")
    @Schema(
            description = "List of show seat UUIDs to book (max 10)",
            example = "[\"e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b\"]")
    private List<UUID> showSeatIds;

    @NotNull(message = "lockToken is required")
    @Schema(
            description = "Lock token issued by Seat Service during seat selection",
            example = "f1e2d3c4-b5a6-7f8e-9d0c-1b2a3f4e5d6c")
    private UUID lockToken;

    @NotNull(message = "paymentMethodId is required")
    @Schema(
            description = "UUID reference of the chosen payment method",
            example = "b1a2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID paymentMethodId;

    @NotBlank(message = "currency is required")
    @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code")
    @Schema(description = "ISO 4217 currency code", example = "USD")
    private String currency;
}
