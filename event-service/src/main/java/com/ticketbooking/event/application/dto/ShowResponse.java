package com.ticketbooking.event.application.dto;

import com.ticketbooking.event.domain.model.ShowStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Immutable read-only response representation of a movie show.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowResponse {
    private UUID id;
    private UUID movieId;
    private String movieTitle;
    private UUID screenId;
    private String screenName;
    private UUID venueId;
    private String venueName;
    private String city;
    private Instant startTime;
    private Instant endTime;
    private BigDecimal basePrice;
    private ShowStatus status;
    private Instant createdAt;
}
