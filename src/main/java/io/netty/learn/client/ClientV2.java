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
import io.netty.learn.business.order.OrderOperationResult;
import io.netty.learn.client.codec.*;
import io.netty.learn.client.codec.dispatcher.OperationResultFuture;
import io.netty.learn.client.codec.dispatcher.RequestPendingCenter;
import io.netty.learn.client.codec.dispatcher.ResponseDispatcherHandler;
import io.netty.learn.common.OperationResult;
import io.netty.learn.common.RequestMessage;
import io.netty.learn.util.IdUtil;

import java.util.concurrent.ExecutionException;

public class ClientV2 {
    //响应-分发式客户端
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        RequestPendingCenter requestPendingCenter = new RequestPendingCenter();
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

                                .addLast(new ResponseDispatcherHandler(requestPendingCenter))

                                .addLast(new OperationToRequestMessageEncoder())
                                .addLast(new LoggingHandler(LogLevel.INFO));
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8090);

        channelFuture.sync();

        long streamId= IdUtil.nextId();

        RequestMessage requestMessage = new RequestMessage(
                streamId,new OrderOperation(1001,"tudou"));

        OperationResultFuture operationResultFuture = new OperationResultFuture();

        requestPendingCenter.add(streamId,operationResultFuture);
        //future是异步执行的
        channelFuture.channel().writeAndFlush(requestMessage);

        OperationResult operationResult = operationResultFuture.get();

        System.out.println(operationResult);

        channelFuture.channel().closeFuture().get();
    }
}
