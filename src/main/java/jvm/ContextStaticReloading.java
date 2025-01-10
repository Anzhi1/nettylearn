package jvm;

import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;

public class ContextStaticReloading {
    public static void main(String[] args)
            throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InstantiationException,
            InvocationTargetException, InterruptedException, NoSuchFieldException {
        Cache cache = new Cache();//根据GCRoots可达原则，Cache会被系统类加载器加载，而且不会被GC回收
        cache.setName("Jack");
        for (;;){
            Object context = newContext(cache);//把cache赋给context，注意context是由自定义类加载器的，所以每次循环都会被新版本的context替换，而cache不会被GC所以一直保持用户态。
            invokeContext(context);
            Thread.sleep(5000);
        }
    }
    public static Object newContext(Cache cache)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException,
            InvocationTargetException, NoSuchFieldException {
        String className = "jvm.ContextStaticReloading$Context";
        Class<?> contextClass = new MyClassLoader("target/classes").loadClass(className);
        Object context = contextClass.newInstance();
        contextClass.getDeclaredField("cache").set(context,cache);//初始化之前显把context对象里面的cache进行赋值，这里要特别注意的是，我们传入的cache对象是由系统类加载器加载的。而Context类里面的Cache类却是由自定义类加载器MyClassLoader加载，这样赋值会造成类型和值不一致，导致抛出IllegalArgumentException异常。解决思路很简单，让他们保持一致就行，但是由于我们Cache必须又系统默认类加载器加载，那么只能让Context里面的Cache显性的指定系统类加载器来加载(因为不指定的话，根据类加载器依赖传导原则，依赖类复用被依赖的类的加载器)。本例子很简单，就是在复写loadClass方法的时候，增加条件!name.contains("Cache")，让MyClassLoader加载Cache时抛给系统类加载器。
        contextClass.getDeclaredMethod("init").invoke(context);
        return context;
    }
    public static void invokeContext(Object context)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        context.getClass().getDeclaredMethod("showUser").invoke(context);
    }
    public static class Context{
        public Cache cache;
        private UserService userService = new UserService();
        public void init(){
            User user = new User();
            user.setName("Rose-Test");
            UserDao userDao = new UserDao();
            userDao.setUser(user);
            userService.setUserDao(userDao);
        }
        public void showUser(){
            System.out.println("from context name is " + userService.getUserMessage());
            System.out.println("from  cache  name is " + cache.getName());
            System.out.println();
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
            return user.getName();
        }
        public void setUser(User user) {
            this.user = user;
        }
    }
    public static class User{
        private String name;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }
    public static class Cache{
        private String name;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }
    public static class MyClassLoader extends ClassLoader{
        private String classPath;
        public MyClassLoader(String classPath) {
            this.classPath = classPath;
        }
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if(!name.contains("java") && !name.contains("Cache") ){//就是这里加条件过滤Cache
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