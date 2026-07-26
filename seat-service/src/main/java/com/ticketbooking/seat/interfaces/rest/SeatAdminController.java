package com.ticketbooking.seat.interfaces.rest;

import com.ticketbooking.common.dto.ApiResponse;
import com.ticketbooking.seat.application.dto.CreateScreenSeatsRequest;
import com.ticketbooking.seat.application.dto.SeatResponse;
import com.ticketbooking.seat.application.mapper.SeatMapper;
import com.ticketbooking.seat.application.service.SeatService;
import com.ticketbooking.seat.domain.entity.Seat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for physical seat layout administration.
 */
@RestController
@RequestMapping("/api/v1/screens")
@Validated
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Seat Layout Management", description = "APIs for managing physical seat layouts of venue screens")
public class SeatAdminController {

    private final SeatService seatService;
    private final SeatMapper seatMapper;

    /**
     * Batch creates physical seat configurations for a venue screen.
     *
     * @param screenId Venue screen identifier.
     * @param request Payload containing single seat definitions.
     * @return ResponseEntity with created seat response list.
     */
    @PostMapping("/{screenId}/seats")
    @Operation(
            summary = "Create screen seats",
            description = "Batch creates physical seat layout definitions for a venue screen")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> createScreenSeats(
            @PathVariable UUID screenId, @Valid @RequestBody CreateScreenSeatsRequest request) {
        log.info("Received request to batch create seats for screen ID: {}", screenId);
        List<Seat> seatsToCreate = seatMapper.toSeatList(request.getSeats());
        List<Seat> createdSeats = seatService.createScreenSeats(screenId, seatsToCreate);
        List<SeatResponse> response = seatMapper.toSeatResponseList(createdSeats);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Screen seats created successfully", response));
    }
}
