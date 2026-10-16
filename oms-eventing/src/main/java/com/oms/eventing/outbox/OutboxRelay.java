package com.oms.eventing.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Publishes committed outbox rows to Kafka keyed by aggregate id.
 */
@Component
@ConditionalOnProperty(name = "oms.outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxRelay(OutboxEventRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${oms.outbox.relay.delay-ms:1000}")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = repository.findByPublishedFalseOrderByOccurredAtAsc();
        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload()).get();
                event.markPublished();
                repository.save(event);
            } catch (Exception e) {
                log.warn("Failed to publish outbox event {}: {}", event.getEventId(), e.getMessage());
                break;
            }
        }
    }
}
