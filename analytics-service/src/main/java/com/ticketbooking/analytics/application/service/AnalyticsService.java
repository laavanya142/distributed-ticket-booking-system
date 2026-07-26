package com.ticketbooking.analytics.application.service;

import com.ticketbooking.analytics.application.model.AnalyticsSummaryDto;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service interface for processing event-driven analytics aggregations and querying dashboard metrics.
 */
public interface AnalyticsService {

    void processBookingCreated(UUID bookingId);

    void processBookingConfirmed(UUID bookingId);

    void processBookingCancelled(UUID bookingId);

    void processPaymentCaptured(UUID paymentId, BigDecimal amount);

    void processPaymentRefunded(UUID paymentId, BigDecimal amount);

    void updateSummary(String metricName, BigDecimal delta);

    AnalyticsSummaryDto getAnalyticsSummary();
}
