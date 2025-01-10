package tomcat.learn;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public class SimpleHttpHandler implements HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(SimpleHttpHandler.class);
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();

    }
}
