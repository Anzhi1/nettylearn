package io.netty.learn.client.codec.dispatcher;

import io.netty.learn.common.OperationResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestPendingCenter {

    private  Map<Long,OperationResultFuture> map = new ConcurrentHashMap<>();

    public void add(Long streamId,OperationResultFuture future){
        map.put(streamId,future);
    }

    public void set(Long streamId, OperationResult result){
        OperationResultFuture operationResultFuture = this.map.get(streamId);
        if(operationResultFuture!=null){
            operationResultFuture.setSuccess(result);
            this.map.remove(streamId);
        }
    }
}
