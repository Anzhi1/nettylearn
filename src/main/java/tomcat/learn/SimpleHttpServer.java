package tomcat.learn;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class SimpleHttpServer implements HttpHandler, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(SimpleHttpServer.class);

    private final HttpServer httpServer;

    public static void main(String[] args){
        String host = "0.0.0.0";
        int port = 8080;
        try(SimpleHttpServer connector = new SimpleHttpServer(host, port)){
            while(true){
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public SimpleHttpServer(String host, int port) throws IOException{
        this.httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
        this.httpServer.createContext("/", this);
        this.httpServer.start();
        log.info("start jerrymouse http server at {} : {}",host, port);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //获取请求方法, URI, Path, Query等
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        String query = uri.getRawQuery();
        log.info("{}: {}?{}",method,path,query);
        //输出响应的header
        Headers respHeaders = exchange.getResponseHeaders();
        respHeaders.set("Content-Type", "text/html; charset=utf-8");
        respHeaders.set("Cache-Control","no-cache");
        exchange.sendResponseHeaders(200,0);
        //输出响应的内容
        String s = "<h1>Hello, world.</h1><p>" + LocalDateTime.now().withNano(0)+"</P>";
        try(OutputStream out = exchange.getResponseBody()){
            out.write(s.getBytes(StandardCharsets.UTF_8));
        }

    }

    @Override
    public void close() throws Exception {
        this.httpServer.stop(3);
    }
}
