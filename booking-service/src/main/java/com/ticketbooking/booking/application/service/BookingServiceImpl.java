package com.ticketbooking.booking.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbooking.booking.application.model.CreateBookingCommand;
import com.ticketbooking.booking.domain.entity.Booking;
import com.ticketbooking.booking.domain.entity.BookingItem;
import com.ticketbooking.booking.domain.entity.BookingStatus;
import com.ticketbooking.booking.domain.entity.IdempotencyKeyEntity;
import com.ticketbooking.booking.domain.entity.IdempotencyKeyId;
import com.ticketbooking.booking.domain.entity.OutboxEvent;
import com.ticketbooking.booking.domain.entity.OutboxStatus;
import com.ticketbooking.booking.domain.exception.BookingAccessDeniedException;
import com.ticketbooking.booking.domain.exception.BookingNotFoundException;
import com.ticketbooking.booking.domain.exception.DuplicateBookingException;
import com.ticketbooking.booking.domain.exception.PaymentFailedException;
import com.ticketbooking.booking.domain.exception.SeatVerificationFailedException;
import com.ticketbooking.booking.domain.repository.BookingRepository;
import com.ticketbooking.booking.domain.repository.IdempotencyKeyRepository;
import com.ticketbooking.booking.domain.repository.OutboxEventRepository;
import com.ticketbooking.booking.infrastructure.client.payment.PaymentChargeResult;
import com.ticketbooking.booking.infrastructure.client.payment.PaymentRefundResult;
import com.ticketbooking.booking.infrastructure.client.payment.PaymentServiceClient;
import com.ticketbooking.booking.infrastructure.client.seat.SeatDetailDto;
import com.ticketbooking.booking.infrastructure.client.seat.SeatServiceClient;
import com.ticketbooking.booking.infrastructure.client.seat.SeatVerificationResult;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core business logic implementation for the Booking Service.
 * Manages the checkout Saga orchestrator, seat lock verification, payment charge, confirmation, cancellation, and expiration flows.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final DateTimeFormatter CODE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final BookingRepository bookingRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final SeatServiceClient seatServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Booking createBooking(CreateBookingCommand command) {
        log.info(
                "Initiating booking creation for user ID: {}, show ID: {}, seats count: {}",
                command.getUserId(),
                command.getShowId(),
                command.getShowSeatIds().size());

        // 1. Idempotency Check
        if (command.getIdempotencyKey() != null) {
            Optional<IdempotencyKeyEntity> existingKey =
                    idempotencyKeyRepository.findById_KeyAndId_UserId(command.getIdempotencyKey(), command.getUserId());
            if (existingKey.isPresent()) {
                UUID existingBookingId = existingKey.get().getBookingId();
                if (existingBookingId != null) {
                    log.info(
                            "Idempotent hit for key {}. Returning existing booking ID {}",
                            command.getIdempotencyKey(),
                            existingBookingId);
                    return bookingRepository
                            .findById(existingBookingId)
                            .orElseThrow(() -> new BookingNotFoundException(existingBookingId));
                }
            }
        }

        // 2. Prevent duplicate active bookings for same user and show
        if (bookingRepository.existsByUserIdAndShowIdAndStatusIn(
                command.getUserId(),
                command.getShowId(),
                List.of(BookingStatus.PENDING, BookingStatus.AWAITING_PAYMENT))) {
            throw new DuplicateBookingException(command.getUserId(), command.getShowId());
        }

        // 3. Read-only Seat Lock Verification (Seat Service sync REST)
        SeatVerificationResult verification = seatServiceClient.verifySeats(
                command.getShowId(), command.getShowSeatIds(), command.getLockToken(), command.getUserId());

        if (verification == null
                || !verification.isValid()
                || verification.getSeats() == null
                || verification.getSeats().isEmpty()) {
            log.warn("Seat lock verification failed for show ID {}", command.getShowId());
            throw new SeatVerificationFailedException(command.getShowId());
        }

        // 4. Calculate total amount server-side from verified seat prices
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SeatDetailDto seatDetail : verification.getSeats()) {
            totalAmount = totalAmount.add(seatDetail.getPrice());
        }

        long ttlSeconds = verification.getTtlSeconds() > 0 ? verification.getTtlSeconds() : 600L;
        Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);

        // 5. Build and persist Booking aggregate in PENDING state (TX1)
        Booking booking = Booking.builder()
                .userId(command.getUserId())
                .showId(command.getShowId())
                .status(BookingStatus.PENDING)
                .lockToken(command.getLockToken())
                .totalAmount(totalAmount)
                .currency(command.getCurrency() != null ? command.getCurrency() : "USD")
                .idempotencyKey(command.getIdempotencyKey())
                .expiresAt(expiresAt)
                .items(new ArrayList<>())
                .build();

        for (SeatDetailDto seatDetail : verification.getSeats()) {
            BookingItem item = BookingItem.builder()
                    .showSeatId(seatDetail.getShowSeatId())
                    .seatLabel(seatDetail.getSeatLabel())
                    .category(seatDetail.getCategory())
                    .priceAtBooking(seatDetail.getPrice())
                    .build();
            booking.addItem(item);
        }

        Booking savedBooking = bookingRepository.save(booking);

        // Persist idempotency key record
        if (command.getIdempotencyKey() != null) {
            idempotencyKeyRepository.save(IdempotencyKeyEntity.builder()
                    .id(new IdempotencyKeyId(command.getIdempotencyKey(), command.getUserId()))
                    .bookingId(savedBooking.getId())
                    .statusCode(201)
                    .responseBody("CREATED")
                    .expiresAt(Instant.now().plusSeconds(86400))
                    .build());
        }

        // Emit outbox event: booking.created
        createOutboxEvent(savedBooking, "booking.created");

        // 6. Initiate Payment (Sync REST)
        savedBooking.transitionTo(BookingStatus.AWAITING_PAYMENT);

        PaymentChargeResult chargeResult = paymentServiceClient.chargePayment(
                savedBooking.getId(),
                command.getUserId(),
                command.getPaymentMethodId(),
                totalAmount,
                savedBooking.getCurrency(),
                "booking_" + savedBooking.getId());

        if (chargeResult != null && chargeResult.isSuccessful()) {
            // 7. Payment Success Path -> CONFIRMED
            savedBooking.transitionTo(BookingStatus.CONFIRMED);
            savedBooking.setPaymentId(chargeResult.getPaymentId());
            savedBooking.setConfirmationCode(generateConfirmationCode());

            createOutboxEvent(savedBooking, "booking.confirmed");
            bookingRepository.save(savedBooking);

            // Confirm seats in Seat Service
            try {
                seatServiceClient.confirmSeats(
                        command.getShowId(), command.getShowSeatIds(), command.getLockToken(), command.getUserId());
            } catch (Exception ex) {
                log.error(
                        "Seat confirmation call failed post-payment for booking {}. Alerting ops.",
                        savedBooking.getId(),
                        ex);
                createOutboxEvent(savedBooking, "booking.seat_confirmation_failed");
            }

            log.info(
                    "Booking {} successfully created and CONFIRMED with code {}",
                    savedBooking.getId(),
                    savedBooking.getConfirmationCode());
            return savedBooking;

        } else {
            // 8. Payment Failure Path -> CANCELLED
            log.warn(
                    "Payment charge failed for booking {}. Executing compensation seat release.", savedBooking.getId());
            savedBooking.transitionTo(BookingStatus.CANCELLED);
            createOutboxEvent(savedBooking, "booking.cancelled");
            bookingRepository.save(savedBooking);

            try {
                seatServiceClient.releaseSeats(
                        command.getShowId(), command.getShowSeatIds(), command.getLockToken(), command.getUserId());
            } catch (Exception ex) {
                log.error("Compensating seat release failed for booking {}: {}", savedBooking.getId(), ex.getMessage());
            }

            String reason = chargeResult != null && chargeResult.getFailureReason() != null
                    ? chargeResult.getFailureReason()
                    : "Payment failed";
            throw new PaymentFailedException(savedBooking.getId(), reason);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBooking(UUID bookingId, UUID userId) {
        log.debug("Fetching booking details for booking ID {} and user ID {}", bookingId, userId);
        Booking booking =
                bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new BookingAccessDeniedException(bookingId, userId);
        }
        return booking;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Booking> getUserBookings(UUID userId, Pageable pageable) {
        log.debug("Fetching user bookings page for user ID {}", userId);
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional
    public Booking cancelBooking(UUID bookingId, UUID userId, String reason) {
        log.info("Initiating cancellation for booking ID {} by user ID {}", bookingId, userId);
        Booking booking =
                bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new BookingAccessDeniedException(bookingId, userId);
        }

        // State machine validates that status must be CONFIRMED
        booking.transitionTo(BookingStatus.CANCELLED);
        Booking cancelledBooking = bookingRepository.save(booking);

        createOutboxEvent(cancelledBooking, "booking.cancellation_requested");

        // Execute refund if payment was captured
        if (booking.getPaymentId() != null) {
            PaymentRefundResult refundResult = paymentServiceClient.refundPayment(
                    booking.getPaymentId(),
                    bookingId,
                    userId,
                    reason != null ? reason : "USER_REQUESTED",
                    "refund_" + bookingId);

            if (refundResult == null || !refundResult.isSuccessful()) {
                log.error("Refund failed during cancellation of booking {}. Outbox event logged.", bookingId);
                createOutboxEvent(cancelledBooking, "booking.refund_failed");
            }
        }

        // Extract showSeatIds for unbook call
        List<UUID> showSeatIds = extractShowSeatIds(booking);
        try {
            seatServiceClient.unbookSeats(booking.getShowId(), showSeatIds, booking.getLockToken(), userId);
        } catch (Exception ex) {
            log.error("Unbook seats call failed during cancellation of booking {}: {}", bookingId, ex.getMessage());
        }

        createOutboxEvent(cancelledBooking, "booking.cancelled");
        log.info("Booking {} successfully CANCELLED", bookingId);
        return cancelledBooking;
    }

    @Override
    @Transactional
    public void expireBooking(UUID bookingId) {
        log.info("Processing expiration for booking ID {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return;
        }

        if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.AWAITING_PAYMENT) {
            booking.transitionTo(BookingStatus.EXPIRED);
            createOutboxEvent(booking, "booking.expired");

            List<UUID> showSeatIds = extractShowSeatIds(booking);
            try {
                seatServiceClient.releaseSeats(
                        booking.getShowId(), showSeatIds, booking.getLockToken(), booking.getUserId());
            } catch (Exception ex) {
                log.error(
                        "Compensating seat release failed during expiration of booking {}: {}",
                        bookingId,
                        ex.getMessage());
            }

            booking.transitionTo(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            log.info("Successfully expired and cancelled booking ID {}", bookingId);
        }
    }

    private void createOutboxEvent(Booking booking, String eventType) {
        try {
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("bookingId", booking.getId());
            payloadMap.put("userId", booking.getUserId());
            payloadMap.put("showId", booking.getShowId());
            payloadMap.put("status", booking.getStatus().name());
            payloadMap.put("totalAmount", booking.getTotalAmount());
            payloadMap.put("currency", booking.getCurrency());
            payloadMap.put("confirmationCode", booking.getConfirmationCode());
            payloadMap.put("paymentId", booking.getPaymentId());

            List<Map<String, Object>> seatItems = new ArrayList<>();
            for (BookingItem item : booking.getItems()) {
                Map<String, Object> seatItem = new HashMap<>();
                seatItem.put("showSeatId", item.getShowSeatId());
                seatItem.put("seatLabel", item.getSeatLabel());
                seatItem.put("category", item.getCategory());
                seatItem.put("price", item.getPriceAtBooking());
                seatItems.add(seatItem);
            }
            payloadMap.put("seats", seatItems);

            String jsonPayload = objectMapper.writeValueAsString(payloadMap);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(booking.getId())
                    .aggregateType("BOOKING")
                    .eventType(eventType)
                    .payload(jsonPayload)
                    .status(OutboxStatus.PENDING)
                    .build();

            outboxEventRepository.save(outboxEvent);
        } catch (Exception ex) {
            log.error(
                    "Failed to construct outbox event payload for booking {}: {}",
                    booking.getId(),
                    ex.getMessage(),
                    ex);
        }
    }

    private String generateConfirmationCode() {
        String dateStr = CODE_DATE_FORMATTER.format(Instant.now());
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(ALPHA_NUMERIC.charAt(RANDOM.nextInt(ALPHA_NUMERIC.length())));
        }
        return String.format("BK-%s-%s", dateStr, sb.toString());
    }

    private List<UUID> extractShowSeatIds(Booking booking) {
        List<UUID> seatIds = new ArrayList<>(booking.getItems().size());
        for (BookingItem item : booking.getItems()) {
            seatIds.add(item.getShowSeatId());
        }
        return seatIds;
    }
}
