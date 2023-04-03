package io.netty.learn.practice.Serializable;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

public class SubReqServer {
    public void bind (int port) throws Exception{
        //配置服务端的NIO线程组
        EventLoopGroup boosGroup = new NioEventLoopGroup(0,new DefaultThreadFactory("boss"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(0,new DefaultThreadFactory("worker"));
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boosGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline
                                    //ObjectDecoder负责对实现Serializable的POJO对象进行解码，它有多个构造函数，支持不同的ClassResolver，在此我们使用weakCachingConcurrentResolver创建
                                    //线程安全的WeakReferenceMap对类加载器进行缓存，它支持多线程并发访问，当虚拟机内存不足时，会释放缓存中的内存，防止内存泄露。为了防止异常码流和解码错位导致
                                    //的内存溢出，这里将单个对象最大序列化后的字节数组长度设置为1M，作为例程它已经足够使用。
                                    .addLast(new ObjectDecoder(1024*1024, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())))
                                    //ObjectEncoder可以在消息发送的时候自动将实现Serializable的POJO对象进行编码，因此用户无需亲自对对象进行手工序列化，只需要关注自己的业务逻辑处理即可，对象
                                    //序列化和反序列化都由Netty的对象编码器搞定。
                                    .addLast(new ObjectEncoder())
                                    .addLast(new SubReqServerHandler());

                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            channelFuture.channel().closeFuture().sync();
        }
        finally {
            //优雅退出，释放线程池资源
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if(args!=null&&args.length>0){
            try {
                port=Integer.valueOf(args[0]);
            }
            catch (NumberFormatException e){
                //采用默认值
            }
        }
        new SubReqServer().bind(port);

    }
}
