package com.oms.allocation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "bulk_purchase")
public class BulkPurchase {

    public enum BulkPurchaseStatus { RECORDED, DELIVERED_TO_ADMIN }

    @Id
    private String bulkId;

    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100)
    private String platformName;

    @PastOrPresent
    @Column(nullable = false)
    private LocalDate placementDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BulkPurchaseStatus status;

    private Instant deliveredToAdminAt;
    private Long platformToAdminDurationSeconds;

    protected BulkPurchase() {}

    public BulkPurchase(String bulkId, String platformName, LocalDate placementDate) {
        this.bulkId = bulkId;
        this.platformName = platformName;
        this.placementDate = placementDate;
        this.status = BulkPurchaseStatus.RECORDED;
    }

    public String getBulkId() { return bulkId; }
    public String getPlatformName() { return platformName; }
    public LocalDate getPlacementDate() { return placementDate; }
    public BulkPurchaseStatus getStatus() { return status; }
    public Instant getDeliveredToAdminAt() { return deliveredToAdminAt; }
    public Long getPlatformToAdminDurationSeconds() { return platformToAdminDurationSeconds; }

    public void markDelivered(Instant at, long durationSeconds) {
        this.status = BulkPurchaseStatus.DELIVERED_TO_ADMIN;
        this.deliveredToAdminAt = at;
        this.platformToAdminDurationSeconds = durationSeconds;
    }
}
