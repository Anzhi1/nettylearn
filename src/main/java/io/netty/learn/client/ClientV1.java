package io.netty.learn.client;

import com.sun.security.jgss.AuthorizationDataEntry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.learn.business.auth.AuthOperation;
import io.netty.learn.business.order.OrderOperation;
import io.netty.learn.client.codec.*;
import io.netty.learn.client.codec.dispatcher.ClientIdleCheckHandler;
import io.netty.learn.client.codec.dispatcher.KeepAliveHandler;
import io.netty.learn.common.RequestMessage;
import io.netty.learn.util.IdUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientV1 {

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private static EventLoopGroup eventLoopGroup = new NioEventLoopGroup(0,new DefaultThreadFactory("client"));

    public static final String LOCAL_IP = "127.0.0.1";

    public static final Integer PORT = 8090;

    private static final Logger log = LoggerFactory.getLogger(ClientV1.class);


    //客户端不区分Handler与childHandler，只有普通的Handler
    public void connect(String ip,int port) throws ExecutionException, InterruptedException {
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class)
                    .group(eventLoopGroup)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            ChannelPipeline pipeline = nioSocketChannel.pipeline();
                            pipeline.addLast(new ClientIdleCheckHandler())
                                    .addLast(new OrderFrameDecoder())
                                    .addLast(new OrderFrameEncoder())
                                    .addLast(new OrderProtocolEncoder())
                                    .addLast(new OrderProtocolDecoder())
                                    .addLast("keepAlive",new KeepAliveHandler())
                                    .addLast(new OperationToRequestMessageEncoder())
                                    .addLast(new LoggingHandler(LogLevel.INFO));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(ip, port);

            channelFuture.sync();
            AuthOperation authOperation = new AuthOperation("admin1","122");
            OrderOperation orderOperation = new OrderOperation(1001, "tudou");
            //future是异步执行的
            channelFuture.channel().writeAndFlush(authOperation);
            channelFuture.channel().writeAndFlush(orderOperation);
            channelFuture.channel().closeFuture().get();
        }
        finally {
            //所有资源释放完成之后，清空资源，再次发起重连操作
            log.info("触发重连");
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                        try {
                            connect(LOCAL_IP,PORT);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }catch (InterruptedException e1){
                        e1.printStackTrace();
                    }
                }
            });
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new ClientV1().connect(LOCAL_IP,PORT);
    }
}
