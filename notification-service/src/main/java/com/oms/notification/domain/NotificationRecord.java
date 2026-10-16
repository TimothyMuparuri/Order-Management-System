package com.oms.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notification_record")
public class NotificationRecord {

    public enum NotificationState { PENDING, SENT, FAILED }

    @Id
    private String orderId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private Instant deliveredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationState state;

    @Column(nullable = false)
    private int attempts;

    protected NotificationRecord() {}

    public NotificationRecord(String orderId, String customerId, Instant deliveredAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.deliveredAt = deliveredAt;
        this.state = NotificationState.PENDING;
        this.attempts = 0;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public NotificationState getState() { return state; }
    public int getAttempts() { return attempts; }

    public void markSent() { this.state = NotificationState.SENT; }
    public void markFailed() { this.state = NotificationState.FAILED; }
    public void incrementAttempts() { this.attempts++; }
}
