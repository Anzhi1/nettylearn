package io.netty.learn.business.keepalive;

import io.netty.learn.common.OperationResult;

public class KeepAliveOperationResult extends OperationResult {
    private final long time;


    public KeepAliveOperationResult(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "KeepAliveOperationResult{" +
                "time=" + time +
                '}';
    }
}
