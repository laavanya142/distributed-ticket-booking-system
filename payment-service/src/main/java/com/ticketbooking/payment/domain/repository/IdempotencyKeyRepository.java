package com.ticketbooking.payment.domain.repository;

import com.ticketbooking.payment.domain.entity.IdempotencyKeyEntity;
import com.ticketbooking.payment.domain.entity.IdempotencyKeyId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing client idempotency records.
 */
@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, IdempotencyKeyId> {

    Optional<IdempotencyKeyEntity> findById_KeyAndId_UserId(String key, UUID userId);
}
