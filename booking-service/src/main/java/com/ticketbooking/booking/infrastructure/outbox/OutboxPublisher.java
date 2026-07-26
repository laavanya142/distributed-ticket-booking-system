package com.ticketbooking.booking.infrastructure.outbox;

import com.ticketbooking.booking.domain.entity.OutboxEvent;
import com.ticketbooking.booking.domain.entity.OutboxStatus;
import com.ticketbooking.booking.domain.repository.OutboxEventRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional Outbox publisher relay polling pending outbox events and publishing them reliably to Kafka.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final int MAX_RETRY_COUNT = 10;
    private static final Map<String, String> TOPIC_MAPPINGS = Map.of(
            "booking.created", "ticket.booking.created",
            "booking.confirmed", "ticket.booking.confirmed",
            "booking.cancelled", "ticket.booking.cancelled",
            "booking.expired", "ticket.booking.expired",
            "booking.refund_failed", "ticket.booking.refund_failed",
            "booking.seat_confirmation_failed", "ticket.booking.seat_confirmation_failed");

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${booking.outbox.poll-interval-ms:1000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents =
                outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events for publishing", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            publishSingleEvent(event);
        }
    }

    private void publishSingleEvent(OutboxEvent event) {
        String topic = TOPIC_MAPPINGS.getOrDefault(event.getEventType(), "ticket.booking.events");
        String key = event.getAggregateId() != null ? event.getAggregateId().toString() : "unknown-aggregate";

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, event.getPayload());
        record.headers()
                .add(new RecordHeader("eventId", event.getId().toString().getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("eventType", event.getEventType().getBytes(StandardCharsets.UTF_8)));

        try {
            kafkaTemplate.send(record).get(); // Synchronous ack wait for outbox relay guarantee

            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(Instant.now());
            outboxEventRepository.save(event);
            log.info(
                    "Successfully published outbox event ID {} of type {} to topic {}",
                    event.getId(),
                    event.getEventType(),
                    topic);

        } catch (Exception ex) {
            int newRetryCount = event.getRetryCount() + 1;
            event.setRetryCount(newRetryCount);

            if (newRetryCount >= MAX_RETRY_COUNT) {
                event.setStatus(OutboxStatus.FAILED);
                log.error(
                        "Outbox event ID {} failed after {} retries. Marked as FAILED.",
                        event.getId(),
                        newRetryCount,
                        ex);
            } else {
                log.warn(
                        "Failed to publish outbox event ID {} (retry {}/{}): {}",
                        event.getId(),
                        newRetryCount,
                        MAX_RETRY_COUNT,
                        ex.getMessage());
            }

            outboxEventRepository.save(event);
        }
    }
}
