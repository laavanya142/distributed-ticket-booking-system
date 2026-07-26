package com.ticketbooking.event.infrastructure.persistence.repository;

import com.ticketbooking.event.infrastructure.persistence.entity.VenueJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing VenueJpaEntity persistence.
 */
@Repository
public interface VenueRepository extends JpaRepository<VenueJpaEntity, UUID> {

    /**
     * Finds all venues located in a specific city (case-insensitive).
     * Serves GET /api/v1/venues?city=...
     */
    List<VenueJpaEntity> findByCityIgnoreCase(String city);
}
