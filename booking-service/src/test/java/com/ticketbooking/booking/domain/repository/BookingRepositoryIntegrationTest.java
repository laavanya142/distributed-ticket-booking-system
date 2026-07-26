package com.ticketbooking.booking.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketbooking.booking.domain.entity.Booking;
import com.ticketbooking.booking.domain.entity.BookingStatus;
import com.ticketbooking.booking.domain.entity.IdempotencyKeyEntity;
import com.ticketbooking.booking.domain.entity.IdempotencyKeyId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class BookingRepositoryIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Test
    @DisplayName("Query expired bookings returning PENDING/AWAITING_PAYMENT past expiresAt")
    void testFindExpiredBookings() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        Instant now = Instant.now();

        // Expired PENDING booking
        Booking expiredPending = bookingRepository.save(Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.PENDING)
                .lockToken(UUID.randomUUID())
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey("key-1")
                .expiresAt(now.minusSeconds(60))
                .build());

        // Expired AWAITING_PAYMENT booking
        Booking expiredAwaiting = bookingRepository.save(Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.AWAITING_PAYMENT)
                .lockToken(UUID.randomUUID())
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey("key-2")
                .expiresAt(now.minusSeconds(30))
                .build());

        // Active non-expired PENDING booking
        bookingRepository.save(Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.PENDING)
                .lockToken(UUID.randomUUID())
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey("key-3")
                .expiresAt(now.plusSeconds(300))
                .build());

        // Expired CONFIRMED booking (should be ignored by sweeper)
        bookingRepository.save(Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.CONFIRMED)
                .lockToken(UUID.randomUUID())
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey("key-4")
                .expiresAt(now.minusSeconds(60))
                .build());

        List<Booking> expired = bookingRepository.findExpiredBookings(
                List.of(BookingStatus.PENDING, BookingStatus.AWAITING_PAYMENT), now);

        assertThat(expired)
                .extracting(Booking::getId)
                .containsExactlyInAnyOrder(expiredPending.getId(), expiredAwaiting.getId());
    }

    @Test
    @DisplayName("Query user bookings with pagination ordered by createdAt descending")
    void testFindByUserIdOrderByCreatedAtDesc() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        Instant now = Instant.now();

        Booking booking1 = bookingRepository.save(Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.CONFIRMED)
                .lockToken(UUID.randomUUID())
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey("key-p1")
                .expiresAt(now.plusSeconds(600))
                .build());

        Booking booking2 = bookingRepository.save(Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.CANCELLED)
                .lockToken(UUID.randomUUID())
                .totalAmount(new BigDecimal("150.00"))
                .currency("USD")
                .idempotencyKey("key-p2")
                .expiresAt(now.plusSeconds(600))
                .build());

        Page<Booking> page = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Booking::getId).contains(booking1.getId(), booking2.getId());
    }

    @Test
    @DisplayName("Check duplicate active booking existence")
    void testExistsByUserIdAndShowIdAndStatusIn() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        Instant now = Instant.now();

        bookingRepository.save(Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(BookingStatus.PENDING)
                .lockToken(UUID.randomUUID())
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey("key-dup-1")
                .expiresAt(now.plusSeconds(600))
                .build());

        boolean exists = bookingRepository.existsByUserIdAndShowIdAndStatusIn(
                userId, showId, List.of(BookingStatus.PENDING, BookingStatus.AWAITING_PAYMENT));

        assertThat(exists).isTrue();

        boolean notExists = bookingRepository.existsByUserIdAndShowIdAndStatusIn(
                userId, UUID.randomUUID(), List.of(BookingStatus.PENDING, BookingStatus.AWAITING_PAYMENT));

        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Lookup idempotency key by composite primary key")
    void testFindByIdempotencyKeyAndUserId() {
        UUID userId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        String key = "idem-key-123";

        idempotencyKeyRepository.save(IdempotencyKeyEntity.builder()
                .id(new IdempotencyKeyId(key, userId))
                .bookingId(bookingId)
                .statusCode(201)
                .responseBody("CREATED")
                .expiresAt(Instant.now().plusSeconds(86400))
                .build());

        Optional<IdempotencyKeyEntity> found = idempotencyKeyRepository.findById_KeyAndId_UserId(key, userId);

        assertThat(found).isPresent();
        assertThat(found.get().getBookingId()).isEqualTo(bookingId);
    }
}
