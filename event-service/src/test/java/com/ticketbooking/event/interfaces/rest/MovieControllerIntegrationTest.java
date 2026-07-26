package com.ticketbooking.event.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbooking.event.application.dto.CreateMovieRequest;
import com.ticketbooking.event.application.dto.UpdateMovieStatusRequest;
import com.ticketbooking.event.domain.model.Genre;
import com.ticketbooking.event.domain.model.Language;
import com.ticketbooking.event.domain.model.MovieStatus;
import com.ticketbooking.event.domain.model.Rating;
import com.ticketbooking.event.infrastructure.persistence.entity.MovieJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.repository.MovieRepository;
import java.time.LocalDate;
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
 * Integration tests for MovieController REST endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class MovieControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Test
    void shouldCreateMovie() throws Exception {
        CreateMovieRequest request = CreateMovieRequest.builder()
                .title("Inception")
                .description("A thief who steals corporate secrets through dream-sharing technology.")
                .genre(Genre.SCI_FI)
                .language(Language.ENGLISH)
                .durationMinutes(148)
                .releaseDate(LocalDate.of(2010, 7, 16))
                .rating(Rating.PG_13)
                .posterUrl("http://example.com/poster.jpg")
                .build();

        mockMvc.perform(post("/api/v1/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Inception"))
                .andExpect(jsonPath("$.data.status").value("UPCOMING"))
                .andExpect(jsonPath("$.data.id").exists());

        assertThat(movieRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldGetMovieById() throws Exception {
        MovieJpaEntity movie = MovieJpaEntity.builder()
                .title("Interstellar")
                .genre(Genre.SCI_FI)
                .language(Language.ENGLISH)
                .durationMinutes(169)
                .releaseDate(LocalDate.of(2014, 11, 7))
                .rating(Rating.PG_13)
                .status(MovieStatus.NOW_SHOWING)
                .build();
        MovieJpaEntity saved = movieRepository.save(movie);

        mockMvc.perform(get("/api/v1/movies/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.data.title").value("Interstellar"));
    }

    @Test
    void shouldReturn404ForMissingMovie() throws Exception {
        UUID missingId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/movies/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("MOVIE_NOT_FOUND"));
    }

    @Test
    void shouldListMoviesByStatus() throws Exception {
        movieRepository.save(MovieJpaEntity.builder()
                .title("Movie 1")
                .genre(Genre.ACTION)
                .language(Language.ENGLISH)
                .durationMinutes(120)
                .releaseDate(LocalDate.now())
                .rating(Rating.PG)
                .status(MovieStatus.NOW_SHOWING)
                .build());

        movieRepository.save(MovieJpaEntity.builder()
                .title("Movie 2")
                .genre(Genre.COMEDY)
                .language(Language.ENGLISH)
                .durationMinutes(100)
                .releaseDate(LocalDate.now().plusDays(10))
                .rating(Rating.G)
                .status(MovieStatus.UPCOMING)
                .build());

        mockMvc.perform(get("/api/v1/movies").param("status", "NOW_SHOWING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Movie 1"));
    }

    @Test
    void shouldUpdateMovieStatus() throws Exception {
        MovieJpaEntity saved = movieRepository.save(MovieJpaEntity.builder()
                .title("Movie Status Test")
                .genre(Genre.DRAMA)
                .language(Language.ENGLISH)
                .durationMinutes(110)
                .releaseDate(LocalDate.now())
                .rating(Rating.R)
                .status(MovieStatus.UPCOMING)
                .build());

        UpdateMovieStatusRequest request = new UpdateMovieStatusRequest(MovieStatus.NOW_SHOWING);

        mockMvc.perform(put("/api/v1/movies/{id}/status", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("NOW_SHOWING"));

        MovieJpaEntity updated = movieRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(MovieStatus.NOW_SHOWING);
    }
}
