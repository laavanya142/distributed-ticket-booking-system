package com.ticketbooking.event.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbooking.event.application.dto.CreateShowRequest;
import com.ticketbooking.event.domain.model.Genre;
import com.ticketbooking.event.domain.model.Language;
import com.ticketbooking.event.domain.model.MovieStatus;
import com.ticketbooking.event.domain.model.Rating;
import com.ticketbooking.event.domain.model.ShowStatus;
import com.ticketbooking.event.infrastructure.persistence.entity.MovieJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.entity.ScreenJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.entity.ShowJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.entity.VenueJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.repository.MovieRepository;
import com.ticketbooking.event.infrastructure.persistence.repository.ScreenRepository;
import com.ticketbooking.event.infrastructure.persistence.repository.ShowRepository;
import com.ticketbooking.event.infrastructure.persistence.repository.VenueRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for ShowController REST endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class ShowControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private ShowRepository showRepository;

    private MovieJpaEntity activeMovie;
    private MovieJpaEntity archivedMovie;
    private ScreenJpaEntity screen;

    @BeforeEach
    void setUp() {
        activeMovie = movieRepository.save(MovieJpaEntity.builder()
                .title("Dune Part Two")
                .genre(Genre.SCI_FI)
                .language(Language.ENGLISH)
                .durationMinutes(166)
                .releaseDate(LocalDate.now())
                .rating(Rating.PG_13)
                .status(MovieStatus.NOW_SHOWING)
                .build());

        archivedMovie = movieRepository.save(MovieJpaEntity.builder()
                .title("Old Classic")
                .genre(Genre.DRAMA)
                .language(Language.ENGLISH)
                .durationMinutes(90)
                .releaseDate(LocalDate.of(1990, 1, 1))
                .rating(Rating.PG)
                .status(MovieStatus.ARCHIVED)
                .build());

        VenueJpaEntity venue = venueRepository.save(VenueJpaEntity.builder()
                .name("PVR BKC")
                .address("Jio World Drive")
                .city("Mumbai")
                .postalCode("400051")
                .build());

        screen = screenRepository.save(ScreenJpaEntity.builder()
                .venue(venue)
                .name("Audi 1")
                .capacity(300)
                .build());
    }

    @Test
    void shouldCreateShowAndCalculateEndTime() throws Exception {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS);
        CreateShowRequest request = CreateShowRequest.builder()
                .movieId(activeMovie.getId())
                .screenId(screen.getId())
                .startTime(startTime)
                .basePrice(new BigDecimal("450.00"))
                .build();

        mockMvc.perform(post("/api/v1/shows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.data.startTime").value(startTime.toString()))
                .andExpect(jsonPath("$.data.endTime")
                        .value(startTime.plus(166 + 15, ChronoUnit.MINUTES).toString()));

        assertThat(showRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldRejectShowForArchivedMovie() throws Exception {
        Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        CreateShowRequest request = CreateShowRequest.builder()
                .movieId(archivedMovie.getId())
                .screenId(screen.getId())
                .startTime(startTime)
                .basePrice(new BigDecimal("200.00"))
                .build();

        mockMvc.perform(post("/api/v1/shows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_MOVIE_STATUS"));
    }

    @Test
    void shouldRejectOverlappingShow() throws Exception {
        Instant show1Start = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS);
        Instant show1End = show1Start.plus(181, ChronoUnit.MINUTES);

        showRepository.save(ShowJpaEntity.builder()
                .movie(activeMovie)
                .screen(screen)
                .startTime(show1Start)
                .endTime(show1End)
                .basePrice(new BigDecimal("400.00"))
                .status(ShowStatus.SCHEDULED)
                .build());

        Instant show2Start = show1Start.plus(60, ChronoUnit.MINUTES);
        CreateShowRequest overlappingRequest = CreateShowRequest.builder()
                .movieId(activeMovie.getId())
                .screenId(screen.getId())
                .startTime(show2Start)
                .basePrice(new BigDecimal("400.00"))
                .build();

        mockMvc.perform(post("/api/v1/shows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlappingRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("SHOW_SCHEDULE_CONFLICT"));
    }

    @Test
    void shouldAllowBackToBackShowScheduling() throws Exception {
        Instant show1Start = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS);
        Instant show1End = show1Start.plus(181, ChronoUnit.MINUTES);

        showRepository.save(ShowJpaEntity.builder()
                .movie(activeMovie)
                .screen(screen)
                .startTime(show1Start)
                .endTime(show1End)
                .basePrice(new BigDecimal("400.00"))
                .status(ShowStatus.SCHEDULED)
                .build());

        CreateShowRequest backToBackRequest = CreateShowRequest.builder()
                .movieId(activeMovie.getId())
                .screenId(screen.getId())
                .startTime(show1End)
                .basePrice(new BigDecimal("400.00"))
                .build();

        mockMvc.perform(post("/api/v1/shows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(backToBackRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(showRepository.findAll()).hasSize(2);
    }
}
