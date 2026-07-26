package com.ticketbooking.booking.infrastructure.scheduler;

import com.ticketbooking.booking.application.service.BookingService;
import com.ticketbooking.booking.domain.entity.Booking;
import com.ticketbooking.booking.domain.entity.BookingStatus;
import com.ticketbooking.booking.domain.repository.BookingRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled background sweeper to detect and clean up stale expired bookings.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BookingExpiryScheduler {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    @Scheduled(cron = "${booking.expiry-sweeper.cron:0 * * * * *}")
    public void processExpiredBookings() {
        Instant now = Instant.now();
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(
                List.of(BookingStatus.PENDING, BookingStatus.AWAITING_PAYMENT), now);

        if (expiredBookings.isEmpty()) {
            return;
        }

        log.info("Found {} expired bookings for cleanup", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                bookingService.expireBooking(booking.getId());
            } catch (ObjectOptimisticLockingFailureException ex) {
                log.info(
                        "Booking {} was updated concurrently (payment likely completed); skipping sweep",
                        booking.getId());
            } catch (Exception ex) {
                log.error(
                        "Unexpected error during expiration sweep for booking ID {}: {}",
                        booking.getId(),
                        ex.getMessage(),
                        ex);
            }
        }
    }
}
