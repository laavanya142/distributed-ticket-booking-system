package com.ticketbooking.auth.application.service;

import com.ticketbooking.auth.application.dto.AuthResponse;
import com.ticketbooking.auth.application.dto.LoginRequest;
import com.ticketbooking.auth.application.dto.RefreshTokenRequest;
import com.ticketbooking.auth.application.dto.RegisterRequest;
import com.ticketbooking.auth.application.dto.RegisterResponse;
import com.ticketbooking.auth.application.dto.TokenValidationResponse;
import java.util.UUID;

/**
 * Application service interface defining core authentication and registration use cases.
 */
public interface AuthService {
    RegisterResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(UUID userId);

    TokenValidationResponse validateToken(String token);
}
