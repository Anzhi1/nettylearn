package tomcat.learn;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;



public class HttpExchangeAdapter implements HttpExchangeRequest, HttpExchangeResponse{

    final HttpExchange exchange;

    public HttpExchangeAdapter(HttpExchange exchange){
        this.exchange = exchange;
    }
    @Override
    public String getRequestMethod() {
        return this.exchange.getRequestMethod();
    }

    @Override
    public URI getRequestURI() {
        return this.exchange.getRequestURI();
    }

    @Override
    public Headers getResponseHeaders() {
        return this.exchange.getResponseHeaders();
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
        this.exchange.sendResponseHeaders(rCode,responseLength);

    }

    @Override
    public OutputStream getResponseBody() {
        return this.exchange.getResponseBody();
    }
}

