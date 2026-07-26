package com.ticketbooking.analytics.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketbooking.analytics.application.model.AnalyticsSummaryDto;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class AnalyticsServiceIntegrationTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("Process Booking Created: Increments total_bookings metric")
    void testProcessBookingCreated() {
        analyticsService.processBookingCreated(UUID.randomUUID());
        analyticsService.processBookingCreated(UUID.randomUUID());

        AnalyticsSummaryDto summary = analyticsService.getAnalyticsSummary();
        assertThat(summary.getTotalBookings()).isEqualTo(2);
    }

    @Test
    @DisplayName("Process Booking Confirmed & Cancelled: Increments confirmed and cancelled metrics")
    void testProcessBookingConfirmedAndCancelled() {
        analyticsService.processBookingConfirmed(UUID.randomUUID());
        analyticsService.processBookingCancelled(UUID.randomUUID());

        AnalyticsSummaryDto summary = analyticsService.getAnalyticsSummary();
        assertThat(summary.getConfirmedBookings()).isEqualTo(1);
        assertThat(summary.getCancelledBookings()).isEqualTo(1);
    }

    @Test
    @DisplayName(
            "Process Payment Captured & Refunded: Updates successful payments, refunded payments, and total revenue")
    void testProcessPaymentCapturedAndRefunded() {
        analyticsService.processPaymentCaptured(UUID.randomUUID(), new BigDecimal("100.00"));
        analyticsService.processPaymentCaptured(UUID.randomUUID(), new BigDecimal("50.00"));
        analyticsService.processPaymentRefunded(UUID.randomUUID(), new BigDecimal("30.00"));

        AnalyticsSummaryDto summary = analyticsService.getAnalyticsSummary();
        assertThat(summary.getSuccessfulPayments()).isEqualTo(2);
        assertThat(summary.getRefundedPayments()).isEqualTo(1);
        assertThat(summary.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("120.00"));
    }
}
