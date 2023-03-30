package io.netty.learn.util;

import java.util.concurrent.atomic.AtomicLong;

public final class IdUtil {
    private static final AtomicLong IDX = new AtomicLong();

    public static long nextId() {
        return IDX.incrementAndGet();
    }
}
