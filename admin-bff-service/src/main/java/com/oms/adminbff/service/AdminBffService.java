package com.oms.adminbff.service;

import com.oms.common.domain.OrderStatus;
import com.oms.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminBffService {

    private final RestClient restClient;
    private final String orderServiceUrl;
    private final String intakeServiceUrl;
    private final String allocationServiceUrl;
    private final String deliveryServiceUrl;

    public AdminBffService(
            RestClient.Builder builder,
            @Value("${oms.order-service.url:http://localhost:8081}") String orderServiceUrl,
            @Value("${oms.intake-service.url:http://localhost:8082}") String intakeServiceUrl,
            @Value("${oms.allocation-service.url:http://localhost:8084}") String allocationServiceUrl,
            @Value("${oms.delivery-service.url:http://localhost:8085}") String deliveryServiceUrl
    ) {
        this.restClient = builder.build();
        this.orderServiceUrl = orderServiceUrl;
        this.intakeServiceUrl = intakeServiceUrl;
        this.allocationServiceUrl = allocationServiceUrl;
        this.deliveryServiceUrl = deliveryServiceUrl;
    }

    /**
     * Create an order via the same intake API the website uses (e.g. WhatsApp manual entry).
     */
    @CacheEvict(value = {"orderSummaries", "orderDetails"}, allEntries = true)
    public Result<Map<String, Object>> createOrder(Map<String, Object> body) {
        try {
            Map<String, Object> payload = body == null ? new HashMap<>() : new HashMap<>(body);
            if (!payload.containsKey("source") || payload.get("source") == null
                    || String.valueOf(payload.get("source")).isBlank()) {
                payload.put("source", "WhatsApp");
            }
            Map<String, Object> response = restClient.post()
                    .uri(intakeServiceUrl + "/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return Result.ok(response);
        } catch (RestClientResponseException e) {
            return Result.validation("ORDER_CREATE_FAILED", e.getResponseBodyAsString());
        } catch (Exception e) {
            return Result.validation("ORDER_CREATE_FAILED", e.getMessage() == null ? "Create failed" : e.getMessage());
        }
    }

    /** Not cached — new customer orders must appear on Refresh immediately. */
    public List<Map<String, Object>> viewOrderList() {
        return restClient.get()
                .uri(orderServiceUrl + "/api/orders")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Cacheable(value = "orderDetails", key = "#orderId")
    public Result<Map<String, Object>> viewOrderDetail(String orderId) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri(orderServiceUrl + "/api/orders/" + orderId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return Result.ok(body);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Result.domain("ORDER_NOT_FOUND", "Order not found: " + orderId);
            }
            return Result.domain("UPSTREAM_ERROR", e.getMessage());
        }
    }

    @CacheEvict(value = {"orderSummaries", "orderDetails"}, allEntries = true)
    public Result<OrderStatus> updateOrderStatus(String orderId, String status) {
        try {
            OrderStatus.valueOf(status);
        } catch (Exception e) {
            return Result.validation("INVALID_STATUS", "Undefined status value: " + status);
        }
        try {
            OrderStatus updated = restClient.put()
                    .uri(orderServiceUrl + "/api/orders/" + orderId + "/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("status", status))
                    .retrieve()
                    .body(OrderStatus.class);
            return Result.ok(updated);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Result.domain("ORDER_NOT_FOUND", "Order not found: " + orderId);
            }
            return Result.validation("STATUS_UPDATE_FAILED", e.getResponseBodyAsString());
        }
    }

    public Result<Map<String, Object>> recordBulkPurchase(Map<String, Object> body) {
        try {
            Map<String, Object> response = restClient.post()
                    .uri(allocationServiceUrl + "/api/bulk-purchases")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return Result.ok(response);
        } catch (RestClientResponseException e) {
            return Result.validation("BULK_RECORD_FAILED", e.getResponseBodyAsString());
        }
    }

    public Result<Map<String, Object>> markReceived(String bulkLineId, Map<String, Object> body) {
        try {
            Map<String, Object> response = restClient.post()
                    .uri(allocationServiceUrl + "/api/bulk-lines/" + bulkLineId + "/received")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return Result.ok(response);
        } catch (RestClientResponseException e) {
            return Result.validation("MARK_RECEIVED_FAILED", e.getResponseBodyAsString());
        }
    }

    public Result<Map<String, Object>> allocate(Map<String, Object> body) {
        try {
            Map<String, Object> response = restClient.post()
                    .uri(allocationServiceUrl + "/api/allocations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return Result.ok(response);
        } catch (RestClientResponseException e) {
            return Result.validation("ALLOCATE_FAILED", e.getResponseBodyAsString());
        }
    }

    @CacheEvict(value = {"orderSummaries", "orderDetails"}, allEntries = true)
    public Result<Map<String, Object>> markOrderDelivered(String orderId, Map<String, Object> body) {
        try {
            Map<String, Object> response = restClient.post()
                    .uri(deliveryServiceUrl + "/api/orders/" + orderId + "/delivered")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body == null ? Map.of() : body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            // Keep order-service status in sync so admin list/detail stay accurate.
            try {
                restClient.put()
                        .uri(orderServiceUrl + "/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("status", "Delivered_To_Customer"))
                        .retrieve()
                        .toBodilessEntity();
            } catch (RestClientResponseException ignored) {
                // Delivery already succeeded; status sync is best-effort.
            }
            return Result.ok(response);
        } catch (RestClientResponseException e) {
            return Result.validation("DELIVER_FAILED", e.getResponseBodyAsString());
        }
    }
}
