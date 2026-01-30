package org.example.basics;

public class TESTT_MultipleCounters {

    private static int counter1 = 0;
    private static int counter2 = 0;

    // Her counter için ayrı synchronized method
    private synchronized static void incrementCounter1(){
        int temp = counter1;
        counter1 = temp + 1;
    }

    private synchronized static void incrementCounter2(){
        int temp = counter2;
        counter2 = temp + 1;
    }

    public static void main(String[] args) throws InterruptedException {

        long totalTime = 0;

        for(int run = 1; run <= 10; run++){
            counter1 = 0;
            counter2 = 0;

            long startTime = System.nanoTime();

            // Thread 1 sadece counter1'i güncelliyor
            Thread thread1 = new Thread(() -> {
                for(int i = 0; i < 100000000; i++){
                    incrementCounter1();
                }
            });

            // Thread 2 sadece counter2'yi güncelliyor
            Thread thread2 = new Thread(() -> {
                for(int i = 0; i < 100000000; i++){
                    incrementCounter2();
                }
            });


            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;
            totalTime += duration;

            int total = counter1 + counter2;
            System.out.println("Run #" + run + " - counter1 = " + counter1 + ", counter2 = " + counter2 +
                             ", total = " + total + " (Expected: 200000) - Time: " + duration + " ms");
        }

        System.out.println("\n[SYNCHRONIZED - Multiple Counters] Average time: " + (totalTime / 10) + " ms");
        System.out.println("(Her thread farklı counter güncelledi → lock contention YOK)");
    }

}
