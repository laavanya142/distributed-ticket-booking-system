package com.ticketbooking.event.interfaces.rest;

import com.ticketbooking.common.dto.ApiResponse;
import com.ticketbooking.event.application.dto.CreateScreenRequest;
import com.ticketbooking.event.application.dto.ScreenResponse;
import com.ticketbooking.event.application.service.ScreenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing Screen auditorium management endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Screen Auditoriums", description = "Endpoints for managing screens within theater venues")
public class ScreenController {

    private final ScreenService screenService;

    @PostMapping("/venues/{venueId}/screens")
    @Operation(summary = "Create an auditorium screen inside a venue")
    public ResponseEntity<ApiResponse<ScreenResponse>> createScreen(
            @PathVariable UUID venueId, @Valid @RequestBody CreateScreenRequest request) {
        log.info("Received request to create screen: {} for venue ID: {}", request.getName(), venueId);
        ScreenResponse response = screenService.createScreen(venueId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Screen registered successfully", response));
    }

    @GetMapping("/venues/{venueId}/screens")
    @Operation(summary = "List all screens belonging to a venue")
    public ResponseEntity<ApiResponse<List<ScreenResponse>>> getScreensByVenue(@PathVariable UUID venueId) {
        log.debug("Received request to fetch screens for venue ID: {}", venueId);
        List<ScreenResponse> list = screenService.getScreensByVenue(venueId);
        return ResponseEntity.ok(ApiResponse.success("Screens retrieved successfully", list));
    }

    @GetMapping("/screens/{id}")
    @Operation(summary = "Get screen details by unique ID")
    public ResponseEntity<ApiResponse<ScreenResponse>> getScreenById(@PathVariable UUID id) {
        log.debug("Received request to fetch screen ID: {}", id);
        ScreenResponse response = screenService.getScreenById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
