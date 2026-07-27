package com.ticketbooking.payment.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Composite primary key for client idempotency records.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKeyId implements Serializable {

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String key;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
}
