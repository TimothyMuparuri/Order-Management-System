package com.oms.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

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

    @Column(length = 200)
    private String productName;

    @Column(length = 120)
    private String storeName;

    @Column(length = 1000)
    private String productUrl;

    @Column(length = 1000)
    private String imageUrl;

    /** Customer-selected size (e.g. M, 7). Null when not applicable. */
    @Column(length = 40)
    private String productSize;

    @Column(precision = 12, scale = 2)
    private BigDecimal unitPrice;

    protected CustomerOrderLine() {}

    public CustomerOrderLine(
            String orderLineId,
            String orderId,
            String itemRef,
            int requestedQty,
            String productName,
            String storeName,
            String productUrl,
            String imageUrl,
            String productSize,
            BigDecimal unitPrice
    ) {
        this.orderLineId = orderLineId;
        this.orderId = orderId;
        this.itemRef = itemRef;
        this.requestedQty = requestedQty;
        this.fullyAllocated = false;
        this.productName = productName;
        this.storeName = storeName;
        this.productUrl = productUrl;
        this.imageUrl = imageUrl;
        this.productSize = productSize;
        this.unitPrice = unitPrice;
    }

    public String getOrderLineId() { return orderLineId; }
    public String getOrderId() { return orderId; }
    public String getItemRef() { return itemRef; }
    public int getRequestedQty() { return requestedQty; }
    public boolean isFullyAllocated() { return fullyAllocated; }
    public String getProductName() { return productName; }
    public String getStoreName() { return storeName; }
    public String getProductUrl() { return productUrl; }
    public String getImageUrl() { return imageUrl; }
    public String getProductSize() { return productSize; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setFullyAllocated(boolean fullyAllocated) { this.fullyAllocated = fullyAllocated; }

    public BigDecimal lineTotal() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(requestedQty));
    }
}
