package com.oms.intake.service;

import com.oms.common.result.Result;
import com.oms.eventing.Topics;
import com.oms.eventing.events.Events;
import com.oms.eventing.outbox.OutboxWriter;
import com.oms.intake.storage.ObjectStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class IntakeApplicationService {

    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE);

    private final ObjectStorage objectStorage;
    private final OutboxWriter outboxWriter;
    private final RestClient restClient;
    private final String orderServiceBaseUrl;

    public IntakeApplicationService(
            ObjectStorage objectStorage,
            OutboxWriter outboxWriter,
            RestClient.Builder restClientBuilder,
            @Value("${oms.order-service.url:http://localhost:8081}") String orderServiceBaseUrl
    ) {
        this.objectStorage = objectStorage;
        this.outboxWriter = outboxWriter;
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
        } catch (IOException e) {
            return Result.domain("STORAGE_ERROR", "Failed to store image");
        }
        outboxWriter.enqueue(imageId, Topics.IMAGES_UPLOADED, new Events.ImageUploaded(
                imageId, objectKey, contentType, file.getSize()));
        return Result.ok(new ImageUploadResponse(imageId, objectKey, contentType, file.getSize()));
    }

    @Transactional
    public Result<Map<String, Object>> submitOrder(SubmitOrderRequest request) {
        List<OrderLineInput> lines = request.lines();
        if (lines == null || lines.isEmpty() || lines.size() > 100) {
            return Result.validation("INVALID_LINE_COUNT", "Order must have 1..100 lines");
        }
        List<Map<String, Object>> validated = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            OrderLineInput line = lines.get(i);
            boolean hasSearch = line.searchTerm() != null && !line.searchTerm().isBlank();
            boolean hasImage = line.imageId() != null && !line.imageId().isBlank();
            if (!hasSearch && !hasImage) {
                return Result.validation("LINE_SPEC_REQUIRED", "Line " + (i + 1) + " needs searchTerm or image");
            }
            if (hasSearch && (line.searchTerm().length() < 1 || line.searchTerm().length() > 200)) {
                return Result.validation("INVALID_SEARCH_TERM", "Line " + (i + 1) + " searchTerm must be 1..200 chars");
            }
            if (line.quantity() < 1 || line.quantity() > 10_000) {
                return Result.validation("INVALID_QUANTITY", "Line " + (i + 1) + " quantity must be 1..10000");
            }
            String itemRef = hasSearch ? ("term:" + line.searchTerm()) : ("image:" + line.imageId());
            if (line.itemRef() != null && !line.itemRef().isBlank()) {
                itemRef = line.itemRef();
            }
            validated.add(Map.of("itemRef", itemRef, "requestedQty", line.quantity()));
        }

        try {
            Map<?, ?> response = restClient.post()
                    .uri(orderServiceBaseUrl + "/api/internal/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("customerId", request.customerId(), "lines", validated))
                    .retrieve()
                    .body(Map.class);
            return Result.ok(Map.of(
                    "orderId", response.get("orderId"),
                    "orderStatus", response.get("orderStatus")
            ));
        } catch (Exception e) {
            return Result.domain("ORDER_CREATE_FAILED", "Failed to create order: " + e.getMessage());
        }
    }

    public record ImageUploadResponse(String imageId, String objectKey, String contentType, long sizeBytes) {}
    public record OrderLineInput(String searchTerm, String imageId, String itemRef, int quantity) {}
    public record SubmitOrderRequest(String customerId, List<OrderLineInput> lines) {}
}
