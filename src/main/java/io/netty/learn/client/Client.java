package io.netty.learn.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.learn.business.order.OrderOperation;
import io.netty.learn.client.codec.OrderFrameDecoder;
import io.netty.learn.client.codec.OrderFrameEncoder;
import io.netty.learn.client.codec.OrderProtocolDecoder;
import io.netty.learn.client.codec.OrderProtocolEncoder;
import io.netty.learn.common.RequestMessage;
import io.netty.learn.util.IdUtil;
import io.netty.util.internal.UnstableApi;

import java.util.concurrent.ExecutionException;

@UnstableApi
public class Client {

    //客户端不区分Handler与childHandler，只有普通的Handler
    public static void main (String[] args) throws ExecutionException, InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class)
                //设置超时时间
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,10*1000)
                .group(new NioEventLoopGroup())
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel)  {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        pipeline.addLast(new OrderFrameDecoder())
                                .addLast(new OrderFrameEncoder())
                                .addLast(new OrderProtocolEncoder())
                                .addLast(new OrderProtocolDecoder())
                                .addLast(new LoggingHandler(LogLevel.INFO));
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect("127.0.0.1",8090);

        channelFuture.sync();
        RequestMessage requestMessage = new RequestMessage(IdUtil.nextId(),new OrderOperation(1001,"tudou"));
        //future是异步执行的
        //用于测试内存泄露，但失败。
        for(int i=0;i<10;i++){
            channelFuture.channel().writeAndFlush(requestMessage);
        }
        channelFuture.channel().writeAndFlush(requestMessage);

        channelFuture.channel().closeFuture().get();
    }
}
