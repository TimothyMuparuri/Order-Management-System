package com.oms.allocation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

@Entity
@Table(name = "bulk_purchase_line")
public class BulkPurchaseLine {

    @Id
    private String bulkLineId;

    @Column(nullable = false)
    private String bulkId;

    @Column(nullable = false)
    private String itemRef;

    @Min(1) @Max(1_000_000)
    @Column(nullable = false)
    private int purchasedQty;

    @DecimalMin("0.01") @DecimalMax("999999999.99")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitCost;

    @Min(0)
    @Column(nullable = false)
    private int receivedQty;

    protected BulkPurchaseLine() {}

    public BulkPurchaseLine(String bulkLineId, String bulkId, String itemRef, int purchasedQty, BigDecimal unitCost) {
        this.bulkLineId = bulkLineId;
        this.bulkId = bulkId;
        this.itemRef = itemRef;
        this.purchasedQty = purchasedQty;
        this.unitCost = unitCost;
        this.receivedQty = 0;
    }

    public String getBulkLineId() { return bulkLineId; }
    public String getBulkId() { return bulkId; }
    public String getItemRef() { return itemRef; }
    public int getPurchasedQty() { return purchasedQty; }
    public BigDecimal getUnitCost() { return unitCost; }
    public int getReceivedQty() { return receivedQty; }
    public void setReceivedQty(int receivedQty) { this.receivedQty = receivedQty; }
}
