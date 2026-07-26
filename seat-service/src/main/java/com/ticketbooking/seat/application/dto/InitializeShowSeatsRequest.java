package com.ticketbooking.seat.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for pre-populating show seats for a newly scheduled show.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitializeShowSeatsRequest {

    @NotNull(message = "Screen ID cannot be null")
    private UUID screenId;

    @NotNull(message = "Base price cannot be null")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;
}
