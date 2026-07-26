package com.ticketbooking.analytics.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketbooking.analytics.application.service.AnalyticsService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class AnalyticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("GET /api/v1/analytics/summary: Returns HTTP 200 with metrics dashboard envelope")
    void getAnalyticsSummary_returnsMetrics() throws Exception {
        analyticsService.processBookingCreated(UUID.randomUUID());
        analyticsService.processBookingConfirmed(UUID.randomUUID());
        analyticsService.processPaymentCaptured(UUID.randomUUID(), new BigDecimal("200.00"));

        mockMvc.perform(get("/api/v1/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalBookings").value(1))
                .andExpect(jsonPath("$.data.confirmedBookings").value(1))
                .andExpect(jsonPath("$.data.successfulPayments").value(1))
                .andExpect(jsonPath("$.data.totalRevenue").value(200.00));
    }
}
