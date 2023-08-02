package designPattern.proxy;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class CglibProxyInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CglibProxyInterceptor.class);

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        before();
        Object object = methodProxy.invokeSuper(o,objects);
        after();
        return object;
    }

    public void before(){
        log.info("proxy before");
    }

    public void after(){
        log.info("proxy after");
    }
}
