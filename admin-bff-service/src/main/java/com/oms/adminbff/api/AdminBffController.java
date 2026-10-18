package com.oms.adminbff.api;

import com.oms.adminbff.service.AdminBffService;
import com.oms.common.result.Result;
import com.oms.common.web.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminBffController {

    private final AdminBffService service;

    public AdminBffController(AdminBffService service) {
        this.service = service;
    }

    @GetMapping("/orders")
    public Object list() {
        return service.viewOrderList();
    }

    @PostMapping("/orders")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        return toResponse(service.createOrder(body), HttpStatus.CREATED);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> detail(@PathVariable String orderId) {
        return toResponse(service.viewOrderDetail(orderId), HttpStatus.OK);
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> status(@PathVariable String orderId, @RequestBody Map<String, String> body) {
        return toResponse(service.updateOrderStatus(orderId, body.get("status")), HttpStatus.OK);
    }

    @PostMapping("/bulk-purchases")
    public ResponseEntity<?> bulk(@RequestBody Map<String, Object> body) {
        return toResponse(service.recordBulkPurchase(body), HttpStatus.CREATED);
    }

    @PostMapping("/bulk-lines/{id}/received")
    public ResponseEntity<?> received(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return toResponse(service.markReceived(id, body), HttpStatus.OK);
    }

    @PostMapping("/allocations")
    public ResponseEntity<?> allocate(@RequestBody Map<String, Object> body) {
        return toResponse(service.allocate(body), HttpStatus.CREATED);
    }

    @PostMapping("/orders/{orderId}/delivered")
    public ResponseEntity<?> delivered(@PathVariable String orderId, @RequestBody(required = false) Map<String, Object> body) {
        return toResponse(service.markOrderDelivered(orderId, body), HttpStatus.OK);
    }

    private <T> ResponseEntity<?> toResponse(Result<T> result, HttpStatus ok) {
        if (result instanceof Result.Success<T> s) {
            return ResponseEntity.status(ok).body(s.value());
        }
        var error = ((Result.Failure<T>) result).error();
        HttpStatus status = error instanceof com.oms.common.result.ValidationError
                ? HttpStatus.BAD_REQUEST : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(ErrorResponse.from(error));
    }
}
