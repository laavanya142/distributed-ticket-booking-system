package com.ticketbooking.booking.infrastructure.client.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result payload returned from Payment Service charge call.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentChargeResult {

    private UUID paymentId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private Instant capturedAt;
    private boolean successful;
    private String failureReason;
}
