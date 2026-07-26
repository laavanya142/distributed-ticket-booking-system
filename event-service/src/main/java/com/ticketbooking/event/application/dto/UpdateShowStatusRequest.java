package com.ticketbooking.event.application.dto;

import com.ticketbooking.event.domain.model.ShowStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for updating show status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShowStatusRequest {

    @NotNull(message = "Show status is required")
    private ShowStatus status;
}
