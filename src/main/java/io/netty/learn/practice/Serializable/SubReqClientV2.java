package io.netty.learn.practice.Serializable;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;

public class SubReqClientV2 {
    public void bind (int port,String host) throws Exception{
        //配置服务端的NIO线程组
        EventLoopGroup clientGroup = new NioEventLoopGroup(0,new DefaultThreadFactory("clientGroup"));
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(MarshallingCodeCFactory.buildMarshallingDecoder())
                                    .addLast(MarshallingCodeCFactory.buildMarshallingEncoder())
                                    .addLast(new SubReqClientHandler());
                        }
                    });

            //发起异步连接操作
            ChannelFuture channelFuture = bootstrap.connect(host,port).sync();

            //等待客户端链路关闭
            channelFuture.channel().closeFuture().sync();
        }
        finally {
            //优雅退出，释放线程池资源
            clientGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String host = "127.0.0.1";
        if(args!=null&&args.length>0){
            try {
                port=Integer.valueOf(args[0]);
            }
            catch (NumberFormatException e){
                //采用默认值
            }
        }
        new SubReqClientV2().bind(port,host);

    }
}
