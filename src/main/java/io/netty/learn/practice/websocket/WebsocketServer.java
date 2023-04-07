package io.netty.learn.practice.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.learn.server.Server;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketServer {
    private static final Logger log = LoggerFactory.getLogger(WebsocketServer.class);

    public void run(int port) throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("boss"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            ChannelPipeline channelPipeline = sc.pipeline();
                            channelPipeline
                                    .addLast("http-codec", new HttpServerCodec())
                                    .addLast("aggregator", new HttpObjectAggregator(65536))
                                    .addLast("http-chunked", new ChunkedWriteHandler())
                                    .addLast("web-socket-handler",new WebSocketServerHandler());

                        }
                    });

            Channel channel = serverBootstrap.bind(port).sync().channel();
            log.info("websocket server started at port {}, open your browser and navigate to http://localhost:{}", port, port);
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                //采用默认值
            }
        }
        new WebsocketServer().run(port);

    }
}
