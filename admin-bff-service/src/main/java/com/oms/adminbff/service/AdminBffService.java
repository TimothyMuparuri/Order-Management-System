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

import java.util.List;
import java.util.Map;

@Service
public class AdminBffService {

    private final RestClient restClient;
    private final String orderServiceUrl;
    private final String allocationServiceUrl;
    private final String deliveryServiceUrl;

    public AdminBffService(
            RestClient.Builder builder,
            @Value("${oms.order-service.url:http://localhost:8081}") String orderServiceUrl,
            @Value("${oms.allocation-service.url:http://localhost:8084}") String allocationServiceUrl,
            @Value("${oms.delivery-service.url:http://localhost:8085}") String deliveryServiceUrl
    ) {
        this.restClient = builder.build();
        this.orderServiceUrl = orderServiceUrl;
        this.allocationServiceUrl = allocationServiceUrl;
        this.deliveryServiceUrl = deliveryServiceUrl;
    }

    @Cacheable("orderSummaries")
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

    public Result<Map<String, Object>> markOrderDelivered(String orderId, Map<String, Object> body) {
        try {
            Map<String, Object> response = restClient.post()
                    .uri(deliveryServiceUrl + "/api/orders/" + orderId + "/delivered")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body == null ? Map.of() : body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return Result.ok(response);
        } catch (RestClientResponseException e) {
            return Result.validation("DELIVER_FAILED", e.getResponseBodyAsString());
        }
    }
}
