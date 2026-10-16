package com.oms.allocation.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oms.allocation.domain.OrderLineAllocationState;
import com.oms.allocation.repo.OrderLineAllocationStateRepository;
import com.oms.eventing.Topics;
import com.oms.eventing.events.Events;
import com.oms.eventing.idempotency.IdempotentConsumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registers order-line allocation state when an order is created,
 * so allocate() can enforce per-line ceilings without a prior bootstrap call.
 */
@Component
public class OrderCreatedListener {

    private final OrderLineAllocationStateRepository stateRepository;
    private final IdempotentConsumer idempotentConsumer;
    private final ObjectMapper objectMapper;

    public OrderCreatedListener(
            OrderLineAllocationStateRepository stateRepository,
            IdempotentConsumer idempotentConsumer,
            ObjectMapper objectMapper
    ) {
        this.stateRepository = stateRepository;
        this.idempotentConsumer = idempotentConsumer;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = Topics.ORDERS_CREATED, groupId = "allocation-service")
    @Transactional
    public void onOrderCreated(String payload) throws Exception {
        Events.OrderCreated event = objectMapper.readValue(payload, Events.OrderCreated.class);
        String key = Topics.ORDERS_CREATED + ":alloc:" + event.orderId();
        idempotentConsumer.processOnce(key, () -> {
            if (event.lines() == null) {
                return;
            }
            for (Events.OrderLineRef line : event.lines()) {
                if (!stateRepository.existsById(line.orderLineId())) {
                    stateRepository.save(new OrderLineAllocationState(
                            line.orderLineId(), event.orderId(), line.requestedQty()));
                }
            }
        });
    }
}
