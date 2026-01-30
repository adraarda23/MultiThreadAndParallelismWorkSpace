package org.example.basics;

public class TESTT_Synchronized {

    private static int counter = 0;

    private synchronized static void increment(){
        // Synchronized ile thread-safe yapıyoruz
        int temp = counter;
        counter = temp + 1;
    };

    public static void main(String[] args) throws InterruptedException {

        long totalTime = 0;

        for(int run = 1; run <= 10; run++){
            counter = 0;

            long startTime = System.nanoTime();

            Thread thread1 = new Thread(() -> {
                for(int i = 0; i < 100000000; i++){
                    increment();
                }
            });

            Thread thread2 = new Thread(() -> {
                for(int i = 0; i < 100000000; i++){
                    increment();
                }
            });

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;
            totalTime += duration;

            System.out.println("Run #" + run + " - counter = " + counter + " (Expected: 200000) - Time: " + duration + " ms");
        }

        System.out.println("\n[SYNCHRONIZED - Single Counter] Average time: " + (totalTime / 10) + " ms");
    }

}
