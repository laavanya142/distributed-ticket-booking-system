package com.ticketbooking.booking.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.ticketbooking.booking.domain.entity.Booking;
import com.ticketbooking.booking.domain.entity.BookingItem;
import com.ticketbooking.booking.domain.entity.BookingStatus;
import com.ticketbooking.booking.domain.entity.OutboxEvent;
import com.ticketbooking.booking.domain.exception.BookingAccessDeniedException;
import com.ticketbooking.booking.domain.exception.BookingStateTransitionException;
import com.ticketbooking.booking.domain.repository.BookingRepository;
import com.ticketbooking.booking.domain.repository.OutboxEventRepository;
import com.ticketbooking.booking.infrastructure.client.payment.PaymentRefundResult;
import com.ticketbooking.booking.infrastructure.client.payment.PaymentServiceClient;
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
class BookingCancellationIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockBean
    private SeatServiceClient seatServiceClient;

    @MockBean
    private PaymentServiceClient paymentServiceClient;

    @Test
    @DisplayName("Successful Cancellation: Refunds payment, unbooks seats, and creates outbox events")
    void cancelBooking_successfulCancellation() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        UUID lockToken = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Booking booking = Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.CONFIRMED)
                .lockToken(lockToken)
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .paymentId(paymentId)
                .idempotencyKey("idem-cancel-success")
                .confirmationCode("BK-20260727-A1B2C3")
                .expiresAt(Instant.now().plusSeconds(600))
                .items(new ArrayList<>())
                .build();

        booking.addItem(BookingItem.builder()
                .showSeatId(seatId)
                .seatLabel("A-1")
                .category("REGULAR")
                .priceAtBooking(new BigDecimal("100.00"))
                .build());

        Booking savedBooking = bookingRepository.save(booking);

        given(paymentServiceClient.refundPayment(
                        eq(paymentId), eq(savedBooking.getId()), eq(userId), anyString(), anyString()))
                .willReturn(PaymentRefundResult.builder()
                        .refundId(UUID.randomUUID())
                        .successful(true)
                        .build());

        Booking result = bookingService.cancelBooking(savedBooking.getId(), userId, "USER_REQUESTED");

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);

        // Verify Payment refund and Seat unbook calls
        verify(paymentServiceClient)
                .refundPayment(eq(paymentId), eq(savedBooking.getId()), eq(userId), anyString(), anyString());
        verify(seatServiceClient).unbookSeats(eq(showId), eq(List.of(seatId)), eq(lockToken), eq(userId));

        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents)
                .extracting(OutboxEvent::getEventType)
                .contains("booking.cancellation_requested", "booking.cancelled");
    }

    @Test
    @DisplayName("Unauthorized Cancellation: Throws BookingAccessDeniedException if userId does not match owner")
    void cancelBooking_unauthorizedUser_throwsException() {
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();

        Booking booking = bookingRepository.save(Booking.builder()
                .userId(ownerId)
                .showId(UUID.randomUUID())
                .status(BookingStatus.CONFIRMED)
                .lockToken(UUID.randomUUID())
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey("idem-cancel-unauth")
                .expiresAt(Instant.now().plusSeconds(600))
                .build());

        assertThatThrownBy(() -> bookingService.cancelBooking(booking.getId(), attackerId, "USER_REQUESTED"))
                .isInstanceOf(BookingAccessDeniedException.class);
    }

    @Test
    @DisplayName("Invalid State Transition: Throws BookingStateTransitionException if booking is already CANCELLED")
    void cancelBooking_alreadyCancelled_throwsException() {
        UUID userId = UUID.randomUUID();

        Booking booking = bookingRepository.save(Booking.builder()
                .userId(userId)
                .showId(UUID.randomUUID())
                .status(BookingStatus.CANCELLED)
                .lockToken(UUID.randomUUID())
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey("idem-already-cancelled")
                .expiresAt(Instant.now().plusSeconds(600))
                .build());

        assertThatThrownBy(() -> bookingService.cancelBooking(booking.getId(), userId, "USER_REQUESTED"))
                .isInstanceOf(BookingStateTransitionException.class);
    }

    @Test
    @DisplayName("Refund Failure: Logs booking.refund_failed outbox event, unbooks seats, and marks CANCELLED")
    void cancelBooking_refundFailure_logsRefundFailedEventAndContinuesUnbook() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        UUID lockToken = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Booking booking = Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.CONFIRMED)
                .lockToken(lockToken)
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .paymentId(paymentId)
                .idempotencyKey("idem-refund-fail")
                .confirmationCode("BK-20260727-REFFAIL")
                .expiresAt(Instant.now().plusSeconds(600))
                .items(new ArrayList<>())
                .build();

        booking.addItem(BookingItem.builder()
                .showSeatId(seatId)
                .seatLabel("A-1")
                .category("REGULAR")
                .priceAtBooking(new BigDecimal("100.00"))
                .build());

        Booking savedBooking = bookingRepository.save(booking);

        given(paymentServiceClient.refundPayment(any(), any(), any(), any(), any()))
                .willReturn(PaymentRefundResult.builder()
                        .successful(false)
                        .failureReason("REFUND_DECLINED")
                        .build());

        Booking result = bookingService.cancelBooking(savedBooking.getId(), userId, "USER_REQUESTED");

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);

        // Verify unbookSeats was still executed
        verify(seatServiceClient).unbookSeats(eq(showId), eq(List.of(seatId)), eq(lockToken), eq(userId));

        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents)
                .extracting(OutboxEvent::getEventType)
                .contains("booking.refund_failed", "booking.cancelled");
    }

    @Test
    @DisplayName("Seat Unbook Failure: Completes cancellation and logs error gracefully")
    void cancelBooking_unbookFailure_completesCancellation() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        UUID lockToken = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Booking booking = Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.CONFIRMED)
                .lockToken(lockToken)
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .paymentId(paymentId)
                .idempotencyKey("idem-unbook-fail")
                .confirmationCode("BK-20260727-UNBOOKFAIL")
                .expiresAt(Instant.now().plusSeconds(600))
                .items(new ArrayList<>())
                .build();

        booking.addItem(BookingItem.builder()
                .showSeatId(seatId)
                .seatLabel("A-1")
                .category("REGULAR")
                .priceAtBooking(new BigDecimal("100.00"))
                .build());

        Booking savedBooking = bookingRepository.save(booking);

        given(paymentServiceClient.refundPayment(any(), any(), any(), any(), any()))
                .willReturn(PaymentRefundResult.builder().successful(true).build());

        doThrow(new RuntimeException("Seat Service 503"))
                .when(seatServiceClient)
                .unbookSeats(any(), anyList(), any(), any());

        Booking result = bookingService.cancelBooking(savedBooking.getId(), userId, "USER_REQUESTED");

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }
}
