package com.ticketbooking.booking.infrastructure.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ticketbooking.booking.domain.entity.OutboxEvent;
import com.ticketbooking.booking.domain.entity.OutboxStatus;
import com.ticketbooking.booking.domain.repository.OutboxEventRepository;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("h2")
class OutboxPublisherIntegrationTest {

    @Autowired
    private OutboxPublisher outboxPublisher;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    @DisplayName("Successful Publish: Updates outbox status to PUBLISHED with timestamp")
    void publishPendingEvents_successfulPublish() {
        OutboxEvent event = outboxEventRepository.save(OutboxEvent.builder()
                .aggregateType("Booking")
                .aggregateId(UUID.randomUUID())
                .eventType("booking.created")
                .payload("{}")
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build());

        given(kafkaTemplate.send(any(ProducerRecord.class)))
                .willReturn(CompletableFuture.completedFuture(new SendResult<>(null, null)));

        outboxPublisher.publishPendingEvents();

        OutboxEvent updated = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(updated.getPublishedAt()).isNotNull();
    }

    @Test
    @DisplayName("Publish Failure: Increments retryCount when Kafka send fails")
    void publishPendingEvents_failure_incrementsRetryCount() {
        OutboxEvent event = outboxEventRepository.save(OutboxEvent.builder()
                .aggregateType("Booking")
                .aggregateId(UUID.randomUUID())
                .eventType("booking.created")
                .payload("{}")
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build());

        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka Broker Unavailable"));

        given(kafkaTemplate.send(any(ProducerRecord.class))).willReturn(failedFuture);

        outboxPublisher.publishPendingEvents();

        OutboxEvent updated = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(updated.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Max Retries Exceeded: Marks status as FAILED when retryCount reaches threshold")
    void publishPendingEvents_maxRetriesExceeded_marksStatusFailed() {
        OutboxEvent event = outboxEventRepository.save(OutboxEvent.builder()
                .aggregateType("Booking")
                .aggregateId(UUID.randomUUID())
                .eventType("booking.created")
                .payload("{}")
                .status(OutboxStatus.PENDING)
                .retryCount(9)
                .build());

        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka Persistent Failure"));

        given(kafkaTemplate.send(any(ProducerRecord.class))).willReturn(failedFuture);

        outboxPublisher.publishPendingEvents();

        OutboxEvent updated = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(updated.getRetryCount()).isEqualTo(10);
    }
}
