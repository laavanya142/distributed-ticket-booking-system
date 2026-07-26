package com.ticketbooking.event.application.mapper;

import com.ticketbooking.event.application.dto.CreateMovieRequest;
import com.ticketbooking.event.application.dto.CreateScreenRequest;
import com.ticketbooking.event.application.dto.CreateVenueRequest;
import com.ticketbooking.event.application.dto.MovieResponse;
import com.ticketbooking.event.application.dto.ScreenResponse;
import com.ticketbooking.event.application.dto.ShowResponse;
import com.ticketbooking.event.application.dto.VenueResponse;
import com.ticketbooking.event.infrastructure.persistence.entity.MovieJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.entity.ScreenJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.entity.ShowJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.entity.VenueJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between JPA Entities and DTO objects.
 */
@Component
public class CatalogMapper {

    public MovieJpaEntity toMovieEntity(CreateMovieRequest request) {
        if (request == null) {
            return null;
        }
        return MovieJpaEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .genre(request.getGenre())
                .language(request.getLanguage())
                .durationMinutes(request.getDurationMinutes())
                .releaseDate(request.getReleaseDate())
                .rating(request.getRating())
                .posterUrl(request.getPosterUrl())
                .build();
    }

    public MovieResponse toMovieResponse(MovieJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return MovieResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .genre(entity.getGenre())
                .language(entity.getLanguage())
                .durationMinutes(entity.getDurationMinutes())
                .releaseDate(entity.getReleaseDate())
                .rating(entity.getRating())
                .posterUrl(entity.getPosterUrl())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public VenueJpaEntity toVenueEntity(CreateVenueRequest request) {
        if (request == null) {
            return null;
        }
        return VenueJpaEntity.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .state(request.getState())
                .country(request.getCountry())
                .build();
    }

    public VenueResponse toVenueResponse(VenueJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return VenueResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .city(entity.getCity())
                .postalCode(entity.getPostalCode())
                .state(entity.getState())
                .country(entity.getCountry())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ScreenJpaEntity toScreenEntity(CreateScreenRequest request, VenueJpaEntity venue) {
        if (request == null) {
            return null;
        }
        return ScreenJpaEntity.builder()
                .venue(venue)
                .name(request.getName())
                .capacity(request.getCapacity())
                .build();
    }

    public ScreenResponse toScreenResponse(ScreenJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return ScreenResponse.builder()
                .id(entity.getId())
                .venueId(entity.getVenue() != null ? entity.getVenue().getId() : null)
                .name(entity.getName())
                .capacity(entity.getCapacity())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ShowResponse toShowResponse(ShowJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return ShowResponse.builder()
                .id(entity.getId())
                .movieId(entity.getMovie() != null ? entity.getMovie().getId() : null)
                .movieTitle(entity.getMovie() != null ? entity.getMovie().getTitle() : null)
                .screenId(entity.getScreen() != null ? entity.getScreen().getId() : null)
                .screenName(entity.getScreen() != null ? entity.getScreen().getName() : null)
                .venueId(
                        entity.getScreen() != null && entity.getScreen().getVenue() != null
                                ? entity.getScreen().getVenue().getId()
                                : null)
                .venueName(
                        entity.getScreen() != null && entity.getScreen().getVenue() != null
                                ? entity.getScreen().getVenue().getName()
                                : null)
                .city(
                        entity.getScreen() != null && entity.getScreen().getVenue() != null
                                ? entity.getScreen().getVenue().getCity()
                                : null)
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .basePrice(entity.getBasePrice())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
