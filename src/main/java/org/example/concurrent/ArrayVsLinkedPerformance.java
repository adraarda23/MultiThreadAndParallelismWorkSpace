package org.example.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ArrayBlockingQueue vs LinkedBlockingQueue Performans Karşılaştırması
 *
 * Gösterir ki:
 * - ArrayBlockingQueue'da producer ve consumer PARALEL çalışır
 * - Lock sadece queue işlemleri sırasında tutulur (çok kısa süre)
 * - Business logic paralel çalışır
 */
public class ArrayVsLinkedPerformance {

    static class Task {
        final int id;
        final long createdAt;

        Task(int id) {
            this.id = id;
            this.createdAt = System.nanoTime();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Producer-Consumer Paralellik Testi ===\n");

        int taskCount = 1000;
        int producerDelay = 10;  // ms - item üretme süresi (business logic)
        int consumerDelay = 10;  // ms - item işleme süresi (business logic)

        System.out.println("Test Parametreleri:");
        System.out.println("- Task sayısı: " + taskCount);
        System.out.println("- Producer delay: " + producerDelay + "ms (item üretme)");
        System.out.println("- Consumer delay: " + consumerDelay + "ms (item işleme)");
        System.out.println();

        // Test 1: ArrayBlockingQueue
        System.out.println("--- Test 1: ArrayBlockingQueue ---");
        long arrayTime = testQueue(new ArrayBlockingQueue<>(100), taskCount, producerDelay, consumerDelay);

        Thread.sleep(1000);

        // Test 2: LinkedBlockingQueue
        System.out.println("\n--- Test 2: LinkedBlockingQueue ---");
        long linkedTime = testQueue(new LinkedBlockingQueue<>(100), taskCount, producerDelay, consumerDelay);

        // Karşılaştırma
        System.out.println("\n=== SONUÇLAR ===");
        System.out.println("ArrayBlockingQueue:  " + arrayTime + "ms");
        System.out.println("LinkedBlockingQueue: " + linkedTime + "ms");
        double diff = ((double) arrayTime / linkedTime - 1) * 100;
        System.out.printf("Fark: %.1f%%\n", Math.abs(diff));

        if (Math.abs(diff) < 15) {
            System.out.println("\n✅ Her ikisi de neredeyse aynı hızda!");
            System.out.println("   Çünkü: Business logic süresi >> Lock süresi");
            System.out.println("   Producer ve consumer paralel çalışıyor!");
        }
    }

    private static long testQueue(
            java.util.concurrent.BlockingQueue<Task> queue,
            int taskCount,
            int producerDelay,
            int consumerDelay
    ) throws InterruptedException {

        AtomicInteger produced = new AtomicInteger(0);
        AtomicInteger consumed = new AtomicInteger(0);
        AtomicInteger parallelCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= taskCount; i++) {
                    // BUSINESS LOGIC - Lock dışında! Paralel çalışabilir
                    Thread.sleep(producerDelay);
                    Task task = new Task(i);

                    // Queue işlemi - Lock tutulur (çok kısa süre)
                    queue.put(task);
                    produced.incrementAndGet();

                    // Paralel çalışma kontrolü
                    if (consumed.get() > 0 && consumed.get() < taskCount) {
                        parallelCount.incrementAndGet();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= taskCount; i++) {
                    // Queue işlemi - Lock tutulur (çok kısa süre)
                    Task task = queue.take();
                    consumed.incrementAndGet();

                    // BUSINESS LOGIC - Lock dışında! Paralel çalışabilir
                    Thread.sleep(consumerDelay);

                    long latency = (System.nanoTime() - task.createdAt) / 1_000_000;
                    if (i == 1 || i == taskCount / 2 || i == taskCount) {
                        System.out.println("  Task-" + task.id + " işlendi (latency: " + latency + "ms)");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();

        long totalTime = System.currentTimeMillis() - startTime;

        System.out.println("  Toplam süre: " + totalTime + "ms");
        System.out.println("  Paralel çalışma tespit sayısı: " + parallelCount.get() + "/" + taskCount);
        System.out.println("  Paralellik oranı: " + (parallelCount.get() * 100 / taskCount) + "%");

        return totalTime;
    }
}
