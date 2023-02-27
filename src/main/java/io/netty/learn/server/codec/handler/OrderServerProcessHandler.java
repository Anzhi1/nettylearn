package io.netty.learn.server.codec.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.learn.common.Operation;
import io.netty.learn.common.OperationResult;
import io.netty.learn.common.RequestMessage;
import io.netty.learn.common.ResponseMessage;


//为什么继承SimpleChannelInboundHandler而不是继承ChannelInboundHandler？  Simple帮我们自动释放Bytebuf
public class OrderServerProcessHandler extends SimpleChannelInboundHandler<RequestMessage> {
    @Override
    public void channelRead0(ChannelHandlerContext ctx, RequestMessage requestMessage) throws Exception {
        Operation operation = requestMessage.getMessageBody();
        OperationResult operationResult = operation.execute();

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessageHeader(requestMessage.getMessageHeader());
        responseMessage.setMessageBody(operationResult);

        ctx.writeAndFlush(responseMessage);
    }
}
