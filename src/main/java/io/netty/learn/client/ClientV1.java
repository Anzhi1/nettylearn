package io.netty.learn.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.learn.business.order.OrderOperation;
import io.netty.learn.client.codec.*;
import io.netty.learn.common.RequestMessage;
import io.netty.learn.util.IdUtil;

import java.util.concurrent.ExecutionException;

public class ClientV1 {
    //客户端不区分Handler与childHandler，只有普通的Handler
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class)
                .group(new NioEventLoopGroup())
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        pipeline.addLast(new OrderFrameDecoder())
                                .addLast(new OrderFrameEncoder())
                                .addLast(new OrderProtocolEncoder())
                                .addLast(new OrderProtocolDecoder())
                                .addLast(new OperationToRequestMessageEncoder())
                                .addLast(new LoggingHandler(LogLevel.INFO));
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8090);

        channelFuture.sync();
        OrderOperation orderOperation = new OrderOperation(1001, "tudou");
        //future是异步执行的
        channelFuture.channel().writeAndFlush(orderOperation);

        channelFuture.channel().closeFuture().get();
    }
}
