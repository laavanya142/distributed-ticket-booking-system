package com.ticketbooking.seat.domain.repository;

import com.ticketbooking.seat.domain.entity.Seat;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for physical Seat entity persistence.
 */
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    /**
     * Finds all active physical seats belonging to a specific screen.
     *
     * @param screenId The screen identifier.
     * @return List of active physical seats.
     */
    List<Seat> findByScreenIdAndActiveTrue(UUID screenId);

    /**
     * Checks if a seat already exists for a screen, row, and seat number.
     *
     * @param screenId Screen identifier.
     * @param rowNumber Row label.
     * @param seatNumber Seat number index.
     * @return true if seat exists.
     */
    boolean existsByScreenIdAndRowNumberAndSeatNumber(UUID screenId, String rowNumber, Integer seatNumber);
}
