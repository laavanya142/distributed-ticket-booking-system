package com.ticketbooking.event.infrastructure.persistence.repository;

import com.ticketbooking.event.infrastructure.persistence.entity.ScreenJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing ScreenJpaEntity persistence.
 */
@Repository
public interface ScreenRepository extends JpaRepository<ScreenJpaEntity, UUID> {

    /**
     * Finds all auditoriums / screens belonging to a specific venue.
     * Serves GET /api/v1/venues/{venueId}/screens
     */
    List<ScreenJpaEntity> findByVenueId(UUID venueId);
}
