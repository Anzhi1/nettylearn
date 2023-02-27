package io.netty.learn.business.order;

import io.netty.learn.common.OperationResult;

public class OrderOperationResult extends OperationResult {

    private final int tableId;

    private final String dish;

    private final boolean complete;

    public OrderOperationResult(int tableId, String dish, boolean complete) {
        this.tableId = tableId;
        this.dish = dish;
        this.complete = complete;
    }

    @Override
    public String toString() {
        return "OrderOperationResult{" +
                "tableId=" + tableId +
                ", dish='" + dish + '\'' +
                ", complete=" + complete +
                '}';
    }
}
