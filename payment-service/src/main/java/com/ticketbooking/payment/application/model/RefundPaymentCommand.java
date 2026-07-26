package com.ticketbooking.payment.application.model;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command object encapsulating payment refund data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundPaymentCommand {

    private UUID paymentId;
    private UUID bookingId;
    private UUID userId;
    private BigDecimal amount;
    private String currency;
    private String reason;
    private String idempotencyKey;
}
