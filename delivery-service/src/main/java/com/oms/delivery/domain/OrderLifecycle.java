package com.oms.delivery.domain;

import com.oms.common.domain.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "order_lifecycle")
public class OrderLifecycle {

    @Id
    private String orderId;

    @Column(nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private LocalDate placementDate;
    private Instant deliveredToCustomerAt;
    private Long adminToCustomerDurationSeconds;

    protected OrderLifecycle() {}

    public OrderLifecycle(String orderId, String customerId, OrderStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public LocalDate getPlacementDate() { return placementDate; }
    public Instant getDeliveredToCustomerAt() { return deliveredToCustomerAt; }
    public Long getAdminToCustomerDurationSeconds() { return adminToCustomerDurationSeconds; }

    public void setStatus(OrderStatus status) { this.status = status; }
    public void setPlacementDate(LocalDate placementDate) { this.placementDate = placementDate; }
    public void setDeliveredToCustomerAt(Instant deliveredToCustomerAt) { this.deliveredToCustomerAt = deliveredToCustomerAt; }
    public void setAdminToCustomerDurationSeconds(Long adminToCustomerDurationSeconds) {
        this.adminToCustomerDurationSeconds = adminToCustomerDurationSeconds;
    }
}
