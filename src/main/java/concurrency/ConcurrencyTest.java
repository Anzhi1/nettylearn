package concurrency;

public class ConcurrencyTest {

    private static final long count = 10000;

    public static void main(String[] args) throws InterruptedException {
        concurrency();
        serial();
    }

    private static void waitAnotherThread() throws InterruptedException {
        Thread t = new Thread(()->{
            System.out.println("hello");
        });
        System.out.println("start");
        //一个线程对象只能调用一次start()方法启动新线程,并在新线程中执行run()方法。一旦run()方法执行完毕，线程就结束了
        t.start();
        t.join();
        System.out.println("end");
    }

    private static void interrupt() throws InterruptedException {
        Thread t = new MyThread();
        t.start();
        Thread.sleep(10);
        //interrupt()方法仅仅向t线程发出了"中断请求"，至于t线程是否能正确响应，要看具体代码。
        t.interrupt();
        t.join();
        System.out.println("end");
    }

    static class MyThread extends Thread {
        public void run(){
            int n = 0;
            while(!isInterrupted()){
                System.out.println(n++);
            }
        }
    }
    private static void concurrency() throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int a = 0;
                for(long i = 0;i<count;i++){
                    a+=5;
                }
            }
        });
        thread.start();
        int b = 0;
        for(long i = 0; i<count;i++){
            b--;
        }
        long time = System.currentTimeMillis() - start;
        thread.join();
        System.out.println("concurrency :"+time+"ms,b="+b);
    }
    private static void serial(){
        long start = System.currentTimeMillis();
        int a = 0;
        for (long i = 0; i < count; i++){
            a+=5;
        }
        int b = 0;
        for (long i = 0; i < count; i++){
            b--;
        }
        long time = System.currentTimeMillis()-start;
        System.out.println("serial:" + time +"ms,b="+b+",a="+a);
    }






}
