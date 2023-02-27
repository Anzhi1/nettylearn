package io.netty.learn.common;

public class ResponseMessage extends Message<OperationResult>{
    public Class getMessageBodyDecodeClass(int opcode){
        return OperationType.fromOpcode(opcode).getOperationResultClass();
    }

}
