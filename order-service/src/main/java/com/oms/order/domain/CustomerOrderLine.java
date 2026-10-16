package com.oms.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "customer_order_line")
public class CustomerOrderLine {

    @Id
    private String orderLineId;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String itemRef;

    @Min(1)
    @Max(999_999)
    @Column(nullable = false)
    private int requestedQty;

    @Column(nullable = false)
    private boolean fullyAllocated;

    protected CustomerOrderLine() {}

    public CustomerOrderLine(String orderLineId, String orderId, String itemRef, int requestedQty) {
        this.orderLineId = orderLineId;
        this.orderId = orderId;
        this.itemRef = itemRef;
        this.requestedQty = requestedQty;
        this.fullyAllocated = false;
    }

    public String getOrderLineId() { return orderLineId; }
    public String getOrderId() { return orderId; }
    public String getItemRef() { return itemRef; }
    public int getRequestedQty() { return requestedQty; }
    public boolean isFullyAllocated() { return fullyAllocated; }
    public void setFullyAllocated(boolean fullyAllocated) { this.fullyAllocated = fullyAllocated; }
}
