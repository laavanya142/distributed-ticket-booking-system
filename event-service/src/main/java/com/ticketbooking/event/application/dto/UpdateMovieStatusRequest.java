package com.ticketbooking.event.application.dto;

import com.ticketbooking.event.domain.model.MovieStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for updating movie lifecycle status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMovieStatusRequest {

    @NotNull(message = "Movie status is required")
    private MovieStatus status;
}
