package com.ticketbooking.analytics.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketbooking.analytics.domain.entity.AnalyticsSummary;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class AnalyticsSummaryRepositoryIntegrationTest {

    @Autowired
    private AnalyticsSummaryRepository repository;

    @Test
    @DisplayName("Find analytics summary by metric name")
    void testFindByMetricName() {
        repository.save(AnalyticsSummary.builder()
                .metricName("total_bookings")
                .metricValue(new BigDecimal("10"))
                .build());

        Optional<AnalyticsSummary> found = repository.findByMetricName("total_bookings");
        assertThat(found).isPresent();
        assertThat(found.get().getMetricValue()).isEqualByComparingTo(new BigDecimal("10"));

        Optional<AnalyticsSummary> missing = repository.findByMetricName("non_existent");
        assertThat(missing).isEmpty();
    }
}
