package com.ticketbooking.booking.domain.repository;

import com.ticketbooking.booking.domain.entity.InboxMessage;
import com.ticketbooking.booking.domain.entity.InboxMessageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for consumer inbox message deduplication.
 */
@Repository
public interface InboxMessageRepository extends JpaRepository<InboxMessage, InboxMessageId> {}
