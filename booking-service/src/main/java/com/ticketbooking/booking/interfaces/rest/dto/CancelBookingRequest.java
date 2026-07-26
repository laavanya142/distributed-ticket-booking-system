package com.ticketbooking.booking.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for cancelling a confirmed booking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for cancelling a confirmed booking")
public class CancelBookingRequest {

    @Schema(description = "Optional reason for cancellation", example = "USER_REQUESTED")
    private String reason;
}
