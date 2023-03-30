package io.netty.learn.server.codec.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    private int counter;

    private static final Logger log = LoggerFactory.getLogger(TimeServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
/*        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] req = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(req);
        String body = new String(req, StandardCharsets.UTF_8).substring(0, req.length - System.getProperty("line.separator").length());*/
        String body = (String) msg;
        log.info("the time server receive order : {},the counter is {}", body, ++counter);
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
        //System.getProperty(“line.separator”)用来返回当前操作系统的行结束符。
        currentTime = currentTime + System.getProperty("line.separator");
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.writeAndFlush(resp);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
