package com.ticketbooking.event.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for scheduling a movie show.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShowRequest {

    @NotNull(message = "Movie ID is required")
    private UUID movieId;

    @NotNull(message = "Screen ID is required")
    private UUID screenId;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private Instant startTime;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
    private BigDecimal basePrice;
}
