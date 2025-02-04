package tomcat.learn;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class SimpleHttpHandler implements HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(SimpleHttpHandler.class);
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
}
