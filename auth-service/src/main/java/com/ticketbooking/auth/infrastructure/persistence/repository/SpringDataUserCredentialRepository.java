package com.ticketbooking.auth.infrastructure.persistence.repository;

import com.ticketbooking.auth.infrastructure.persistence.entity.UserCredentialJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for user credentials.
 */
public interface SpringDataUserCredentialRepository extends JpaRepository<UserCredentialJpaEntity, UUID> {
    Optional<UserCredentialJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
