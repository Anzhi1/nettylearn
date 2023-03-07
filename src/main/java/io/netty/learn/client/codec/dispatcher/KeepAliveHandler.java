package io.netty.learn.client.codec.dispatcher;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.learn.business.keepalive.KeepAliveOperation;
import io.netty.learn.common.RequestMessage;
import io.netty.learn.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeepAliveHandler extends ChannelDuplexHandler {
    private static final Logger log = LoggerFactory.getLogger(KeepAliveHandler.class);
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt== IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT){
            log.info("write idle happen, send keepAlive request");
            KeepAliveOperation keepAliveOperation = new KeepAliveOperation();
            RequestMessage requestMessage= new RequestMessage(IdUtil.nextId(),keepAliveOperation);
            ctx.writeAndFlush(requestMessage);
        }
        super.userEventTriggered(ctx, evt);
    }
}
