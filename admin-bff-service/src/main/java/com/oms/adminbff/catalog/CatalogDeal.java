package com.oms.adminbff.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "catalog_deal")
public class CatalogDeal {

    @Id
    private String id;

    @Column(nullable = false)
    private String store;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(nullable = false, length = 2000)
    private String productUrl;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal wasPrice;

    private String badge;

    @Column(length = 2000)
    private String sourceImageUrl;

    private String imageFileName;

    private String imageContentType;

    private Instant lastScrapedAt;

    private String scrapeStatus;

    @Column(length = 1000)
    private String scrapeError;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getWasPrice() {
        return wasPrice;
    }

    public void setWasPrice(BigDecimal wasPrice) {
        this.wasPrice = wasPrice;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getSourceImageUrl() {
        return sourceImageUrl;
    }

    public void setSourceImageUrl(String sourceImageUrl) {
        this.sourceImageUrl = sourceImageUrl;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    public void setImageContentType(String imageContentType) {
        this.imageContentType = imageContentType;
    }

    public Instant getLastScrapedAt() {
        return lastScrapedAt;
    }

    public void setLastScrapedAt(Instant lastScrapedAt) {
        this.lastScrapedAt = lastScrapedAt;
    }

    public String getScrapeStatus() {
        return scrapeStatus;
    }

    public void setScrapeStatus(String scrapeStatus) {
        this.scrapeStatus = scrapeStatus;
    }

    public String getScrapeError() {
        return scrapeError;
    }

    public void setScrapeError(String scrapeError) {
        this.scrapeError = scrapeError;
    }
}
