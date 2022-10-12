package com.sean.comment;

import org.junit.jupiter.api.Test;

public class testThreadLocal {
    @Test
    public void testThreadLocal01(){
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                ThreadLocal<Integer> threadLocal = new ThreadLocal<>();
                ThreadLocal<Double> threadLocal1 = new ThreadLocal<>();
                threadLocal.set(20);
                threadLocal1.set(20.0);
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t1");
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                ThreadLocal<Integer> threadLocal = new ThreadLocal<>();
                threadLocal.set(21);
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t2");
        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                ThreadLocal<Integer> threadLocal = new ThreadLocal<>();
                threadLocal.set(22);
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t3");
        t1.start();
        t2.start();
        t3.start();
        System.out.println("ok");
    }
}
