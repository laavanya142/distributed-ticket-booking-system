package com.ticketbooking.seat.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbooking.seat.application.dto.ConfirmSeatsRequest;
import com.ticketbooking.seat.application.dto.InitializeShowSeatsRequest;
import com.ticketbooking.seat.application.dto.LockSeatsRequest;
import com.ticketbooking.seat.application.dto.ReleaseSeatsRequest;
import com.ticketbooking.seat.domain.entity.Seat;
import com.ticketbooking.seat.domain.entity.SeatCategory;
import com.ticketbooking.seat.domain.entity.ShowSeat;
import com.ticketbooking.seat.domain.entity.ShowSeatStatus;
import com.ticketbooking.seat.domain.repository.SeatRepository;
import com.ticketbooking.seat.domain.repository.ShowSeatRepository;
import com.ticketbooking.seat.infrastructure.redis.SeatLockManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class ShowSeatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @MockBean
    private SeatLockManager seatLockManager;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(seatLockManager.getDefaultTtlSeconds()).thenReturn(600L);
        Mockito.lenient()
                .when(seatLockManager.lockSeats(any(), any(), any(), any(), anyLong()))
                .thenReturn(true);
        Mockito.lenient()
                .when(seatLockManager.releaseSeats(any(), any(), any(), any()))
                .thenReturn(true);
    }

    @Nested
    @DisplayName("2. Show Seat Initialization Scenarios")
    class InitializationTests {

        @Test
        @DisplayName(
                "Should initialize show seats with pricing tier multipliers (1.0x Regular, 1.2x Premium, 1.5x VIP)")
        void initializeShowSeats_successAndPricingVerification() throws Exception {
            UUID screenId = UUID.randomUUID();
            UUID showId = UUID.randomUUID();

            Seat regularSeat = seatRepository.save(Seat.builder()
                    .screenId(screenId)
                    .rowNumber("A")
                    .seatNumber(1)
                    .category(SeatCategory.REGULAR)
                    .active(true)
                    .build());

            Seat premiumSeat = seatRepository.save(Seat.builder()
                    .screenId(screenId)
                    .rowNumber("B")
                    .seatNumber(1)
                    .category(SeatCategory.PREMIUM)
                    .active(true)
                    .build());

            Seat vipSeat = seatRepository.save(Seat.builder()
                    .screenId(screenId)
                    .rowNumber("C")
                    .seatNumber(1)
                    .category(SeatCategory.VIP)
                    .active(true)
                    .build());

            InitializeShowSeatsRequest request = InitializeShowSeatsRequest.builder()
                    .screenId(screenId)
                    .basePrice(new BigDecimal("100.00"))
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/shows/{showId}/seats/initialize", showId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data").value(3));

            List<ShowSeat> showSeats = showSeatRepository.findByShowIdWithSeat(showId);
            assertThat(showSeats).hasSize(3);

            ShowSeat ssRegular = showSeats.stream()
                    .filter(s -> s.getSeat().getId().equals(regularSeat.getId()))
                    .findFirst()
                    .orElseThrow();
            assertThat(ssRegular.getPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(ssRegular.getStatus()).isEqualTo(ShowSeatStatus.AVAILABLE);

            ShowSeat ssPremium = showSeats.stream()
                    .filter(s -> s.getSeat().getId().equals(premiumSeat.getId()))
                    .findFirst()
                    .orElseThrow();
            assertThat(ssPremium.getPrice()).isEqualByComparingTo(new BigDecimal("120.00"));

            ShowSeat ssVip = showSeats.stream()
                    .filter(s -> s.getSeat().getId().equals(vipSeat.getId()))
                    .findFirst()
                    .orElseThrow();
            assertThat(ssVip.getPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
        }

        @Test
        @DisplayName("Should return 0 when screen has no active seats during show seat initialization")
        void initializeShowSeats_noActiveSeats() throws Exception {
            UUID screenId = UUID.randomUUID();
            UUID showId = UUID.randomUUID();

            InitializeShowSeatsRequest request = InitializeShowSeatsRequest.builder()
                    .screenId(screenId)
                    .basePrice(new BigDecimal("100.00"))
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/shows/{showId}/seats/initialize", showId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data").value(0));
        }
    }

    @Nested
    @DisplayName("3. Seat Map Query Scenarios")
    class SeatMapTests {

        @Test
        @DisplayName("Should fetch complete seat map and accurately aggregate counts")
        void getShowSeatMap_success() throws Exception {
            UUID screenId = UUID.randomUUID();
            UUID showId = UUID.randomUUID();

            Seat seat1 = seatRepository.save(Seat.builder()
                    .screenId(screenId)
                    .rowNumber("A")
                    .seatNumber(1)
                    .category(SeatCategory.REGULAR)
                    .active(true)
                    .build());
            Seat seat2 = seatRepository.save(Seat.builder()
                    .screenId(screenId)
                    .rowNumber("A")
                    .seatNumber(2)
                    .category(SeatCategory.REGULAR)
                    .active(true)
                    .build());
            Seat seat3 = seatRepository.save(Seat.builder()
                    .screenId(screenId)
                    .rowNumber("A")
                    .seatNumber(3)
                    .category(SeatCategory.REGULAR)
                    .active(true)
                    .build());

            showSeatRepository.save(ShowSeat.builder()
                    .showId(showId)
                    .seat(seat1)
                    .status(ShowSeatStatus.AVAILABLE)
                    .price(new BigDecimal("50.00"))
                    .build());

            showSeatRepository.save(ShowSeat.builder()
                    .showId(showId)
                    .seat(seat2)
                    .status(ShowSeatStatus.LOCKED)
                    .lockedAt(Instant.now())
                    .lockToken(UUID.randomUUID())
                    .lockedByUserId(UUID.randomUUID())
                    .price(new BigDecimal("50.00"))
                    .build());

            showSeatRepository.save(ShowSeat.builder()
                    .showId(showId)
                    .seat(seat3)
                    .status(ShowSeatStatus.BOOKED)
                    .price(new BigDecimal("50.00"))
                    .build());

            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/shows/{showId}/seats", showId))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.showId").value(showId.toString()))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.totalSeats").value(3))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.availableSeats")
                            .value(1))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.lockedSeats").value(1))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.bookedSeats").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.seats", hasSize(3)));
        }
    }

    @Nested
    @DisplayName("4. Seat Locking Scenarios")
    class LockingTests {

        @Test
        @DisplayName("Should successfully lock available show seats with lockToken and userId")
        void lockSeats_success() throws Exception {
            UUID screenId = UUID.randomUUID();
            UUID showId = UUID.randomUUID();
            UUID lockToken = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Seat seat1 = seatRepository.save(Seat.builder()
                    .screenId(screenId)
                    .rowNumber("A")
                    .seatNumber(1)
                    .category(SeatCategory.REGULAR)
                    .active(true)
                    .build());

            ShowSeat showSeat = showSeatRepository.save(ShowSeat.builder()
                    .showId(showId)
                    .seat(seat1)
                    .status(ShowSeatStatus.AVAILABLE)
                    .price(new BigDecimal("100.00"))
                    .build());

            LockSeatsRequest request = LockSeatsRequest.builder()
                    .showSeatIds(List.of(showSeat.getId()))
                    .lockToken(lockToken)
                    .userId(userId)
                    .ttlSeconds(600)
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/shows/{showId}/seats/lock", showId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.showId").value(showId.toString()))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.lockToken").value(lockToken.toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.lockedSeatIds[0]")
                            .value(showSeat.getId().toString()));

            ShowSeat updated = showSeatRepository.findById(showSeat.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(ShowSeatStatus.LOCKED);
            assertThat(updated.getLockToken()).isEqualTo(lockToken);
            assertThat(updated.getLockedByUserId()).isEqualTo(userId);
            assertThat(updated.getLockedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should reject lock request when seats are already locked or booked")
        void lockSeats_alreadyLockedOrBooked() throws Exception {
            UUID screenId = UUID.randomUUID();
            UUID showId = UUID.randomUUID();
            UUID existingToken = UUID.randomUUID();

            Seat seat1 = seatRepository.save(Seat.builder()
                    .screenId(screenId)
                    .rowNumber("A")
                    .seatNumber(1)
                    .category(SeatCategory.REGULAR)
                    .active(true)
                    .build());

            ShowSeat lockedSeat = showSeatRepository.save(ShowSeat.builder()
                    .showId(showId)
                    .seat(seat1)
                    .status(ShowSeatStatus.LOCKED)
                    .lockedAt(Instant.now())
                    .lockToken(existingToken)
                    .lockedByUserId(UUID.randomUUID())
                    .price(new BigDecimal("100.00"))
                    .build());

            LockSeatsRequest request = LockSeatsRequest.builder()
                    .showSeatIds(List.of(lockedSeat.getId()))
                    .lockToken(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .ttlSeconds(600)
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/shows/{showId}/seats/lock", showId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("SEAT_ALREADY_LOCKED"));
        }

        @Test
        @DisplayName("Should return 404 Not Found when requested seat ID does not exist")
        void lockSeats_invalidSeatIds() throws Exception {
            UUID showId = UUID.randomUUID();
            UUID nonExistentSeatId = UUID.randomUUID();

            LockSeatsRequest request = LockSeatsRequest.builder()
                    .showSeatIds(List.of(nonExistentSeatId))
                    .lockToken(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .ttlSeconds(600)
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/shows/{showId}/seats/lock", showId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("SEAT_NOT_FOUND"));
        }

        @Test
        @DisplayName("Should reject lock request exceeding maximum batch size of 10 seats")
        void lockSeats_batchSizeExceeded() throws Exception {
            UUID showId = UUID.randomUUID();
            List<UUID> elevenSeatIds = new ArrayList<>();
            for (int i = 0; i < 11; i++) {
                elevenSeatIds.add(UUID.randomUUID());
            }

            LockSeatsRequest request = LockSeatsRequest.builder()
                    .showSeatIds(elevenSeatIds)
                    .lockToken(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .ttlSeconds(600)
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/shows/{showId}/seats/lock", showId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("5. Seat Release Scenarios")
    class ReleaseTests {

        @Test
        @DisplayName("Should successfully release locked seats back to AVAILABLE status")
        void releaseSeats_success() throws Exception {
            UUID screenId = UUID.randomUUID();
            UUID showId = UUID.randomUUID();
            UUID lockToken = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Seat seat1 = seatRepository.save(Seat.builder()
                    .screenId(screenId)
                    .rowNumber("A")
                    .seatNumber(1)
                    .category(SeatCategory.REGULAR)
                    .active(true)
                    .build());

            ShowSeat lockedSeat = showSeatRepository.save(ShowSeat.builder()
                    .showId(showId)
                    .seat(seat1)
                    .status(ShowSeatStatus.LOCKED)
                    .lockedAt(Instant.now())
                    .lockToken(lockToken)
                    .lockedByUserId(userId)
                    .price(new BigDecimal("100.00"))
                    .build());

            ReleaseSeatsRequest request = ReleaseSeatsRequest.builder()
                    .showSeatIds(List.of(lockedSeat.getId()))
                    .lockToken(lockToken)
                    .userId(userId)
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/shows/{showId}/seats/release", showId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));

            ShowSeat released = showSeatRepository.findById(lockedSeat.getId()).orElseThrow();
            assertThat(released.getStatus()).isEqualTo(ShowSeatStatus.AVAILABLE);
            assertThat(released.getLockToken()).isNull();
            assertThat(released.getLockedByUserId()).isNull();
            assertThat(released.getLockedAt()).isNull();
        }

        @Test
        @DisplayName("Should reject seat release when lock token or userId does not match owner")
        void releaseSeats_invalidLockTokenOrUserId() throws Exception {
            UUID screenId = UUID.randomUUID();
            UUID showId = UUID.randomUUID();
            UUID ownerToken = UUID.randomUUID();
            UUID ownerUserId = UUID.randomUUID();
            UUID unauthorizedToken = UUID.randomUUID();

            Seat seat1 = seatRepository.save(Seat.builder()
                    .screenId(screenId)
                    .rowNumber("A")
                    .seatNumber(1)
                    .category(SeatCategory.REGULAR)
                    .active(true)
                    .build());

            ShowSeat lockedSeat = showSeatRepository.save(ShowSeat.builder()
                    .showId(showId)
                    .seat(seat1)
                    .status(ShowSeatStatus.LOCKED)
                    .lockedAt(Instant.now())
                    .lockToken(ownerToken)
                    .lockedByUserId(ownerUserId)
                    .price(new BigDecimal("100.00"))
                    .build());

            ReleaseSeatsRequest request = ReleaseSeatsRequest.builder()
                    .showSeatIds(List.of(lockedSeat.getId()))
                    .lockToken(unauthorizedToken)
                    .userId(ownerUserId)
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/shows/{showId}/seats/release", showId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("INVALID_SEAT_LOCK"));
        }
    }

    @Nested
    @DisplayName("6. Seat Confirm Scenarios")
    class ConfirmTests {

        @Test
        @DisplayName("Should successfully confirm locked seats as BOOKED when lockToken and userId match")
        void confirmSeats_success() throws Exception {
            UUID screenId = UUID.randomUUID();
            UUID showId = UUID.randomUUID();
            UUID lockToken = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Seat seat1 = seatRepository.save(Seat.builder()
                    .screenId(screenId)
                    .rowNumber("A")
                    .seatNumber(1)
                    .category(SeatCategory.REGULAR)
                    .active(true)
                    .build());

            ShowSeat lockedSeat = showSeatRepository.save(ShowSeat.builder()
                    .showId(showId)
                    .seat(seat1)
                    .status(ShowSeatStatus.LOCKED)
                    .lockedAt(Instant.now())
                    .lockToken(lockToken)
                    .lockedByUserId(userId)
                    .price(new BigDecimal("100.00"))
                    .build());

            ConfirmSeatsRequest request = ConfirmSeatsRequest.builder()
                    .showSeatIds(List.of(lockedSeat.getId()))
                    .lockToken(lockToken)
                    .userId(userId)
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/shows/{showId}/seats/confirm", showId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));

            ShowSeat confirmed = showSeatRepository.findById(lockedSeat.getId()).orElseThrow();
            assertThat(confirmed.getStatus()).isEqualTo(ShowSeatStatus.BOOKED);
            assertThat(confirmed.getLockToken()).isNull();
            assertThat(confirmed.getLockedByUserId()).isNull();
            assertThat(confirmed.getLockedAt()).isNull();
        }

        @Test
        @DisplayName("Should reject seat confirmation and modify nothing if lockToken or userId mismatches")
        void confirmSeats_mismatchedLockTokenOrUserId() throws Exception {
            UUID screenId = UUID.randomUUID();
            UUID showId = UUID.randomUUID();
            UUID ownerToken = UUID.randomUUID();
            UUID ownerUserId = UUID.randomUUID();
            UUID wrongUserId = UUID.randomUUID();

            Seat seat1 = seatRepository.save(Seat.builder()
                    .screenId(screenId)
                    .rowNumber("A")
                    .seatNumber(1)
                    .category(SeatCategory.REGULAR)
                    .active(true)
                    .build());

            ShowSeat lockedSeat = showSeatRepository.save(ShowSeat.builder()
                    .showId(showId)
                    .seat(seat1)
                    .status(ShowSeatStatus.LOCKED)
                    .lockedAt(Instant.now())
                    .lockToken(ownerToken)
                    .lockedByUserId(ownerUserId)
                    .price(new BigDecimal("100.00"))
                    .build());

            ConfirmSeatsRequest request = ConfirmSeatsRequest.builder()
                    .showSeatIds(List.of(lockedSeat.getId()))
                    .lockToken(ownerToken)
                    .userId(wrongUserId)
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/shows/{showId}/seats/confirm", showId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("INVALID_SEAT_LOCK"));

            ShowSeat untouched = showSeatRepository.findById(lockedSeat.getId()).orElseThrow();
            assertThat(untouched.getStatus()).isEqualTo(ShowSeatStatus.LOCKED);
            assertThat(untouched.getLockToken()).isEqualTo(ownerToken);
            assertThat(untouched.getLockedByUserId()).isEqualTo(ownerUserId);
        }

        @Test
        @DisplayName("Should reject seat confirmation and modify nothing if seat is not in LOCKED status")
        void confirmSeats_seatNotLocked() throws Exception {
            UUID screenId = UUID.randomUUID();
            UUID showId = UUID.randomUUID();
            UUID lockToken = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Seat seat1 = seatRepository.save(Seat.builder()
                    .screenId(screenId)
                    .rowNumber("A")
                    .seatNumber(1)
                    .category(SeatCategory.REGULAR)
                    .active(true)
                    .build());

            ShowSeat availableSeat = showSeatRepository.save(ShowSeat.builder()
                    .showId(showId)
                    .seat(seat1)
                    .status(ShowSeatStatus.AVAILABLE)
                    .price(new BigDecimal("100.00"))
                    .build());

            ConfirmSeatsRequest request = ConfirmSeatsRequest.builder()
                    .showSeatIds(List.of(availableSeat.getId()))
                    .lockToken(lockToken)
                    .userId(userId)
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/shows/{showId}/seats/confirm", showId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("INVALID_SEAT_LOCK"));

            ShowSeat untouched =
                    showSeatRepository.findById(availableSeat.getId()).orElseThrow();
            assertThat(untouched.getStatus()).isEqualTo(ShowSeatStatus.AVAILABLE);
        }
    }
}
