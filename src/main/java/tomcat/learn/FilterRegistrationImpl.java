package tomcat.learn;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebInitParam;
import tomcat.learn.support.InitParameters;

import java.util.*;

public class FilterRegistrationImpl implements FilterRegistration.Dynamic {
    final ServletContext servletContext;
    final String name;
    final Filter filter;

    final InitParameters initParameters = new InitParameters();
    final List<String> urlPatterns = new ArrayList<>();
    boolean initialized = false;

    public FilterRegistrationImpl(ServletContext servletContext, String name, Filter filter){
        this.servletContext = servletContext;
        this.name = name;
        this.filter = filter;
    }

    public FilterConfig getFilterConfig(){
        return new FilterConfig() {
            @Override
            public String getFilterName() {
                return FilterRegistrationImpl.this.name;
            }

            @Override
            public ServletContext getServletContext() {
                return FilterRegistrationImpl.this.servletContext;
            }

            @Override
            public String getInitParameter(String name) {
                return FilterRegistrationImpl.this.initParameters.getInitParameters(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return FilterRegistrationImpl.this.initParameters.getInitParameterNames();
            }
        };
    }

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
        throw new UnsupportedOperationException("addMappingForServletNames");
    }

    @Override
    public Collection<String> getServletNameMappings() {
        return List.of();
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
        checkNoInitialized("addMappingForUrlPatterns");
        if (!dispatcherTypes.contains(DispatcherType.REQUEST) || dispatcherTypes.size() != 1) {
            throw new IllegalArgumentException("Only support DispatcherType.REQUEST.");
        }
        if (urlPatterns == null || urlPatterns.length == 0) {
            throw new IllegalArgumentException("Missing urlPatterns.");
        }
        for (String urlPattern : urlPatterns) {
            this.urlPatterns.add(urlPattern);
        }


    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        return this.urlPatterns;
    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {
        checkNoInitialized("setInitParameter");
        if(isAsyncSupported){
            throw new UnsupportedOperationException("Async is not supported. ");
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getClassName() {
        return filter.getClass().getName();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        checkNoInitialized("setInitParameter");
        return this.initParameters.setInitParameter(name,value);

    }

    @Override
    public String getInitParameter(String name) {
        return this.initParameters.getInitParameters(name);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        checkNoInitialized("setInitParameter");
        return this.initParameters.setInitParameters(initParameters);
    }

    @Override
    public Map<String, String> getInitParameters() {
        return this.initParameters.getInitParameters();
    }

    private void checkNoInitialized(String name){
        if(this.initialized){
            throw new IllegalStateException("Cannot call " + name + "after initialization.");
        }
    }
}
