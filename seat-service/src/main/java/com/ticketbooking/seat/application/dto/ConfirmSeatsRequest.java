package com.ticketbooking.seat.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for confirming locked show seats post-payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmSeatsRequest {

    @NotEmpty(message = "Show seat IDs list cannot be empty")
    private List<UUID> showSeatIds;

    @NotNull(message = "Lock token cannot be null")
    private UUID lockToken;

    @NotNull(message = "User ID cannot be null")
    private UUID userId;
}
