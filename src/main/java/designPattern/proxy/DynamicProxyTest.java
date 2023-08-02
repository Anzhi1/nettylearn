package designPattern.proxy;

import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class DynamicProxyTest {
    public static void main(String[] args){
        InterStellar interStellar = new InterStellar();
        InvocationHandler invocationHandler = new MyInvocationHandler(interStellar);
        //JDK的动态代理通过Proxy类的静态方法newProxyInstance动态创建代理
        //JDK的动态代理只能基于接口
        Movie dynamicProxy = (Movie) Proxy.newProxyInstance(InterStellar.class.getClassLoader(),InterStellar.class.getInterfaces(),invocationHandler);
        dynamicProxy.play();

        //Cglib的代理可以基于类
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY,"D:\\class");
        //创建Enhancer对象，类似于JDK动态代理的Proxy类
        Enhancer enhancer = new Enhancer();
        //设置目标类的字节码文件
        enhancer.setSuperclass(InterStellar.class);
        //设置回调函数
        enhancer.setCallback(new CglibProxyInterceptor());
        //这里的Create方法就是正式创建代理类
        InterStellar interStellar1 = (InterStellar) enhancer.create();
        interStellar1.play();
    }
}
