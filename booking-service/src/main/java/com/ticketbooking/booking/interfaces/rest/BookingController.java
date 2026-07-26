package com.ticketbooking.booking.interfaces.rest;

import com.ticketbooking.booking.application.model.CreateBookingCommand;
import com.ticketbooking.booking.application.service.BookingService;
import com.ticketbooking.booking.domain.entity.Booking;
import com.ticketbooking.booking.interfaces.rest.dto.BookingResponse;
import com.ticketbooking.booking.interfaces.rest.dto.CancelBookingRequest;
import com.ticketbooking.booking.interfaces.rest.dto.CreateBookingRequest;
import com.ticketbooking.booking.interfaces.rest.mapper.BookingMapper;
import com.ticketbooking.common.constants.HeaderConstants;
import com.ticketbooking.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints for booking creation, retrieval, and cancellation.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@Validated
@Slf4j
@RequiredArgsConstructor
@Tag(
        name = "Booking Management",
        description = "Saga Orchestrator APIs for checkout creation, retrieval, and cancellation")
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    /**
     * Initiates booking checkout (lock verification, PENDING persistence, payment charge, confirmation).
     *
     * @param userId Authenticated user ID injected by Gateway.
     * @param idempotencyKey Client idempotency deduplication header.
     * @param request Booking creation request payload.
     * @return ResponseEntity with created BookingResponse.
     */
    @PostMapping
    @Operation(
            summary = "Create booking",
            description = "Initiates the checkout Saga: verifies seat locks, charges payment, and confirms seats")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "Booking created and confirmed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "402",
                description = "Payment charge failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "Seats unavailable or lock token mismatch")
    })
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Parameter(hidden = true) @RequestHeader(value = HeaderConstants.USER_ID) UUID userId,
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateBookingRequest request) {
        log.info("Received booking request from user ID: {} for show ID: {}", userId, request.getShowId());

        CreateBookingCommand command = bookingMapper.toCommand(request, userId, idempotencyKey);
        Booking booking = bookingService.createBooking(command);
        BookingResponse response = bookingMapper.toBookingResponse(booking);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", response));
    }

    /**
     * Retrieves details of a specific booking owned by the authenticated user.
     *
     * @param bookingId Booking identifier.
     * @param userId Authenticated user ID.
     * @return ResponseEntity with BookingResponse payload.
     */
    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking details", description = "Fetches details of a specific booking owned by the user")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Booking details retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "User not authorized to view this booking"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @PathVariable UUID bookingId,
            @Parameter(hidden = true) @RequestHeader(value = HeaderConstants.USER_ID) UUID userId) {
        log.debug("Received request to fetch booking ID: {} for user ID: {}", bookingId, userId);

        Booking booking = bookingService.getBooking(bookingId, userId);
        BookingResponse response = bookingMapper.toBookingResponse(booking);

        return ResponseEntity.ok(ApiResponse.success("Booking details retrieved successfully", response));
    }

    /**
     * Returns a paginated list of bookings for the authenticated user.
     *
     * @param userId Authenticated user ID.
     * @param page Page index (0-indexed).
     * @param size Page size.
     * @return ResponseEntity with Page of BookingResponse.
     */
    @GetMapping
    @Operation(
            summary = "List user bookings",
            description = "Retrieves a paginated list of bookings owned by the authenticated user")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Paginated list of bookings retrieved")
    })
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getUserBookings(
            @Parameter(hidden = true) @RequestHeader(value = HeaderConstants.USER_ID) UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Received request to fetch bookings page {} size {} for user ID: {}", page, size, userId);

        Page<Booking> bookings = bookingService.getUserBookings(userId, PageRequest.of(page, size));
        Page<BookingResponse> responsePage = bookings.map(bookingMapper::toBookingResponse);

        return ResponseEntity.ok(ApiResponse.success("User bookings retrieved successfully", responsePage));
    }

    /**
     * Cancels a confirmed booking (issues refund and unbooks seats).
     *
     * @param bookingId Booking identifier.
     * @param userId Authenticated user ID.
     * @param request Cancellation request payload.
     * @return ResponseEntity with updated cancelled BookingResponse.
     */
    @PostMapping("/{bookingId}/cancel")
    @Operation(
            summary = "Cancel booking",
            description = "Cancels a confirmed booking, refunds payment, and unbooks seats")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Booking cancelled successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "User not authorized to cancel this booking"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "Booking is not in a cancellable state")
    })
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable UUID bookingId,
            @Parameter(hidden = true) @RequestHeader(value = HeaderConstants.USER_ID) UUID userId,
            @Valid @RequestBody(required = false) CancelBookingRequest request) {
        log.info("Received request to cancel booking ID: {} for user ID: {}", bookingId, userId);

        String reason = request != null ? request.getReason() : "USER_REQUESTED";
        Booking booking = bookingService.cancelBooking(bookingId, userId, reason);
        BookingResponse response = bookingMapper.toBookingResponse(booking);

        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }
}
