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

import com.ticketbooking.booking.application.model.CreateBookingCommand;
import com.ticketbooking.booking.domain.entity.Booking;
import com.ticketbooking.booking.domain.entity.BookingStatus;
import com.ticketbooking.booking.domain.entity.OutboxEvent;
import com.ticketbooking.booking.domain.exception.DuplicateBookingException;
import com.ticketbooking.booking.domain.exception.PaymentFailedException;
import com.ticketbooking.booking.domain.exception.SeatVerificationFailedException;
import com.ticketbooking.booking.domain.repository.BookingRepository;
import com.ticketbooking.booking.domain.repository.OutboxEventRepository;
import com.ticketbooking.booking.infrastructure.client.payment.PaymentChargeResult;
import com.ticketbooking.booking.infrastructure.client.payment.PaymentServiceClient;
import com.ticketbooking.booking.infrastructure.client.seat.SeatDetailDto;
import com.ticketbooking.booking.infrastructure.client.seat.SeatServiceClient;
import com.ticketbooking.booking.infrastructure.client.seat.SeatVerificationResult;
import java.math.BigDecimal;
import java.time.Instant;
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
class BookingCreationIntegrationTest {

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
    @DisplayName(
            "Successful Booking Checkout: Verifies lock, charges payment, confirms seats, and creates outbox events")
    void createBooking_successfulCheckout() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        UUID lockToken = UUID.randomUUID();
        UUID paymentMethodId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        SeatDetailDto seatDetail = SeatDetailDto.builder()
                .showSeatId(seatId)
                .seatLabel("A-12")
                .category("PREMIUM")
                .price(new BigDecimal("120.00"))
                .build();

        given(seatServiceClient.verifySeats(eq(showId), eq(List.of(seatId)), eq(lockToken), eq(userId)))
                .willReturn(SeatVerificationResult.builder()
                        .valid(true)
                        .seats(List.of(seatDetail))
                        .ttlSeconds(600L)
                        .build());

        given(paymentServiceClient.chargePayment(any(), eq(userId), eq(paymentMethodId), any(), eq("USD"), anyString()))
                .willReturn(PaymentChargeResult.builder()
                        .paymentId(paymentId)
                        .status("CAPTURED")
                        .successful(true)
                        .build());

        CreateBookingCommand command = CreateBookingCommand.builder()
                .userId(userId)
                .showId(showId)
                .showSeatIds(List.of(seatId))
                .lockToken(lockToken)
                .paymentMethodId(paymentMethodId)
                .currency("USD")
                .idempotencyKey("idem-create-success")
                .build();

        Booking result = bookingService.createBooking(command);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(result.getPaymentId()).isEqualTo(paymentId);
        assertThat(result.getConfirmationCode()).startsWith("BK-");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getSeatLabel()).isEqualTo("A-12");

        // Verify Seat Service confirm was invoked
        verify(seatServiceClient).confirmSeats(eq(showId), eq(List.of(seatId)), eq(lockToken), eq(userId));

        // Verify outbox events (booking.created and booking.confirmed)
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents).extracting(OutboxEvent::getEventType).contains("booking.created", "booking.confirmed");
    }

    @Test
    @DisplayName("Duplicate Idempotency Key: Returns cached booking without repeating charge or lock verify")
    void createBooking_duplicateIdempotencyKey_returnsCachedBooking() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        UUID lockToken = UUID.randomUUID();
        UUID paymentMethodId = UUID.randomUUID();
        String idempotencyKey = "idem-duplicate-key";

        given(seatServiceClient.verifySeats(any(), anyList(), any(), any()))
                .willReturn(SeatVerificationResult.builder()
                        .valid(true)
                        .seats(List.of(SeatDetailDto.builder()
                                .showSeatId(seatId)
                                .seatLabel("A-1")
                                .category("REGULAR")
                                .price(new BigDecimal("100.00"))
                                .build()))
                        .ttlSeconds(600L)
                        .build());

        given(paymentServiceClient.chargePayment(any(), any(), any(), any(), any(), any()))
                .willReturn(PaymentChargeResult.builder()
                        .paymentId(UUID.randomUUID())
                        .successful(true)
                        .build());

        CreateBookingCommand command = CreateBookingCommand.builder()
                .userId(userId)
                .showId(showId)
                .showSeatIds(List.of(seatId))
                .lockToken(lockToken)
                .paymentMethodId(paymentMethodId)
                .currency("USD")
                .idempotencyKey(idempotencyKey)
                .build();

        Booking firstBooking = bookingService.createBooking(command);
        Booking secondBooking = bookingService.createBooking(command);

        assertThat(secondBooking.getId()).isEqualTo(firstBooking.getId());
    }

    @Test
    @DisplayName("Duplicate Active Booking: Throws DuplicateBookingException if active booking exists")
    void createBooking_duplicateActiveBooking_throwsException() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();

        bookingRepository.save(Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.PENDING)
                .lockToken(UUID.randomUUID())
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey("idem-dup-active")
                .expiresAt(Instant.now().plusSeconds(600))
                .build());

        CreateBookingCommand command = CreateBookingCommand.builder()
                .userId(userId)
                .showId(showId)
                .showSeatIds(List.of(UUID.randomUUID()))
                .lockToken(UUID.randomUUID())
                .paymentMethodId(UUID.randomUUID())
                .currency("USD")
                .idempotencyKey("idem-new-req")
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(command)).isInstanceOf(DuplicateBookingException.class);
    }

    @Test
    @DisplayName("Invalid Seat Verification: Throws SeatVerificationFailedException when verify returns false")
    void createBooking_invalidSeatVerification_throwsException() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();

        given(seatServiceClient.verifySeats(any(), anyList(), any(), any()))
                .willReturn(SeatVerificationResult.builder().valid(false).build());

        CreateBookingCommand command = CreateBookingCommand.builder()
                .userId(userId)
                .showId(showId)
                .showSeatIds(List.of(UUID.randomUUID()))
                .lockToken(UUID.randomUUID())
                .paymentMethodId(UUID.randomUUID())
                .currency("USD")
                .idempotencyKey("idem-invalid-verify")
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(command))
                .isInstanceOf(SeatVerificationFailedException.class);
    }

    @Test
    @DisplayName(
            "Payment Failure: Transitions to CANCELLED, compensates seat release, and throws PaymentFailedException")
    void createBooking_paymentFailure_executesCompensationAndThrowsException() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        UUID lockToken = UUID.randomUUID();

        given(seatServiceClient.verifySeats(any(), anyList(), any(), any()))
                .willReturn(SeatVerificationResult.builder()
                        .valid(true)
                        .seats(List.of(SeatDetailDto.builder()
                                .showSeatId(seatId)
                                .seatLabel("A-1")
                                .category("REGULAR")
                                .price(new BigDecimal("100.00"))
                                .build()))
                        .ttlSeconds(600L)
                        .build());

        given(paymentServiceClient.chargePayment(any(), any(), any(), any(), any(), any()))
                .willReturn(PaymentChargeResult.builder()
                        .successful(false)
                        .failureReason("CARD_DECLINED")
                        .build());

        CreateBookingCommand command = CreateBookingCommand.builder()
                .userId(userId)
                .showId(showId)
                .showSeatIds(List.of(seatId))
                .lockToken(lockToken)
                .paymentMethodId(UUID.randomUUID())
                .currency("USD")
                .idempotencyKey("idem-pay-fail")
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(command)).isInstanceOf(PaymentFailedException.class);

        // Verify compensating seat release was called
        verify(seatServiceClient).releaseSeats(eq(showId), eq(List.of(seatId)), eq(lockToken), eq(userId));

        // Verify outbox events contain booking.created and booking.cancelled
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents).extracting(OutboxEvent::getEventType).contains("booking.created", "booking.cancelled");
    }

    @Test
    @DisplayName(
            "Seat Confirmation Failure: Retains CONFIRMED status and logs booking.seat_confirmation_failed outbox event")
    void createBooking_seatConfirmationFailure_logsOutboxEventAndRetainsConfirmedState() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        UUID lockToken = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        given(seatServiceClient.verifySeats(any(), anyList(), any(), any()))
                .willReturn(SeatVerificationResult.builder()
                        .valid(true)
                        .seats(List.of(SeatDetailDto.builder()
                                .showSeatId(seatId)
                                .seatLabel("A-1")
                                .category("REGULAR")
                                .price(new BigDecimal("100.00"))
                                .build()))
                        .ttlSeconds(600L)
                        .build());

        given(paymentServiceClient.chargePayment(any(), any(), any(), any(), any(), any()))
                .willReturn(PaymentChargeResult.builder()
                        .paymentId(paymentId)
                        .successful(true)
                        .build());

        // Simulate Seat confirm REST call throwing an exception
        doThrow(new RuntimeException("Seat Service 503 Timeout"))
                .when(seatServiceClient)
                .confirmSeats(any(), anyList(), any(), any());

        CreateBookingCommand command = CreateBookingCommand.builder()
                .userId(userId)
                .showId(showId)
                .showSeatIds(List.of(seatId))
                .lockToken(lockToken)
                .paymentMethodId(UUID.randomUUID())
                .currency("USD")
                .idempotencyKey("idem-confirm-fail")
                .build();

        Booking result = bookingService.createBooking(command);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents).extracting(OutboxEvent::getEventType).contains("booking.seat_confirmation_failed");
    }
}
