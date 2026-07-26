package com.ticketbooking.payment.domain.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result model returned by third-party payment gateway refunds.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundGatewayResult {

    private boolean successful;
    private String refundTransactionId;
    private String failureReason;
}
