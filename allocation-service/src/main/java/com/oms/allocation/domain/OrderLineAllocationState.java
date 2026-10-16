package com.oms.allocation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_line_allocation_state")
public class OrderLineAllocationState {

    @Id
    private String orderLineId;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private int requestedQty;

    @Column(nullable = false)
    private int allocatedQty;

    @Column(nullable = false)
    private boolean fullyAllocated;

    protected OrderLineAllocationState() {}

    public OrderLineAllocationState(String orderLineId, String orderId, int requestedQty) {
        this.orderLineId = orderLineId;
        this.orderId = orderId;
        this.requestedQty = requestedQty;
        this.allocatedQty = 0;
        this.fullyAllocated = false;
    }

    public String getOrderLineId() { return orderLineId; }
    public String getOrderId() { return orderId; }
    public int getRequestedQty() { return requestedQty; }
    public int getAllocatedQty() { return allocatedQty; }
    public boolean isFullyAllocated() { return fullyAllocated; }

    public void addAllocated(int qty) {
        this.allocatedQty += qty;
        this.fullyAllocated = this.allocatedQty >= this.requestedQty;
    }
}
