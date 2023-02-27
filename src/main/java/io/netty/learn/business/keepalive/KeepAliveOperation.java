package io.netty.learn.business.keepalive;

import io.netty.learn.common.Operation;
import io.netty.learn.common.OperationResult;

public class KeepAliveOperation extends Operation {
    private long time;


    @Override
    public OperationResult execute() {
        return new KeepAliveOperationResult(time);
    }
}
