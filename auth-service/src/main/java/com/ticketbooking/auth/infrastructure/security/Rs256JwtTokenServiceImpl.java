package com.ticketbooking.auth.infrastructure.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.ticketbooking.auth.application.service.JwtTokenService;
import com.ticketbooking.auth.domain.model.UserCredential;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Infrastructure service implementing JwtTokenService using RS256 asymmetric cryptographic algorithms.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Rs256JwtTokenServiceImpl implements JwtTokenService {

    private final KeyPair rsaKeyPair;

    @Value("${jwt.issuer:https://auth.ticketbooking.com}")
    private String issuer;

    @Value("${jwt.access-token-validity-seconds:900}")
    private long accessTokenValiditySeconds;

    @Value("${jwt.key-id:bookmyshow-rs256-key-1}")
    private String keyId;

    @Override
    public String generateAccessToken(UserCredential user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenValiditySeconds);

        return Jwts.builder()
                .header()
                .keyId(keyId)
                .and()
                .issuer(issuer)
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(rsaKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }

    @Override
    public String generateRefreshToken() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(rsaKeyPair.getPublic()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    @Override
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    @Override
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    @Override
    public long getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    @Override
    public String getJwkSetJson() {
        RSAPublicKey publicKey = (RSAPublicKey) rsaKeyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) rsaKeyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(keyId)
                .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                .use(com.nimbusds.jose.jwk.KeyUse.SIGNATURE)
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey.toPublicJWK());
        return jwkSet.toString();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(rsaKeyPair.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
