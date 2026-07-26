package com.ticketbooking.payment.domain.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result model returned by third-party payment gateway charges.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayResult {

    private boolean successful;
    private String providerTransactionId;
    private String failureReason;
    private String status;
}
