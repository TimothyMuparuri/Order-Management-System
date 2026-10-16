package com.oms.order.service;

import com.oms.common.domain.OrderStatus;
import com.oms.common.result.Result;
import com.oms.eventing.Topics;
import com.oms.eventing.events.Events;
import com.oms.eventing.outbox.OutboxWriter;
import com.oms.order.api.dto.OrderDtos;
import com.oms.order.domain.CustomerOrder;
import com.oms.order.domain.CustomerOrderLine;
import com.oms.order.repo.CustomerOrderLineRepository;
import com.oms.order.repo.CustomerOrderRepository;
import com.oms.order.repo.CustomerRepository;
import com.oms.order.repo.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderApplicationService {

    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;
    private final CustomerOrderRepository orderRepository;
    private final CustomerOrderLineRepository lineRepository;
    private final OutboxWriter outboxWriter;

    public OrderApplicationService(
            CustomerRepository customerRepository,
            ItemRepository itemRepository,
            CustomerOrderRepository orderRepository,
            CustomerOrderLineRepository lineRepository,
            OutboxWriter outboxWriter
    ) {
        this.customerRepository = customerRepository;
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.lineRepository = lineRepository;
        this.outboxWriter = outboxWriter;
    }

    public boolean customerExists(String customerId) {
        return customerRepository.existsById(customerId);
    }

    public boolean itemExists(String itemRef) {
        return itemRepository.existsById(itemRef);
    }

    @Transactional
    public Result<OrderDtos.OrderResponse> createOrder(String customerId, List<OrderDtos.ValidatedLine> lines) {
        if (customerId == null || customerId.isBlank() || !customerRepository.existsById(customerId)) {
            return Result.validation("CUSTOMER_NOT_FOUND", "Customer does not exist");
        }
        if (lines == null || lines.isEmpty()) {
            return Result.validation("LINES_REQUIRED", "At least one order line is required");
        }
        for (OrderDtos.ValidatedLine line : lines) {
            if (line.itemRef() == null || line.itemRef().isBlank() || !itemRepository.existsById(line.itemRef())) {
                return Result.validation("ITEM_NOT_FOUND", "Item does not exist: " + line.itemRef());
            }
            if (line.requestedQty() < 1 || line.requestedQty() > 999_999) {
                return Result.validation("INVALID_QUANTITY", "Quantity must be 1..999999 for item " + line.itemRef());
            }
        }

        String orderId = UUID.randomUUID().toString();
        CustomerOrder order = new CustomerOrder(orderId, customerId, OrderStatus.Created);
        orderRepository.save(order);

        List<Events.OrderLineRef> eventLines = new ArrayList<>();
        List<OrderDtos.LineView> views = new ArrayList<>();
        for (OrderDtos.ValidatedLine line : lines) {
            String lineId = UUID.randomUUID().toString();
            CustomerOrderLine entity = new CustomerOrderLine(lineId, orderId, line.itemRef(), line.requestedQty());
            lineRepository.save(entity);
            eventLines.add(new Events.OrderLineRef(lineId, line.itemRef(), line.requestedQty()));
            views.add(new OrderDtos.LineView(lineId, line.itemRef(), line.requestedQty(), false));
        }

        outboxWriter.enqueue(orderId, Topics.ORDERS_CREATED, new Events.OrderCreated(
                orderId, customerId, eventLines, System.currentTimeMillis()));

        return Result.ok(new OrderDtos.OrderResponse(orderId, customerId, OrderStatus.Created, views));
    }

    @Transactional(readOnly = true)
    public Result<OrderDtos.OrderResponse> getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .<Result<OrderDtos.OrderResponse>>map(order -> Result.ok(toResponse(order)))
                .orElseGet(() -> Result.domain("ORDER_NOT_FOUND", "Order not found: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderDtos.OrderSummary> listOrders() {
        return orderRepository.findAll().stream()
                .map(o -> new OrderDtos.OrderSummary(o.getOrderId(), o.getOrderStatus()))
                .toList();
    }

    @Transactional
    public Result<OrderStatus> updateStatus(String orderId, String statusValue) {
        CustomerOrder order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return Result.domain("ORDER_NOT_FOUND", "Order not found: " + orderId);
        }
        OrderStatus status;
        try {
            status = OrderStatus.valueOf(statusValue);
        } catch (Exception e) {
            return Result.validation("INVALID_STATUS", "Undefined status value: " + statusValue);
        }
        order.setOrderStatus(status);
        orderRepository.save(order);
        return Result.ok(status);
    }

    private OrderDtos.OrderResponse toResponse(CustomerOrder order) {
        List<OrderDtos.LineView> lines = lineRepository.findByOrderId(order.getOrderId()).stream()
                .map(l -> new OrderDtos.LineView(l.getOrderLineId(), l.getItemRef(), l.getRequestedQty(), l.isFullyAllocated()))
                .toList();
        return new OrderDtos.OrderResponse(order.getOrderId(), order.getCustomerId(), order.getOrderStatus(), lines);
    }
}
