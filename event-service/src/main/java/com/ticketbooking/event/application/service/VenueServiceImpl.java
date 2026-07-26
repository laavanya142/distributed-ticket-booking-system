package com.ticketbooking.event.application.service;

import com.ticketbooking.event.application.dto.CreateVenueRequest;
import com.ticketbooking.event.application.dto.VenueResponse;
import com.ticketbooking.event.application.mapper.CatalogMapper;
import com.ticketbooking.event.domain.exception.VenueNotFoundException;
import com.ticketbooking.event.infrastructure.persistence.entity.VenueJpaEntity;
import com.ticketbooking.event.infrastructure.persistence.repository.VenueRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of VenueService interface.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final CatalogMapper catalogMapper;

    @Override
    @Transactional
    public VenueResponse createVenue(CreateVenueRequest request) {
        log.info("Creating new venue: {} in city: {}", request.getName(), request.getCity());
        VenueJpaEntity entity = catalogMapper.toVenueEntity(request);
        VenueJpaEntity saved = venueRepository.save(entity);
        return catalogMapper.toVenueResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueResponse> getVenuesByCity(String city) {
        log.debug("Fetching venues for city: {}", city);
        List<VenueJpaEntity> venues = venueRepository.findByCityIgnoreCase(city);
        return venues.stream().map(catalogMapper::toVenueResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VenueResponse getVenueById(UUID id) {
        log.debug("Fetching venue by ID: {}", id);
        VenueJpaEntity entity = venueRepository.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
        return catalogMapper.toVenueResponse(entity);
    }
}
