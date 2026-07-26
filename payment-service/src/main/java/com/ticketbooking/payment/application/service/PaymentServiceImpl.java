package com.ticketbooking.payment.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbooking.payment.application.model.ChargePaymentCommand;
import com.ticketbooking.payment.application.model.RefundPaymentCommand;
import com.ticketbooking.payment.domain.entity.IdempotencyKeyEntity;
import com.ticketbooking.payment.domain.entity.IdempotencyKeyId;
import com.ticketbooking.payment.domain.entity.OutboxEvent;
import com.ticketbooking.payment.domain.entity.OutboxStatus;
import com.ticketbooking.payment.domain.entity.Payment;
import com.ticketbooking.payment.domain.entity.PaymentStatus;
import com.ticketbooking.payment.domain.exception.DuplicatePaymentException;
import com.ticketbooking.payment.domain.exception.PaymentNotFoundException;
import com.ticketbooking.payment.domain.exception.PaymentProcessingException;
import com.ticketbooking.payment.domain.exception.RefundFailedException;
import com.ticketbooking.payment.domain.gateway.PaymentGateway;
import com.ticketbooking.payment.domain.gateway.PaymentGatewayResult;
import com.ticketbooking.payment.domain.gateway.RefundGatewayResult;
import com.ticketbooking.payment.domain.repository.IdempotencyKeyRepository;
import com.ticketbooking.payment.domain.repository.OutboxEventRepository;
import com.ticketbooking.payment.domain.repository.PaymentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core application service implementing payment processing, refund handling, idempotency, and transactional outbox.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final PaymentGateway paymentGateway;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Payment charge(ChargePaymentCommand command) {
        log.info(
                "Processing charge for booking ID: {}, user ID: {}, amount: {} {}",
                command.getBookingId(),
                command.getUserId(),
                command.getAmount(),
                command.getCurrency());

        // Idempotency check
        if (command.getIdempotencyKey() != null && !command.getIdempotencyKey().isBlank()) {
            Optional<IdempotencyKeyEntity> existingKey =
                    idempotencyKeyRepository.findById_KeyAndId_UserId(command.getIdempotencyKey(), command.getUserId());
            if (existingKey.isPresent() && existingKey.get().getPaymentId() != null) {
                log.info(
                        "Duplicate idempotency key '{}' hit for user ID: {}. Returning cached payment.",
                        command.getIdempotencyKey(),
                        command.getUserId());
                return findById(existingKey.get().getPaymentId());
            }
        }

        // Check duplicate active payment
        if (paymentRepository.existsByBookingIdAndStatus(command.getBookingId(), PaymentStatus.CAPTURED)) {
            throw new DuplicatePaymentException(command.getBookingId());
        }

        // Step 1: Create Payment in INITIATED state
        Payment payment = Payment.builder()
                .bookingId(command.getBookingId())
                .userId(command.getUserId())
                .amount(command.getAmount())
                .currency(command.getCurrency() != null ? command.getCurrency() : "USD")
                .status(PaymentStatus.INITIATED)
                .paymentMethodId(command.getPaymentMethodId())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Emit payment.initiated outbox event
        saveOutboxEvent(
                savedPayment.getId(),
                "payment.initiated",
                Map.of(
                        "paymentId", savedPayment.getId(),
                        "bookingId", savedPayment.getBookingId(),
                        "userId", savedPayment.getUserId(),
                        "amount", savedPayment.getAmount(),
                        "currency", savedPayment.getCurrency(),
                        "paymentMethodId", savedPayment.getPaymentMethodId(),
                        "status", "INITIATED"));

        // Step 2: Invoke Gateway
        PaymentGatewayResult gatewayResult;
        try {
            gatewayResult = paymentGateway.processCharge(
                    savedPayment.getId(),
                    savedPayment.getAmount(),
                    savedPayment.getCurrency(),
                    savedPayment.getPaymentMethodId());
        } catch (Exception ex) {
            log.error("Exception thrown by payment gateway during charge: {}", ex.getMessage());
            savedPayment.fail(ex.getMessage());
            paymentRepository.save(savedPayment);

            saveOutboxEvent(
                    savedPayment.getId(),
                    "payment.failed",
                    Map.of(
                            "paymentId", savedPayment.getId(),
                            "bookingId", savedPayment.getBookingId(),
                            "userId", savedPayment.getUserId(),
                            "failureReason", ex.getMessage(),
                            "status", "FAILED"));
            throw new PaymentProcessingException(ex.getMessage());
        }

        // Step 3: Handle Gateway Outcome
        if (gatewayResult.isSuccessful()) {
            savedPayment.capture(gatewayResult.getProviderTransactionId());
            paymentRepository.save(savedPayment);

            saveOutboxEvent(
                    savedPayment.getId(),
                    "payment.captured",
                    Map.of(
                            "paymentId", savedPayment.getId(),
                            "bookingId", savedPayment.getBookingId(),
                            "userId", savedPayment.getUserId(),
                            "amount", savedPayment.getAmount(),
                            "currency", savedPayment.getCurrency(),
                            "providerTransactionId", gatewayResult.getProviderTransactionId(),
                            "status", "CAPTURED"));

            saveIdempotencyKey(command.getIdempotencyKey(), command.getUserId(), savedPayment.getId(), 200, "CAPTURED");
            return savedPayment;
        } else {
            savedPayment.fail(gatewayResult.getFailureReason());
            paymentRepository.save(savedPayment);

            saveOutboxEvent(
                    savedPayment.getId(),
                    "payment.failed",
                    Map.of(
                            "paymentId", savedPayment.getId(),
                            "bookingId", savedPayment.getBookingId(),
                            "userId", savedPayment.getUserId(),
                            "failureReason", gatewayResult.getFailureReason(),
                            "status", "FAILED"));

            saveIdempotencyKey(command.getIdempotencyKey(), command.getUserId(), savedPayment.getId(), 402, "FAILED");
            throw new PaymentProcessingException(gatewayResult.getFailureReason());
        }
    }

    @Override
    @Transactional
    public Payment refund(RefundPaymentCommand command) {
        log.info(
                "Processing refund for payment ID: {}, booking ID: {}, user ID: {}",
                command.getPaymentId(),
                command.getBookingId(),
                command.getUserId());

        Payment payment = findById(command.getPaymentId());

        RefundGatewayResult refundResult;
        try {
            refundResult = paymentGateway.processRefund(
                    payment.getId(), payment.getProviderTransactionId(), payment.getAmount(), payment.getCurrency());
        } catch (Exception ex) {
            log.error("Exception thrown by payment gateway during refund: {}", ex.getMessage());
            payment.failRefund(ex.getMessage());
            paymentRepository.save(payment);

            saveOutboxEvent(
                    payment.getId(),
                    "payment.refund_failed",
                    Map.of(
                            "paymentId", payment.getId(),
                            "bookingId", payment.getBookingId(),
                            "userId", payment.getUserId(),
                            "failureReason", ex.getMessage(),
                            "status", "REFUND_FAILED"));
            throw new RefundFailedException(ex.getMessage());
        }

        if (refundResult.isSuccessful()) {
            payment.refund(refundResult.getRefundTransactionId());
            paymentRepository.save(payment);

            saveOutboxEvent(
                    payment.getId(),
                    "payment.refunded",
                    Map.of(
                            "paymentId", payment.getId(),
                            "bookingId", payment.getBookingId(),
                            "userId", payment.getUserId(),
                            "amount", payment.getAmount(),
                            "currency", payment.getCurrency(),
                            "refundTransactionId", refundResult.getRefundTransactionId(),
                            "status", "REFUNDED"));

            return payment;
        } else {
            payment.failRefund(refundResult.getFailureReason());
            paymentRepository.save(payment);

            saveOutboxEvent(
                    payment.getId(),
                    "payment.refund_failed",
                    Map.of(
                            "paymentId", payment.getId(),
                            "bookingId", payment.getBookingId(),
                            "userId", payment.getUserId(),
                            "failureReason", refundResult.getFailureReason(),
                            "status", "REFUND_FAILED"));

            throw new RefundFailedException(refundResult.getFailureReason());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findById(UUID paymentId) {
        return paymentRepository.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByBookingId(UUID bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    private void saveOutboxEvent(UUID aggregateId, String eventType, Map<String, Object> payloadMap) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payloadMap);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(aggregateId)
                    .aggregateType("Payment")
                    .eventType(eventType)
                    .payload(payloadJson)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .createdAt(Instant.now())
                    .build();
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event payload for eventType {}: {}", eventType, e.getMessage(), e);
            throw new RuntimeException("Payload serialization error", e);
        }
    }

    private void saveIdempotencyKey(String key, UUID userId, UUID paymentId, int statusCode, String responseBody) {
        if (key == null || key.isBlank() || userId == null) {
            return;
        }
        IdempotencyKeyEntity entity = IdempotencyKeyEntity.builder()
                .id(new IdempotencyKeyId(key, userId))
                .paymentId(paymentId)
                .statusCode(statusCode)
                .responseBody(responseBody)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(86400))
                .build();
        idempotencyKeyRepository.save(entity);
    }
}
