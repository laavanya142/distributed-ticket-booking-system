package com.ticketbooking.booking.infrastructure.client.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result payload returned from Payment Service refund call.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundResult {

    private UUID refundId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private Instant refundedAt;
    private boolean successful;
    private String failureReason;
}
