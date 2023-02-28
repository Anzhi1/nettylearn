package io.netty.learn.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.learn.server.codec.OrderFrameDecoder;
import io.netty.learn.server.codec.OrderFrameEncoder;
import io.netty.learn.server.codec.OrderProtocolDecoder;
import io.netty.learn.server.codec.OrderProtocolEncoder;
import io.netty.learn.server.codec.handler.OrderServerProcessHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ExecutionException;

public class Server {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //完善线程名方便调试
                .group(new NioEventLoopGroup(0,new DefaultThreadFactory("boss")),
                        new NioEventLoopGroup(0,new DefaultThreadFactory("worker ")))
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        //完善handler名称方便调试
                        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast("frameDecoder",new OrderFrameDecoder())
                                .addLast(new OrderFrameEncoder())
                                .addLast(new OrderProtocolEncoder())
                                .addLast(new OrderProtocolDecoder())

                                .addLast(new OrderServerProcessHandler());
                    }
                });
        ChannelFuture channelFuture = serverBootstrap.bind(8090).sync();
        channelFuture.channel().closeFuture().get();
    }
}
