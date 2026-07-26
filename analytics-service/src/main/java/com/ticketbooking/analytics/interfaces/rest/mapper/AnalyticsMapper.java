package com.ticketbooking.analytics.interfaces.rest.mapper;

import com.ticketbooking.analytics.application.model.AnalyticsSummaryDto;
import com.ticketbooking.analytics.interfaces.rest.dto.AnalyticsSummaryResponse;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper converting between AnalyticsSummaryDto and AnalyticsSummaryResponse REST payload.
 */
@Mapper(componentModel = "spring")
public interface AnalyticsMapper {

    /**
     * Maps AnalyticsSummaryDto application model to AnalyticsSummaryResponse DTO.
     *
     * @param dto Application DTO model.
     * @return AnalyticsSummaryResponse payload.
     */
    AnalyticsSummaryResponse toAnalyticsSummaryResponse(AnalyticsSummaryDto dto);
}
