package com.ticketbooking.analytics.interfaces.rest;

import com.ticketbooking.analytics.application.model.AnalyticsSummaryDto;
import com.ticketbooking.analytics.application.service.AnalyticsService;
import com.ticketbooking.analytics.interfaces.rest.dto.AnalyticsSummaryResponse;
import com.ticketbooking.analytics.interfaces.rest.mapper.AnalyticsMapper;
import com.ticketbooking.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin REST controller exposing dashboard analytics summary metrics.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@Slf4j
@RequiredArgsConstructor
@Tag(
        name = "Analytics Reporting API",
        description = "Endpoints for fetching system-wide booking, payment, and revenue dashboard analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsMapper analyticsMapper;

    /**
     * Retrieves system-wide aggregated metrics summary dashboard.
     *
     * @return ResponseEntity with AnalyticsSummaryResponse payload.
     */
    @GetMapping("/summary")
    @Operation(
            summary = "Get analytics summary",
            description = "Fetches system-wide booking, payment, and total revenue aggregation metrics")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Analytics summary metrics retrieved successfully")
    })
    public ResponseEntity<ApiResponse<AnalyticsSummaryResponse>> getAnalyticsSummary() {
        log.debug("Received request to fetch analytics summary dashboard");

        AnalyticsSummaryDto summaryDto = analyticsService.getAnalyticsSummary();
        AnalyticsSummaryResponse response = analyticsMapper.toAnalyticsSummaryResponse(summaryDto);

        return ResponseEntity.ok(ApiResponse.success("Analytics summary retrieved successfully", response));
    }
}
