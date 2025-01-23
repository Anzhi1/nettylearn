package tomcat.learn.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class LogFilter implements Filter {
    final Logger log = LoggerFactory.getLogger(LogFilter.class);
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        log.info("{}: {}", req.getMethod(), req.getRequestURI());
        filterChain.doFilter(servletRequest,servletResponse);
    }
}
