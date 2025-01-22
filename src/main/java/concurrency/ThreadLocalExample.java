package concurrency;

import java.util.PriorityQueue;

public class ThreadLocalExample {

    private static ThreadLocal<Integer> threadLocal = ThreadLocal.withInitial(()->0);

    public static void main(String [] args){
        Runnable task = ()->{
            int value = threadLocal.get();
            value+=1;
            threadLocal.set(value);
            System.out.println(Thread.currentThread().getName() + " value: " + threadLocal.get());
        };

        Thread thread1 = new Thread(task, "Thread-1");
        Thread thread2 = new Thread(task, "Thread-2");

        thread1.start();
        thread2.start();


    }

}
