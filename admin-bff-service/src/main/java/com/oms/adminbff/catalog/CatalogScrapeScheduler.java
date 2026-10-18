package com.oms.adminbff.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CatalogScrapeScheduler implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogScrapeScheduler.class);

    private final CatalogService catalogService;
    private final boolean scrapeOnStartup;

    public CatalogScrapeScheduler(
            CatalogService catalogService,
            @Value("${oms.catalog.scrape-on-startup:true}") boolean scrapeOnStartup
    ) {
        this.catalogService = catalogService;
        this.scrapeOnStartup = scrapeOnStartup;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        catalogService.seedIfEmpty();
        if (!scrapeOnStartup) {
            return;
        }
        boolean needsImages = catalogService.listDeals().stream()
                .anyMatch(d -> d.get("imageUrl") == null);
        if (needsImages) {
            log.info("Catalog images missing — starting scrape in background");
            Thread.ofVirtual().name("catalog-startup-scrape").start(catalogService::scrapeAll);
        }
    }

    /** Every Sunday at 03:00 (server local time). */
    @Scheduled(cron = "${oms.catalog.cron:0 0 3 * * SUN}")
    public void weeklyScrape() {
        log.info("Weekly catalog scrape starting");
        catalogService.scrapeAll();
    }
}
