package io.netty.learn.practice.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger log = LoggerFactory.getLogger(WebSocketServerHandler.class);

    private WebSocketServerHandshaker webSocketServerHandshaker;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        //传统的http接入
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);

        }
        //Websocket接入
        else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        } else {
            log.error("消息类型异常");
            ctx.close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        //如果Http解码失败，则返回Http异常
        if (!request.getDecoderResult().isSuccess()
                || (!"websocket".equals(request.headers().get("Upgrade")))) {
            sendHttpResponse(ctx,request,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.BAD_REQUEST));
            return;
        }

        //构造握手相应返回，本机测试
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://localhost:8080/websocket", null, false);
        webSocketServerHandshaker = wsFactory.newHandshaker(request);
        if (webSocketServerHandshaker == null) {
            log.error("webSocketServerHandShaker初始化失败");
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            webSocketServerHandshaker.handshake(ctx.channel(), request);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        //判断是否为关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            log.info("接收到连接关闭指令，关闭");
            webSocketServerHandshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        //判断是否为ping消息
        else if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        //本例程仅支持文本消息，不支持二进制消息
        else if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }

        //返回应答消息
        String requestContent = ((TextWebSocketFrame) frame).text();
        log.info("ctx.channel{} receives {}", ctx.channel(), requestContent);
        ctx.channel().write(new TextWebSocketFrame(requestContent + "Welcome to netty Websocket practice,now is " + new Date().toString()));

    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
        //返回应答给客户端
        if (response.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(response.getStatus().toString(), CharsetUtil.UTF_8);
            response.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(response, response.content().readableBytes());
        }
        //如果是非KeepAlive,关闭连接
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(response);
        if (!HttpUtil.isKeepAlive(request) || response.getStatus().code() != 200) {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
