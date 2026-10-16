package com.oms.allocation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "allocation")
public class Allocation {

    @Id
    private String allocationId;

    @Column(nullable = false)
    private String bulkLineId;

    @Column(nullable = false)
    private String orderLineId;

    @Column(nullable = false)
    private String orderId;

    @Min(1)
    @Column(nullable = false)
    private int quantity;

    protected Allocation() {}

    public Allocation(String allocationId, String bulkLineId, String orderLineId, String orderId, int quantity) {
        this.allocationId = allocationId;
        this.bulkLineId = bulkLineId;
        this.orderLineId = orderLineId;
        this.orderId = orderId;
        this.quantity = quantity;
    }

    public String getAllocationId() { return allocationId; }
    public String getBulkLineId() { return bulkLineId; }
    public String getOrderLineId() { return orderLineId; }
    public String getOrderId() { return orderId; }
    public int getQuantity() { return quantity; }
}
