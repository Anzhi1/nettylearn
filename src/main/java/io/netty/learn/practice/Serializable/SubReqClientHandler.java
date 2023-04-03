package io.netty.learn.practice.Serializable;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubReqClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SubReqClientHandler.class);
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for(int i =0 ; i<10;i++){
            ctx.write(getReq(i));
        }
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("receive server response {}",msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private SubscribeReq getReq(int i){
        SubscribeReq req = new SubscribeReq();
        req.setSubReqId(i);
        req.setAddress("testAddress");
        req.setPhoneNumber("18800201234");
        req.setProductName("testProductName");
        req.setUserName("test");
        return req;
    }
}
