package com.ticketbooking.seat.application.dto;

import jakarta.validation.constraints.Min;
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
 * Request DTO for acquiring atomic temporary locks on show seats during checkout.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockSeatsRequest {

    @NotEmpty(message = "Show seat IDs list cannot be empty")
    @Size(max = 10, message = "Cannot lock more than 10 seats per request")
    private List<UUID> showSeatIds;

    @NotNull(message = "Lock token cannot be null")
    private UUID lockToken;

    @Builder.Default
    @Min(value = 0, message = "TTL seconds cannot be negative")
    private long ttlSeconds = 0L;
}
