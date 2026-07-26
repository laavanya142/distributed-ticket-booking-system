package com.ticketbooking.auth.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Core domain entity representing a refresh token used for session continuation and token rotation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    private UUID id;
    private String token;
    private UUID userId;
    private Instant expiryDate;
    private boolean revoked;
    private Instant createdAt;

    /**
     * Checks if the refresh token has expired or been explicitly revoked.
     *
     * @return true if expired or revoked, false otherwise.
     */
    public boolean isExpiredOrRevoked() {
        return revoked || Instant.now().isAfter(expiryDate);
    }
}
