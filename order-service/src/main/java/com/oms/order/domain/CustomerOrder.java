package com.oms.order.domain;

import com.oms.common.domain.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "customer_order")
public class CustomerOrder {

    @Id
    private String orderId;

    @Column(nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    private Instant deliveredToCustomerAt;
    private Long adminToCustomerDurationSeconds;

    protected CustomerOrder() {}

    public CustomerOrder(String orderId, String customerId, OrderStatus orderStatus) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderStatus = orderStatus;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public OrderStatus getOrderStatus() { return orderStatus; }
    public Instant getDeliveredToCustomerAt() { return deliveredToCustomerAt; }
    public Long getAdminToCustomerDurationSeconds() { return adminToCustomerDurationSeconds; }

    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }
    public void setDeliveredToCustomerAt(Instant deliveredToCustomerAt) { this.deliveredToCustomerAt = deliveredToCustomerAt; }
    public void setAdminToCustomerDurationSeconds(Long adminToCustomerDurationSeconds) {
        this.adminToCustomerDurationSeconds = adminToCustomerDurationSeconds;
    }
}
