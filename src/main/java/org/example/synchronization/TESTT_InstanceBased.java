package org.example.synchronization;

public class TESTT_InstanceBased {

    // Instance-based synchronization için Counter sınıfı
    static class Counter {
        private int count = 0;

        // synchronized instance method → 'this' objesini kilitler
        public synchronized void increment() {
            int temp = count;
            count = temp + 1;
        }

        public int getCount() {
            return count;
        }
    }

    public static void main(String[] args) throws InterruptedException {

        long totalTime = 0;

        for(int run = 1; run <= 10; run++){
            // AYNI Counter objesini her iki thread de kullanacak (shared state!)
            Counter sharedCounter = new Counter();

            long startTime = System.nanoTime();

            // Thread 1 sharedCounter objesini kullanıyor
            Thread thread1 = new Thread(() -> {
                for(int i = 0; i < 100000000; i++){
                    sharedCounter.increment();  // AYNI objeye erişim
                }
            });

            // Thread 2 AYNI sharedCounter objesini kullanıyor
            Thread thread2 = new Thread(() -> {
                for(int i = 0; i < 100000000; i++){
                    sharedCounter.increment();  // AYNI objeye erişim - synchronized GEREKLI!
                }
            });

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;
            totalTime += duration;

            System.out.println("Run #" + run + " - count = " + sharedCounter.getCount() +
                             " (Expected: 200000000) - Time: " + duration + " ms");
        }

        System.out.println("\n[INSTANCE BASED - SHARED COUNTER] Average time: " + (totalTime / 10) + " ms");
        System.out.println("(Her iki thread de AYNI Counter instance'ını kullandı → synchronized GEREKLİ!)");
    }

}
