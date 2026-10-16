package com.oms.delivery.api;

import com.oms.common.result.Result;
import com.oms.common.web.ErrorResponse;
import com.oms.delivery.service.DeliveryApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DeliveryController {

    private final DeliveryApplicationService service;

    public DeliveryController(DeliveryApplicationService service) {
        this.service = service;
    }

    @PostMapping("/orders/{orderId}/delivered")
    public ResponseEntity<?> markDelivered(@PathVariable String orderId, @RequestBody Map<String, String> body) {
        Instant at = body.containsKey("deliveredAt") ? Instant.parse(body.get("deliveredAt")) : Instant.now();
        return toResponse(service.markOrderDelivered(orderId, at), HttpStatus.OK);
    }

    @GetMapping("/orders/{orderId}/transitions")
    public Object history(@PathVariable String orderId) {
        return service.history(orderId);
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
