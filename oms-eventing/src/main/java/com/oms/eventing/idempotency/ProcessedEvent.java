package com.oms.eventing.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "processed_event")
public class ProcessedEvent {

    @Id
    private String eventKey;

    @Column(nullable = false)
    private Instant processedAt;

    protected ProcessedEvent() {}

    public ProcessedEvent(String eventKey, Instant processedAt) {
        this.eventKey = eventKey;
        this.processedAt = processedAt;
    }

    public String getEventKey() { return eventKey; }
    public Instant getProcessedAt() { return processedAt; }
}
