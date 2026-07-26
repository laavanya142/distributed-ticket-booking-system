package com.ticketbooking.booking.infrastructure.scheduler;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.ticketbooking.booking.application.service.BookingService;
import com.ticketbooking.booking.domain.entity.Booking;
import com.ticketbooking.booking.domain.entity.BookingStatus;
import com.ticketbooking.booking.domain.repository.BookingRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class BookingExpirySchedulerIntegrationTest {

    @Autowired
    private BookingExpiryScheduler bookingExpiryScheduler;

    @Autowired
    private BookingRepository bookingRepository;

    @MockBean
    private BookingService bookingService;

    @Test
    @DisplayName("Process Expired Bookings: Sweeps all expired bookings and continues processing when one fails")
    void processExpiredBookings_sweepsBatchAndIsolatesExceptions() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();

        Booking booking1 = bookingRepository.save(Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.PENDING)
                .lockToken(UUID.randomUUID())
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey("idem-sweep-1")
                .expiresAt(Instant.now().minusSeconds(60))
                .build());

        Booking booking2 = bookingRepository.save(Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.AWAITING_PAYMENT)
                .lockToken(UUID.randomUUID())
                .totalAmount(new BigDecimal("150.00"))
                .currency("USD")
                .idempotencyKey("idem-sweep-2")
                .expiresAt(Instant.now().minusSeconds(30))
                .build());

        // Simulate booking1 throwing an optimistic locking exception during sweep
        doThrow(new ObjectOptimisticLockingFailureException(Booking.class, booking1.getId()))
                .when(bookingService)
                .expireBooking(eq(booking1.getId()));

        bookingExpiryScheduler.processExpiredBookings();

        // Verify expireBooking was called for BOTH booking1 and booking2
        verify(bookingService).expireBooking(eq(booking1.getId()));
        verify(bookingService).expireBooking(eq(booking2.getId()));
    }
}
