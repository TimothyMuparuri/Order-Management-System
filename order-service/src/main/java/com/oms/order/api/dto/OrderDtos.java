package com.oms.order.api.dto;

import com.oms.common.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() {}

    public record ValidatedLine(
            String itemRef,
            int requestedQty,
            String productName,
            String storeName,
            String productUrl,
            String imageUrl,
            String productSize,
            BigDecimal unitPrice
    ) {
        public ValidatedLine(String itemRef, int requestedQty) {
            this(itemRef, requestedQty, null, null, null, null, null, null);
        }
    }

    public record CreateOrderRequest(
            String customerId,
            String customerName,
            String email,
            String phone,
            String address,
            String source,
            List<ValidatedLine> lines
    ) {
        public CreateOrderRequest(String customerId, String customerName, List<ValidatedLine> lines) {
            this(customerId, customerName, null, null, null, "Web", lines);
        }
    }

    public record LineView(
            String orderLineId,
            String itemRef,
            int requestedQty,
            boolean fullyAllocated,
            String productName,
            String storeName,
            String productUrl,
            String imageUrl,
            String productSize,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}

    public record OrderResponse(
            String orderId,
            String customerId,
            String customerName,
            String email,
            String phone,
            String address,
            String source,
            OrderStatus orderStatus,
            BigDecimal orderTotal,
            List<LineView> lines
    ) {}

    public record OrderSummary(
            String orderId,
            String customerId,
            String customerName,
            String phone,
            String source,
            OrderStatus orderStatus,
            BigDecimal orderTotal
    ) {}

    public record UpdateStatusRequest(String status) {}
}
