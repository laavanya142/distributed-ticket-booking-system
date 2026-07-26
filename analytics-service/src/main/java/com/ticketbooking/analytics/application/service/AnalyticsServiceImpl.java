package com.ticketbooking.analytics.application.service;

import com.ticketbooking.analytics.application.model.AnalyticsSummaryDto;
import com.ticketbooking.analytics.domain.entity.AnalyticsSummary;
import com.ticketbooking.analytics.domain.repository.AnalyticsSummaryRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AnalyticsService performing real-time metric incrementing and summary aggregation.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    public static final String METRIC_TOTAL_BOOKINGS = "total_bookings";
    public static final String METRIC_CONFIRMED_BOOKINGS = "confirmed_bookings";
    public static final String METRIC_CANCELLED_BOOKINGS = "cancelled_bookings";
    public static final String METRIC_SUCCESSFUL_PAYMENTS = "successful_payments";
    public static final String METRIC_REFUNDED_PAYMENTS = "refunded_payments";
    public static final String METRIC_TOTAL_REVENUE = "total_revenue";

    private final AnalyticsSummaryRepository repository;

    @Override
    @Transactional
    public void processBookingCreated(UUID bookingId) {
        log.info("Processing analytics for booking created: {}", bookingId);
        updateSummary(METRIC_TOTAL_BOOKINGS, BigDecimal.ONE);
    }

    @Override
    @Transactional
    public void processBookingConfirmed(UUID bookingId) {
        log.info("Processing analytics for booking confirmed: {}", bookingId);
        updateSummary(METRIC_CONFIRMED_BOOKINGS, BigDecimal.ONE);
    }

    @Override
    @Transactional
    public void processBookingCancelled(UUID bookingId) {
        log.info("Processing analytics for booking cancelled: {}", bookingId);
        updateSummary(METRIC_CANCELLED_BOOKINGS, BigDecimal.ONE);
    }

    @Override
    @Transactional
    public void processPaymentCaptured(UUID paymentId, BigDecimal amount) {
        log.info("Processing analytics for payment captured: {}, amount: {}", paymentId, amount);
        updateSummary(METRIC_SUCCESSFUL_PAYMENTS, BigDecimal.ONE);
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            updateSummary(METRIC_TOTAL_REVENUE, amount);
        }
    }

    @Override
    @Transactional
    public void processPaymentRefunded(UUID paymentId, BigDecimal amount) {
        log.info("Processing analytics for payment refunded: {}, amount: {}", paymentId, amount);
        updateSummary(METRIC_REFUNDED_PAYMENTS, BigDecimal.ONE);
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            updateSummary(METRIC_TOTAL_REVENUE, amount.negate());
        }
    }

    @Override
    @Transactional
    public void updateSummary(String metricName, BigDecimal delta) {
        Optional<AnalyticsSummary> existing = repository.findByMetricName(metricName);
        AnalyticsSummary summary;
        if (existing.isPresent()) {
            summary = existing.get();
            summary.setMetricValue(summary.getMetricValue().add(delta));
            summary.setLastUpdated(Instant.now());
        } else {
            summary = AnalyticsSummary.builder()
                    .metricName(metricName)
                    .metricValue(delta)
                    .lastUpdated(Instant.now())
                    .build();
        }
        repository.save(summary);
        log.debug("Updated metric {} by {}. New value: {}", metricName, delta, summary.getMetricValue());
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsSummaryDto getAnalyticsSummary() {
        long totalBookings = getMetricLong(METRIC_TOTAL_BOOKINGS);
        long confirmedBookings = getMetricLong(METRIC_CONFIRMED_BOOKINGS);
        long cancelledBookings = getMetricLong(METRIC_CANCELLED_BOOKINGS);
        long successfulPayments = getMetricLong(METRIC_SUCCESSFUL_PAYMENTS);
        long refundedPayments = getMetricLong(METRIC_REFUNDED_PAYMENTS);
        BigDecimal totalRevenue = getMetricBigDecimal(METRIC_TOTAL_REVENUE);

        return AnalyticsSummaryDto.builder()
                .totalBookings(totalBookings)
                .confirmedBookings(confirmedBookings)
                .cancelledBookings(cancelledBookings)
                .successfulPayments(successfulPayments)
                .refundedPayments(refundedPayments)
                .totalRevenue(totalRevenue)
                .build();
    }

    private long getMetricLong(String metricName) {
        return repository
                .findByMetricName(metricName)
                .map(s -> s.getMetricValue().longValue())
                .orElse(0L);
    }

    private BigDecimal getMetricBigDecimal(String metricName) {
        return repository
                .findByMetricName(metricName)
                .map(AnalyticsSummary::getMetricValue)
                .orElse(BigDecimal.ZERO);
    }
}
