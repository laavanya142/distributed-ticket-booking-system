package com.ticketbooking.auth.domain.repository;

import com.ticketbooking.auth.domain.model.UserCredential;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for managing UserCredential persistence.
 * Implemented in the infrastructure layer.
 */
public interface UserCredentialRepository {
    Optional<UserCredential> findById(UUID id);

    Optional<UserCredential> findByEmail(String email);

    UserCredential save(UserCredential userCredential);

    boolean existsByEmail(String email);
}
