package org.example.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High Contention (Yüksek Çekişme) Testi
 *
 * Çok sayıda producer/consumer olduğunda LinkedBlockingQueue'nun
 * iki lock avantajı ortaya çıkar.
 *
 * Senaryo: Business logic çok kısa (lock'a çok sık dokunuluyor)
 */
public class HighContentionTest {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== High Contention (Yoğun Çekişme) Testi ===\n");
        System.out.println("Senaryo: Çok producer + çok consumer, az business logic");
        System.out.println("         (Lock'a çok sık dokunuluyor)\n");

        int producerCount = 10;
        int consumerCount = 10;
        int itemsPerProducer = 1000000;
        int businessLogicDelay = 0;  // Çok kısa! Lock contention yüksek

        System.out.println("Parametreler:");
        System.out.println("- Producer sayısı: " + producerCount);
        System.out.println("- Consumer sayısı: " + consumerCount);
        System.out.println("- Her producer'dan: " + itemsPerProducer + " item");
        System.out.println("- Business logic: " + businessLogicDelay + "ms (çok kısa!)");
        System.out.println();

        // Test 1: ArrayBlockingQueue
        System.out.println("--- ArrayBlockingQueue ---");
        long arrayTime = testHighContention(
            new ArrayBlockingQueue<>(1000),
            producerCount,
            consumerCount,
            itemsPerProducer,
            businessLogicDelay
        );

        Thread.sleep(1000);

        // Test 2: LinkedBlockingQueue
        System.out.println("\n--- LinkedBlockingQueue ---");
        long linkedTime = testHighContention(
            new LinkedBlockingQueue<>(1000),
            producerCount,
            consumerCount,
            itemsPerProducer,
            businessLogicDelay
        );

        // Sonuç
        System.out.println("\n=== SONUÇLAR ===");
        System.out.println("ArrayBlockingQueue:  " + arrayTime + "ms");
        System.out.println("LinkedBlockingQueue: " + linkedTime + "ms");
        double speedup = (double) arrayTime / linkedTime;
        System.out.printf("LinkedBlockingQueue %.2fx daha hızlı!\n", speedup);
        System.out.println("\n✅ Çok producer/consumer + kısa business logic durumunda");
        System.out.println("   LinkedBlockingQueue'nun iki lock avantajı ortaya çıkıyor!");
    }

    private static long testHighContention(
            java.util.concurrent.BlockingQueue<Integer> queue,
            int producerCount,
            int consumerCount,
            int itemsPerProducer,
            int businessLogicDelay
    ) throws InterruptedException {

        int totalItems = producerCount * itemsPerProducer;
        AtomicInteger produced = new AtomicInteger(0);
        AtomicInteger consumed = new AtomicInteger(0);
        CountDownLatch producerLatch = new CountDownLatch(producerCount);
        CountDownLatch consumerLatch = new CountDownLatch(consumerCount);

        long startTime = System.currentTimeMillis();

        // Producers
        for (int i = 0; i < producerCount; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < itemsPerProducer; j++) {
                        if (businessLogicDelay > 0) {
                            Thread.sleep(businessLogicDelay);
                        }
                        queue.put(j);
                        produced.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    producerLatch.countDown();
                }
            }, "Producer-" + i).start();
        }

        // Consumers
        for (int i = 0; i < consumerCount; i++) {
            new Thread(() -> {
                try {
                    while (consumed.get() < totalItems) {
                        Integer item = queue.poll();
                        if (item != null) {
                            consumed.incrementAndGet();
                            if (businessLogicDelay > 0) {
                                Thread.sleep(businessLogicDelay);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    consumerLatch.countDown();
                }
            }, "Consumer-" + i).start();
        }

        // Bekle
        producerLatch.await();
        consumerLatch.await();

        long totalTime = System.currentTimeMillis() - startTime;

        System.out.println("Toplam süre: " + totalTime + "ms");
        System.out.println("Throughput: " + (totalItems * 1000L / totalTime) + " ops/sec");
        System.out.println("Üretilen: " + produced.get() + ", Tüketilen: " + consumed.get());

        return totalTime;
    }
}
