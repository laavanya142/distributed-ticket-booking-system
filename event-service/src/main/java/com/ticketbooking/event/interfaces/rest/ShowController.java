package com.ticketbooking.event.interfaces.rest;

import com.ticketbooking.common.dto.ApiResponse;
import com.ticketbooking.event.application.dto.CreateShowRequest;
import com.ticketbooking.event.application.dto.ShowResponse;
import com.ticketbooking.event.application.dto.UpdateShowStatusRequest;
import com.ticketbooking.event.application.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing Show scheduling management endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/shows")
@RequiredArgsConstructor
@Tag(name = "Show Scheduling", description = "Endpoints for scheduling, searching, and managing movie shows")
public class ShowController {

    private final ShowService showService;

    @PostMapping
    @Operation(summary = "Schedule a new movie showtime")
    public ResponseEntity<ApiResponse<ShowResponse>> createShow(@Valid @RequestBody CreateShowRequest request) {
        log.info(
                "Received request to create show for movie: {} on screen: {}",
                request.getMovieId(),
                request.getScreenId());
        ShowResponse response = showService.createShow(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Show scheduled successfully", response));
    }

    @GetMapping
    @Operation(summary = "Search scheduled showtimes for a movie in a city on a specific date")
    public ResponseEntity<ApiResponse<List<ShowResponse>>> getShowsByMovieAndCity(
            @RequestParam UUID movieId,
            @RequestParam String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.debug("Received request to fetch shows for movie: {} in city: {} on date: {}", movieId, city, date);
        List<ShowResponse> list = showService.getShowsByMovieAndCity(movieId, city, date);
        return ResponseEntity.ok(ApiResponse.success("Shows retrieved successfully", list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get show details by unique ID")
    public ResponseEntity<ApiResponse<ShowResponse>> getShowById(@PathVariable UUID id) {
        log.debug("Received request to fetch show ID: {}", id);
        ShowResponse response = showService.getShowById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update show status (e.g. CANCELLED, COMPLETED)")
    public ResponseEntity<ApiResponse<ShowResponse>> updateShowStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateShowStatusRequest request) {
        log.info("Received request to update status for show ID: {} to {}", id, request.getStatus());
        ShowResponse response = showService.updateShowStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Show status updated successfully", response));
    }
}
