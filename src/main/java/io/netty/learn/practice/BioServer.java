package io.netty.learn.practice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BioServer {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                //采用默认值
            }
        }
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("the time server is start in port : " + port);
            Socket socket = null;
            while (true) {
                socket = serverSocket.accept();
                System.out.println("接收到连接");
                new Thread(new TimeServerHandler(socket)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (serverSocket != null) {
                System.out.println("the time server close");
                serverSocket.close();
                serverSocket = null;
            }
        }
    }
}
