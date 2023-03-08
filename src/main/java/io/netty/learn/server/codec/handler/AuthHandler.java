package io.netty.learn.server.codec.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.learn.business.auth.AuthOperation;
import io.netty.learn.business.auth.AuthOperationResult;
import io.netty.learn.common.Operation;
import io.netty.learn.common.OperationResult;
import io.netty.learn.common.RequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class AuthHandler extends SimpleChannelInboundHandler<RequestMessage> {
    private static final Logger log = LoggerFactory.getLogger(AuthHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RequestMessage requestMessage) throws Exception {
        try {
            Operation operation=requestMessage.getMessageBody();
            if(operation instanceof AuthOperation){
                AuthOperation authOperation = (AuthOperation) operation;
                AuthOperationResult res = (AuthOperationResult) authOperation.execute();
                if(res.isPass()){
                    log.info("pass auth");
                }else{
                    log.error("fail to auth");
                    channelHandlerContext.close();
                }
            }
            else{
                log.error("expect auth operation");
                channelHandlerContext.close();
            }
        }
        catch (Exception e){
            log.error("exception happens");
            channelHandlerContext.close();
        }
        finally {
            //完成授权/授权失败后移除Handler
            channelHandlerContext.pipeline().remove(this);
        }

    }
}
