package com.oms.eventing.idempotency;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * Deduplicates Kafka redelivery on event id / aggregate id + version.
 */
@Component
public class IdempotentConsumer {

    private final ProcessedEventRepository repository;

    public IdempotentConsumer(ProcessedEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public boolean processOnce(String eventKey, Runnable handler) {
        if (repository.existsById(eventKey)) {
            return false;
        }
        handler.run();
        repository.save(new ProcessedEvent(eventKey, Instant.now()));
        return true;
    }

    @Transactional
    public <T> void processOnce(String eventKey, T payload, Consumer<T> handler) {
        processOnce(eventKey, () -> handler.accept(payload));
    }
}
