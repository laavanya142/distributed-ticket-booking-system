package com.ticketbooking.analytics.domain.repository;

import com.ticketbooking.analytics.domain.entity.AnalyticsSummary;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for AnalyticsSummary metric persistence operations.
 */
@Repository
public interface AnalyticsSummaryRepository extends JpaRepository<AnalyticsSummary, UUID> {

    Optional<AnalyticsSummary> findByMetricName(String metricName);
}
