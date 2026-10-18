package com.oms.intake.api;

import com.oms.common.result.Result;
import com.oms.common.web.ErrorResponse;
import com.oms.intake.service.IntakeApplicationService;
import com.oms.intake.service.IntakeApplicationService.ImageUploadResponse;
import com.oms.intake.service.IntakeApplicationService.SubmitOrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.oms.intake.service.IntakeApplicationService.ImagePayload;

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

    @GetMapping("/images/{imageId}")
    public ResponseEntity<?> getImage(@PathVariable String imageId) {
        Result<ImagePayload> result = service.loadImage(imageId);
        if (result instanceof Result.Success<ImagePayload> s) {
            ImagePayload image = s.value();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=604800")
                    .contentType(MediaType.parseMediaType(image.contentType()))
                    .body(image.bytes());
        }
        var error = ((Result.Failure<ImagePayload>) result).error();
        HttpStatus status = "IMAGE_NOT_FOUND".equals(error.code()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(ErrorResponse.from(error));
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
