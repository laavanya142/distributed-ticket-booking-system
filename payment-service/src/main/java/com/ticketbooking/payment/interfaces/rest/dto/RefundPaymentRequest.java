package com.ticketbooking.payment.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for refunding a payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for refunding a payment")
public class RefundPaymentRequest {

    @Schema(description = "Reason for refund request", example = "USER_CANCELLED_BOOKING")
    private String reason;
}
