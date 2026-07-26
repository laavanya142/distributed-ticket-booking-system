package com.ticketbooking.booking.domain.repository;

import com.ticketbooking.booking.domain.entity.OutboxEvent;
import com.ticketbooking.booking.domain.entity.OutboxStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Transactional Outbox event persistence.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Queries pending outbox events ordered by creation time for relay publishing.
     *
     * @param status Outbox event status (e.g. PENDING).
     * @return List of pending outbox events up to limit.
     */
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
