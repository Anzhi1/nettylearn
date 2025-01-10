package jvm;

import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;

public class ClassReloading {
    public static void main(String[] args)
            throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InstantiationException,
            InvocationTargetException, InterruptedException {
        for (;;){//用死循环让线程持续运行未中断状态
            //通过反射调用目标类的入口方法
            String className = "jvm.ClassReloading$User";
            Class<?> target = new MyClassLoader("target/classes").loadClass(className);
            //加载进来的类，通过反射调用execute方法
            target.getDeclaredMethod("execute").invoke(target.newInstance());
            //HelloWorld.class.getDeclaredMethod("execute").invoke(HelloWorld.class.newInstance());
            //如果换成系统默认类加载器的话，因为双亲委派原则，默认使用应用类加载器，而且能加载一次
            //休眠是为了在删除旧类编译新类的这段时间内不执行加载动作
            //不然会找不到类文件
            Thread.sleep(10000);
        }
    }

    //自定义类加载器加载的目标类
    public static class User {
        public void execute() throws InterruptedException {
            say();
            //ask();
        }
        public void ask(){
            System.out.println("what is your name");
        }
        public void say(){
            System.out.println("my name is lucy");
        }
    }

    //下面是自定义类加载器，跟第一个例子一样，可略过
    public static class MyClassLoader extends ClassLoader{
        private String classPath;
        public MyClassLoader(String classPath) {
            this.classPath = classPath;
        }
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if(!name.contains("java")){
                byte[] data = new byte[0];
                try {
                    data = loadByte(name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return defineClass(name,data,0,data.length);
            }else{
                return super.loadClass(name);
            }
        }
        private byte[] loadByte(String name) throws Exception {
            name = name.replaceAll("\\.", "/");
            String dir = classPath + "/" + name + ".class";
            FileInputStream fis = new FileInputStream(dir);
            int len = fis.available();
            byte[] data = new byte[len];
            fis.read(data);
            fis.close();
            return data;
        }
    }
}