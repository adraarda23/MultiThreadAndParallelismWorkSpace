package org.example.concurrent;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * DelayQueue Örneği
 *
 * - Elemanlar belli bir süre sonra alınabilir hale gelir
 * - İçeride PriorityQueue kullanır (en erken expire olan başta)
 * - Thread-safe ve blocking
 *
 * Kullanım alanları:
 * - Cache expiration
 * - Scheduled tasks
 * - Session timeout
 * - Rate limiting
 */
public class DelayQueueExample {

    /**
     * Delayed interface implement eden task sınıfı
     */
    static class DelayedTask implements Delayed {
        private final String name;
        private final long startTime;  // Ne zaman alınabilir olacak (millisecond)

        public DelayedTask(String name, long delayMs) {
            this.name = name;
            this.startTime = System.currentTimeMillis() + delayMs;
        }

        /**
         * Kalan süreyi döner (negatif = süresi dolmuş)
         */
        @Override
        public long getDelay(TimeUnit unit) {
            long diff = startTime - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        /**
         * Sıralama: En erken expire olan başta
         */
        @Override
        public int compareTo(Delayed o) {
            DelayedTask other = (DelayedTask) o;
            return Long.compare(this.startTime, other.startTime);
        }

        public String getName() {
            return name;
        }

        public long getRemainingTime() {
            return startTime - System.currentTimeMillis();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== DelayQueue Örneği ===\n");

        DelayQueue<DelayedTask> queue = new DelayQueue<>();

        // Task'leri farklı delay'lerle ekle
        System.out.println("📥 Task'ler ekleniyor...\n");
        queue.put(new DelayedTask("Task-1", 5000));   // 5 saniye sonra
        System.out.println("✅ Task-1 eklendi (5 saniye delay)");

        queue.put(new DelayedTask("Task-2", 2000));   // 2 saniye sonra
        System.out.println("✅ Task-2 eklendi (2 saniye delay)");

        queue.put(new DelayedTask("Task-3", 8000));   // 8 saniye sonra
        System.out.println("✅ Task-3 eklendi (8 saniye delay)");

        queue.put(new DelayedTask("Task-4", 1000));   // 1 saniye sonra
        System.out.println("✅ Task-4 eklendi (1 saniye delay)");

        System.out.println("\n📊 Kuyruk boyutu: " + queue.size());
        System.out.println("⏳ Task'ler çıkarılıyor (süreleri dolana kadar BEKLER)...\n");

        // Consumer thread - Task'leri sıraya göre alır
        Thread consumer = new Thread(() -> {
            try {
                while (!queue.isEmpty()) {
                    System.out.println("🔍 [CONSUMER] Bir task bekleniyor...");

                    // take() süresi dolana kadar BEKLER!
                    DelayedTask task = queue.take();

                    System.out.println("✅ [CONSUMER] " + task.getName() +
                                     " alındı! (Kalan süre: 0ms)");
                    System.out.println("   📊 Kuyrukta kalan: " + queue.size() + "\n");
                }
                System.out.println("🎉 [CONSUMER] Tüm task'ler tamamlandı!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        long startTime = System.currentTimeMillis();
        consumer.start();
        consumer.join();
        long totalTime = System.currentTimeMillis() - startTime;

        System.out.println("\n⏱️  Toplam süre: " + totalTime + "ms");
        System.out.println("\n💡 Task'ler sırayla çıktı:");
        System.out.println("   1. Task-4 (1 saniye)");
        System.out.println("   2. Task-2 (2 saniye)");
        System.out.println("   3. Task-1 (5 saniye)");
        System.out.println("   4. Task-3 (8 saniye)");
        System.out.println("   → Ekleme sırasına göre DEĞİL, delay'e göre! ✅");
    }
}
