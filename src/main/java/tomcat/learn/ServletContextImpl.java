package tomcat.learn;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tomcat.learn.filter.HelloFilter;
import tomcat.learn.filter.LogFilter;
import tomcat.learn.mapping.FilterMapping;
import tomcat.learn.mapping.ServletMapping;
import tomcat.learn.utils.AnnoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ServletContextImpl implements ServletContext {

    private static final Logger log = LoggerFactory.getLogger(ServletContextImpl.class);
    Map<String, ServletRegistrationImpl> servletRegistrations = new HashMap<>();
    Map<String, Servlet> nameToServlets = new HashMap<>();
    List<ServletMapping> servletMappings = new ArrayList<>();

    Map<String, FilterRegistrationImpl> filterRegistrations = new HashMap<>();
    Map<String, Filter> nameToFilters = new HashMap<>();
    List<FilterMapping> filterMappings = new ArrayList<>();

    public void initialize(List<Class<?>> servletClasses) throws ServletException {
        //依次添加每个Servlet
        for (Class<?> c : servletClasses) {
            //获取@WebServlet注解
            WebServlet ws = c.getAnnotation(WebServlet.class);
            if(ws!=null){
                Class<? extends Servlet> clazz = (Class<? extends Servlet>) c;
                // 创建一个ServletRegistration.Dynamic:
                ServletRegistration.Dynamic registration = this.addServlet(AnnoUtils.getServletName(clazz), clazz);
                registration.addMapping(AnnoUtils.getServletUrlPatterns(clazz));
                registration.setInitParameters(AnnoUtils.getServletInitParams(clazz));
            }

        }
        //实例化Servlet:
        for (String name : this.servletRegistrations.keySet()) {
            var registration = this.servletRegistrations.get(name);
            registration.servlet.init(registration.getServletConfig());
            this.nameToServlets.put(name, registration.servlet);
            for (String urlPattern : registration.getMappings()) {
                this.servletMappings.add(new ServletMapping(urlPattern, registration.servlet));
            }
            registration.initialized = true;
        }

        initFilters(List.of(HelloFilter.class, LogFilter.class));

    }

    public void initFilters(List<Class<?>> filterClasses) throws ServletException {
        for (Class<?> c : filterClasses) {
            //获取WebFilter注解:
            WebFilter wf = c.getAnnotation(WebFilter.class);
            //添加Filter
            if (wf != null) {
                Class<? extends Filter> clazz = (Class<? extends Filter>) c;
                FilterRegistration.Dynamic registration = this.addFilter(AnnoUtils.getFilterName(clazz), clazz);
                registration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, AnnoUtils.getFilterUrlPatterns(clazz));
                registration.setInitParameters(AnnoUtils.getFilterInitParams(clazz));
            }
        }
        //init filters
        for(String name : this.filterRegistrations.keySet()){
            var registration = this.filterRegistrations.get(name);
            //调用Filter.init()方法
            registration.filter.init(registration.getFilterConfig());
            this.nameToFilters.put(name, registration.filter);
            //将Filter定义的每个URL映射编译为正则表达式;
            for(String urlPattern : registration.getUrlPatternMappings()){
                this.filterMappings.add(new FilterMapping(urlPattern, registration.filter));
            }
        }

    }

    public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //请求路径
        String path = request.getRequestURI();
        //搜索Servlet
        Servlet servlet = null;
        for (ServletMapping mapping : this.servletMappings) {
            if (mapping.matches(path)) {
                //路径匹配
                servlet = mapping.servlet;
                break;
            }
        }
        if (servlet == null) {
            //未匹配到任何Servlet显示404 Not Found
            PrintWriter pw = response.getWriter();
            pw.write("<h1>404 Not Found</h1><p>No mapping for URL: " + path + "</p>");
            pw.close();
            return;
        }
        //查找Filter
        List<Filter> enabledFilters = new ArrayList<>();
        for(FilterMapping mapping : this.filterMappings){
            if(mapping.matches(path)){
                enabledFilters.add(mapping.filter);
            }
        }
        Filter[] filters = enabledFilters.toArray(Filter[]::new);
        log.info("process {} by filter {}, servlet {}", path, Arrays.toString(filters), servlet);
        FilterChain chain = new FilterChainImpl(filters, servlet);
        try {
            chain.doFilter(request, response);
        }catch (ServletException e){
            log.error(e.getMessage());
        }


    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public ServletContext getContext(String uripath) {
        if ("".equals(uripath)) {
            return this;
        }
        // all others are not exist:
        return null;
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String file) {
        String defaultMime = "application/octet-stream";
        Map<String, String> mimes = Map.of(".html", "text/html", ".txt", "text/plain", ".png", "image/png", ".jpg", "image/jpeg");
        int n = file.lastIndexOf('.');
        if (n == -1) {
            return defaultMime;
        }
        String ext = file.substring(n);
        return mimes.getOrDefault(ext, defaultMime);
    }

    @Override
    public Set<String> getResourcePaths(String s) {
        return Set.of();
    }

    @Override
    public URL getResource(String s) throws MalformedURLException {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String s) {
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String s) {
        return null;
    }

    @Override
    public void log(String s) {

    }

    @Override
    public void log(String s, Throwable throwable) {

    }

    @Override
    public String getRealPath(String s) {
        return "";
    }

    @Override
    public String getServerInfo() {
        return "";
    }

    @Override
    public String getInitParameter(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public boolean setInitParameter(String s, String s1) {
        throw new UnsupportedOperationException("setInitParameter");
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public String getServletContextName() {
        return "";
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String s, String s1) {
        return null;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String name, Servlet servlet) {
        var registration = new ServletRegistrationImpl(this, name, servlet);
        this.servletRegistrations.put(name, registration);
        return registration;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String s, Class<? extends Servlet> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("class is null.");
        }
        Servlet servlet = null;
        try {
            servlet = createInstance(clazz);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        return addServlet(s, servlet);
    }

    @Override
    public ServletRegistration.Dynamic addJspFile(String s, String s1) {
        return null;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return createInstance(clazz);
    }

    @Override
    public ServletRegistration getServletRegistration(String name) {
        return this.servletRegistrations.get(name);
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return Map.copyOf(this.servletRegistrations);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String name, String className) {
        try {
            return addFilter(name, (Class<? extends Filter>) Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String name, Filter filter) {
        var registration = new FilterRegistrationImpl(this, name, filter);
        this.filterRegistrations.put(name, registration);
        return registration;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String name, Class<? extends Filter> clazz) {
        try {
            return addFilter(name, clazz.newInstance());
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> aClass) throws ServletException {
        return null;
    }

    @Override
    public FilterRegistration getFilterRegistration(String s) {
        return null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return Map.of();
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> set) {

    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return Set.of();
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return Set.of();
    }

    @Override
    public void addListener(String s) {

    }

    @Override
    public <T extends EventListener> void addListener(T t) {

    }

    @Override
    public void addListener(Class<? extends EventListener> aClass) {

    }

    @Override
    public <T extends EventListener> T createListener(Class<T> aClass) throws ServletException {
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public void declareRoles(String... strings) {

    }

    @Override
    public String getVirtualServerName() {
        return "";
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int i) {

    }

    @Override
    public String getRequestCharacterEncoding() {
        return "";
    }

    @Override
    public void setRequestCharacterEncoding(String s) {

    }

    @Override
    public String getResponseCharacterEncoding() {
        return "";
    }

    @Override
    public void setResponseCharacterEncoding(String s) {

    }


    @SuppressWarnings("unchecked")
    private <T> T createInstance(String className) throws ServletException {
        Class<T> clazz;
        try {
            clazz = (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found.", e);
        }
        return createInstance(clazz);
    }

    private <T> T createInstance(Class<T> clazz) throws ServletException {
        try {
            Constructor<T> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new ServletException("Cannot instantiate class " + clazz.getName(), e);
        }
    }
}
