package io.netty.learn.practice.Serializable;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class SubReqServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SubReqServerHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SubscribeReq req = (SubscribeReq) msg;
        if("test".equals(req.getUserName())){
            log.info("Service accept {}",req);
            ctx.writeAndFlush(getResp(req.getSubReqId()));
        }

    }
    private SubscribeResp getResp(Integer reqId){
        SubscribeResp resp = new SubscribeResp();
        resp.setRespCode(0);
        resp.setSubReqId(reqId);
        resp.setDesc("success");
        return resp;
    }
}
