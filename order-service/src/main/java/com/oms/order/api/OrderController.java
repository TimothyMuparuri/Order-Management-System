package com.oms.order.api;

import com.oms.common.domain.OrderStatus;
import com.oms.common.result.Result;
import com.oms.common.web.ErrorResponse;
import com.oms.order.api.dto.OrderDtos;
import com.oms.order.service.OrderApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderApplicationService service;

    public OrderController(OrderApplicationService service) {
        this.service = service;
    }

    @PostMapping("/internal/orders")
    public ResponseEntity<?> createOrder(@RequestBody OrderDtos.CreateOrderRequest request) {
        return toResponse(
                service.createOrder(
                        request.customerId(),
                        request.customerName(),
                        request.email(),
                        request.phone(),
                        request.address(),
                        request.source(),
                        request.lines()
                ),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/orders")
    public List<OrderDtos.OrderSummary> listOrders() {
        return service.listOrders();
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        return toResponse(service.getOrder(orderId), HttpStatus.OK);
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String orderId, @RequestBody OrderDtos.UpdateStatusRequest request) {
        return toResponse(service.updateStatus(orderId, request.status()), HttpStatus.OK);
    }

    @GetMapping("/internal/customers/{customerId}/exists")
    public Map<String, Boolean> customerExists(@PathVariable String customerId) {
        return Map.of("exists", service.customerExists(customerId));
    }

    @GetMapping("/internal/items/{itemRef}/exists")
    public Map<String, Boolean> itemExists(@PathVariable String itemRef) {
        return Map.of("exists", service.itemExists(itemRef));
    }

    private <T> ResponseEntity<?> toResponse(Result<T> result, HttpStatus successStatus) {
        if (result instanceof Result.Success<T> s) {
            return ResponseEntity.status(successStatus).body(s.value());
        }
        var error = ((Result.Failure<T>) result).error();
        HttpStatus status = error instanceof com.oms.common.result.ValidationError
                ? HttpStatus.BAD_REQUEST
                : HttpStatus.NOT_FOUND;
        if ("ORDER_NOT_FOUND".equals(error.code())) {
            status = HttpStatus.NOT_FOUND;
        }
        return ResponseEntity.status(status).body(ErrorResponse.from(error));
    }
}
