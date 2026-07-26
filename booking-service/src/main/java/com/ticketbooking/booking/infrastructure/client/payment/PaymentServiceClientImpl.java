package com.ticketbooking.booking.infrastructure.client.payment;

import java.math.BigDecimal;
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
 * REST client implementation communicating synchronously with Payment Service endpoints.
 */
@Component
@Slf4j
public class PaymentServiceClientImpl implements PaymentServiceClient {

    private final RestTemplate restTemplate;
    private final String paymentServiceBaseUrl;

    public PaymentServiceClientImpl(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${services.payment-service.url:http://localhost:8084}") String paymentServiceBaseUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.paymentServiceBaseUrl = paymentServiceBaseUrl;
    }

    @Override
    public PaymentChargeResult chargePayment(
            UUID bookingId,
            UUID userId,
            UUID paymentMethodId,
            BigDecimal amount,
            String currency,
            String idempotencyKey) {
        String url = String.format("%s/api/v1/payments/charge", paymentServiceBaseUrl);
        log.info("Calling Payment Service charge endpoint for booking ID: {}", bookingId);

        PaymentChargeRequestDto request = PaymentChargeRequestDto.builder()
                .bookingId(bookingId)
                .userId(userId)
                .paymentMethodId(paymentMethodId)
                .amount(amount)
                .currency(currency)
                .description("Booking checkout charge")
                .idempotencyKey(idempotencyKey)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", idempotencyKey);
        HttpEntity<PaymentChargeRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<PaymentChargeResult> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, PaymentChargeResult.class);
            PaymentChargeResult body = response.getBody();
            if (body != null) {
                body.setSuccessful(true);
                return body;
            }
            return PaymentChargeResult.builder()
                    .successful(false)
                    .failureReason("Empty response body from Payment Service")
                    .build();
        } catch (Exception ex) {
            log.error("Payment charge failed for booking ID {}: {}", bookingId, ex.getMessage());
            return PaymentChargeResult.builder()
                    .successful(false)
                    .failureReason(ex.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentRefundResult refundPayment(
            UUID paymentId, UUID bookingId, UUID userId, String reason, String idempotencyKey) {
        String url = String.format("%s/api/v1/payments/%s/refund", paymentServiceBaseUrl, paymentId);
        log.info("Calling Payment Service refund endpoint for payment ID: {}", paymentId);

        PaymentRefundRequestDto request = PaymentRefundRequestDto.builder()
                .bookingId(bookingId)
                .userId(userId)
                .reason(reason)
                .idempotencyKey(idempotencyKey)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", idempotencyKey);
        HttpEntity<PaymentRefundRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<PaymentRefundResult> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, PaymentRefundResult.class);
            PaymentRefundResult body = response.getBody();
            if (body != null) {
                body.setSuccessful(true);
                return body;
            }
            return PaymentRefundResult.builder()
                    .successful(false)
                    .failureReason("Empty response body from Payment Service refund")
                    .build();
        } catch (Exception ex) {
            log.error("Payment refund failed for payment ID {}: {}", paymentId, ex.getMessage());
            return PaymentRefundResult.builder()
                    .successful(false)
                    .failureReason(ex.getMessage())
                    .build();
        }
    }
}
