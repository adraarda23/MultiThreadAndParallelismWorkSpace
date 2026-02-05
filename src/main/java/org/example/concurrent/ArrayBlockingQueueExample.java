package org.example.concurrent;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * ArrayBlockingQueue Örneği
 *
 * - Thread-safe, sabit boyutlu, bloklanabilir kuyruk
 * - FIFO (First-In-First-Out) mantığıyla çalışır
 * - Producer-Consumer pattern için idealdir
 */
public class ArrayBlockingQueueExample {

    public static void main(String[] args) {
        // Kapasite 5 olan kuyruk
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(5);

        System.out.println("=== ArrayBlockingQueue Producer-Consumer Örneği ===\n");

        // PRODUCER Thread - Ürün üreten
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 100; i++) {
                    String product = "Ürün-" + i;
                    System.out.println("🏭 [PRODUCER] Üretiliyor: " + product +
                                     " (Kuyruk boyutu: " + queue.size() + ")");
                    queue.put(product);  // Kuyruk doluysa BEKLER
                    System.out.println("✅ [PRODUCER] Kuyruğa eklendi: " + product);
                    Thread.sleep(300);  // Üretim süresi
                }
                System.out.println("\n🏭 [PRODUCER] Üretim tamamlandı!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // CONSUMER Thread - Ürün tüketen
        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(1000);  // Biraz gecikmeyle başla

                for (int i = 1; i <= 100; i++) {
                    System.out.println("🛒 [CONSUMER] Ürün bekleniyor...");
                    String product = queue.take();  // Kuyruk boşsa BEKLER
                    System.out.println("📦 [CONSUMER] Tüketiliyor: " + product +
                                     " (Kalan: " + queue.size() + ")");
                    Thread.sleep(500);  // Tüketim süresi (üretimden yavaş)
                }
                System.out.println("\n🛒 [CONSUMER] Tüketim tamamlandı!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n✅ Program tamamlandı!");
    }
}
