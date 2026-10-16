package com.oms.common.web;

import com.oms.common.result.OmsError;

public record ErrorResponse(String code, String message, String type) {
    public static ErrorResponse from(OmsError error) {
        String type = error instanceof com.oms.common.result.ValidationError ? "ValidationError" : "DomainError";
        return new ErrorResponse(error.code(), error.message(), type);
    }
}
