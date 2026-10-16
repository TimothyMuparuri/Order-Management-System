package com.oms.common.time;

import java.time.Duration;
import java.time.Instant;

/**
 * Non-negative whole-second duration helper (Req 7.1, 7.2).
 * Caller must ensure {@code to >= from}; otherwise reject (Req 7.7).
 */
public final class Durations {
    private Durations() {}

    public static long wholeSeconds(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to required");
        }
        return Duration.between(from, to).getSeconds();
    }
}
