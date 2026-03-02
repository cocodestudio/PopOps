package com.cocode.popops.core;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Exponential backoff with jitter.
 */
public final class BackoffManager {
    private static final long BASE = 15_000L;
    private static final long MAX = 5 * 60_000L;
    private static long current = BASE;
    private BackoffManager() {
    }

    public static synchronized long nextDelay() {
        long jitter = (long) ((0.75 + ThreadLocalRandom.current().nextDouble() * 0.5) * current); // 75% - 125%
        current = Math.min(MAX, current * 2);
        return jitter;
    }

    public static synchronized void increase() {
        current = Math.min(MAX, current * 2);
    }

    public static synchronized void reset() {
        current = BASE;
    }
}
