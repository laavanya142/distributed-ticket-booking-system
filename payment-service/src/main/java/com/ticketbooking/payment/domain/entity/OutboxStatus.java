package com.ticketbooking.payment.domain.entity;

/**
 * Processing status of a transactional outbox event.
 */
public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
