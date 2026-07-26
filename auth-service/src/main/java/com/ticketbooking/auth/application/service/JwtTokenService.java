package com.ticketbooking.auth.application.service;

import com.ticketbooking.auth.domain.model.UserCredential;
import java.util.UUID;

/**
 * Application interface defining cryptographic token operations and JWKS export.
 */
public interface JwtTokenService {
    String generateAccessToken(UserCredential user);

    String generateRefreshToken();

    boolean validateToken(String token);

    UUID extractUserId(String token);

    String extractEmail(String token);

    String extractRole(String token);

    long getAccessTokenValiditySeconds();

    String getJwkSetJson();
}
