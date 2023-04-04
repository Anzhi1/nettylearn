package io.netty.learn.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpFileServer {
    private static final Logger log = LoggerFactory.getLogger(HttpFileServer.class);

    private static final String defaultUrl = "/users/until";

    public void run(final int port, final String url) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("bossGroup"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("workerGroup"));

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            sc.pipeline()
                                    //http请求消息解码器
                                    .addLast("http-decoder", new HttpRequestDecoder())
                                    //httpObjectAggregator解码器，可以将多个消息转换为单一的FullHttpRequest或者FullHttpResponse
                                    //原因是HTTP解码器在每个HTTP消息中会生成多个消息对象 (1)HttpRequest/HttpResponse (2)HttpContent (3)LastHttpContent
                                    .addLast("http-aggregator", new HttpObjectAggregator(65536))
                                    //http响应编码器，对HTTP响应消息进行编码
                                    .addLast("http-encoder", new HttpResponseEncoder())
                                    //支持异步发送大的码流(例如大的文件传输)，但不占用过多的内存，防止发生Java内存溢出错误
                                    .addLast("http-chunked", new ChunkedWriteHandler())
                                    .addLast("http-server_handler",new HttpFileServerHandler(url));
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            log.info("http文件服务器启动，端口为{}", port);

            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        String url = defaultUrl;
        if (args.length > 1) {
            url = args[1];
        }
        new HttpFileServer().run(port, url);
    }
}
