package com.ticketbooking.event.application.dto;

import com.ticketbooking.event.domain.model.Genre;
import com.ticketbooking.event.domain.model.Language;
import com.ticketbooking.event.domain.model.MovieStatus;
import com.ticketbooking.event.domain.model.Rating;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Immutable read-only response representation of a movie.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {
    private UUID id;
    private String title;
    private String description;
    private Genre genre;
    private Language language;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private Rating rating;
    private String posterUrl;
    private MovieStatus status;
    private Instant createdAt;
}
