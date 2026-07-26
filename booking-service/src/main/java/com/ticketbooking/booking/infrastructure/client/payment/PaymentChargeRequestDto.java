package com.ticketbooking.booking.infrastructure.client.payment;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload sent to Payment Service to charge a payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentChargeRequestDto {

    private UUID bookingId;
    private UUID userId;
    private UUID paymentMethodId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String idempotencyKey;
}
