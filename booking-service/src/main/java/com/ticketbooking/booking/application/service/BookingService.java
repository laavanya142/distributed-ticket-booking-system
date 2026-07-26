package com.ticketbooking.booking.application.service;

import com.ticketbooking.booking.application.model.CreateBookingCommand;
import com.ticketbooking.booking.domain.entity.Booking;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Primary application service interface orchestrating booking checkout, confirmation, cancellation, and expiration flows.
 */
public interface BookingService {

    /**
     * Initiates and orchestrates booking checkout (lock verification, PENDING persistence, payment charge, confirmation).
     *
     * @param command Create booking command parameters.
     * @return Completed Booking entity.
     */
    Booking createBooking(CreateBookingCommand command);

    /**
     * Fetches details of a specific booking owned by the specified user or admin.
     *
     * @param bookingId Booking identifier.
     * @param userId Requesting user identifier.
     * @return Matching Booking entity.
     */
    Booking getBooking(UUID bookingId, UUID userId);

    /**
     * Returns a paginated list of bookings owned by a user.
     *
     * @param userId Requesting user identifier.
     * @param pageable Page parameters.
     * @return Page of user's bookings.
     */
    Page<Booking> getUserBookings(UUID userId, Pageable pageable);

    /**
     * User-initiated cancellation of a confirmed booking (refund, unbook seats, publish outbox event).
     *
     * @param bookingId Booking identifier.
     * @param userId Owning user identifier.
     * @param reason Cancellation reason.
     * @return Updated cancelled Booking entity.
     */
    Booking cancelBooking(UUID bookingId, UUID userId, String reason);

    /**
     * Background expiration sweeper workflow processing an expired booking (release seats, outbox event).
     *
     * @param bookingId Expired booking identifier.
     */
    void expireBooking(UUID bookingId);
}
