package io.netty.learn.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import io.netty.handler.ipfilter.RuleBasedIpFilter;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.learn.server.codec.OrderFrameDecoder;
import io.netty.learn.server.codec.OrderFrameEncoder;
import io.netty.learn.server.codec.OrderProtocolDecoder;
import io.netty.learn.server.codec.OrderProtocolEncoder;
import io.netty.learn.server.codec.handler.AuthHandler;
import io.netty.learn.server.codec.handler.MetricHandler;
import io.netty.learn.server.codec.handler.OrderServerProcessHandler;
import io.netty.learn.server.codec.handler.ServerIdleCheckHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;

import java.util.concurrent.ExecutionException;


public class Server {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        //可以共享的Handler metricHandler
        MetricHandler metricHandler = new MetricHandler();
        //定义线程池
        UnorderedThreadPoolEventExecutor businessThreadPool = new UnorderedThreadPoolEventExecutor(10,new DefaultThreadFactory("business"));
        NioEventLoopGroup businessThreadPool2 = new NioEventLoopGroup(0,new DefaultThreadFactory("business2"));
        GlobalTrafficShapingHandler globalTrafficShapingHandler = new GlobalTrafficShapingHandler(new NioEventLoopGroup(),100*1024*1024,100*1024*1024);
        IpSubnetFilterRule ipFilterRule = new IpSubnetFilterRule("127.1.0.1",16, IpFilterRuleType.REJECT);
        RuleBasedIpFilter ruleBasedIpFilter = new RuleBasedIpFilter(ipFilterRule);
        //为什么NioEventLoopGroup会慢？只用了一个线程去处理，所以一般都用别的
        AuthHandler authHandler = new AuthHandler();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //完善线程名方便调试
                .group(new NioEventLoopGroup(0,new DefaultThreadFactory("boss")),
                        new NioEventLoopGroup(0,new DefaultThreadFactory("worker")))
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        //完善handler名称方便调试
                        pipeline.addLast(new LoggingHandler(LogLevel.INFO))
                                //黑白名单
                                .addLast("ipFilter", ruleBasedIpFilter)
                                //限流套件
                                .addLast("shapingHandler",globalTrafficShapingHandler)
                                //空闲检测Handler
                                .addLast("IdleCheck",new ServerIdleCheckHandler())
                                .addLast("frameDecoder",new OrderFrameDecoder())
                                .addLast("frameEncoder",new OrderFrameEncoder())
                                .addLast("orderProtocolEncoder",new OrderProtocolEncoder())
                                .addLast("orderProtocolDecoder",new OrderProtocolDecoder())
                                .addLast("metricsHandler",metricHandler)
                                //牺牲一定的延迟，增加了系统的吞吐量，使用netty自带的FlushConsolidationHandler
                               .addLast("flushEnhance",new FlushConsolidationHandler(5,true))

                                //必须放在OrderProtocolDecoder之后，因为需要的是RequestMessage
                                .addLast("authHandler",authHandler)
                                //业务处理handler,可能会很耗时,使用线程池来处理
                                .addLast(businessThreadPool,new OrderServerProcessHandler());
                    }
                });
        ChannelFuture channelFuture = serverBootstrap.bind(8090).sync();
        channelFuture.channel().closeFuture().get();
    }
}
