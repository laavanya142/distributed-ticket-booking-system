package com.ticketbooking.booking.domain.entity;

/**
 * Enumeration representing the processing status of a transactional outbox event.
 */
public enum OutboxStatus {

    /**
     * Pending publication to Kafka.
     */
    PENDING,

    /**
     * Successfully published to Kafka and acknowledged.
     */
    PUBLISHED,

    /**
     * Failed publication after maximum retries.
     */
    FAILED
}
