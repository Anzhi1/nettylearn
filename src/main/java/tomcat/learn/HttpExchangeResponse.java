package tomcat.learn;

import com.sun.net.httpserver.Headers;

import java.io.IOException;
import java.io.OutputStream;

interface HttpExchangeResponse{
    Headers getResponseHeaders();
    void sendResponseHeaders(int rCode, long responseLength) throws IOException;
    OutputStream getResponseBody();
}