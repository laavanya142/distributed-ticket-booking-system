package com.ticketbooking.payment.domain.repository;

import com.ticketbooking.payment.domain.entity.OutboxEvent;
import com.ticketbooking.payment.domain.entity.OutboxStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing OutboxEvent aggregate persistence.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
