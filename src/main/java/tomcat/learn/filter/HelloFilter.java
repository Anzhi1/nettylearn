package tomcat.learn.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

@WebFilter(urlPatterns = "/hello")
public class HelloFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(HelloFilter.class);

    Set<String> names = Set.of("Bob","Alice", "Tom", "Jerry");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String name = req.getParameter("name");
        log.info("check parameter name = {}", name);
        if(name !=null && names.contains(name)){
            filterChain.doFilter(servletRequest,servletResponse);
        }else{
            log.warn("Access denied: name = {}",name);
            HttpServletResponse resp = (HttpServletResponse) servletResponse;
            resp.sendError(403,"Forbidden");
        }
    }
}
