package com.ticketbooking.event.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbooking.event.application.dto.CreateVenueRequest;
import com.ticketbooking.event.infrastructure.persistence.entity.VenueJpaEntity;
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
 * Integration tests for VenueController REST endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class VenueControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VenueRepository venueRepository;

    @Test
    void shouldCreateVenue() throws Exception {
        CreateVenueRequest request = CreateVenueRequest.builder()
                .name("PVR ICON")
                .address("High Street Phoenix")
                .city("Mumbai")
                .postalCode("400013")
                .state("Maharashtra")
                .country("India")
                .build();

        mockMvc.perform(post("/api/v1/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("PVR ICON"))
                .andExpect(jsonPath("$.data.city").value("Mumbai"))
                .andExpect(jsonPath("$.data.id").exists());

        assertThat(venueRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldGetVenueById() throws Exception {
        VenueJpaEntity venue = venueRepository.save(VenueJpaEntity.builder()
                .name("INOX Nariman Point")
                .address("CR2 Mall")
                .city("Mumbai")
                .postalCode("400021")
                .build());

        mockMvc.perform(get("/api/v1/venues/{id}", venue.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(venue.getId().toString()))
                .andExpect(jsonPath("$.data.name").value("INOX Nariman Point"));
    }

    @Test
    void shouldSearchVenuesByCity() throws Exception {
        venueRepository.save(VenueJpaEntity.builder()
                .name("Venue Mumbai")
                .address("Addr 1")
                .city("Mumbai")
                .postalCode("400001")
                .build());

        venueRepository.save(VenueJpaEntity.builder()
                .name("Venue Delhi")
                .address("Addr 2")
                .city("Delhi")
                .postalCode("110001")
                .build());

        mockMvc.perform(get("/api/v1/venues").param("city", "mumbai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Venue Mumbai"));
    }

    @Test
    void shouldReturn404ForMissingVenue() throws Exception {
        UUID missingId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/venues/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VENUE_NOT_FOUND"));
    }
}
