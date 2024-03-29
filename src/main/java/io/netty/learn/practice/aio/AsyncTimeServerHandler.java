package io.netty.learn.practice.aio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

public class AsyncTimeServerHandler implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(AsyncTimeServerHandler.class);

    private int port;
    CountDownLatch latch;
    AsynchronousServerSocketChannel serverSocketChannel;
    public AsyncTimeServerHandler(int port){
        this.port=port;
        try {
            serverSocketChannel = AsynchronousServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            log.info("the time server is start in port:{}",port);
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    @Override
    public void run() {
        latch = new CountDownLatch(1);
        doAccept();
        try {
            latch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public void doAccept(){
        serverSocketChannel.accept(this,new AcceptCompletionHandler());
    }
}
