package jvm;

import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * 应用上下文热加载
 */
public class ContextReloading {
    public static void main(String[] args)
            throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InstantiationException,
            InvocationTargetException, InterruptedException {
        for (;;){
            Object context = newContext();//创建应用上下文
            invokeContext(context);//通过上下文对象context调用业务方法
            Thread.sleep(5000);
        }
    }

    //创建应用的上下文，context是整个应用的GC roots，创建完返回对象之前调用init()初始化对象
    public static Object newContext()
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException,
            InvocationTargetException {
        String className = "jvm.ContextReloading$Context";
        //通过自定义类加载器加载Context类
        Class<?> contextClass = new MyClassLoader("target/classes").loadClass(className);
        Object context = contextClass.newInstance();//通过反射创建对象
        contextClass.getDeclaredMethod("init").invoke(context);//通过反射调用初始化方法init()
        return context;
    }
    //业务方法，调用context的业务方法showUser()
    public static void invokeContext(Object context)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        context.getClass().getDeclaredMethod("showUser").invoke(context);
    }


    public static class Context{
        private UserService userService = new UserService();
        public String showUser(){
            return userService.getUserMessage();
        }
        //初始化对象
        public void init(){
            UserDao userDao = new UserDao();
            userDao.setUser(new User());
            userService.setUserDao(userDao);
        }
    }


    public static class UserService{
        private UserDao userDao;
        public String getUserMessage(){
            return userDao.getUserName();
        }
        public void setUserDao(UserDao userDao) {
            this.userDao = userDao;
        }
    }


    public static class UserDao{
        private User user;
        public String getUserName(){
            //关键操作，运行main方法后切换下面方法，编译后下一次调用生效
            return user.getName();
            //return user.getFullName();
        }
        public void setUser(User user) {
            this.user = user;
        }
    }

    public static class User{
        private String name = "lucy";
        private String fullName = "hank.lucy";

        public String getName() {
            System.out.println("my name is " + name);
            return name;
        }
        public String getFullName() {
            System.out.println("my full name is " + fullName);
            return name;
        }
    }

    //跟之前的类加载器一模一样，可以略过
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