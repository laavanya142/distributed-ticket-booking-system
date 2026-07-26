package com.ticketbooking.auth.interfaces.rest;

import com.ticketbooking.auth.application.service.JwtTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the RS256 JSON Web Key Set (JWKS) for downstream stateless token verification.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "JWKS", description = "Public JSON Web Key Set endpoint")
public class JwksController {

    private final JwtTokenService jwtTokenService;

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retrieve public RSA keys in JWKS format")
    public ResponseEntity<String> getJwkSet() {
        log.debug("Serving JWKS public keys");
        return ResponseEntity.ok(jwtTokenService.getJwkSetJson());
    }
}
