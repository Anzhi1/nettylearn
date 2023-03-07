package io.netty.learn.business.order;

import com.google.common.util.concurrent.Uninterruptibles;
import io.netty.learn.common.Operation;
import io.netty.learn.common.OperationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class OrderOperation extends Operation {

    private int tableId;
    private String dish;

    private static final Logger log = LoggerFactory.getLogger(OrderOperation.class);

    public OrderOperation(int tableId, String dish) {
        this.tableId = tableId;
        this.dish = dish;
    }

    @Override
    public OperationResult execute() {
        log.info("order's executing startup with orderRequest: ");
        //execute order logic
        //停3s,用于模拟业务处理耗时
        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
        log.info("order's executing complete");

        return new OrderOperationResult(tableId,dish,true);
    }

    @Override
    public String toString() {
        return "OrderOperation{" +
                "tableId=" + tableId +
                ", dish='" + dish + '\'' +
                '}';
    }
}
