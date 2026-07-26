package com.ticketbooking.auth.domain.repository;

import com.ticketbooking.auth.domain.model.RefreshToken;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for managing RefreshToken persistence.
 */
public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);

    RefreshToken save(RefreshToken refreshToken);

    void deleteByUserId(UUID userId);
}
