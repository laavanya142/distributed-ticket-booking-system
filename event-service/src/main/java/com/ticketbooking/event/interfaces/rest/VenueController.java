package com.ticketbooking.event.interfaces.rest;

import com.ticketbooking.common.dto.ApiResponse;
import com.ticketbooking.event.application.dto.CreateVenueRequest;
import com.ticketbooking.event.application.dto.VenueResponse;
import com.ticketbooking.event.application.service.VenueService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing Venue management endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
@Tag(name = "Venue Management", description = "Endpoints for creating and browsing theater venues")
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    @Operation(summary = "Register a new theater venue")
    public ResponseEntity<ApiResponse<VenueResponse>> createVenue(@Valid @RequestBody CreateVenueRequest request) {
        log.info("Received request to create venue: {} in {}", request.getName(), request.getCity());
        VenueResponse response = venueService.createVenue(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Venue registered successfully", response));
    }

    @GetMapping
    @Operation(summary = "List all venues in a specific city")
    public ResponseEntity<ApiResponse<List<VenueResponse>>> getVenuesByCity(@RequestParam String city) {
        log.debug("Received request to fetch venues for city: {}", city);
        List<VenueResponse> list = venueService.getVenuesByCity(city);
        return ResponseEntity.ok(ApiResponse.success("Venues retrieved successfully", list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get venue details by unique ID")
    public ResponseEntity<ApiResponse<VenueResponse>> getVenueById(@PathVariable UUID id) {
        log.debug("Received request to fetch venue ID: {}", id);
        VenueResponse response = venueService.getVenueById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
