package org.example.synchronization;

public class TESTT {

    private static int counter = 0;

    private static void increment(){
        // Race condition'ı daha belirgin yapmak için
        // okuma-yazma işlemlerini ayırıyoruz
        int temp = counter;
        counter = temp + 1;
    };

    public static void main(String[] args) throws InterruptedException {

        long totalTime = 0;

        // Birden fazla kez çalıştırıp farklı sonuçlar görelim
        for(int run = 1; run <= 10; run++){
            counter = 0; // Her run için sıfırlıyoruz

            long startTime = System.nanoTime();

            Thread thread1 = new Thread(() -> {
                for(int i = 0; i < 100000000; i++){ // 1000 → 100000
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
            long duration = (endTime - startTime) / 1_000_000; // ms cinsinden
            totalTime += duration;

            System.out.println("Run #" + run + " - counter = " + counter + " (Expected: 200000) - Time: " + duration + " ms");
        }

        System.out.println("\n[UNSYNCHRONIZED] Average time: " + (totalTime / 10) + " ms");
    }

}
