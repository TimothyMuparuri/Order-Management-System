package com.oms.adminbff.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final CatalogDealRepository dealRepository;
    private final CatalogStoreRepository storeRepository;
    private final ProductPageScraper scraper;
    private final ObjectMapper objectMapper;
    private final Path imageDir;
    private final long delayMs;
    private final AtomicBoolean scraping = new AtomicBoolean(false);

    public CatalogService(
            CatalogDealRepository dealRepository,
            CatalogStoreRepository storeRepository,
            ProductPageScraper scraper,
            ObjectMapper objectMapper,
            @Value("${oms.catalog.image-dir:./data/catalog-images}") String imageDir,
            @Value("${oms.catalog.scrape-delay-ms:1500}") long delayMs
    ) throws IOException {
        this.dealRepository = dealRepository;
        this.storeRepository = storeRepository;
        this.scraper = scraper;
        this.objectMapper = objectMapper;
        this.imageDir = Path.of(imageDir).toAbsolutePath().normalize();
        this.delayMs = delayMs;
        Files.createDirectories(this.imageDir);
    }

    @Transactional
    public void seedIfEmpty() throws IOException {
        if (dealRepository.count() > 0 && storeRepository.count() > 0) {
            return;
        }
        JsonNode root;
        try (InputStream in = new ClassPathResource("catalog-seed.json").getInputStream()) {
            root = objectMapper.readTree(in);
        }
        if (dealRepository.count() == 0) {
            for (JsonNode n : root.path("deals")) {
                CatalogDeal deal = dealRepository.findById(n.path("id").asText()).orElseGet(CatalogDeal::new);
                deal.setId(n.path("id").asText());
                deal.setStore(n.path("store").asText());
                deal.setCategory(n.path("category").asText());
                deal.setName(n.path("name").asText());
                deal.setProductUrl(n.path("url").asText());
                deal.setPrice(BigDecimal.valueOf(n.path("price").asDouble()));
                if (n.hasNonNull("was")) {
                    deal.setWasPrice(BigDecimal.valueOf(n.path("was").asDouble()));
                }
                if (n.hasNonNull("badge")) {
                    deal.setBadge(n.path("badge").asText());
                }
                if (deal.getScrapeStatus() == null) {
                    deal.setScrapeStatus("PENDING");
                }
                dealRepository.save(deal);
            }
            log.info("Seeded {} catalog deals", dealRepository.count());
        }
        if (storeRepository.count() == 0) {
            for (JsonNode n : root.path("stores")) {
                CatalogStore store = new CatalogStore();
                store.setId(n.path("id").asText());
                store.setName(n.path("name").asText());
                store.setWebsiteUrl(n.path("url").asText());
                store.setCategory(n.path("category").asText());
                store.setMeta(textOrNull(n, "meta"));
                store.setSale(textOrNull(n, "sale"));
                store.setScrapeStatus("PENDING");
                storeRepository.save(store);
            }
            log.info("Seeded {} catalog stores", storeRepository.count());
        }
    }

    public Map<String, Object> scrapeAll() {
        if (!scraping.compareAndSet(false, true)) {
            return Map.of("status", "BUSY", "message", "A scrape is already running");
        }
        try {
            int dealOk = 0;
            int dealFail = 0;
            int storeOk = 0;
            int storeFail = 0;
            for (CatalogDeal deal : dealRepository.findAll()) {
                if (scrapeDeal(deal)) {
                    dealOk++;
                } else {
                    dealFail++;
                }
                sleepQuietly();
            }
            for (CatalogStore store : storeRepository.findAll()) {
                if (scrapeStore(store)) {
                    storeOk++;
                } else {
                    storeFail++;
                }
                sleepQuietly();
            }
            Map<String, Object> result = new HashMap<>();
            result.put("status", "DONE");
            result.put("dealsOk", dealOk);
            result.put("dealsFailed", dealFail);
            result.put("storesOk", storeOk);
            result.put("storesFailed", storeFail);
            result.put("finishedAt", Instant.now().toString());
            log.info("Catalog scrape finished: {}", result);
            return result;
        } finally {
            scraping.set(false);
        }
    }

    public boolean scrapeDeal(CatalogDeal deal) {
        try {
            var imageUrl = scraper.findPrimaryImageUrl(deal.getProductUrl());
            if (imageUrl.isEmpty()) {
                deal.setScrapeStatus("NO_IMAGE");
                deal.setScrapeError("No usable product image on page (favicon/logo ignored)");
                deal.setLastScrapedAt(Instant.now());
                dealRepository.save(deal);
                return false;
            }
            var downloaded = scraper.downloadImage(imageUrl.get());
            String ext = extensionFor(downloaded.contentType());
            String fileName = "deal-" + deal.getId() + ext;
            Files.write(imageDir.resolve(fileName), downloaded.bytes());
            deal.setSourceImageUrl(imageUrl.get());
            deal.setImageFileName(fileName);
            deal.setImageContentType(downloaded.contentType());
            deal.setScrapeStatus("OK");
            deal.setScrapeError(null);
            deal.setLastScrapedAt(Instant.now());
            dealRepository.save(deal);
            return true;
        } catch (Exception e) {
            deal.setScrapeStatus("ERROR");
            deal.setScrapeError(trim(e.getMessage(), 900));
            deal.setLastScrapedAt(Instant.now());
            dealRepository.save(deal);
            log.warn("Deal scrape failed for {}: {}", deal.getId(), e.getMessage());
            return false;
        }
    }

    public boolean scrapeStore(CatalogStore store) {
        try {
            var imageUrl = scraper.findPrimaryImageUrl(store.getWebsiteUrl());
            if (imageUrl.isEmpty()) {
                store.setScrapeStatus("NO_IMAGE");
                store.setLastScrapedAt(Instant.now());
                storeRepository.save(store);
                return false;
            }
            var downloaded = scraper.downloadImage(imageUrl.get());
            String ext = extensionFor(downloaded.contentType());
            String fileName = "store-" + store.getId() + ext;
            Files.write(imageDir.resolve(fileName), downloaded.bytes());
            store.setSourceImageUrl(imageUrl.get());
            store.setImageFileName(fileName);
            store.setImageContentType(downloaded.contentType());
            store.setScrapeStatus("OK");
            store.setLastScrapedAt(Instant.now());
            storeRepository.save(store);
            return true;
        } catch (Exception e) {
            store.setScrapeStatus("ERROR");
            store.setLastScrapedAt(Instant.now());
            storeRepository.save(store);
            log.warn("Store scrape failed for {}: {}", store.getId(), e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> listDeals() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (CatalogDeal d : dealRepository.findAllByOrderByStoreAscNameAsc()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId());
            m.put("store", d.getStore());
            m.put("category", d.getCategory());
            m.put("name", d.getName());
            m.put("url", d.getProductUrl());
            m.put("price", d.getPrice());
            m.put("was", d.getWasPrice());
            m.put("badge", d.getBadge());
            m.put("imageUrl", d.getImageFileName() == null ? null : "/api/catalog/images/deal/" + d.getId());
            m.put("scrapeStatus", d.getScrapeStatus());
            m.put("lastScrapedAt", d.getLastScrapedAt());
            out.add(m);
        }
        return out;
    }

    public List<Map<String, Object>> listStores() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (CatalogStore s : storeRepository.findAllByOrderByNameAsc()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            m.put("url", s.getWebsiteUrl());
            m.put("category", s.getCategory());
            m.put("meta", s.getMeta());
            m.put("sale", s.getSale());
            m.put("imageUrl", s.getImageFileName() == null ? null : "/api/catalog/images/store/" + s.getId());
            m.put("scrapeStatus", s.getScrapeStatus());
            m.put("lastScrapedAt", s.getLastScrapedAt());
            out.add(m);
        }
        return out;
    }

    public ImagePayload loadDealImage(String id) throws IOException {
        CatalogDeal deal = dealRepository.findById(id)
                .orElseThrow(() -> new IOException("Deal not found: " + id));
        if (deal.getImageFileName() == null) {
            throw new IOException("No image for deal: " + id);
        }
        Path path = imageDir.resolve(deal.getImageFileName());
        return new ImagePayload(Files.readAllBytes(path),
                deal.getImageContentType() == null ? "image/jpeg" : deal.getImageContentType());
    }

    public ImagePayload loadStoreImage(String id) throws IOException {
        CatalogStore store = storeRepository.findById(id)
                .orElseThrow(() -> new IOException("Store not found: " + id));
        if (store.getImageFileName() == null) {
            throw new IOException("No image for store: " + id);
        }
        Path path = imageDir.resolve(store.getImageFileName());
        return new ImagePayload(Files.readAllBytes(path),
                store.getImageContentType() == null ? "image/jpeg" : store.getImageContentType());
    }

    public record ImagePayload(byte[] bytes, String contentType) {
    }

    private void sleepQuietly() {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String extensionFor(String contentType) {
        if (contentType == null) {
            return ".jpg";
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }

    private String textOrNull(JsonNode n, String field) {
        return n.hasNonNull(field) ? n.path(field).asText() : null;
    }

    private String trim(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
