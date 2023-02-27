package io.netty.learn.client.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.learn.business.order.OrderOperation;
import io.netty.learn.common.Operation;
import io.netty.learn.common.RequestMessage;
import io.netty.learn.util.IdUtil;

import java.util.List;

public class OperationToRequestMessageEncoder extends MessageToMessageEncoder<Operation> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Operation operation, List<Object> list) throws Exception {
        RequestMessage requestMessage = new RequestMessage(IdUtil.nextId(),operation);
        list.add(requestMessage);

    }
}
