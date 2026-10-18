package com.oms.adminbff.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Live product previews. Stores with a public catalog API are queried as JSON.
 * Stores whose search HTML already contains products are read from that page.
 * Curated catalog matches fill gaps when a store is down or blocks the request.
 */
@Service
public class LiveSearchService {

    private static final Logger log = LoggerFactory.getLogger(LiveSearchService.class);
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int PER_STORE = 3;
    private static final int MAX_RESULTS = 30;

    private final CatalogService catalogService;
    private final ObjectMapper mapper;
    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(8))
            .build();
    private final ExecutorService pool = Executors.newFixedThreadPool(12);

    public LiveSearchService(CatalogService catalogService, ObjectMapper mapper) {
        this.catalogService = catalogService;
        this.mapper = mapper;
    }

    public Map<String, Object> search(String query) {
        String q = query == null ? "" : query.trim();
        if (q.isBlank()) {
            return Map.of(
                    "query", "",
                    "results", List.of(),
                    "storesQueried", List.of(),
                    "storesOk", List.of(),
                    "storesFailed", List.of(),
                    "note", "Enter a product name to search."
            );
        }

        List<Map<String, Object>> catalogHits = catalogMatches(q);
        List<Supplier<StoreBatch>> jobs = List.of(
                () -> searchTakealot(q),
                () -> searchShopify("https://stevemadden.co.za", "Steve Madden", "Shoes", q),
                () -> searchShopify("https://www.edgars.co.za", "Edgars", "Fashion", q),
                () -> searchShopify("https://www.ackermans.co.za", "Ackermans", "Fashion", q),
                () -> searchShopify("https://www.pepstores.com", "PEP", "Fashion", q),
                () -> searchShopify("https://www.dunns.co.za", "Dunns", "Fashion", q),
                () -> searchBash("brand/jet", "Jet", "Fashion", q),
                () -> searchBash("brand/foschini", "Foschini", "Fashion", q),
                () -> searchBash(null, null, "Fashion", q),
                () -> searchCottonOn(q)
        );
        List<String> storesQueried = List.of(
                "Takealot", "Steve Madden", "Edgars", "Ackermans", "PEP", "Dunns",
                "Jet", "Foschini", "Bash", "Cotton On"
        );

        List<CompletableFuture<StoreBatch>> futures = new ArrayList<>();
        for (Supplier<StoreBatch> job : jobs) {
            futures.add(CompletableFuture.supplyAsync(job, pool));
        }

        List<String> storesOk = new ArrayList<>();
        List<String> storesFailed = new ArrayList<>();
        List<Map<String, Object>> live = new ArrayList<>();
        for (CompletableFuture<StoreBatch> future : futures) {
            try {
                StoreBatch batch = future.get(18, TimeUnit.SECONDS);
                if (batch.ok() && !batch.items().isEmpty()) {
                    storesOk.add(batch.store());
                    live.addAll(batch.items());
                } else {
                    storesFailed.add(batch.store());
                }
            } catch (Exception e) {
                log.debug("Live search store future failed: {}", e.getMessage());
            }
        }

        Map<String, Map<String, Object>> merged = new LinkedHashMap<>();
        for (Map<String, Object> row : catalogHits) {
            merged.put(key(row), row);
        }
        for (Map<String, Object> row : live) {
            merged.putIfAbsent(key(row), row);
        }

        List<Map<String, Object>> results = new ArrayList<>(merged.values());
        results.sort(Comparator.comparing(r -> "catalog".equals(r.get("source")) ? 0 : 1));
        if (results.size() > MAX_RESULTS) {
            results = new ArrayList<>(results.subList(0, MAX_RESULTS));
        }

        String note;
        if (results.isEmpty()) {
            note = "No matches yet. Try another product name — or add it from a paste link on the cart.";
        } else if (storesOk.isEmpty()) {
            note = "Showing curated catalog matches. Live store search was blocked or unavailable.";
        } else {
            note = "Previews from " + String.join(", ", storesOk)
                    + (storesFailed.isEmpty() ? "" : ". No live results from " + String.join(", ", storesFailed))
                    + ". Stay on this site — Add to order sends it to our shopper.";
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", q);
        body.put("results", results);
        body.put("storesQueried", storesQueried);
        body.put("storesOk", storesOk);
        body.put("storesFailed", storesFailed);
        body.put("note", note);
        return body;
    }

    private StoreBatch searchTakealot(String q) {
        String url = "https://api.takealot.com/rest/v-1-14-0/searches/products?qsearch="
                + enc(q) + "&pagecount=" + PER_STORE;
        try {
            JsonNode root = getJson(url);
            JsonNode results = root.path("sections").path("products").path("results");
            List<Map<String, Object>> items = new ArrayList<>();
            if (results.isArray()) {
                for (JsonNode node : results) {
                    if (items.size() >= PER_STORE) {
                        break;
                    }
                    JsonNode view = node.path("product_views");
                    JsonNode core = view.path("core");
                    String title = text(core, "title");
                    String slug = text(core, "slug");
                    long id = core.path("id").asLong(0);
                    if (title == null || slug == null || id == 0) {
                        continue;
                    }
                    String productUrl = "https://www.takealot.com/" + slug + "/PLID" + id;
                    BigDecimal price = null;
                    JsonNode prices = view.path("buybox_summary").path("prices");
                    if (prices.isArray() && !prices.isEmpty()) {
                        price = money(prices.get(0));
                    }
                    BigDecimal was = money(view.path("buybox_summary").path("listing_price"));
                    if (price != null && was != null && was.compareTo(price) <= 0) {
                        was = null;
                    }
                    String image = null;
                    JsonNode images = view.path("gallery").path("images");
                    if (images.isArray() && !images.isEmpty()) {
                        image = images.get(0).asText("").replace("{size}", "325x325");
                        if (image.isBlank()) {
                            image = null;
                        }
                    }
                    items.add(preview("Takealot", title, productUrl, image, price, was, "Tech"));
                }
            }
            return new StoreBatch("Takealot", !items.isEmpty(), items);
        } catch (Exception e) {
            log.warn("Takealot live search failed: {}", e.getMessage());
            return new StoreBatch("Takealot", false, List.of());
        }
    }

    /** Shopify's public suggest endpoint. Price is already in rands (for example "249.99"). */
    private StoreBatch searchShopify(String origin, String store, String category, String q) {
        String url = origin + "/search/suggest.json?q=" + enc(q)
                + "&resources%5Btype%5D=product&resources%5Blimit%5D=" + PER_STORE;
        try {
            JsonNode products = getJson(url).path("resources").path("results").path("products");
            List<Map<String, Object>> items = new ArrayList<>();
            if (products.isArray()) {
                for (JsonNode product : products) {
                    if (items.size() >= PER_STORE) {
                        break;
                    }
                    String title = text(product, "title");
                    String path = text(product, "url");
                    if (title == null || path == null) {
                        continue;
                    }
                    String productUrl = path.startsWith("http") ? path : origin + path;
                    int queryIndex = productUrl.indexOf('?');
                    if (queryIndex > 0) {
                        productUrl = productUrl.substring(0, queryIndex);
                    }
                    BigDecimal price = money(product.path("price"));
                    BigDecimal was = money(product.path("compare_at_price_min"));
                    if (price != null && was != null && was.compareTo(price) <= 0) {
                        was = null;
                    }
                    String image = text(product.path("featured_image"), "url");
                    if (image == null) {
                        image = text(product, "image");
                    }
                    items.add(preview(store, title, productUrl, image, price, was, category));
                }
            }
            return new StoreBatch(store, !items.isEmpty(), items);
        } catch (Exception e) {
            log.warn("{} live search failed: {}", store, e.getMessage());
            return new StoreBatch(store, false, List.of());
        }
    }

    /**
     * Bash (TFG) public intelligent-search API. Jet and Foschini are brand facets.
     * A search with no facet returns the product's own brand, which is how Sportscene
     * sneakers (adidas, Nike, and others) show up on Bash.
     */
    private StoreBatch searchBash(String facet, String storeOverride, String category, String q) {
        String batchName = storeOverride == null ? "Bash" : storeOverride;
        String url = "https://bash.com/api/io/_v/api/intelligent-search/product_search/"
                + (facet == null ? "" : facet)
                + "?query=" + enc(q) + "&count=" + PER_STORE + "&hideUnavailableItems=true";
        try {
            JsonNode products = getJson(url).path("products");
            List<Map<String, Object>> items = new ArrayList<>();
            if (products.isArray()) {
                for (JsonNode product : products) {
                    if (items.size() >= PER_STORE) {
                        break;
                    }
                    String title = text(product, "productName");
                    String path = text(product, "link");
                    if (title == null || path == null) {
                        continue;
                    }
                    String productUrl = path.startsWith("http") ? path : "https://bash.com" + path;
                    String store = storeOverride;
                    if (store == null) {
                        store = text(product, "brand");
                        if (store == null) {
                            store = "Bash";
                        }
                    }
                    BigDecimal price = money(product.path("priceRange").path("sellingPrice").path("lowPrice"));
                    BigDecimal was = money(product.path("priceRange").path("listPrice").path("lowPrice"));
                    if (price != null && was != null && was.compareTo(price) <= 0) {
                        was = null;
                    }
                    String image = null;
                    JsonNode itemsNode = product.path("items");
                    if (itemsNode.isArray() && !itemsNode.isEmpty()) {
                        JsonNode images = itemsNode.get(0).path("images");
                        if (images.isArray() && !images.isEmpty()) {
                            image = text(images.get(0), "imageUrl");
                        }
                    }
                    items.add(preview(store, title, productUrl, image, price, was, category));
                }
            }
            return new StoreBatch(batchName, !items.isEmpty(), items);
        } catch (Exception e) {
            log.warn("{} live search failed: {}", batchName, e.getMessage());
            return new StoreBatch(batchName, false, List.of());
        }
    }

    /** Cotton On publishes products in the search page HTML, including a price JSON blob. */
    private StoreBatch searchCottonOn(String q) {
        String url = "https://cottonon.com/ZA/search/?q=" + enc(q);
        try {
            Document doc = fetchHtml(url);
            Map<String, BigDecimal> prices = cottonOnPrices(doc);
            List<Map<String, Object>> items = new ArrayList<>();
            for (Element link : doc.select("a.name-link[href]")) {
                if (items.size() >= PER_STORE) {
                    break;
                }
                String href = absUrl(link, "https://cottonon.com");
                String name = textOr(link.attr("title"), link.text());
                if (href == null || name == null || name.length() < 3) {
                    continue;
                }
                if (name.regionMatches(true, 0, "Go to Product:", 0, "Go to Product:".length())) {
                    name = name.substring("Go to Product:".length()).trim();
                }
                int queryIndex = href.indexOf('?');
                if (queryIndex > 0) {
                    href = href.substring(0, queryIndex);
                }
                items.add(preview(
                        "Cotton On",
                        cleanName(name),
                        href,
                        nearestImage(link),
                        prices.get(name.toLowerCase(Locale.ROOT)),
                        null,
                        "Fashion"
                ));
            }
            return new StoreBatch("Cotton On", !items.isEmpty(), items);
        } catch (Exception e) {
            log.warn("Cotton On live search failed: {}", e.getMessage());
            return new StoreBatch("Cotton On", false, List.of());
        }
    }

    private Map<String, BigDecimal> cottonOnPrices(Document doc) {
        Map<String, BigDecimal> prices = new LinkedHashMap<>();
        for (Element node : doc.select("[data-gtag]")) {
            String raw = node.attr("data-gtag").trim();
            if (!raw.startsWith("{") && !raw.startsWith("[")) {
                continue;
            }
            try {
                collectCottonOnPrice(mapper.readTree(raw), prices);
            } catch (Exception ignored) {
                /* analytics blob, not a product */
            }
        }
        return prices;
    }

    private void collectCottonOnPrice(JsonNode node, Map<String, BigDecimal> prices) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                collectCottonOnPrice(child, prices);
            }
            return;
        }
        if (!node.isObject()) {
            return;
        }
        String name = text(node, "name");
        if (name == null) {
            name = text(node, "item_name");
        }
        BigDecimal price = money(node.path("price"));
        if (name != null && price != null) {
            prices.putIfAbsent(name.toLowerCase(Locale.ROOT), price);
        }
        for (String field : List.of("impressions", "products")) {
            if (node.has(field)) {
                collectCottonOnPrice(node.get(field), prices);
            }
        }
    }

    private List<Map<String, Object>> catalogMatches(String q) {
        String lower = q.toLowerCase(Locale.ROOT);
        String[] tokens = lower.split("\\s+");
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> deal : catalogService.listDeals()) {
            String hay = String.valueOf(deal.getOrDefault("name", "")).toLowerCase(Locale.ROOT)
                    + " " + String.valueOf(deal.getOrDefault("store", "")).toLowerCase(Locale.ROOT)
                    + " " + String.valueOf(deal.getOrDefault("category", "")).toLowerCase(Locale.ROOT);
            boolean match = hay.contains(lower)
                    || java.util.Arrays.stream(tokens).filter(t -> t.length() > 1)
                    .allMatch(t -> tokenMatch(hay, t));
            if (!match) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", "catalog-" + deal.get("id"));
            row.put("store", deal.get("store"));
            row.put("name", deal.get("name"));
            row.put("url", deal.get("url"));
            row.put("price", deal.get("price"));
            row.put("was", deal.get("was"));
            row.put("category", deal.get("category"));
            row.put("badge", deal.get("badge"));
            row.put("imageUrl", deal.get("imageUrl"));
            row.put("source", "catalog");
            out.add(row);
            if (out.size() >= 8) {
                break;
            }
        }
        return out;
    }

    private JsonNode getJson(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(12))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json,text/plain,*/*")
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode() + " for " + url);
        }
        return mapper.readTree(response.body());
    }

    private Document fetchHtml(String url) throws Exception {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml")
                .header("Accept-Language", "en-ZA,en;q=0.9")
                .timeout(12_000)
                .maxBodySize(2_000_000)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .get();
    }

    private Map<String, Object> preview(
            String store,
            String name,
            String url,
            String imageUrl,
            BigDecimal price,
            BigDecimal was,
            String category
    ) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", "live-" + store.toLowerCase(Locale.ROOT).replace(' ', '-') + "-" + Integer.toHexString(url.hashCode()));
        row.put("store", store);
        row.put("name", name);
        row.put("url", url);
        row.put("imageUrl", imageUrl);
        row.put("price", price);
        row.put("was", was);
        row.put("category", category);
        row.put("badge", null);
        row.put("source", "live");
        return row;
    }

    private static boolean tokenMatch(String hay, String token) {
        if (hay.contains(token)) {
            return true;
        }
        if (token.endsWith("s") && token.length() > 3 && hay.contains(token.substring(0, token.length() - 1))) {
            return true;
        }
        return hay.contains(token + "s");
    }

    private static String key(Map<String, Object> row) {
        Object url = row.get("url");
        if (url != null) {
            return String.valueOf(url).toLowerCase(Locale.ROOT);
        }
        return String.valueOf(row.get("store")) + "|" + String.valueOf(row.get("name"));
    }

    private static String enc(String q) {
        return URLEncoder.encode(q, StandardCharsets.UTF_8);
    }

    private static BigDecimal money(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String raw = node.asText("").replace(",", "").trim();
        if (raw.isEmpty() || "0".equals(raw) || "0.0".equals(raw) || "0.00".equals(raw)) {
            return null;
        }
        try {
            BigDecimal value = new BigDecimal(raw);
            if (value.signum() <= 0) {
                return null;
            }
            if (raw.indexOf('.') < 0 && value.compareTo(new BigDecimal("10000")) > 0) {
                value = value.movePointLeft(2);
            }
            return value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String text(JsonNode node, String field) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        String value = node.path(field).asText("").trim();
        return value.isEmpty() ? null : value;
    }

    private static String absUrl(Element a, String origin) {
        String href = a.hasAttr("abs:href") ? a.attr("abs:href") : a.attr("href");
        if (href == null || href.isBlank() || href.startsWith("#") || href.startsWith("javascript:")) {
            return null;
        }
        if (href.startsWith("//")) {
            return "https:" + href;
        }
        if (href.startsWith("http")) {
            return href;
        }
        if (href.startsWith("/")) {
            return origin + href;
        }
        return origin + "/" + href;
    }

    private static String nearestImage(Element el) {
        Element scope = el;
        Element img = null;
        for (int i = 0; i < 6 && scope != null; i++) {
            img = scope.selectFirst("img[src], img[data-src], img[data-original]");
            if (img != null) {
                break;
            }
            scope = scope.parent();
        }
        if (img == null) {
            return null;
        }
        String src = firstNonBlank(img.attr("abs:src"), img.attr("abs:data-src"), img.attr("abs:data-original"), img.attr("src"));
        if (src == null || src.contains("data:image") || src.contains("placeholder") || src.contains("favicon")) {
            return null;
        }
        if (src.startsWith("//")) {
            src = "https:" + src;
        }
        return src;
    }

    private static String textOr(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String cleanName(String name) {
        String cleaned = name.replaceAll("\\s+", " ").trim();
        if (cleaned.length() > 160) {
            cleaned = cleaned.substring(0, 160).trim();
        }
        return cleaned;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private record StoreBatch(String store, boolean ok, List<Map<String, Object>> items) {}
}
