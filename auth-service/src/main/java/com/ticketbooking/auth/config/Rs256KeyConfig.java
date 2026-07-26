package com.ticketbooking.auth.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures RSA 2048-bit cryptographic key pair for RS256 token signing and verification.
 */
@Slf4j
@Configuration
public class Rs256KeyConfig {

    @Bean
    public KeyPair rsaKeyPair() throws NoSuchAlgorithmException {
        log.info("Initializing RSA 2048-bit KeyPairGenerator for RS256 JWT signing");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }
}
