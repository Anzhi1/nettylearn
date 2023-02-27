package io.netty.learn.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.learn.common.ResponseMessage;

import java.util.List;


public class OrderProtocolEncoder extends MessageToMessageEncoder<ResponseMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ResponseMessage responseMessage, List<Object> list) throws Exception {
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        responseMessage.encode(buf);
        list.add(buf);

    }
}
