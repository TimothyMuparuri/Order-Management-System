package com.oms.delivery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oms.common.domain.OrderStatus;
import com.oms.common.result.Result;
import com.oms.common.time.Durations;
import com.oms.delivery.domain.OrderLifecycle;
import com.oms.delivery.domain.StatusTransitionRecord;
import com.oms.delivery.repo.OrderLifecycleRepository;
import com.oms.delivery.repo.StatusTransitionRecordRepository;
import com.oms.eventing.Topics;
import com.oms.eventing.events.Events;
import com.oms.eventing.idempotency.IdempotentConsumer;
import com.oms.eventing.outbox.OutboxWriter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class DeliveryApplicationService {

    private final OrderLifecycleRepository lifecycleRepository;
    private final StatusTransitionRecordRepository transitionRepository;
    private final OutboxWriter outboxWriter;
    private final IdempotentConsumer idempotentConsumer;
    private final ObjectMapper objectMapper;

    public DeliveryApplicationService(
            OrderLifecycleRepository lifecycleRepository,
            StatusTransitionRecordRepository transitionRepository,
            OutboxWriter outboxWriter,
            IdempotentConsumer idempotentConsumer,
            ObjectMapper objectMapper
    ) {
        this.lifecycleRepository = lifecycleRepository;
        this.transitionRepository = transitionRepository;
        this.outboxWriter = outboxWriter;
        this.idempotentConsumer = idempotentConsumer;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = Topics.ORDERS_CREATED, groupId = "delivery-service")
    @Transactional
    public void onOrderCreated(String payload) throws Exception {
        Events.OrderCreated event = objectMapper.readValue(payload, Events.OrderCreated.class);
        String key = Topics.ORDERS_CREATED + ":" + event.orderId();
        idempotentConsumer.processOnce(key, () -> {
            if (lifecycleRepository.existsById(event.orderId())) {
                return;
            }
            OrderLifecycle lifecycle = new OrderLifecycle(event.orderId(), event.customerId(), OrderStatus.Created);
            lifecycle.setPlacementDate(LocalDate.now(ZoneOffset.UTC));
            lifecycleRepository.save(lifecycle);
            transitionRepository.save(new StatusTransitionRecord(
                    event.orderId(), null, OrderStatus.Created, Instant.now()));
        });
    }

    @KafkaListener(topics = Topics.ORDERS_FULLY_ALLOCATED, groupId = "delivery-service")
    @Transactional
    public void onFullyAllocated(String payload) throws Exception {
        Events.OrderFullyAllocated event = objectMapper.readValue(payload, Events.OrderFullyAllocated.class);
        String key = Topics.ORDERS_FULLY_ALLOCATED + ":" + event.orderId() + ":" + event.allocatedAtEpochMillis();
        idempotentConsumer.processOnce(key, () -> transition(event.orderId(), OrderStatus.Created, OrderStatus.Allocated));
    }

    @Transactional
    public Result<OrderStatus> transition(String orderId, OrderStatus expectedCurrent, OrderStatus next) {
        OrderLifecycle lifecycle = lifecycleRepository.findById(orderId).orElse(null);
        if (lifecycle == null) {
            return Result.domain("ORDER_NOT_FOUND", "Order not found");
        }
        if (lifecycle.getStatus() != expectedCurrent) {
            return Result.validation("INVALID_TRANSITION",
                    "Cannot transition from " + lifecycle.getStatus() + " to " + next);
        }
        OrderStatus prior = lifecycle.getStatus();
        lifecycle.setStatus(next);
        lifecycleRepository.save(lifecycle);
        transitionRepository.save(new StatusTransitionRecord(orderId, prior, next, Instant.now()));
        return Result.ok(next);
    }

    @Transactional
    public Result<DeliveryMarkResponse> markOrderDelivered(String orderId, Instant deliveredAt) {
        OrderLifecycle lifecycle = lifecycleRepository.findById(orderId).orElse(null);
        if (lifecycle == null) {
            return Result.domain("ORDER_NOT_FOUND", "Order not found");
        }
        if (lifecycle.getStatus() != OrderStatus.Allocated) {
            return Result.validation("NOT_ALLOCATED", "Order must be Allocated before delivery");
        }
        if (lifecycle.getDeliveredToCustomerAt() != null) {
            return Result.validation("ALREADY_DELIVERED", "Order already marked delivered");
        }
        if (lifecycle.getPlacementDate() == null) {
            return Result.validation("MISSING_PLACEMENT_DATE", "Placement date is required");
        }
        Instant placement = lifecycle.getPlacementDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        if (deliveredAt.isBefore(placement)) {
            return Result.validation("INVALID_TIMESTAMP", "Delivered timestamp precedes placement date");
        }
        long seconds = Durations.wholeSeconds(placement, deliveredAt);
        lifecycle.setDeliveredToCustomerAt(deliveredAt);
        lifecycle.setAdminToCustomerDurationSeconds(seconds);
        OrderStatus prior = lifecycle.getStatus();
        lifecycle.setStatus(OrderStatus.Delivered_To_Customer);
        lifecycleRepository.save(lifecycle);
        transitionRepository.save(new StatusTransitionRecord(
                orderId, prior, OrderStatus.Delivered_To_Customer, deliveredAt));

        outboxWriter.enqueue(orderId, Topics.ORDERS_DELIVERED, new Events.OrderDeliveredToCustomer(
                orderId, lifecycle.getCustomerId(), deliveredAt.toEpochMilli(), seconds));
        outboxWriter.enqueue(orderId, Topics.NOTIFICATIONS_REQUESTED, new Events.NotificationRequested(
                orderId, lifecycle.getCustomerId(), deliveredAt.toEpochMilli()));

        return Result.ok(new DeliveryMarkResponse(orderId, seconds, OrderStatus.Delivered_To_Customer));
    }

    @Transactional(readOnly = true)
    public List<StatusTransitionRecord> history(String orderId) {
        return transitionRepository.findByOrderIdOrderByTimestampAsc(orderId);
    }

    public record DeliveryMarkResponse(String orderId, long durationSeconds, OrderStatus status) {}
}
