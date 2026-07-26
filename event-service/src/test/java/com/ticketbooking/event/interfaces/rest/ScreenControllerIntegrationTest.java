package com.ticketbooking.event.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbooking.event.application.dto.CreateScreenRequest;
import com.ticketbooking.event.infrastructure.persistence.entity.ScreenJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.entity.VenueJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.repository.ScreenRepository;
import com.ticketbooking.event.infrastructure.persistence.repository.VenueRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for ScreenController REST endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class ScreenControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Test
    void shouldCreateScreen() throws Exception {
        VenueJpaEntity venue = venueRepository.save(VenueJpaEntity.builder()
                .name("PVR Lower Parel")
                .address("Phoenix Mall")
                .city("Mumbai")
                .postalCode("400013")
                .build());

        CreateScreenRequest request = CreateScreenRequest.builder()
                .name("IMAX Screen 1")
                .capacity(250)
                .build();

        mockMvc.perform(post("/api/v1/venues/{venueId}/screens", venue.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("IMAX Screen 1"))
                .andExpect(jsonPath("$.data.capacity").value(250))
                .andExpect(jsonPath("$.data.venueId").value(venue.getId().toString()));

        assertThat(screenRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldRejectScreenForMissingVenue() throws Exception {
        UUID missingVenueId = UUID.randomUUID();
        CreateScreenRequest request = CreateScreenRequest.builder()
                .name("Orphan Screen")
                .capacity(100)
                .build();

        mockMvc.perform(post("/api/v1/venues/{venueId}/screens", missingVenueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VENUE_NOT_FOUND"));

        assertThat(screenRepository.findAll()).isEmpty();
    }

    @Test
    void shouldGetScreensByVenue() throws Exception {
        VenueJpaEntity venue = venueRepository.save(VenueJpaEntity.builder()
                .name("Cinepolis")
                .address("Andheri West")
                .city("Mumbai")
                .postalCode("400053")
                .build());

        screenRepository.save(ScreenJpaEntity.builder()
                .venue(venue)
                .name("Screen 1")
                .capacity(150)
                .build());

        screenRepository.save(ScreenJpaEntity.builder()
                .venue(venue)
                .name("Screen 2")
                .capacity(180)
                .build());

        mockMvc.perform(get("/api/v1/venues/{venueId}/screens", venue.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}
