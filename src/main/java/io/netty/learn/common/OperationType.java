package io.netty.learn.common;

import io.netty.learn.business.auth.AuthOperation;
import io.netty.learn.business.auth.AuthOperationResult;
import io.netty.learn.business.keepalive.KeepAliveOperation;
import io.netty.learn.business.keepalive.KeepAliveOperationResult;
import io.netty.learn.business.order.OrderOperation;
import io.netty.learn.business.order.OrderOperationResult;

import java.util.function.Predicate;

public enum OperationType {
    AUTH(1, AuthOperation.class, AuthOperationResult.class),
    KEEP_ALIVE(2,KeepAliveOperation.class, KeepAliveOperationResult.class),
    ORDER(3, OrderOperation.class, OrderOperationResult.class);

    private int opCode;
    private Class<? extends Operation> operationClass;
    private Class<? extends OperationResult> operationResultClass;

    OperationType(int opCode, Class<? extends Operation> operationClass, Class<? extends OperationResult> operationResultClass) {
        this.opCode = opCode;
        this.operationClass = operationClass;
        this.operationResultClass = operationResultClass;
    }

    public int getOpCode() {
        return opCode;
    }

    public void setOpCode(int opCode) {
        this.opCode = opCode;
    }

    public Class<? extends Operation> getOperationClass() {
        return operationClass;
    }

    public void setOperationClass(Class<? extends Operation> operationClass) {
        this.operationClass = operationClass;
    }

    public Class<? extends OperationResult> getOperationResultClass() {
        return operationResultClass;
    }

    public void setOperationResultClass(Class<? extends OperationResult> operationResultClass) {
        this.operationResultClass = operationResultClass;
    }

    public static OperationType fromOpcode(int type){
        return getOperationType(requestType->requestType.opCode==type);
    }
    public static OperationType fromOperation(Operation operation){
        return getOperationType(requestType->requestType.operationClass==operation.getClass());
    }
    private static OperationType getOperationType(Predicate<OperationType> predicate){
        OperationType[] values = values();
        for(OperationType operationType:values){
            if(predicate.test(operationType)){
                return operationType;
            }
        }
        throw new AssertionError("no found type");
    }
}
