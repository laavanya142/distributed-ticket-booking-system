package com.ticketbooking.booking.infrastructure.client.payment;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload sent to Payment Service to refund a payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundRequestDto {

    private UUID bookingId;
    private UUID userId;
    private String reason;
    private String idempotencyKey;
}
