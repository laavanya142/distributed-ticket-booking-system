package com.ticketbooking.event.infrastructure.persistence.repository;

import com.ticketbooking.event.domain.model.MovieStatus;
import com.ticketbooking.event.infrastructure.persistence.entity.MovieJpaEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing MovieJpaEntity persistence.
 */
@Repository
public interface MovieRepository extends JpaRepository<MovieJpaEntity, UUID> {

    /**
     * Finds movies by status (e.g. NOW_SHOWING, UPCOMING) for public catalog browsing.
     * Serves GET /api/v1/movies?status=...
     */
    Page<MovieJpaEntity> findByStatus(MovieStatus status, Pageable pageable);
}
