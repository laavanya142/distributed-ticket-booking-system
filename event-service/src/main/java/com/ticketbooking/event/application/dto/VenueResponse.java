package com.ticketbooking.event.application.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Immutable read-only response representation of a venue multiplex.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueResponse {
    private UUID id;
    private String name;
    private String address;
    private String city;
    private String postalCode;
    private String state;
    private String country;
    private Instant createdAt;
}
