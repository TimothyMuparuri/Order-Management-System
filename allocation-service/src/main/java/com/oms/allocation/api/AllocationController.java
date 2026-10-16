package com.oms.allocation.api;

import com.oms.allocation.service.AllocationApplicationService;
import com.oms.allocation.service.AllocationApplicationService.AllocateRequest;
import com.oms.allocation.service.AllocationApplicationService.BulkPurchaseRequest;
import com.oms.common.result.Result;
import com.oms.common.web.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AllocationController {

    private final AllocationApplicationService service;

    public AllocationController(AllocationApplicationService service) {
        this.service = service;
    }

    @PostMapping("/bulk-purchases")
    public ResponseEntity<?> recordBulk(@RequestBody BulkPurchaseRequest request) {
        return toResponse(service.recordBulkPurchase(request), HttpStatus.CREATED);
    }

    @PostMapping("/bulk-lines/{bulkLineId}/received")
    public ResponseEntity<?> markReceived(@PathVariable String bulkLineId, @RequestBody Map<String, Integer> body) {
        return toResponse(service.markReceived(bulkLineId, body.getOrDefault("receivedQty", -1)), HttpStatus.OK);
    }

    @PostMapping("/allocations")
    public ResponseEntity<?> allocate(@RequestBody AllocateRequest request) {
        return toResponse(service.allocate(request), HttpStatus.CREATED);
    }

    private <T> ResponseEntity<?> toResponse(Result<T> result, HttpStatus ok) {
        if (result instanceof Result.Success<T> s) {
            Object body = s.value() == null ? Map.of("ok", true) : s.value();
            return ResponseEntity.status(ok).body(body);
        }
        var error = ((Result.Failure<T>) result).error();
        HttpStatus status = error instanceof com.oms.common.result.ValidationError
                ? HttpStatus.BAD_REQUEST : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(ErrorResponse.from(error));
    }
}
