package com.oms.common.result;

public sealed interface OmsError permits ValidationError, DomainError {
    String code();
    String message();
}
