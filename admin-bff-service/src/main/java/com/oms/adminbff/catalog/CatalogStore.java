package com.oms.adminbff.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "catalog_store")
public class CatalogStore {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2000)
    private String websiteUrl;

    @Column(nullable = false)
    private String category;

    private String meta;

    private String sale;

    @Column(length = 2000)
    private String sourceImageUrl;

    private String imageFileName;

    private String imageContentType;

    private Instant lastScrapedAt;

    private String scrapeStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public String getSale() {
        return sale;
    }

    public void setSale(String sale) {
        this.sale = sale;
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
}
