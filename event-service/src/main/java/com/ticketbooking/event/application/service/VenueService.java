package com.ticketbooking.event.application.service;

import com.ticketbooking.event.application.dto.CreateVenueRequest;
import com.ticketbooking.event.application.dto.VenueResponse;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing Venue domain operations.
 */
public interface VenueService {
    VenueResponse createVenue(CreateVenueRequest request);

    List<VenueResponse> getVenuesByCity(String city);

    VenueResponse getVenueById(UUID id);
}
