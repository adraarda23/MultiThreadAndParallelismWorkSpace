package org.example.basics;

public class StartThread {
    public static void main(String[] args) {
        var runnable = new Thread(new Runnable1());
        var runnable2 = new Thread(new Runnable2());
        runnable.start();
        runnable2.start();

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
