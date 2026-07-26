package com.ticketbooking.analytics.application.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object encapsulating aggregated analytics metrics dashboard data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryDto {

    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private long successfulPayments;
    private long refundedPayments;
    private BigDecimal totalRevenue;
}
