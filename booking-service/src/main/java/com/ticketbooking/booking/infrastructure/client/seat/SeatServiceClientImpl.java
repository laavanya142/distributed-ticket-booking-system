package com.ticketbooking.booking.infrastructure.client.seat;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * REST client implementation communicating synchronously with Seat Service endpoints.
 */
@Component
@Slf4j
public class SeatServiceClientImpl implements SeatServiceClient {

    private final RestTemplate restTemplate;
    private final String seatServiceBaseUrl;

    public SeatServiceClientImpl(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${services.seat-service.url:http://localhost:8082}") String seatServiceBaseUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.seatServiceBaseUrl = seatServiceBaseUrl;
    }

    @Override
    public SeatVerificationResult verifySeats(UUID showId, List<UUID> showSeatIds, UUID lockToken, UUID userId) {
        String url = String.format("%s/api/v1/shows/%s/seats/verify", seatServiceBaseUrl, showId);
        log.info("Calling Seat Service verify endpoint at URL: {}", url);

        VerifySeatsRequestDto request = VerifySeatsRequestDto.builder()
                .showSeatIds(showSeatIds)
                .lockToken(lockToken)
                .userId(userId)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<VerifySeatsRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<SeatVerificationResult> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, SeatVerificationResult.class);
            return response.getBody();
        } catch (Exception ex) {
            log.warn("Seat Service verification call failed for show ID {}: {}", showId, ex.getMessage());
            return SeatVerificationResult.builder().valid(false).build();
        }
    }

    @Override
    public void confirmSeats(UUID showId, List<UUID> showSeatIds, UUID lockToken, UUID userId) {
        String url = String.format("%s/api/v1/shows/%s/seats/confirm", seatServiceBaseUrl, showId);
        log.info("Calling Seat Service confirm endpoint at URL: {}", url);

        ConfirmSeatsRequestDto request = ConfirmSeatsRequestDto.builder()
                .showSeatIds(showSeatIds)
                .lockToken(lockToken)
                .userId(userId)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ConfirmSeatsRequestDto> entity = new HttpEntity<>(request, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    @Override
    public void releaseSeats(UUID showId, List<UUID> showSeatIds, UUID lockToken, UUID userId) {
        String url = String.format("%s/api/v1/shows/%s/seats/release", seatServiceBaseUrl, showId);
        log.info("Calling Seat Service release endpoint at URL: {}", url);

        ReleaseSeatsRequestDto request = ReleaseSeatsRequestDto.builder()
                .showSeatIds(showSeatIds)
                .lockToken(lockToken)
                .userId(userId)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ReleaseSeatsRequestDto> entity = new HttpEntity<>(request, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    @Override
    public void unbookSeats(UUID showId, List<UUID> showSeatIds, UUID lockToken, UUID userId) {
        String url = String.format("%s/api/v1/shows/%s/seats/unbook", seatServiceBaseUrl, showId);
        log.info("Calling Seat Service unbook endpoint at URL: {}", url);

        UnbookSeatsRequestDto request = UnbookSeatsRequestDto.builder()
                .showSeatIds(showSeatIds)
                .lockToken(lockToken)
                .userId(userId)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UnbookSeatsRequestDto> entity = new HttpEntity<>(request, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }
}
