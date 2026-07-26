package com.ticketbooking.event.application.service;

import com.ticketbooking.event.application.dto.CreateMovieRequest;
import com.ticketbooking.event.application.dto.MovieResponse;
import com.ticketbooking.event.application.mapper.CatalogMapper;
import com.ticketbooking.event.domain.exception.MovieNotFoundException;
import com.ticketbooking.event.domain.model.MovieStatus;
import com.ticketbooking.event.infrastructure.persistence.entity.MovieJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.repository.MovieRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of MovieService interface.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final CatalogMapper catalogMapper;

    @Override
    @Transactional
    public MovieResponse createMovie(CreateMovieRequest request) {
        log.info("Creating new movie: {}", request.getTitle());
        MovieJpaEntity entity = catalogMapper.toMovieEntity(request);
        entity.setStatus(MovieStatus.UPCOMING);
        MovieJpaEntity saved = movieRepository.save(entity);
        return catalogMapper.toMovieResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> getMovies(MovieStatus status, Pageable pageable) {
        log.debug("Fetching movies with status: {}", status);
        Page<MovieJpaEntity> page =
                (status != null) ? movieRepository.findByStatus(status, pageable) : movieRepository.findAll(pageable);
        return page.map(catalogMapper::toMovieResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getMovieById(UUID id) {
        log.debug("Fetching movie by ID: {}", id);
        MovieJpaEntity entity = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        return catalogMapper.toMovieResponse(entity);
    }

    @Override
    @Transactional
    public MovieResponse updateMovieStatus(UUID id, MovieStatus status) {
        log.info("Updating status for movie ID: {} to {}", id, status);
        MovieJpaEntity entity = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        entity.setStatus(status);
        MovieJpaEntity updated = movieRepository.save(entity);
        return catalogMapper.toMovieResponse(updated);
    }
}
