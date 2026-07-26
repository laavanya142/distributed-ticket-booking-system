package com.ticketbooking.seat.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for batch creating physical seat layouts for a screen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateScreenSeatsRequest {

    @NotEmpty(message = "Seats list cannot be empty")
    @Valid
    private List<CreateSingleSeatRequest> seats;
}
