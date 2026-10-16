package com.oms.common.result;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Typed success / failure result used across OMS services.
 * Success maps to HTTP 2xx; ValidationError / DomainError map to HTTP 4xx.
 */
public sealed interface Result<T> permits Result.Success, Result.Failure {

    record Success<T>(T value) implements Result<T> {
        public Success {
            Objects.requireNonNull(value, "value");
        }
    }

    record Failure<T>(OmsError error) implements Result<T> {
        public Failure {
            Objects.requireNonNull(error, "error");
        }
    }

    static <T> Result<T> ok(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> fail(OmsError error) {
        return new Failure<>(error);
    }

    static <T> Result<T> validation(String code, String message) {
        return fail(new ValidationError(code, message));
    }

    static <T> Result<T> domain(String code, String message) {
        return fail(new DomainError(code, message));
    }

    default boolean isSuccess() {
        return this instanceof Success<T>;
    }

    default boolean isFailure() {
        return this instanceof Failure<T>;
    }

    default Optional<T> toOptional() {
        return this instanceof Success<T> s ? Optional.of(s.value()) : Optional.empty();
    }

    default Optional<OmsError> errorOptional() {
        return this instanceof Failure<T> f ? Optional.of(f.error()) : Optional.empty();
    }

    default <U> Result<U> map(Function<T, U> mapper) {
        if (this instanceof Success<T> s) {
            return ok(mapper.apply(s.value()));
        }
        @SuppressWarnings("unchecked")
        Failure<U> failure = (Failure<U>) this;
        return failure;
    }

    default T orElseThrow() {
        if (this instanceof Success<T> s) {
            return s.value();
        }
        OmsError err = ((Failure<T>) this).error();
        throw new IllegalStateException(err.code() + ": " + err.message());
    }
}
