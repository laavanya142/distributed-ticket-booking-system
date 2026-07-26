package com.ticketbooking.payment.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for charging a payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for charging a payment")
public class ChargePaymentRequest {

    @NotNull(message = "bookingId is required")
    @Schema(description = "UUID of the booking being paid for", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID bookingId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than zero")
    @Schema(description = "Payment charge amount", example = "240.00")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code")
    @Schema(description = "ISO 4217 currency code", example = "USD")
    private String currency;

    @NotNull(message = "paymentMethodId is required")
    @Schema(description = "UUID reference of chosen payment method", example = "b1a2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID paymentMethodId;
}
