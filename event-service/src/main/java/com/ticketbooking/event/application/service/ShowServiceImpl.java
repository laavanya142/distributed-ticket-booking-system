package com.ticketbooking.event.application.service;

import com.ticketbooking.event.application.dto.CreateShowRequest;
import com.ticketbooking.event.application.dto.ShowResponse;
import com.ticketbooking.event.application.mapper.CatalogMapper;
import com.ticketbooking.event.domain.exception.InvalidMovieStatusException;
import com.ticketbooking.event.domain.exception.MovieNotFoundException;
import com.ticketbooking.event.domain.exception.ScreenNotFoundException;
import com.ticketbooking.event.domain.exception.ShowNotFoundException;
import com.ticketbooking.event.domain.exception.ShowScheduleConflictException;
import com.ticketbooking.event.domain.model.MovieStatus;
import com.ticketbooking.event.domain.model.ShowStatus;
import com.ticketbooking.event.infrastructure.persistence.entity.MovieJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.entity.ScreenJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.entity.ShowJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.repository.MovieRepository;
import com.ticketbooking.event.infrastructure.persistence.repository.ScreenRepository;
import com.ticketbooking.event.infrastructure.persistence.repository.ShowRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ShowService interface.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final CatalogMapper catalogMapper;

    @Value("${event.show.cleaning-buffer-minutes:15}")
    private int cleaningBufferMinutes;

    @Override
    @Transactional
    public ShowResponse createShow(CreateShowRequest request) {
        log.info(
                "Scheduling new show for movie: {} on screen: {} at {}",
                request.getMovieId(),
                request.getScreenId(),
                request.getStartTime());

        MovieJpaEntity movie = movieRepository
                .findById(request.getMovieId())
                .orElseThrow(() -> new MovieNotFoundException(request.getMovieId()));

        if (MovieStatus.ARCHIVED.equals(movie.getStatus())) {
            throw new InvalidMovieStatusException("Cannot schedule a show for an archived movie: " + movie.getTitle());
        }

        ScreenJpaEntity screen = screenRepository
                .findById(request.getScreenId())
                .orElseThrow(() -> new ScreenNotFoundException(request.getScreenId()));

        Duration totalSlotDuration = Duration.ofMinutes((long) movie.getDurationMinutes() + cleaningBufferMinutes);
        Instant endTime = request.getStartTime().plus(totalSlotDuration);

        boolean hasOverlap =
                showRepository.existsOverlappingShow(request.getScreenId(), request.getStartTime(), endTime);
        if (hasOverlap) {
            throw new ShowScheduleConflictException(request.getScreenId(), request.getStartTime(), endTime);
        }

        ShowJpaEntity show = ShowJpaEntity.builder()
                .movie(movie)
                .screen(screen)
                .startTime(request.getStartTime())
                .endTime(endTime)
                .basePrice(request.getBasePrice())
                .status(ShowStatus.SCHEDULED)
                .build();

        ShowJpaEntity saved = showRepository.save(show);
        return catalogMapper.toShowResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowResponse> getShowsByMovieAndCity(UUID movieId, String city, LocalDate date) {
        log.debug("Fetching shows for movie: {} in city: {} on date: {}", movieId, city, date);
        LocalDate targetDate = (date != null) ? date : LocalDate.now(ZoneOffset.UTC);
        Instant startWindow = targetDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endWindow = targetDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        List<ShowJpaEntity> shows = showRepository.findShowsByMovieAndCity(movieId, city, startWindow, endWindow);
        return shows.stream().map(catalogMapper::toShowResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ShowResponse getShowById(UUID id) {
        log.debug("Fetching show by ID: {}", id);
        ShowJpaEntity entity = showRepository.findById(id).orElseThrow(() -> new ShowNotFoundException(id));
        return catalogMapper.toShowResponse(entity);
    }

    @Override
    @Transactional
    public ShowResponse updateShowStatus(UUID id, ShowStatus status) {
        log.info("Updating status for show ID: {} to {}", id, status);
        ShowJpaEntity entity = showRepository.findById(id).orElseThrow(() -> new ShowNotFoundException(id));
        entity.setStatus(status);
        ShowJpaEntity updated = showRepository.save(entity);
        return catalogMapper.toShowResponse(updated);
    }
}
