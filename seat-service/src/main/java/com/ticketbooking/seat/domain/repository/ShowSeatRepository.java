package com.ticketbooking.seat.domain.repository;

import com.ticketbooking.seat.domain.entity.ShowSeat;
import com.ticketbooking.seat.domain.entity.ShowSeatStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for per-show dynamic ShowSeat entity persistence.
 */
public interface ShowSeatRepository extends JpaRepository<ShowSeat, UUID> {

    /**
     * Finds all show seats for a show with physical Seat details joined to prevent N+1 queries.
     *
     * @param showId Show identifier.
     * @return List of show seats with physical seats fetched.
     */
    @Query("SELECT ss FROM ShowSeat ss JOIN FETCH ss.seat WHERE ss.showId = :showId")
    List<ShowSeat> findByShowIdWithSeat(@Param("showId") UUID showId);

    /**
     * Finds specific show seats by show ID and list of seat IDs with physical Seat details joined.
     *
     * @param showId Show identifier.
     * @param ids List of show seat IDs.
     * @return List of matching show seats.
     */
    @Query("SELECT ss FROM ShowSeat ss JOIN FETCH ss.seat WHERE ss.showId = :showId AND ss.id IN :ids")
    List<ShowSeat> findByShowIdAndIdInWithSeat(@Param("showId") UUID showId, @Param("ids") List<UUID> ids);

    /**
     * Finds show seats by show ID and status.
     *
     * @param showId Show identifier.
     * @param status Show seat status.
     * @return List of matching show seats.
     */
    List<ShowSeat> findByShowIdAndStatus(UUID showId, ShowSeatStatus status);
}
