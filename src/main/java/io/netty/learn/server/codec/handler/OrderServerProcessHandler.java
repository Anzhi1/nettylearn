package io.netty.learn.server.codec.handler;

import com.google.common.util.concurrent.Uninterruptibles;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.learn.common.Operation;
import io.netty.learn.common.OperationResult;
import io.netty.learn.common.RequestMessage;
import io.netty.learn.common.ResponseMessage;

import java.util.concurrent.TimeUnit;


//为什么继承SimpleChannelInboundHandler而不是继承ChannelInboundHandler？  Simple帮我们自动释放Bytebuf
public class OrderServerProcessHandler extends SimpleChannelInboundHandler<RequestMessage> {
    @Override
    public void channelRead0(ChannelHandlerContext ctx, RequestMessage requestMessage){

     //   ByteBuf buffer = ctx.alloc().buffer();  用于测试内存泄漏检测，但失败。
        Operation operation = requestMessage.getMessageBody();
        OperationResult operationResult = operation.execute();
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessageHeader(requestMessage.getMessageHeader());
        responseMessage.setMessageBody(operationResult);
        //停3s,用于模拟业务处理耗时
        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);

        //加急式，每次写都flush
        ctx.writeAndFlush(responseMessage);
    }
}
