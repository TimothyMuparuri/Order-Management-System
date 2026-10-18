package com.oms.intake.service;

import com.oms.common.result.Result;
import com.oms.eventing.Topics;
import com.oms.eventing.events.Events;
import com.oms.eventing.publish.DomainEventPublisher;
import com.oms.intake.storage.ObjectStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class IntakeApplicationService {

    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE);

    private final ObjectStorage objectStorage;
    private final DomainEventPublisher events;
    private final RestClient restClient;
    private final String orderServiceBaseUrl;

    public IntakeApplicationService(
            ObjectStorage objectStorage,
            DomainEventPublisher events,
            RestClient.Builder restClientBuilder,
            @Value("${oms.order-service.url:http://localhost:8081}") String orderServiceBaseUrl
    ) {
        this.objectStorage = objectStorage;
        this.events = events;
        this.restClient = restClientBuilder.build();
        this.orderServiceBaseUrl = orderServiceBaseUrl;
    }

    @Transactional
    public Result<ImageUploadResponse> uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.validation("IMAGE_REQUIRED", "Image file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return Result.validation("INVALID_IMAGE_FORMAT", "Only JPEG or PNG images are accepted");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            return Result.validation("IMAGE_TOO_LARGE", "Image must be at most 10 MB");
        }
        String imageId = UUID.randomUUID().toString();
        String objectKey = "images/" + imageId;
        try {
            objectStorage.store(objectKey, file.getBytes(), contentType);
            objectStorage.store(objectKey + ".type", contentType.getBytes(java.nio.charset.StandardCharsets.UTF_8), "text/plain");
        } catch (IOException e) {
            return Result.domain("STORAGE_ERROR", "Failed to store image");
        }
        events.publish(imageId, Topics.IMAGES_UPLOADED, new Events.ImageUploaded(
                imageId, objectKey, contentType, file.getSize()));
        return Result.ok(new ImageUploadResponse(imageId, objectKey, contentType, file.getSize()));
    }

    public Result<ImagePayload> loadImage(String imageId) {
        if (imageId == null || imageId.isBlank()) {
            return Result.validation("IMAGE_REQUIRED", "Image id is required");
        }
        String objectKey = "images/" + imageId.trim();
        try {
            byte[] bytes = objectStorage.load(objectKey);
            String contentType = "image/jpeg";
            try {
                String stored = new String(objectStorage.load(objectKey + ".type"), java.nio.charset.StandardCharsets.UTF_8).trim();
                if (!stored.isBlank()) {
                    contentType = stored;
                }
            } catch (IOException ignored) {
                contentType = sniffContentType(bytes);
            }
            return Result.ok(new ImagePayload(bytes, contentType));
        } catch (IOException e) {
            return Result.domain("IMAGE_NOT_FOUND", "Image not found: " + imageId);
        }
    }

    private static String sniffContentType(byte[] bytes) {
        if (bytes != null && bytes.length >= 4
                && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return "image/png";
        }
        return "image/jpeg";
    }

    @Transactional
    public Result<Map<String, Object>> submitOrder(SubmitOrderRequest request) {
        if (request.customerId() == null || request.customerId().isBlank()) {
            return Result.validation("CUSTOMER_REQUIRED", "Customer id is required");
        }
        if (request.customerName() == null || request.customerName().isBlank()) {
            return Result.validation("CUSTOMER_NAME_REQUIRED", "Customer name is required");
        }
        if (request.phone() == null || request.phone().isBlank()) {
            return Result.validation("PHONE_REQUIRED", "Phone number is required so admin can contact you");
        }
        List<OrderLineInput> lines = request.lines();
        if (lines == null || lines.isEmpty() || lines.size() > 100) {
            return Result.validation("INVALID_LINE_COUNT", "Order must have 1..100 lines");
        }
        List<Map<String, Object>> validated = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            OrderLineInput line = lines.get(i);
            boolean hasSearch = line.searchTerm() != null && !line.searchTerm().isBlank();
            boolean hasImage = line.imageId() != null && !line.imageId().isBlank();
            boolean hasCatalogImage = line.imageUrl() != null && !line.imageUrl().isBlank();
            boolean hasName = line.productName() != null && !line.productName().isBlank();
            if (!hasSearch && !hasImage && !hasName) {
                return Result.validation("LINE_SPEC_REQUIRED", "Line " + (i + 1) + " needs a product name, search term, or image");
            }
            if (hasSearch && (line.searchTerm().length() < 1 || line.searchTerm().length() > 200)) {
                return Result.validation("INVALID_SEARCH_TERM", "Line " + (i + 1) + " searchTerm must be 1..200 chars");
            }
            if (line.quantity() < 1 || line.quantity() > 10_000) {
                return Result.validation("INVALID_QUANTITY", "Line " + (i + 1) + " quantity must be 1..10000");
            }

            String label = hasName ? line.productName().trim()
                    : hasSearch ? line.searchTerm().trim()
                    : ("image:" + line.imageId());
            String itemRef;
            if (line.itemRef() != null && !line.itemRef().isBlank()) {
                itemRef = line.itemRef();
            } else if (hasImage) {
                itemRef = "image:" + line.imageId();
            } else {
                itemRef = "term:" + label;
            }
            if (itemRef.length() > 240) {
                itemRef = itemRef.substring(0, 240);
            }

            Map<String, Object> row = new HashMap<>();
            row.put("itemRef", itemRef);
            row.put("requestedQty", line.quantity());
            row.put("productName", label);
            if (line.storeName() != null && !line.storeName().isBlank()) {
                row.put("storeName", line.storeName().trim());
            }
            if (line.productUrl() != null && !line.productUrl().isBlank()) {
                row.put("productUrl", line.productUrl().trim());
            }
            if (line.productSize() != null && !line.productSize().isBlank()) {
                row.put("productSize", line.productSize().trim());
            }
            if (hasCatalogImage) {
                row.put("imageUrl", line.imageUrl().trim());
            }
            if (line.unitPrice() != null) {
                row.put("unitPrice", line.unitPrice());
            }
            validated.add(row);
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("customerId", request.customerId().trim());
            body.put("customerName", request.customerName().trim());
            body.put("phone", request.phone().trim());
            if (request.email() != null && !request.email().isBlank()) {
                body.put("email", request.email().trim());
            }
            if (request.address() != null && !request.address().isBlank()) {
                body.put("address", request.address().trim());
            }
            String source = request.source() == null || request.source().isBlank() ? "Web" : request.source().trim();
            body.put("source", source);
            body.put("lines", validated);

            Map<?, ?> response = restClient.post()
                    .uri(orderServiceBaseUrl + "/api/internal/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", response.get("orderId"));
            result.put("customerId", response.get("customerId"));
            result.put("customerName", response.get("customerName"));
            result.put("email", response.get("email"));
            result.put("phone", response.get("phone"));
            result.put("address", response.get("address"));
            result.put("source", response.get("source") != null ? response.get("source") : source);
            result.put("orderStatus", response.get("orderStatus"));
            result.put("orderTotal", response.get("orderTotal"));
            result.put("lines", response.get("lines"));
            return Result.ok(result);
        } catch (Exception e) {
            return Result.domain("ORDER_CREATE_FAILED", "Failed to create order: " + e.getMessage());
        }
    }

    public record ImageUploadResponse(String imageId, String objectKey, String contentType, long sizeBytes) {}

    public record ImagePayload(byte[] bytes, String contentType) {}

    public record OrderLineInput(
            String searchTerm,
            String imageId,
            String itemRef,
            String productName,
            String storeName,
            String productUrl,
            String imageUrl,
            String productSize,
            java.math.BigDecimal unitPrice,
            int quantity
    ) {}

    public record SubmitOrderRequest(
            String customerId,
            String customerName,
            String email,
            String phone,
            String address,
            String source,
            List<OrderLineInput> lines
    ) {
        public SubmitOrderRequest(String customerId, String customerName, List<OrderLineInput> lines) {
            this(customerId, customerName, null, null, null, "Web", lines);
        }
    }
}
