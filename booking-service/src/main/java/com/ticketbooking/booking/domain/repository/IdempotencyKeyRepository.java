package com.ticketbooking.booking.domain.repository;

import com.ticketbooking.booking.domain.entity.IdempotencyKeyEntity;
import com.ticketbooking.booking.domain.entity.IdempotencyKeyId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing client idempotency key records.
 */
@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, IdempotencyKeyId> {

    /**
     * Finds an idempotency record by key and user ID.
     *
     * @param key Client key string.
     * @param userId Owning user identifier.
     * @return Optional containing matching record if present.
     */
    Optional<IdempotencyKeyEntity> findById_KeyAndId_UserId(String key, UUID userId);
}
