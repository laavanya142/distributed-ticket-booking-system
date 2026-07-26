package com.ticketbooking.booking.domain.repository;

import com.ticketbooking.booking.domain.entity.BookingItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing BookingItem persistence.
 */
@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, UUID> {

    /**
     * Finds all seat items associated with a parent booking ID.
     *
     * @param bookingId Parent booking identifier.
     * @return List of matching BookingItem entities.
     */
    List<BookingItem> findByBookingId(UUID bookingId);
}
