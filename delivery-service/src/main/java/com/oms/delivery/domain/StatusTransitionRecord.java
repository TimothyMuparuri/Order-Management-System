package com.oms.delivery.domain;

import com.oms.common.domain.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "status_transition_record")
public class StatusTransitionRecord {

    @Id
    private String id;

    @Column(nullable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    private OrderStatus priorStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus newStatus;

    @Column(nullable = false)
    private Instant timestamp;

    protected StatusTransitionRecord() {}

    public StatusTransitionRecord(String orderId, OrderStatus priorStatus, OrderStatus newStatus, Instant timestamp) {
        this.id = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.priorStatus = priorStatus;
        this.newStatus = newStatus;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public OrderStatus getPriorStatus() { return priorStatus; }
    public OrderStatus getNewStatus() { return newStatus; }
    public Instant getTimestamp() { return timestamp; }
}
