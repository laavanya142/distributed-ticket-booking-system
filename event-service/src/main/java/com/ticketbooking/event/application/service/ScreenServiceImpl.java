package com.ticketbooking.event.application.service;

import com.ticketbooking.event.application.dto.CreateScreenRequest;
import com.ticketbooking.event.application.dto.ScreenResponse;
import com.ticketbooking.event.application.mapper.CatalogMapper;
import com.ticketbooking.event.domain.exception.ScreenNotFoundException;
import com.ticketbooking.event.domain.exception.VenueNotFoundException;
import com.ticketbooking.event.infrastructure.persistence.entity.ScreenJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.entity.VenueJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.repository.ScreenRepository;
import com.ticketbooking.event.infrastructure.persistence.repository.VenueRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ScreenService interface.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScreenServiceImpl implements ScreenService {

    private final ScreenRepository screenRepository;
    private final VenueRepository venueRepository;
    private final CatalogMapper catalogMapper;

    @Override
    @Transactional
    public ScreenResponse createScreen(UUID venueId, CreateScreenRequest request) {
        log.info("Creating new screen: {} for venue ID: {}", request.getName(), venueId);
        VenueJpaEntity venue = venueRepository.findById(venueId).orElseThrow(() -> new VenueNotFoundException(venueId));
        ScreenJpaEntity screen = catalogMapper.toScreenEntity(request, venue);
        ScreenJpaEntity saved = screenRepository.save(screen);
        return catalogMapper.toScreenResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScreenResponse> getScreensByVenue(UUID venueId) {
        log.debug("Fetching screens for venue ID: {}", venueId);
        if (!venueRepository.existsById(venueId)) {
            throw new VenueNotFoundException(venueId);
        }
        List<ScreenJpaEntity> screens = screenRepository.findByVenueId(venueId);
        return screens.stream().map(catalogMapper::toScreenResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ScreenResponse getScreenById(UUID id) {
        log.debug("Fetching screen by ID: {}", id);
        ScreenJpaEntity entity = screenRepository.findById(id).orElseThrow(() -> new ScreenNotFoundException(id));
        return catalogMapper.toScreenResponse(entity);
    }
}
