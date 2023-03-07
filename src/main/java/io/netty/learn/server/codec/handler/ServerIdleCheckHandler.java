package io.netty.learn.server.codec.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ServerIdleCheckHandler extends IdleStateHandler {
    private final Logger log = LoggerFactory.getLogger(ServerIdleCheckHandler.class);
    public ServerIdleCheckHandler() {
        super(10, 0, 0, TimeUnit.SECONDS);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if(evt==IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT){
            log.info("空闲检测生效，关闭链接");
            ctx.close();
            return ;
        }
        super.channelIdle(ctx, evt);
    }
}
