package com.oms.adminbff.catalog;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;
    private final LiveSearchService liveSearchService;

    public CatalogController(CatalogService catalogService, LiveSearchService liveSearchService) {
        this.catalogService = catalogService;
        this.liveSearchService = liveSearchService;
    }

    /** Live + curated product previews for the storefront Find-it search. */
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam("q") String q) {
        return liveSearchService.search(q);
    }

    @GetMapping("/deals")
    public ResponseEntity<?> deals() {
        return ResponseEntity.ok(Map.of(
                "deals", catalogService.listDeals(),
                "source", "catalog-scraper"
        ));
    }

    @GetMapping("/stores")
    public ResponseEntity<?> stores() {
        return ResponseEntity.ok(Map.of(
                "stores", catalogService.listStores(),
                "source", "catalog-scraper"
        ));
    }

    @GetMapping("/images/deal/{id}")
    public ResponseEntity<byte[]> dealImage(@PathVariable String id) {
        try {
            var image = catalogService.loadDealImage(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic().getHeaderValue())
                    .contentType(MediaType.parseMediaType(image.contentType()))
                    .body(image.bytes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/images/store/{id}")
    public ResponseEntity<byte[]> storeImage(@PathVariable String id) {
        try {
            var image = catalogService.loadStoreImage(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic().getHeaderValue())
                    .contentType(MediaType.parseMediaType(image.contentType()))
                    .body(image.bytes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/scrape")
    public ResponseEntity<?> scrapeNow() {
        return ResponseEntity.accepted().body(catalogService.scrapeAll());
    }
}
