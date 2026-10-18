package com.oms.adminbff.catalog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProductPageScraper {

    private static final Logger log = LoggerFactory.getLogger(ProductPageScraper.class);
    private static final String USER_AGENT =
            "Mozilla/5.0 (compatible; SAPersonalShopperCatalog/1.0; +https://localhost; weekly product image refresh)";
    private static final Pattern JSON_LD_IMAGE = Pattern.compile(
            "\"image\"\\s*:\\s*(?:\"([^\"]+)\"|\\[\\s*\"([^\"]+)\")",
            Pattern.CASE_INSENSITIVE);

    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public Optional<String> findPrimaryImageUrl(String pageUrl) {
        try {
            Document doc = Jsoup.connect(pageUrl)
                    .userAgent(USER_AGENT)
                    .timeout(20_000)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .get();

            Optional<String> fromMeta = firstPresent(
                    abs(doc, metaContent(doc, "og:image")),
                    abs(doc, metaContent(doc, "og:image:secure_url")),
                    abs(doc, metaContent(doc, "twitter:image")),
                    abs(doc, metaContent(doc, "twitter:image:src"))
            ).filter(this::looksLikeProductImage);
            if (fromMeta.isPresent()) {
                return fromMeta;
            }

            Optional<String> fromJsonLd = extractFromJsonLd(doc.html())
                    .flatMap(u -> abs(doc, u))
                    .filter(this::looksLikeProductImage);
            if (fromJsonLd.isPresent()) {
                return fromJsonLd;
            }

            Elements imgs = doc.select(
                    "img[src*=product], img[src*=Product], img.product-image, img[itemprop=image], " +
                            "main img[src], .product img[src], [data-testid=product] img[src]");
            for (Element img : imgs) {
                String src = firstNonBlank(img.attr("abs:src"), img.attr("abs:data-src"), img.attr("abs:data-original"));
                if (looksLikeProductImage(src)) {
                    return Optional.of(src);
                }
            }

            for (Element img : doc.select("img[src]")) {
                String src = firstNonBlank(img.attr("abs:src"), img.attr("abs:data-src"));
                if (looksLikeProductImage(src)) {
                    return Optional.of(src);
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to scrape page {}: {}", pageUrl, e.getMessage());
            return Optional.empty();
        }
    }

    public DownloadedImage downloadImage(String imageUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(imageUrl))
                .timeout(Duration.ofSeconds(25))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
                .GET()
                .build();
        HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Image download HTTP " + response.statusCode() + " for " + imageUrl);
        }
        byte[] body = response.body();
        if (body == null || body.length < 4_000) {
            throw new IOException("Image too small or empty for " + imageUrl + " (" + (body == null ? 0 : body.length) + " bytes)");
        }
        String contentType = response.headers().firstValue("content-type").orElse("image/jpeg");
        if (contentType.contains(";")) {
            contentType = contentType.substring(0, contentType.indexOf(';')).trim();
        }
        if (!contentType.startsWith("image/")) {
            contentType = guessContentType(imageUrl);
        }
        return new DownloadedImage(body, contentType);
    }

    public record DownloadedImage(byte[] bytes, String contentType) {
    }

    private Optional<String> extractFromJsonLd(String html) {
        Matcher m = JSON_LD_IMAGE.matcher(html);
        if (m.find()) {
            String url = firstNonBlank(m.group(1), m.group(2));
            return Optional.ofNullable(url);
        }
        return Optional.empty();
    }

    private Optional<String> abs(Document doc, String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }
        String trimmed = url.trim();
        if (trimmed.startsWith("//")) {
            trimmed = "https:" + trimmed;
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return Optional.of(trimmed);
        }
        try {
            return Optional.of(doc.location() == null || doc.location().isBlank()
                    ? trimmed
                    : URI.create(doc.location()).resolve(trimmed).toString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String metaContent(Document doc, String property) {
        Element byProp = doc.selectFirst("meta[property=" + property + "]");
        if (byProp != null && !byProp.attr("content").isBlank()) {
            return byProp.attr("content");
        }
        Element byName = doc.selectFirst("meta[name=" + property + "]");
        if (byName != null && !byName.attr("content").isBlank()) {
            return byName.attr("content");
        }
        return null;
    }

    private boolean looksLikeProductImage(String src) {
        if (src == null || src.isBlank()) {
            return false;
        }
        String lower = src.toLowerCase(Locale.ROOT);
        if (lower.contains("logo") || lower.contains("favicon") || lower.contains("icon")
                || lower.contains("sprite") || lower.contains("pixel") || lower.contains("1x1")
                || lower.contains("placeholder") || lower.endsWith(".svg") || lower.endsWith(".ico")
                || lower.contains("_ico") || lower.contains("apple-touch")) {
            return false;
        }
        return lower.contains(".jpg") || lower.contains(".jpeg") || lower.contains(".png")
                || lower.contains(".webp") || lower.contains("/image") || lower.contains("cdn")
                || lower.contains("shopify") || lower.contains("product");
    }

    private String guessContentType(String url) {
        String lower = url.toLowerCase(Locale.ROOT);
        if (lower.contains(".png")) {
            return "image/png";
        }
        if (lower.contains(".webp")) {
            return "image/webp";
        }
        if (lower.contains(".gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }

    @SafeVarargs
    private final Optional<String> firstPresent(Optional<String>... values) {
        for (Optional<String> value : values) {
            if (value != null && value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
