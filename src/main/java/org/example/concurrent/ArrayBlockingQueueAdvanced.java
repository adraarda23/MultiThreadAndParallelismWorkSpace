package org.example.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * ArrayBlockingQueue İleri Seviye Örnek
 *
 * - Blocking vs Non-blocking operasyonlar
 * - Timeout ile bekleme
 * - Fair vs Unfair mode
 */
public class ArrayBlockingQueueAdvanced {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1. Blocking vs Non-Blocking Operasyonlar ===\n");
        blockingVsNonBlocking();

        Thread.sleep(2000);
        System.out.println("\n\n=== 2. Timeout ile Bekleme ===\n");
        timeoutExample();

        Thread.sleep(2000);
        System.out.println("\n\n=== 3. Fair vs Unfair Mode ===\n");
        fairVsUnfair();
    }

    /**
     * put/take (blocking) vs offer/poll (non-blocking)
     */
    private static void blockingVsNonBlocking() throws InterruptedException {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(2);

        // Kuyruğu doldur
        queue.put("Item-1");
        queue.put("Item-2");
        System.out.println("✅ Kuyruk dolu: " + queue);

        // OFFER - Yer yoksa false döner, BEKLEMEZ
        boolean added = queue.offer("Item-3");
        System.out.println("❌ offer('Item-3') sonucu: " + added + " (kuyruk dolu)");

        // POLL - Boşsa null döner, BEKLEMEZ
        String item = queue.poll();
        System.out.println("✅ poll() sonucu: " + item);

        // Şimdi yer açıldı, offer başarılı olur
        added = queue.offer("Item-3");
        System.out.println("✅ offer('Item-3') sonucu: " + added + " (yer var)");

        System.out.println("📋 Son durum: " + queue);
    }

    /**
     * Timeout ile bekleme örneği
     */
    private static void timeoutExample() throws InterruptedException {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
        queue.put("Existing-Item");

        System.out.println("📋 Kuyruk: " + queue);

        // 2 saniye bekle, eklemeyi dene
        System.out.println("⏳ 2 saniye bekleyerek eklemeye çalışıyorum...");
        boolean added = queue.offer("New-Item", 2, TimeUnit.SECONDS);
        System.out.println("❌ Sonuç: " + added + " (zaman aşımı)");

        // Başka thread'de eleman çıkar
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                String removed = queue.take();
                System.out.println("🗑️  [Yardımcı Thread] Çıkarıldı: " + removed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // Tekrar dene - bu sefer başarılı olacak
        System.out.println("⏳ 3 saniye bekleyerek eklemeye çalışıyorum...");
        added = queue.offer("New-Item", 3, TimeUnit.SECONDS);
        System.out.println("✅ Sonuç: " + added + " (1 saniye sonra yer açıldı)");
        System.out.println("📋 Son durum: " + queue);
    }

    /**
     * Fair vs Unfair mode karşılaştırması
     */
    private static void fairVsUnfair() throws InterruptedException {
        System.out.println("--- UNFAIR MODE (Default) ---");
        testFairness(new ArrayBlockingQueue<>(1, false));

        Thread.sleep(1000);

        System.out.println("\n--- FAIR MODE ---");
        testFairness(new ArrayBlockingQueue<>(1, true));
    }

    private static void testFairness(ArrayBlockingQueue<String> queue) throws InterruptedException {
        // 5 thread oluştur, hepsi kuyruğa eleman eklemeye çalışsın
        for (int i = 1; i <= 5; i++) {
            final int threadNum = i;
            new Thread(() -> {
                try {
                    for (int j = 0; j < 2; j++) {
                        queue.put("T" + threadNum + "-Item" + j);
                        System.out.println("Thread-" + threadNum + " ekledi");
                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Thread-" + i).start();
        }

        Thread.sleep(50);  // Thread'lerin başlamasını bekle

        // 10 eleman çıkar
        for (int i = 0; i < 10; i++) {
            String item = queue.take();
            System.out.println("  ← Çıkarıldı: " + item);
            Thread.sleep(20);
        }
    }
}
