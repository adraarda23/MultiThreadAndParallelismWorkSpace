package org.example.basics;

import java.util.Date;

public class StartThread {
    public static void main(String[] args){
//        var runnable = new Thread(new Runnable1());
//        var runnable2 = new Thread(new Runnable2());
//        runnable.start();
//        runnable2.start();


        Thread thread1 = new Thread(()->{
            int i =0;
            while(true){
                try {
                    Thread.sleep(50);
                    System.out.println("thread1 : " + i++);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        Thread thread2 = new Thread(()->{
            int i =0;
            while(true){
                try {
                    Thread.sleep(50);
                    System.out.println("thread2 : " + i++);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        thread1.start();
        thread2.start();

    }


}

class Runnable1 implements Runnable {
    @Override
    public void run() {
        while (true) {
            System.out.println("runnable1");
        }
    }
}

class Runnable2 implements Runnable {
    @Override
    public void run() {
        while (true) {
            System.out.println("runnable2");
        }
    }
}
