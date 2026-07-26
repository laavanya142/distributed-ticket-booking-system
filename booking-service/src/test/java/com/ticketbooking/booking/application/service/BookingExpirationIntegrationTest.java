package com.ticketbooking.booking.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.ticketbooking.booking.domain.entity.Booking;
import com.ticketbooking.booking.domain.entity.BookingItem;
import com.ticketbooking.booking.domain.entity.BookingStatus;
import com.ticketbooking.booking.domain.entity.OutboxEvent;
import com.ticketbooking.booking.domain.repository.BookingRepository;
import com.ticketbooking.booking.domain.repository.OutboxEventRepository;
import com.ticketbooking.booking.infrastructure.client.seat.SeatServiceClient;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class BookingExpirationIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockBean
    private SeatServiceClient seatServiceClient;

    @Test
    @DisplayName(
            "Successful Expiration: Transitions PENDING booking to EXPIRED/CANCELLED, releases seats, and creates booking.expired outbox event")
    void expireBooking_successfulExpiration() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        UUID lockToken = UUID.randomUUID();

        Booking booking = Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.PENDING)
                .lockToken(lockToken)
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey("idem-expire-success")
                .expiresAt(Instant.now().minusSeconds(10))
                .items(new ArrayList<>())
                .build();

        booking.addItem(BookingItem.builder()
                .showSeatId(seatId)
                .seatLabel("A-1")
                .category("REGULAR")
                .priceAtBooking(new BigDecimal("100.00"))
                .build());

        Booking savedBooking = bookingRepository.save(booking);

        bookingService.expireBooking(savedBooking.getId());

        Booking updated = bookingRepository.findById(savedBooking.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(BookingStatus.CANCELLED);

        // Verify Seat Service release was invoked
        verify(seatServiceClient).releaseSeats(eq(showId), eq(List.of(seatId)), eq(lockToken), eq(userId));

        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents).extracting(OutboxEvent::getEventType).contains("booking.expired");
    }

    @Test
    @DisplayName("Release Seat Failure: Logs error gracefully and completes expiration transition")
    void expireBooking_releaseSeatFailure_completesExpiration() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        UUID lockToken = UUID.randomUUID();

        Booking booking = Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.AWAITING_PAYMENT)
                .lockToken(lockToken)
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey("idem-release-fail")
                .expiresAt(Instant.now().minusSeconds(10))
                .items(new ArrayList<>())
                .build();

        booking.addItem(BookingItem.builder()
                .showSeatId(seatId)
                .seatLabel("A-1")
                .category("REGULAR")
                .priceAtBooking(new BigDecimal("100.00"))
                .build());

        Booking savedBooking = bookingRepository.save(booking);

        doThrow(new RuntimeException("Seat Service 500 Failure"))
                .when(seatServiceClient)
                .releaseSeats(any(), anyList(), any(), any());

        bookingService.expireBooking(savedBooking.getId());

        Booking updated = bookingRepository.findById(savedBooking.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }
}
