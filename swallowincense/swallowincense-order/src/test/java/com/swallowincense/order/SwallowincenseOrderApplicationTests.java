package com.swallowincense.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.*;

@SpringBootTest
class SwallowincenseOrderApplicationTests {
    @Autowired
    ThreadPoolExecutor executor;
    @Test
    void contextLoads() {
        new Thread(() -> System.out.println("run")).start();
        Callable<String> a = () -> "aa";
        try {
            String call = a.call();
            System.out.println(call);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Thread thread = new Thread(new FutureTask<String>(() -> {
            System.out.println("aaa");
            System.out.println(Thread.currentThread().getName());
            return null;
        }));
        thread.start();
    }


    private static ArrayList<String> list = new ArrayList<String>();

    /*public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            list.add(String.valueOf(i));
        }
        Iterator iter = list.iterator();
        String value = null;
        while (iter.hasNext()) {
            value = (String) iter.next();
            System.out.print(value + ", ");
            //list.remove(value);//在用Iterator遍历的过程中，如果在原集合对象上进行修改就会报ConcurrentModificationException错误
            iter.remove();
        }
    }*/

    private static CopyOnWriteArrayList<String> safelist = new CopyOnWriteArrayList<String>();
    private static void testFailSafe() {
        for(int i=0;i<5;i++) {
            safelist.add(String.valueOf(i));
        }
        String value=null;
        Iterator iter=safelist.iterator();
        while(iter.hasNext()) {
            value = (String)iter.next();
            System.out.print(value+", ");
            safelist.remove(value);//可以在原集合对象上操作，没有报错
        }
    }


    @Test
    void deadLock() {
            String lockA = "A锁";
            String lockB = "B锁";

        executor.execute(()->{
                new HoldThread(lockA,lockB).run();
            });
        executor.execute(()->{
                new HoldThread(lockB,lockA).run();
            });
        }

    class HoldThread implements Runnable {
        private String lockA;
        private String lockB;

        public HoldThread(String lockA, String lockB) {
            this.lockA = lockA;
            this.lockB = lockB;
        }

        @Override
        public void run() {
            synchronized (lockA) {
                System.out.println(Thread.currentThread().getName() + "\t 自己持有：" + lockA + "\t 尝试获取：" + lockB);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (lockB) {
                    System.out.println(Thread.currentThread().getName() + "\t 自己持有：" + lockB + "\t 尝试获取：" + lockA);
                }
            }
        }
    }

    private final Integer a;

    public SwallowincenseOrderApplicationTests(Integer b){
        a = b;
    }

    public Integer getA(){
        return a;
    }

    /*public void setA(Integer a){
        this.a = a;
    }*/

    public static void main(String[] args) {
        Integer b = 132;
        SwallowincenseOrderApplicationTests applicationTests = new SwallowincenseOrderApplicationTests(b);
        System.out.println(applicationTests.getA());
    }

}
