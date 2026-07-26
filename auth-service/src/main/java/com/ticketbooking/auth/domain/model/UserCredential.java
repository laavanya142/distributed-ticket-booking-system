package com.ticketbooking.auth.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Core domain entity representing a user's login credentials and security state.
 * Completely independent of database or framework annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCredential {
    private UUID id;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
