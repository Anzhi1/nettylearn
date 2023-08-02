package designPattern.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MyInvocationHandler implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(MyInvocationHandler.class);
    private Object object;

    public MyInvocationHandler(Object object){
        this.object=object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        before();
        Object invoke = method.invoke(object,args);
        after();
        return invoke;
    }

    public void before(){
        log.info("proxy before");
    }

    public void after(){
        log.info("proxy after");
    }
}
