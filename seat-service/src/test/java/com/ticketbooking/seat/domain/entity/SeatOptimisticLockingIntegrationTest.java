package com.ticketbooking.seat.domain.entity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ticketbooking.seat.domain.repository.SeatRepository;
import com.ticketbooking.seat.domain.repository.ShowSeatRepository;
import com.ticketbooking.seat.infrastructure.redis.SeatLockManager;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
class SeatOptimisticLockingIntegrationTest {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private EntityManager entityManager;

    @MockBean
    private SeatLockManager seatLockManager;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(seatLockManager.getDefaultTtlSeconds()).thenReturn(600L);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("Should throw JpaOptimisticLockingFailureException when concurrent updates occur on the same ShowSeat")
    void optimisticLocking_concurrentUpdateConflict() {
        UUID screenId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();

        Seat seat = seatRepository.save(Seat.builder()
                .screenId(screenId)
                .rowNumber("A")
                .seatNumber(1)
                .category(SeatCategory.REGULAR)
                .active(true)
                .build());

        ShowSeat showSeat = showSeatRepository.save(ShowSeat.builder()
                .showId(showId)
                .seat(seat)
                .status(ShowSeatStatus.AVAILABLE)
                .price(new BigDecimal("100.00"))
                .build());

        UUID showSeatId = showSeat.getId();

        // Load entity in first update context
        ShowSeat entity1 = showSeatRepository.findById(showSeatId).orElseThrow();

        // Load entity in second update context before entity1 is modified
        ShowSeat entity2 = showSeatRepository.findById(showSeatId).orElseThrow();

        // First update locks seat
        entity1.lock(UUID.randomUUID(), UUID.randomUUID(), Instant.now());
        showSeatRepository.saveAndFlush(entity1);

        // Second update attempts to modify stale version
        entity2.lock(UUID.randomUUID(), UUID.randomUUID(), Instant.now());

        assertThatThrownBy(() -> showSeatRepository.saveAndFlush(entity2))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }
}
