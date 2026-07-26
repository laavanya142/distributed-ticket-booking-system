package com.ticketbooking.seat.interfaces.rest;

import com.ticketbooking.common.dto.ApiResponse;
import com.ticketbooking.seat.application.dto.InitializeShowSeatsRequest;
import com.ticketbooking.seat.application.dto.LockSeatsRequest;
import com.ticketbooking.seat.application.dto.LockSeatsResponse;
import com.ticketbooking.seat.application.dto.ReleaseSeatsRequest;
import com.ticketbooking.seat.application.dto.ShowSeatDetailResponse;
import com.ticketbooking.seat.application.dto.ShowSeatMapResponse;
import com.ticketbooking.seat.application.mapper.SeatMapper;
import com.ticketbooking.seat.application.service.SeatService;
import com.ticketbooking.seat.domain.entity.ShowSeat;
import com.ticketbooking.seat.domain.entity.ShowSeatStatus;
import com.ticketbooking.seat.infrastructure.redis.SeatLockManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for per-show seat map availability queries and atomic seat locking during checkout.
 */
@RestController
@RequestMapping("/api/v1/shows")
@Validated
@Slf4j
@RequiredArgsConstructor
@Tag(
        name = "Show Seat Inventory",
        description = "APIs for querying seat map availability and atomic seat locking during checkout")
public class ShowSeatController {

    private final SeatService seatService;
    private final SeatMapper seatMapper;
    private final SeatLockManager seatLockManager;

    /**
     * Retrieves the complete real-time seat availability map for a show.
     *
     * @param showId Show identifier.
     * @return ResponseEntity with seat map response payload.
     */
    @GetMapping("/{showId}/seats")
    @Operation(
            summary = "Fetch show seat map",
            description = "Retrieves real-time seat availability map and seat pricing for a show")
    public ResponseEntity<ApiResponse<ShowSeatMapResponse>> getShowSeatMap(@PathVariable UUID showId) {
        log.debug("Received request to fetch seat map for show ID: {}", showId);
        List<ShowSeat> showSeats = seatService.getShowSeatMap(showId);
        List<ShowSeatDetailResponse> seatDetails = seatMapper.toShowSeatDetailResponseList(showSeats);

        int total = showSeats.size();
        int available = 0;
        int locked = 0;
        int booked = 0;

        for (ShowSeat ss : showSeats) {
            if (ss.getStatus() == ShowSeatStatus.AVAILABLE) {
                available++;
            } else if (ss.getStatus() == ShowSeatStatus.LOCKED) {
                locked++;
            } else if (ss.getStatus() == ShowSeatStatus.BOOKED) {
                booked++;
            }
        }

        ShowSeatMapResponse mapResponse = ShowSeatMapResponse.builder()
                .showId(showId)
                .totalSeats(total)
                .availableSeats(available)
                .lockedSeats(locked)
                .bookedSeats(booked)
                .seats(seatDetails)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Show seat map retrieved successfully", mapResponse));
    }

    /**
     * Eagerly initializes per-show seat inventory when a show is created.
     *
     * @param showId Show identifier.
     * @param request Payload containing screenId and basePrice.
     * @return ResponseEntity with count of initialized seats.
     */
    @PostMapping("/{showId}/seats/initialize")
    @Operation(
            summary = "Initialize show seats",
            description = "Eagerly pre-populates per-show seat records for a newly created show")
    public ResponseEntity<ApiResponse<Integer>> initializeShowSeats(
            @PathVariable UUID showId, @Valid @RequestBody InitializeShowSeatsRequest request) {
        log.info("Received request to initialize seats for show ID: {}", showId);
        int count = seatService.initializeShowSeats(showId, request.getScreenId(), request.getBasePrice());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Show seats initialized successfully", count));
    }

    /**
     * Atomically locks a batch of show seats for checkout.
     *
     * @param showId Show identifier.
     * @param request Payload containing seat IDs, lockToken, and optional TTL.
     * @return ResponseEntity with LockSeatsResponse payload.
     */
    @PostMapping("/{showId}/seats/lock")
    @Operation(
            summary = "Lock seats",
            description = "Atomically holds a batch of seats for checkout using Redis Lua scripts")
    public ResponseEntity<ApiResponse<LockSeatsResponse>> lockSeats(
            @PathVariable UUID showId, @Valid @RequestBody LockSeatsRequest request) {
        log.info(
                "Received request to lock {} seats for show ID: {}",
                request.getShowSeatIds().size(),
                showId);
        List<ShowSeat> lockedSeats = seatService.lockSeats(
                showId, request.getShowSeatIds(), request.getLockToken(), request.getTtlSeconds());

        List<UUID> lockedSeatIds = new ArrayList<>(lockedSeats.size());
        for (ShowSeat ss : lockedSeats) {
            lockedSeatIds.add(ss.getId());
        }

        long ttl = request.getTtlSeconds() > 0 ? request.getTtlSeconds() : seatLockManager.getDefaultTtlSeconds();
        Instant lockedAt =
                lockedSeats.isEmpty() ? Instant.now() : lockedSeats.get(0).getLockedAt();
        Instant expiresAt = lockedAt.plusSeconds(ttl);

        LockSeatsResponse response = LockSeatsResponse.builder()
                .showId(showId)
                .lockToken(request.getLockToken())
                .lockedSeatIds(lockedSeatIds)
                .expiresAt(expiresAt)
                .ttlSeconds(ttl)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Seats locked successfully", response));
    }

    /**
     * Explicitly releases a batch of locked seats back to AVAILABLE status.
     *
     * @param showId Show identifier.
     * @param request Payload containing seat IDs and lockToken.
     * @return ResponseEntity with empty success payload.
     */
    @PostMapping("/{showId}/seats/release")
    @Operation(summary = "Release seats", description = "Explicitly releases locked seats back to AVAILABLE status")
    public ResponseEntity<ApiResponse<Void>> releaseSeats(
            @PathVariable UUID showId, @Valid @RequestBody ReleaseSeatsRequest request) {
        log.info(
                "Received request to release {} seats for show ID: {}",
                request.getShowSeatIds().size(),
                showId);
        seatService.releaseSeats(showId, request.getShowSeatIds(), request.getLockToken());
        return ResponseEntity.ok(ApiResponse.<Void>success("Seats released successfully", null));
    }
}
