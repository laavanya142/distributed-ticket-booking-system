package com.ticketbooking.seat.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbooking.seat.application.dto.CreateScreenSeatsRequest;
import com.ticketbooking.seat.application.dto.CreateSingleSeatRequest;
import com.ticketbooking.seat.domain.entity.Seat;
import com.ticketbooking.seat.domain.entity.SeatCategory;
import com.ticketbooking.seat.domain.repository.SeatRepository;
import com.ticketbooking.seat.infrastructure.redis.SeatLockManager;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class SeatAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SeatRepository seatRepository;

    @MockBean
    private SeatLockManager seatLockManager;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(seatLockManager.getDefaultTtlSeconds()).thenReturn(600L);
    }

    @Test
    @DisplayName("Should successfully batch create physical seats for a screen")
    void createScreenSeats_success() throws Exception {
        UUID screenId = UUID.randomUUID();

        CreateSingleSeatRequest seat1 = CreateSingleSeatRequest.builder()
                .rowNumber("A")
                .seatNumber(1)
                .category(SeatCategory.REGULAR)
                .build();

        CreateSingleSeatRequest seat2 = CreateSingleSeatRequest.builder()
                .rowNumber("A")
                .seatNumber(2)
                .category(SeatCategory.PREMIUM)
                .build();

        CreateSingleSeatRequest seat3 = CreateSingleSeatRequest.builder()
                .rowNumber("B")
                .seatNumber(1)
                .category(SeatCategory.VIP)
                .build();

        CreateScreenSeatsRequest request = CreateScreenSeatsRequest.builder()
                .seats(List.of(seat1, seat2, seat3))
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/screens/{screenId}/seats", screenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data", hasSize(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].rowNumber").value("A"))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.data[0].seatNumber").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].category").value("REGULAR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[2].category").value("VIP"));

        List<Seat> savedSeats = seatRepository.findByScreenIdAndActiveTrue(screenId);
        assertThat(savedSeats).hasSize(3);
    }

    @Test
    @DisplayName("Should ignore duplicate seats when batch creating screen seats")
    void createScreenSeats_duplicateIgnored() throws Exception {
        UUID screenId = UUID.randomUUID();

        // Existing seat in database
        Seat existing = Seat.builder()
                .screenId(screenId)
                .rowNumber("A")
                .seatNumber(1)
                .category(SeatCategory.REGULAR)
                .active(true)
                .build();
        seatRepository.save(existing);

        CreateSingleSeatRequest seat1 = CreateSingleSeatRequest.builder()
                .rowNumber("A")
                .seatNumber(1)
                .category(SeatCategory.REGULAR)
                .build();

        CreateSingleSeatRequest seat2 = CreateSingleSeatRequest.builder()
                .rowNumber("A")
                .seatNumber(2)
                .category(SeatCategory.PREMIUM)
                .build();

        CreateScreenSeatsRequest request =
                CreateScreenSeatsRequest.builder().seats(List.of(seat1, seat2)).build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/screens/{screenId}/seats", screenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data", hasSize(1)))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.data[0].seatNumber").value(2));

        List<Seat> savedSeats = seatRepository.findByScreenIdAndActiveTrue(screenId);
        assertThat(savedSeats).hasSize(2);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when seat request payload is empty or invalid")
    void createScreenSeats_invalidRequest() throws Exception {
        UUID screenId = UUID.randomUUID();

        CreateScreenSeatsRequest emptyRequest = CreateScreenSeatsRequest.builder()
                .seats(Collections.emptyList())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/screens/{screenId}/seats", screenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
}
