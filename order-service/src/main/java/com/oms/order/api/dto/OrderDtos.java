package com.oms.order.api.dto;

import com.oms.common.domain.OrderStatus;

import java.util.List;

public final class OrderDtos {
    private OrderDtos() {}

    public record ValidatedLine(String itemRef, int requestedQty) {}

    public record CreateOrderRequest(String customerId, List<ValidatedLine> lines) {}

    public record LineView(String orderLineId, String itemRef, int requestedQty, boolean fullyAllocated) {}

    public record OrderResponse(
            String orderId,
            String customerId,
            OrderStatus orderStatus,
            List<LineView> lines
    ) {}

    public record OrderSummary(String orderId, OrderStatus orderStatus) {}

    public record UpdateStatusRequest(String status) {}
}
