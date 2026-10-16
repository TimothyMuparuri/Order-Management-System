package com.oms.common.result;

public record ValidationError(String code, String message) implements OmsError {
    public ValidationError {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code required");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message required");
        }
    }
}
