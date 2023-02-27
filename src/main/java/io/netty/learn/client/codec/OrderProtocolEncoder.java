package io.netty.learn.client.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.learn.common.RequestMessage;
import io.netty.learn.common.ResponseMessage;

import java.util.List;


public class OrderProtocolEncoder extends MessageToMessageEncoder<RequestMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RequestMessage requestMessage, List<Object> list) throws Exception {
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        requestMessage.encode(buf);
        list.add(buf);

    }
}
