package com.ticketbooking.seat.application.dto;

import com.ticketbooking.seat.domain.entity.SeatCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Single seat specification within screen creation request payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSingleSeatRequest {

    @NotBlank(message = "Row number cannot be blank")
    @Size(max = 5, message = "Row number length cannot exceed 5 characters")
    private String rowNumber;

    @NotNull(message = "Seat number cannot be null")
    @Min(value = 1, message = "Seat number must be at least 1")
    private Integer seatNumber;

    @NotNull(message = "Seat category cannot be null")
    private SeatCategory category;
}
