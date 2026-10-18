package com.oms.order.service;

import com.oms.common.domain.OrderStatus;
import com.oms.common.result.Result;
import com.oms.eventing.Topics;
import com.oms.eventing.events.Events;
import com.oms.eventing.publish.DomainEventPublisher;
import com.oms.order.api.dto.OrderDtos;
import com.oms.order.domain.ContactInfo;
import com.oms.order.domain.Customer;
import com.oms.order.domain.CustomerOrder;
import com.oms.order.domain.CustomerOrderLine;
import com.oms.order.domain.Item;
import com.oms.order.domain.ItemSpec;
import com.oms.order.repo.CustomerOrderLineRepository;
import com.oms.order.repo.CustomerOrderRepository;
import com.oms.order.repo.CustomerRepository;
import com.oms.order.repo.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderApplicationService {

    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;
    private final CustomerOrderRepository orderRepository;
    private final CustomerOrderLineRepository lineRepository;
    private final DomainEventPublisher events;

    public OrderApplicationService(
            CustomerRepository customerRepository,
            ItemRepository itemRepository,
            CustomerOrderRepository orderRepository,
            CustomerOrderLineRepository lineRepository,
            DomainEventPublisher events
    ) {
        this.customerRepository = customerRepository;
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.lineRepository = lineRepository;
        this.events = events;
    }

    public boolean customerExists(String customerId) {
        return customerRepository.existsById(customerId);
    }

    public boolean itemExists(String itemRef) {
        return itemRepository.existsById(itemRef);
    }

    @Transactional
    public Result<OrderDtos.OrderResponse> createOrder(
            String customerId,
            String customerName,
            List<OrderDtos.ValidatedLine> lines
    ) {
        return createOrder(customerId, customerName, null, null, null, "Web", lines);
    }

    @Transactional
    public Result<OrderDtos.OrderResponse> createOrder(
            String customerId,
            String customerName,
            String email,
            String phone,
            String address,
            List<OrderDtos.ValidatedLine> lines
    ) {
        return createOrder(customerId, customerName, email, phone, address, "Web", lines);
    }

    @Transactional
    public Result<OrderDtos.OrderResponse> createOrder(
            String customerId,
            String customerName,
            String email,
            String phone,
            String address,
            String source,
            List<OrderDtos.ValidatedLine> lines
    ) {
        if (customerId == null || customerId.isBlank()) {
            return Result.validation("CUSTOMER_REQUIRED", "Customer id is required");
        }
        if (customerName == null || customerName.isBlank()) {
            return Result.validation("CUSTOMER_NAME_REQUIRED", "Customer name is required");
        }
        if (phone == null || phone.isBlank()) {
            return Result.validation("PHONE_REQUIRED", "Phone number is required");
        }
        if (lines == null || lines.isEmpty()) {
            return Result.validation("LINES_REQUIRED", "At least one order line is required");
        }

        ContactInfo contact = new ContactInfo(
                blankToNull(email),
                phone.trim(),
                blankToNull(address)
        );
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            customer = new Customer(customerId, customerName.trim(), contact);
            customerRepository.save(customer);
        } else {
            customer.setName(customerName.trim());
            customer.setContact(contact);
            customerRepository.save(customer);
        }

        for (OrderDtos.ValidatedLine line : lines) {
            if (line.itemRef() == null || line.itemRef().isBlank()) {
                return Result.validation("ITEM_REQUIRED", "Item reference is required");
            }
            if (line.requestedQty() < 1 || line.requestedQty() > 999_999) {
                return Result.validation("INVALID_QUANTITY", "Quantity must be 1..999999 for item " + line.itemRef());
            }
            ensureItem(line);
        }

        String orderId = UUID.randomUUID().toString();
        CustomerOrder order = new CustomerOrder(orderId, customerId, OrderStatus.Created, source);
        orderRepository.save(order);

        List<Events.OrderLineRef> eventLines = new ArrayList<>();
        List<OrderDtos.LineView> views = new ArrayList<>();
        BigDecimal orderTotal = BigDecimal.ZERO;
        for (OrderDtos.ValidatedLine line : lines) {
            String lineId = UUID.randomUUID().toString();
            CustomerOrderLine entity = new CustomerOrderLine(
                    lineId,
                    orderId,
                    line.itemRef(),
                    line.requestedQty(),
                    blankToNull(line.productName()),
                    blankToNull(line.storeName()),
                    blankToNull(line.productUrl()),
                    blankToNull(line.imageUrl()),
                    blankToNull(line.productSize()),
                    line.unitPrice()
            );
            lineRepository.save(entity);
            eventLines.add(new Events.OrderLineRef(lineId, line.itemRef(), line.requestedQty()));
            OrderDtos.LineView view = toLineView(entity);
            views.add(view);
            orderTotal = orderTotal.add(view.lineTotal() != null ? view.lineTotal() : BigDecimal.ZERO);
        }

        events.publish(orderId, Topics.ORDERS_CREATED, new Events.OrderCreated(
                orderId, customerId, eventLines, System.currentTimeMillis()));

        return Result.ok(toOrderResponse(
                orderId, customerId, customer, order.getOrderSource(), OrderStatus.Created, orderTotal, views));
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
                .map(o -> {
                    Customer customer = customerRepository.findById(o.getCustomerId()).orElse(null);
                    String name = customer != null ? customer.getName() : null;
                    String phone = customer != null && customer.getContact() != null
                            ? customer.getContact().getPhone() : null;
                    BigDecimal total = lineRepository.findByOrderId(o.getOrderId()).stream()
                            .map(CustomerOrderLine::lineTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new OrderDtos.OrderSummary(
                            o.getOrderId(),
                            o.getCustomerId(),
                            name,
                            phone,
                            o.getOrderSource(),
                            o.getOrderStatus(),
                            total
                    );
                })
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

    private void ensureItem(OrderDtos.ValidatedLine line) {
        if (itemRepository.existsById(line.itemRef())) {
            return;
        }
        ItemSpec spec;
        if (line.itemRef().startsWith("image:")) {
            spec = ItemSpec.image(line.itemRef().substring("image:".length()));
        } else if (line.itemRef().startsWith("term:")) {
            spec = ItemSpec.searchTerm(line.itemRef().substring("term:".length()));
        } else if (line.productName() != null && !line.productName().isBlank()) {
            spec = ItemSpec.searchTerm(line.productName());
        } else {
            spec = ItemSpec.searchTerm(line.itemRef());
        }
        itemRepository.save(new Item(line.itemRef(), spec));
    }

    private OrderDtos.OrderResponse toResponse(CustomerOrder order) {
        Customer customer = customerRepository.findById(order.getCustomerId()).orElse(null);
        List<OrderDtos.LineView> lines = lineRepository.findByOrderId(order.getOrderId()).stream()
                .map(this::toLineView)
                .toList();
        BigDecimal orderTotal = lines.stream()
                .map(l -> l.lineTotal() != null ? l.lineTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return toOrderResponse(
                order.getOrderId(),
                order.getCustomerId(),
                customer,
                order.getOrderSource(),
                order.getOrderStatus(),
                orderTotal,
                lines
        );
    }

    private OrderDtos.OrderResponse toOrderResponse(
            String orderId,
            String customerId,
            Customer customer,
            String source,
            OrderStatus status,
            BigDecimal orderTotal,
            List<OrderDtos.LineView> lines
    ) {
        String name = customer != null ? customer.getName() : null;
        ContactInfo contact = customer != null ? customer.getContact() : null;
        return new OrderDtos.OrderResponse(
                orderId,
                customerId,
                name,
                contact != null ? contact.getEmail() : null,
                contact != null ? contact.getPhone() : null,
                contact != null ? contact.getAddress() : null,
                source == null || source.isBlank() ? "Web" : source,
                status,
                orderTotal,
                lines
        );
    }

    private OrderDtos.LineView toLineView(CustomerOrderLine line) {
        return new OrderDtos.LineView(
                line.getOrderLineId(),
                line.getItemRef(),
                line.getRequestedQty(),
                line.isFullyAllocated(),
                line.getProductName(),
                line.getStoreName(),
                line.getProductUrl(),
                line.getImageUrl(),
                line.getProductSize(),
                line.getUnitPrice(),
                line.lineTotal()
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
