package io.netty.learn.practice.nio;

public class NioClient {
    public static void main(String[] args){
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                //采用默认值
            }
        }
        new Thread(new TimeClientHandler("127.0.0.1",port),"TimeClient-").start();
    }
}
