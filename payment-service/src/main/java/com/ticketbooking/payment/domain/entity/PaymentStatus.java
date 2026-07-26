package com.ticketbooking.payment.domain.entity;

/**
 * Lifecycle states of a payment transaction.
 */
public enum PaymentStatus {
    INITIATED,
    PROCESSING,
    CAPTURED,
    FAILED,
    REFUNDED,
    REFUND_FAILED
}
