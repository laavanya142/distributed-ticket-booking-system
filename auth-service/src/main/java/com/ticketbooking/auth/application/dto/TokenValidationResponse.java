package com.ticketbooking.auth.application.dto;

import com.ticketbooking.auth.domain.model.Role;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload returning token validation result and user identity claims.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {
    private boolean valid;
    private UUID userId;
    private String email;
    private Role role;
}
