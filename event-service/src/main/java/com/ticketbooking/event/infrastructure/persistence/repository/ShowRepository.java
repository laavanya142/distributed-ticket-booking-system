package com.ticketbooking.event.infrastructure.persistence.repository;

import com.ticketbooking.event.infrastructure.persistence.entity.ShowJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing ShowJpaEntity persistence.
 */
@Repository
public interface ShowRepository extends JpaRepository<ShowJpaEntity, UUID> {

    /**
     * Finds active scheduled shows for a movie in a specific city within a date time window.
     * Serves GET /api/v1/shows?movieId=...&city=...&startTime=...&endTime=...
     */
    @Query("SELECT s FROM ShowJpaEntity s JOIN FETCH s.screen sc JOIN FETCH sc.venue v "
            + "WHERE s.movie.id = :movieId AND LOWER(v.city) = LOWER(:city) "
            + "AND s.startTime >= :startWindow AND s.startTime <= :endWindow "
            + "AND s.status = 'SCHEDULED'")
    List<ShowJpaEntity> findShowsByMovieAndCity(
            @Param("movieId") UUID movieId,
            @Param("city") String city,
            @Param("startWindow") Instant startWindow,
            @Param("endWindow") Instant endWindow);

    /**
     * Checks if a show time interval overlaps with an existing non-cancelled show on the same screen.
     * Concurrency Assumption: Admin show creation operations use pessimistic database locking or transactional check
     * to prevent race conditions during schedule creation.
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM ShowJpaEntity s "
            + "WHERE s.screen.id = :screenId AND s.status != 'CANCELLED' "
            + "AND (:startTime < s.endTime AND :endTime > s.startTime)")
    boolean existsOverlappingShow(
            @Param("screenId") UUID screenId, @Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
}
