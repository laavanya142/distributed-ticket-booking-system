package com.ticketbooking.payment.interfaces.rest.dto;

import com.ticketbooking.payment.domain.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload representing payment transaction details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response details for a payment transaction")
public class PaymentResponse {

    @Schema(description = "Payment ID", example = "d1e2f3a4-b5c6-7d8e-9f0a-1b2c3d4e5f6a")
    private UUID id;

    @Schema(description = "Booking ID reference", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID bookingId;

    @Schema(description = "User ID reference", example = "b1a2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID userId;

    @Schema(description = "Payment amount", example = "240.00")
    private BigDecimal amount;

    @Schema(description = "ISO currency code", example = "USD")
    private String currency;

    @Schema(description = "Current payment status", example = "CAPTURED")
    private PaymentStatus status;

    @Schema(description = "Payment method ID reference", example = "c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")
    private UUID paymentMethodId;

    @Schema(description = "Provider transaction reference ID", example = "TX-8A7B6C5D")
    private String providerTransactionId;

    @Schema(description = "Reason for failure if status is FAILED or REFUND_FAILED", example = "INSUFFICIENT_FUNDS")
    private String failureReason;

    @Schema(description = "Payment creation timestamp")
    private Instant createdAt;

    @Schema(description = "Payment update timestamp")
    private Instant updatedAt;
}
