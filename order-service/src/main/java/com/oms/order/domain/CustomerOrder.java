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

    /** How the order arrived: Web, WhatsApp, etc. */
    @Column(length = 40)
    private String orderSource;

    private Instant deliveredToCustomerAt;
    private Long adminToCustomerDurationSeconds;

    protected CustomerOrder() {}

    public CustomerOrder(String orderId, String customerId, OrderStatus orderStatus) {
        this(orderId, customerId, orderStatus, "Web");
    }

    public CustomerOrder(String orderId, String customerId, OrderStatus orderStatus, String orderSource) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderStatus = orderStatus;
        this.orderSource = normalizeSource(orderSource);
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public OrderStatus getOrderStatus() { return orderStatus; }
    public String getOrderSource() { return orderSource == null || orderSource.isBlank() ? "Web" : orderSource; }
    public Instant getDeliveredToCustomerAt() { return deliveredToCustomerAt; }
    public Long getAdminToCustomerDurationSeconds() { return adminToCustomerDurationSeconds; }

    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }
    public void setDeliveredToCustomerAt(Instant deliveredToCustomerAt) { this.deliveredToCustomerAt = deliveredToCustomerAt; }
    public void setAdminToCustomerDurationSeconds(Long adminToCustomerDurationSeconds) {
        this.adminToCustomerDurationSeconds = adminToCustomerDurationSeconds;
    }

    private static String normalizeSource(String source) {
        if (source == null || source.isBlank()) {
            return "Web";
        }
        String trimmed = source.trim();
        if (trimmed.equalsIgnoreCase("whatsapp")) {
            return "WhatsApp";
        }
        if (trimmed.equalsIgnoreCase("web")) {
            return "Web";
        }
        return trimmed.length() > 40 ? trimmed.substring(0, 40) : trimmed;
    }
}
