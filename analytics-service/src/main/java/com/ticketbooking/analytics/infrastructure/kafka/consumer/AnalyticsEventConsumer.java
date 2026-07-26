package com.ticketbooking.analytics.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbooking.analytics.application.service.AnalyticsService;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Event consumer listening to booking and payment Kafka domain events to maintain real-time analytics summary metrics.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AnalyticsEventConsumer {

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {
                "ticket.booking.created",
                "ticket.booking.confirmed",
                "ticket.booking.cancelled",
                "ticket.payment.captured",
                "ticket.payment.refunded"
            },
            groupId = "${spring.kafka.consumer.group-id:analytics-service-group}")
    public void consumeAnalyticsEvent(String messagePayload, Acknowledgment ack) {
        log.info("Received Kafka analytics event payload: {}", messagePayload);

        try {
            Map<String, Object> map = objectMapper.readValue(messagePayload, Map.class);
            String eventType = (String) map.getOrDefault("eventType", "");

            UUID aggregateId = extractUuid(map, "bookingId", "paymentId", "aggregateId");
            BigDecimal amount = extractBigDecimal(map, "amount");

            switch (eventType) {
                case "booking.created" -> analyticsService.processBookingCreated(aggregateId);
                case "booking.confirmed" -> analyticsService.processBookingConfirmed(aggregateId);
                case "booking.cancelled" -> analyticsService.processBookingCancelled(aggregateId);
                case "payment.captured" -> analyticsService.processPaymentCaptured(aggregateId, amount);
                case "payment.refunded" -> analyticsService.processPaymentRefunded(aggregateId, amount);
                default -> {
                    if (messagePayload.contains("booking") || messagePayload.contains("Booking")) {
                        analyticsService.processBookingCreated(aggregateId);
                    } else {
                        log.debug("Unrecognized eventType '{}' for analytics processing", eventType);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error processing analytics Kafka event: {}", ex.getMessage(), ex);
        } finally {
            if (ack != null) {
                ack.acknowledge();
            }
        }
    }

    private UUID extractUuid(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                try {
                    return UUID.fromString((String) map.get(key));
                } catch (Exception ignored) {
                    // Fallback to next key
                }
            }
        }
        return UUID.randomUUID();
    }

    private BigDecimal extractBigDecimal(Map<String, Object> map, String key) {
        if (map.containsKey(key) && map.get(key) != null) {
            Object val = map.get(key);
            if (val instanceof Number) {
                return new BigDecimal(val.toString());
            } else if (val instanceof String) {
                return new BigDecimal((String) val);
            }
        }
        return BigDecimal.ZERO;
    }
}
