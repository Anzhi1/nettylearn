package tomcat.learn;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tomcat.learn.Servlet.HelloServlet;
import tomcat.learn.Servlet.IndexServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.List;


public class HttpConnector implements HttpHandler, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(SimpleHttpHandler.class);

    //持有ServletContext实例
    final ServletContextImpl servletContext;
    final HttpServer httpServer;

    public HttpConnector() throws IOException, ServletException {
        //创建ServletContext
        this.servletContext = new ServletContextImpl();
        //初始化Servlet
        this.servletContext.initialize(List.of(IndexServlet.class, HelloServlet.class));

        String host = "0.0.0.0";
        int port = 8080;
        this.httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
        this.httpServer.createContext("/", this);
        this.httpServer.start();
        log.info("start jerry mouse http server at {} : {}",host, port);
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var adapter = new HttpExchangeAdapter(exchange);
        var request = new HttpServletRequestImpl(adapter);
        var response = new HttpServletResponseImpl(adapter);
        //process
        try {
            this.servletContext.process(request, response);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String name = request.getParameter("name");
        String html = "<h1>Hello, " + (name == null ? "world" : name) + ".</h1>";
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();
        pw.write(html);
        pw.close();
    }

    @Override
    public void close() throws Exception {
        this.httpServer.stop(3);
    }
}
