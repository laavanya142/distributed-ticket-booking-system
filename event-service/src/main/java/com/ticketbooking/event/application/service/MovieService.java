package com.ticketbooking.event.application.service;

import com.ticketbooking.event.application.dto.CreateMovieRequest;
import com.ticketbooking.event.application.dto.MovieResponse;
import com.ticketbooking.event.domain.model.MovieStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing Movie domain operations.
 */
public interface MovieService {
    MovieResponse createMovie(CreateMovieRequest request);

    Page<MovieResponse> getMovies(MovieStatus status, Pageable pageable);

    MovieResponse getMovieById(UUID id);

    MovieResponse updateMovieStatus(UUID id, MovieStatus status);
}
