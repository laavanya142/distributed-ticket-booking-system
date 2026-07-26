package com.ticketbooking.seat.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ticketbooking.seat.domain.entity.Seat;
import com.ticketbooking.seat.domain.entity.SeatCategory;
import com.ticketbooking.seat.domain.entity.ShowSeat;
import com.ticketbooking.seat.domain.entity.ShowSeatStatus;
import com.ticketbooking.seat.domain.exception.SeatAlreadyLockedException;
import com.ticketbooking.seat.domain.repository.SeatRepository;
import com.ticketbooking.seat.domain.repository.ShowSeatRepository;
import com.ticketbooking.seat.infrastructure.redis.SeatLockManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class SeatFailureScenarioIntegrationTest {

    @Autowired
    private SeatService seatService;

    @Autowired
    private SeatRepository seatRepository;

    @SpyBean
    private ShowSeatRepository showSeatRepository;

    @MockBean
    private SeatLockManager seatLockManager;

    @BeforeEach
    void setUp() {
        given(seatLockManager.getDefaultTtlSeconds()).willReturn(600L);
    }

    @Test
    @DisplayName("7. Redis Failure: Should abort locking and leave database unchanged if Redis locking fails")
    void redisFailure_abortsLockingAndPreservesDatabaseState() {
        UUID screenId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID lockToken = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

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

        // Simulate Redis lock failure (returns false)
        given(seatLockManager.lockSeats(
                        eq(showId), eq(List.of(showSeat.getId())), eq(lockToken), eq(userId), anyLong()))
                .willReturn(false);

        assertThatThrownBy(() -> seatService.lockSeats(showId, List.of(showSeat.getId()), lockToken, userId, 600))
                .isInstanceOf(SeatAlreadyLockedException.class);

        // Verify DB remains untouched
        ShowSeat current = showSeatRepository.findById(showSeat.getId()).orElseThrow();
        assertThat(current.getStatus()).isEqualTo(ShowSeatStatus.AVAILABLE);
        assertThat(current.getLockToken()).isNull();
        assertThat(current.getLockedByUserId()).isNull();
    }

    @Test
    @DisplayName("8. PostgreSQL Failure: Should trigger compensating Redis release if database persistence fails")
    void postgresFailure_executesCompensatingRedisRelease() {
        UUID screenId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID lockToken = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

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

        List<UUID> seatIds = List.of(showSeat.getId());

        // Redis lock succeeds
        given(seatLockManager.lockSeats(eq(showId), eq(seatIds), eq(lockToken), eq(userId), anyLong()))
                .willReturn(true);

        // DB save fails
        Mockito.doThrow(new RuntimeException("Database connection failure during save"))
                .when(showSeatRepository)
                .saveAll(any());

        assertThatThrownBy(() -> seatService.lockSeats(showId, seatIds, lockToken, userId, 600))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection failure");

        // Verify compensating Redis release was executed with userId
        verify(seatLockManager).releaseSeats(eq(showId), eq(seatIds), eq(lockToken), eq(userId));
    }
}
