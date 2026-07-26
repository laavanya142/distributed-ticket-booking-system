package com.ticketbooking.common.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base abstract class for all asynchronous Kafka domain events.
 * Guarantees trace correlation and idempotency tracking across messaging pipelines.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {

    /**
     * Unique identifier for idempotency checking and Inbox deduplication.
     */
    private String eventId = UUID.randomUUID().toString();

    /**
     * The domain event classification string (e.g., "ticket.seat.locked").
     */
    private String eventType;

    /**
     * Timestamp when the event occurred.
     */
    private Instant timestamp = Instant.now();

    /**
     * Distributed trace correlation ID.
     */
    private String correlationId;
}
