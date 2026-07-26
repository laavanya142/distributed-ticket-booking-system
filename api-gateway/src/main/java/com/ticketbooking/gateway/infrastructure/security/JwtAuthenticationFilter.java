package com.ticketbooking.gateway.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.ticketbooking.common.dto.ErrorResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

/**
 * Reactive Global Filter executing RS256 JWT validation and header enrichment at the API Gateway boundary.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    private final String jwksUri;
    private final String issuer;
    private final List<PathPattern> excludedPathPatterns;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();

    public JwtAuthenticationFilter(
            @Value("${jwt.jwks-uri:http://localhost:8081/.well-known/jwks.json}") String jwksUri,
            @Value("${jwt.issuer:https://auth.ticketbooking.com}") String issuer,
            @Value("${jwt.excluded-paths}") List<String> excludedPaths,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper) {
        this.jwksUri = jwksUri;
        this.issuer = issuer;
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder.build();

        PathPatternParser parser = new PathPatternParser();
        this.excludedPathPatterns = excludedPaths != null
                ? excludedPaths.stream().map(parser::parse).toList()
                : List.of();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isExcludedPath(path)) {
            log.debug("Path {} is excluded from JWT authentication", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Missing or malformed Authorization header for path: {}", path);
            return onError(exchange, "Missing or malformed Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            log.debug("Empty Bearer token for path: {}", path);
            return onError(exchange, "Empty Bearer token", HttpStatus.UNAUTHORIZED);
        }

        String kid = extractKeyId(token);
        if (kid == null) {
            log.debug("Missing 'kid' in JWT header for path: {}", path);
            return onError(exchange, "Malformed JWT header", HttpStatus.UNAUTHORIZED);
        }

        return getPublicKey(kid)
                .flatMap(publicKey -> validateAndForward(exchange, chain, token, publicKey))
                .switchIfEmpty(Mono.defer(() -> onError(
                        exchange, "Unable to resolve public key for JWT verification", HttpStatus.UNAUTHORIZED)))
                .onErrorResume(e -> {
                    log.debug("JWT processing error for path {}: {}", path, e.getMessage());
                    return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
                });
    }

    private Mono<Void> validateAndForward(
            ServerWebExchange exchange, GatewayFilterChain chain, String token, PublicKey publicKey) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (issuer != null && !issuer.equals(claims.getIssuer())) {
                log.debug("Invalid JWT issuer: {}", claims.getIssuer());
                return onError(exchange, "Invalid JWT issuer", HttpStatus.UNAUTHORIZED);
            }

            String userId = claims.get("userId", String.class);
            if (userId == null) {
                userId = claims.getSubject();
            }
            String role = claims.get("role", String.class);
            String email = claims.get("email", String.class);

            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", userId != null ? userId : "")
                    .header("X-User-Role", role != null ? role : "")
                    .header("X-User-Email", email != null ? email : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            log.debug("JWT signature/claims verification failed: {}", e.getMessage());
            return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractKeyId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            byte[] headerBytes = Base64.getUrlDecoder().decode(parts[0]);
            Map<String, Object> headerMap = objectMapper.readValue(headerBytes, Map.class);
            return (String) headerMap.get("kid");
        } catch (Exception e) {
            log.debug("Failed to extract kid from JWT header: {}", e.getMessage());
            return null;
        }
    }

    private Mono<PublicKey> getPublicKey(String kid) {
        PublicKey cachedKey = keyCache.get(kid);
        if (cachedKey != null) {
            return Mono.just(cachedKey);
        }

        return webClient.get().uri(jwksUri).retrieve().bodyToMono(String.class).mapNotNull(json -> {
            try {
                JWKSet jwkSet = JWKSet.parse(json);
                com.nimbusds.jose.jwk.JWK jwk = jwkSet.getKeyByKeyId(kid);
                if (jwk instanceof RSAKey rsaKey) {
                    PublicKey publicKey = rsaKey.toRSAPublicKey();
                    keyCache.put(kid, publicKey);
                    return publicKey;
                }
            } catch (Exception e) {
                log.error("Failed to parse RSA public key from JWKS for kid: {}", kid, e);
            }
            return null;
        });
    }

    private boolean isExcludedPath(String path) {
        PathContainer pathContainer = PathContainer.parsePath(path);
        return excludedPathPatterns.stream().anyMatch(pattern -> pattern.matches(pathContainer));
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errMessage, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.of(status.name(), errMessage, "gateway-trace");

        return response.writeWith(Mono.fromCallable(() -> {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().allocateBuffer(bytes.length);
            buffer.write(bytes);
            return buffer;
        }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
