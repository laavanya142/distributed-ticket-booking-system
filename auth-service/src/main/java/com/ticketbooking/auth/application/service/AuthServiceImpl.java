package com.ticketbooking.auth.application.service;

import com.ticketbooking.auth.application.dto.AuthResponse;
import com.ticketbooking.auth.application.dto.LoginRequest;
import com.ticketbooking.auth.application.dto.RefreshTokenRequest;
import com.ticketbooking.auth.application.dto.RegisterRequest;
import com.ticketbooking.auth.application.dto.RegisterResponse;
import com.ticketbooking.auth.application.dto.TokenValidationResponse;
import com.ticketbooking.auth.domain.exception.InvalidCredentialException;
import com.ticketbooking.auth.domain.exception.TokenRefreshException;
import com.ticketbooking.auth.domain.exception.UserAlreadyExistsException;
import com.ticketbooking.auth.domain.model.RefreshToken;
import com.ticketbooking.auth.domain.model.Role;
import com.ticketbooking.auth.domain.model.UserCredential;
import com.ticketbooking.auth.domain.repository.RefreshTokenRepository;
import com.ticketbooking.auth.domain.repository.UserCredentialRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates user registration, authentication, and token rotation use cases.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserCredentialRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Processing user registration for email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        UserCredential user = UserCredential.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        UserCredential savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login attempt for email: {}", request.getEmail());
        UserCredential user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialException("Invalid email or password"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Authentication failed for email: {}", request.getEmail());
            throw new InvalidCredentialException("Invalid email or password");
        }

        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshTokenString = jwtTokenService.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(refreshTokenString)
                .userId(user.getId())
                .expiryDate(Instant.now().plusSeconds(604800)) // 7 days default
                .revoked(false)
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("User authenticated successfully: {}", user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .tokenType("Bearer")
                .expiresIn(jwtTokenService.getAccessTokenValiditySeconds())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Processing refresh token rotation");
        RefreshToken existingToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found in database"));

        if (existingToken.isExpiredOrRevoked()) {
            log.warn("Refresh token expired or revoked for token ID: {}", existingToken.getId());
            throw new TokenRefreshException("Refresh token is expired or revoked. Please login again.");
        }

        UserCredential user = userRepository
                .findById(existingToken.getUserId())
                .orElseThrow(() -> new TokenRefreshException("Associated user account not found"));

        // Rotate token: revoke old one and issue a new one
        existingToken.setRevoked(true);
        refreshTokenRepository.save(existingToken);

        String newAccessToken = jwtTokenService.generateAccessToken(user);
        String newRefreshTokenString = jwtTokenService.generateRefreshToken();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(newRefreshTokenString)
                .userId(user.getId())
                .expiryDate(Instant.now().plusSeconds(604800))
                .revoked(false)
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(newRefreshToken);
        log.info("Refresh token rotated successfully for user: {}", user.getId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenString)
                .tokenType("Bearer")
                .expiresIn(jwtTokenService.getAccessTokenValiditySeconds())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public void logout(UUID userId) {
        log.info("Processing logout and token revocation for user: {}", userId);
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        if (!jwtTokenService.validateToken(token)) {
            return TokenValidationResponse.builder().valid(false).build();
        }

        try {
            UUID userId = jwtTokenService.extractUserId(token);
            String email = jwtTokenService.extractEmail(token);
            String roleStr = jwtTokenService.extractRole(token);
            Role role = Role.valueOf(roleStr);

            return TokenValidationResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .email(email)
                    .role(role)
                    .build();
        } catch (Exception e) {
            log.warn("Token claims extraction failed during validation: {}", e.getMessage());
            return TokenValidationResponse.builder().valid(false).build();
        }
    }
}
