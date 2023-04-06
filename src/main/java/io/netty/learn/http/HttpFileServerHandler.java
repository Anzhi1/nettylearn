package io.netty.learn.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpUtil.setContentLength;

public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger log = LoggerFactory.getLogger(HttpFileServerHandler.class);

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    private final String url;


    public HttpFileServerHandler(String url) {
        this.url = url;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) throws Exception {
        if (!request.getDecoderResult().isSuccess()) {
            log.info("解码失败");
            sendError(channelHandlerContext,HttpResponseStatus.BAD_REQUEST);
            return;
        }
        if (request.getMethod() != HttpMethod.GET) {
            sendError(channelHandlerContext,HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        final String uri = request.uri();
        final String path = sanitizedUrl(uri);
        log.info("path的值为{}",path);
        if(path==null){
            sendError(channelHandlerContext,HttpResponseStatus.FORBIDDEN);
        }
        File file = new File(path);
        if (file.isHidden() || !file.exists()) {
            sendError(channelHandlerContext,HttpResponseStatus.NOT_FOUND);
            return;
        }
        if (file.isDirectory()) {
            if (uri.endsWith("/")) {
                sendListing(channelHandlerContext,file);
            } else {
                sendRedirect(channelHandlerContext,uri+'/');

            }
            return;
        }
        if (!file.isFile()) {
            sendError(channelHandlerContext,HttpResponseStatus.FORBIDDEN);
            return;
        }
        RandomAccessFile randomAccessFile = null;
        try {
            //以只读的方式打开文件
            randomAccessFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            sendError(channelHandlerContext,HttpResponseStatus.NOT_FOUND);
            return;
        }

        long fileLength = randomAccessFile.length();
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        setContentLength(response,fileLength);
        setContentTypeHeader(response,file);
        if(isKeepAlive(request)){
            response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaders.Values.KEEP_ALIVE);
        }
        channelHandlerContext.write(response);
        ChannelFuture sendFileFuture;
        sendFileFuture = channelHandlerContext.write(new ChunkedFile(randomAccessFile, 0, fileLength, 8192), channelHandlerContext.newProgressivePromise());

        //为sendFileFuture增加GenericFutureListener，如果发送完成，打印"Transfer complete"
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture channelProgressiveFuture, long progress, long total) throws Exception {
                if (total < 0) {
                    log.error("Transfer progress a {}", progress);
                } else {
                    log.error("Transfer progress b {}", progress);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture channelProgressiveFuture) throws Exception {
                log.info("Transfer completed");
            }
        });
        //由于使用chunked编码，最后需要发送一个编码结束的空消息体，将LastHttpContent的EMPTY_LAST_CONTENT发送到缓冲区中，标识所有
        //的消息体已经发送完成，同时调用flush方法将之前在发送缓冲区的消息刷新到SocketChannel中发送给对方。
        ChannelFuture lastContentFuture = channelHandlerContext.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

        //如果是非KeepAlive的，最后一包消息发送完成之后，服务端要主动关闭连接
        if (!isKeepAlive(request)) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


    private String sanitizedUrl(String uri) {
        try {
            uri = URLDecoder.decode(uri, StandardCharsets.UTF_8);
        } catch (Exception e) {
            try {
                uri = URLDecoder.decode(uri, StandardCharsets.ISO_8859_1);
            } catch (Exception e1) {
                log.info("采用默认uri");
            }
        }
        if (!uri.startsWith(this.url) || !uri.startsWith("/")) {
            log.error("url不符合");
            return null;
        }
        log.info("替换前的uri为{}",uri);
        uri = uri.replace('/', File.separatorChar);
        if (uri.contains(File.separator + '.')
                || uri.contains('.' + File.separator)
                || uri.startsWith(".")
                || uri.endsWith(".")
                || INSECURE_URI.matcher(uri).matches()) {
            log.error("url不符合");
            return null;
        }
        return System.getProperty("user.dir")  + uri;

    }
    private static void sendListing(ChannelHandlerContext ctx,File dir){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html;charset=UTF-8");
        StringBuilder buf = new StringBuilder();
        String dirPath=dir.getPath();
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append(dirPath);
        buf.append(" 目录：");
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>");
        buf.append(dirPath).append(" 目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
        for(File f:dir.listFiles()){
            if(f.isHidden()||!f.canRead()){
                continue;
            }
            String name=f.getName();
            if(!ALLOWED_FILE_NAME.matcher(name).matches()){
                continue;
            }
            buf.append("<li>链接：<a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }
        buf.append("</ul></body></html>\r\n");
        //分配对应消息的缓冲对象
        ByteBuf buffer= Unpooled.copiedBuffer(buf,CharsetUtil.UTF_8);
        //将对象写进response中
        response.content().writeBytes(buffer);
        //释放缓冲区
        buffer.release();
        //将响应消息发送到缓冲区并刷新到SocketChannel中
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
    private static void sendRedirect(ChannelHandlerContext ctx,String newUri){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.LOCATION,newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx,HttpResponseStatus status){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,status,
                Unpooled.copiedBuffer("Failure:"+status.toString()+"\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void setContentTypeHeader(HttpResponse response,File file){
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,
                mimeTypesMap.getContentType(file.getPath()));

    }

}
