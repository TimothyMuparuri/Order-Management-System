package com.oms.order.service;

import com.oms.common.result.Result;
import com.oms.eventing.outbox.OutboxWriter;
import com.oms.order.api.dto.OrderDtos;
import com.oms.order.domain.ContactInfo;
import com.oms.order.domain.Customer;
import com.oms.order.domain.Item;
import com.oms.order.domain.ItemSpec;
import com.oms.order.repo.CustomerOrderLineRepository;
import com.oms.order.repo.CustomerOrderRepository;
import com.oms.order.repo.CustomerRepository;
import com.oms.order.repo.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("order-management-system")
class OrderApplicationServiceTest {

    @Mock CustomerRepository customerRepository;
    @Mock ItemRepository itemRepository;
    @Mock CustomerOrderRepository orderRepository;
    @Mock CustomerOrderLineRepository lineRepository;
    @Mock OutboxWriter outboxWriter;

    OrderApplicationService service;

    @BeforeEach
    void setUp() {
        service = new OrderApplicationService(
                customerRepository, itemRepository, orderRepository, lineRepository, outboxWriter);
    }

    @Test
    void emptyOrderListReturnsEmptyWithoutError() {
        when(orderRepository.findAll()).thenReturn(List.of());
        assertThat(service.listOrders()).isEmpty();
    }

    @Test
    void nonexistentOrderReturnsNotFound() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());
        Result<OrderDtos.OrderResponse> result = service.getOrder("missing");
        assertThat(result.isFailure()).isTrue();
        assertThat(result.errorOptional().orElseThrow().code()).isEqualTo("ORDER_NOT_FOUND");
    }

    @Test
    void createOrderPersistsWhenValid() {
        when(customerRepository.existsById("c1")).thenReturn(true);
        when(itemRepository.existsById("sku-1")).thenReturn(true);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Result<OrderDtos.OrderResponse> result = service.createOrder(
                "c1", List.of(new OrderDtos.ValidatedLine("sku-1", 2)));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow().customerId()).isEqualTo("c1");
    }
}
