package com.ticketbooking.analytics.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload containing system-wide analytics summary metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response details for aggregated system analytics metrics")
public class AnalyticsSummaryResponse {

    @Schema(description = "Total number of created bookings", example = "150")
    private long totalBookings;

    @Schema(description = "Total number of confirmed bookings", example = "120")
    private long confirmedBookings;

    @Schema(description = "Total number of cancelled bookings", example = "15")
    private long cancelledBookings;

    @Schema(description = "Total number of captured successful payments", example = "120")
    private long successfulPayments;

    @Schema(description = "Total number of refunded payments", example = "10")
    private long refundedPayments;

    @Schema(description = "Total accumulated net revenue", example = "24000.00")
    private BigDecimal totalRevenue;
}
