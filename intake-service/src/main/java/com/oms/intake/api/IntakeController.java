package com.oms.intake.api;

import com.oms.common.result.Result;
import com.oms.common.web.ErrorResponse;
import com.oms.intake.service.IntakeApplicationService;
import com.oms.intake.service.IntakeApplicationService.ImageUploadResponse;
import com.oms.intake.service.IntakeApplicationService.SubmitOrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class IntakeController {

    private final IntakeApplicationService service;

    public IntakeController(IntakeApplicationService service) {
        this.service = service;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestPart("file") MultipartFile file) {
        return toResponse(service.uploadImage(file), HttpStatus.CREATED);
    }

    @PostMapping("/orders")
    public ResponseEntity<?> submitOrder(@RequestBody SubmitOrderRequest request) {
        return toResponse(service.submitOrder(request), HttpStatus.CREATED);
    }

    private <T> ResponseEntity<?> toResponse(Result<T> result, HttpStatus ok) {
        if (result instanceof Result.Success<T> s) {
            return ResponseEntity.status(ok).body(s.value());
        }
        var error = ((Result.Failure<T>) result).error();
        HttpStatus status = error instanceof com.oms.common.result.ValidationError
                ? HttpStatus.BAD_REQUEST : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status).body(ErrorResponse.from(error));
    }
}
