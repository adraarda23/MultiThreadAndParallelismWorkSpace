package org.example.concurrent;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Lock Tutma Süresi Demonstrasyonu
 *
 * Gösterir ki:
 * - Queue operasyonu çok hızlıdır (microsaniye)
 * - Business logic çok yavaştır (millisaniye)
 * - Producer ve consumer çoğu zaman paralel çalışır
 */
public class LockHoldTimeDemo {

    public static void main(String[] args) throws InterruptedException {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

        System.out.println("=== Lock Tutma Süresi Demonstrasyonu ===\n");
        System.out.println("Producer ve Consumer'ın zaman harcama analizi:\n");

        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    // 1. BUSINESS LOGIC (Lock dışında - paralel çalışabilir)
                    long businessStart = System.nanoTime();
                    System.out.println("🏭 [PRODUCER] Item-" + i + " üretiliyor...");
                    Thread.sleep(100);  // Simüle: veritabanı okuma, hesaplama vs.
                    long businessEnd = System.nanoTime();
                    long businessTime = (businessEnd - businessStart) / 1_000_000;

                    // 2. QUEUE OPERASYONU (Lock içinde - çok kısa süre)
                    long queueStart = System.nanoTime();
                    queue.put("Item-" + i);
                    long queueEnd = System.nanoTime();
                    long queueTime = (queueEnd - queueStart) / 1_000;  // microsaniye

                    System.out.println("   [PRODUCER] Item-" + i + " kuyruğa eklendi");
                    System.out.println("   ⏱️  Business logic süresi: " + businessTime + "ms");
                    System.out.println("   ⏱️  Queue işlem süresi: " + queueTime + "μs (microsaniye)");
                    System.out.println("   📊 Lock tutma oranı: " +
                                     String.format("%.4f%%", (queueTime / 1000.0) * 100 / businessTime));
                    System.out.println();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(50);  // Biraz gecikme

                for (int i = 1; i <= 5; i++) {
                    // 1. QUEUE OPERASYONU (Lock içinde - çok kısa süre)
                    long queueStart = System.nanoTime();
                    String item = queue.take();
                    long queueEnd = System.nanoTime();
                    long queueTime = (queueEnd - queueStart) / 1_000;  // microsaniye

                    System.out.println("🛒 [CONSUMER] " + item + " kuyruktan alındı");
                    System.out.println("   ⏱️  Queue işlem süresi: " + queueTime + "μs");

                    // 2. BUSINESS LOGIC (Lock dışında - paralel çalışabilir)
                    long businessStart = System.nanoTime();
                    System.out.println("   [CONSUMER] " + item + " işleniyor...");
                    Thread.sleep(150);  // Simüle: API çağrısı, veritabanı yazma vs.
                    long businessEnd = System.nanoTime();
                    long businessTime = (businessEnd - businessStart) / 1_000_000;

                    System.out.println("   [CONSUMER] " + item + " işlendi");
                    System.out.println("   ⏱️  Business logic süresi: " + businessTime + "ms");
                    System.out.println("   📊 Lock tutma oranı: " +
                                     String.format("%.4f%%", (queueTime / 1000.0) * 100 / businessTime));
                    System.out.println();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        long totalStart = System.currentTimeMillis();
        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
        long totalTime = System.currentTimeMillis() - totalStart;

        System.out.println("=== SONUÇ ===");
        System.out.println("Toplam süre: " + totalTime + "ms");
        System.out.println();
        System.out.println("✅ Producer ve Consumer çoğu zaman PARALEL çalıştı!");
        System.out.println("   - Business logic: ~100-150ms (paralel)");
        System.out.println("   - Lock tutma: ~0.001-0.1ms (çok kısa)");
        System.out.println("   - Lock tutma oranı: %0.001-%0.1");
        System.out.println();
        System.out.println("🔍 Tek lock olsa bile, lock tutma süresi o kadar kısa ki");
        System.out.println("   producer ve consumer aynı anda çalışıyor gibi görünür!");
    }
}
