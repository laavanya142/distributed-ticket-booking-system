package com.ticketbooking.event.application.service;

import com.ticketbooking.event.application.dto.CreateScreenRequest;
import com.ticketbooking.event.application.dto.ScreenResponse;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing Screen domain operations.
 */
public interface ScreenService {
    ScreenResponse createScreen(UUID venueId, CreateScreenRequest request);

    List<ScreenResponse> getScreensByVenue(UUID venueId);

    ScreenResponse getScreenById(UUID id);
}
