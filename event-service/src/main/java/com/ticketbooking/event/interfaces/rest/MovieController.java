package com.ticketbooking.event.interfaces.rest;

import com.ticketbooking.common.dto.ApiResponse;
import com.ticketbooking.event.application.dto.CreateMovieRequest;
import com.ticketbooking.event.application.dto.MovieResponse;
import com.ticketbooking.event.application.dto.UpdateMovieStatusRequest;
import com.ticketbooking.event.application.service.MovieService;
import com.ticketbooking.event.domain.model.MovieStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing Movie catalog management endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Tag(name = "Movie Catalog", description = "Endpoints for searching, creating, and updating movie catalog metadata")
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    @Operation(summary = "Register a new movie in the catalog")
    public ResponseEntity<ApiResponse<MovieResponse>> createMovie(@Valid @RequestBody CreateMovieRequest request) {
        log.info("Received request to create movie: {}", request.getTitle());
        MovieResponse response = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Movie registered successfully", response));
    }

    @GetMapping
    @Operation(summary = "Fetch paginated movies, optionally filtered by status")
    public ResponseEntity<ApiResponse<Page<MovieResponse>>> getMovies(
            @RequestParam(required = false) MovieStatus status, @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Received request to fetch movies with status: {}", status);
        Page<MovieResponse> page = movieService.getMovies(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Movies retrieved successfully", page));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movie details by unique ID")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieById(@PathVariable UUID id) {
        log.debug("Received request to fetch movie ID: {}", id);
        MovieResponse response = movieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update movie status (e.g. NOW_SHOWING, ARCHIVED)")
    public ResponseEntity<ApiResponse<MovieResponse>> updateMovieStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateMovieStatusRequest request) {
        log.info("Received request to update status for movie ID: {} to {}", id, request.getStatus());
        MovieResponse response = movieService.updateMovieStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Movie status updated successfully", response));
    }
}
