package com.ticketbooking.event.application.service;

import com.ticketbooking.event.application.dto.CreateShowRequest;
import com.ticketbooking.event.application.dto.ShowResponse;
import com.ticketbooking.event.domain.model.ShowStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing Show domain operations.
 */
public interface ShowService {
    ShowResponse createShow(CreateShowRequest request);

    List<ShowResponse> getShowsByMovieAndCity(UUID movieId, String city, LocalDate date);

    ShowResponse getShowById(UUID id);

    ShowResponse updateShowStatus(UUID id, ShowStatus status);
}
