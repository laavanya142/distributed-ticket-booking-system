package com.ticketbooking.auth.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;

import com.ticketbooking.auth.domain.model.Role;
import com.ticketbooking.auth.domain.model.UserCredential;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class Rs256JwtTokenServiceImplTest {

    private Rs256JwtTokenServiceImpl jwtTokenService;
    private UserCredential testUser;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        jwtTokenService = new Rs256JwtTokenServiceImpl(keyPair);
        ReflectionTestUtils.setField(jwtTokenService, "issuer", "https://auth.ticketbooking.com");
        ReflectionTestUtils.setField(jwtTokenService, "accessTokenValiditySeconds", 900L);
        ReflectionTestUtils.setField(jwtTokenService, "keyId", "test-key-id");

        testUser = UserCredential.builder()
                .id(UUID.randomUUID())
                .email("test@ticketbooking.com")
                .passwordHash("hashed-password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void testGenerateAndValidateAccessToken() {
        String token = jwtTokenService.generateAccessToken(testUser);
        assertNotNull(token);
        assertTrue(jwtTokenService.validateToken(token));

        assertEquals(testUser.getId(), jwtTokenService.extractUserId(token));
        assertEquals(testUser.getEmail(), jwtTokenService.extractEmail(token));
        assertEquals("ROLE_USER", jwtTokenService.extractRole(token));
    }

    @Test
    void testInvalidTokenValidation() {
        assertFalse(jwtTokenService.validateToken("invalid.jwt.token"));
    }

    @Test
    void testGenerateRefreshToken() {
        String refreshToken = jwtTokenService.generateRefreshToken();
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }

    @Test
    void testGetJwkSetJson() {
        String jwks = jwtTokenService.getJwkSetJson();
        assertNotNull(jwks);
        assertTrue(jwks.contains("keys"));
        assertTrue(jwks.contains("test-key-id"));
        assertTrue(jwks.contains("RS256"));
    }
}
