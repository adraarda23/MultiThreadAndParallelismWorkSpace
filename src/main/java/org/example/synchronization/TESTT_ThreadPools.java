package org.example.synchronization;

import java.util.concurrent.*;

/**
 * THREAD POOL ÖRNEKLERİ
 *
 * Thread Pool: Önceden oluşturulmuş thread'lerin bulunduğu havuz.
 * Her görev için yeni thread yaratmak yerine, hazır thread'leri kullanırız.
 *
 * AVANTAJLAR:
 * - Thread yaratma maliyetini azaltır
 * - Thread sayısını kontrol eder (resource sınırı)
 * - Task queue ile iş yönetimi
 */
public class TESTT_ThreadPools {

    /**
     * 1. SINGLE THREAD EXECUTOR
     * ==========================
     * Tek bir thread ile çalışır
     * Görevler SIRAYLA işlenir (FIFO - First In First Out)
     */
    static void example1_SingleThreadExecutor() {
        System.out.println("=== 1. SingleThreadExecutor ===\n");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        // 5 görev gönder
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task-" + taskId + " başladı - Thread: "
                    + Thread.currentThread().getName());
                sleep(1000);
                System.out.println("Task-" + taskId + " bitti");
            });
        }

        executor.shutdown();
        awaitTermination(executor);
        System.out.println("\n✓ Tüm görevler SIRAYLA tamamlandı (tek thread)\n");
    }

    /**
     * 2. FIXED THREAD POOL
     * =====================
     * Sabit sayıda thread (örn: 3 thread)
     * Görevler PARALEL işlenir (3 görev aynı anda)
     */
    static void example2_FixedThreadPool() {
        System.out.println("=== 2. FixedThreadPool (3 thread) ===\n");

        ExecutorService executor = Executors.newFixedThreadPool(3);

        // 6 görev gönder
        for (int i = 1; i <= 6; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task-" + taskId + " başladı - Thread: "
                    + Thread.currentThread().getName());
                sleep(2000);
                System.out.println("Task-" + taskId + " bitti");
            });
        }

        executor.shutdown();
        awaitTermination(executor);
        System.out.println("\n✓ 6 görev, 3'er 3'er paralel işlendi\n");
    }

    /**
     * 3. CACHED THREAD POOL
     * ======================
     * İhtiyaca göre thread oluşturur
     * Thread 60 saniye boşsa, yok edilir
     * Kısa süreli çok sayıda görev için uygun
     */
    static void example3_CachedThreadPool() {
        System.out.println("=== 3. CachedThreadPool ===\n");

        ExecutorService executor = Executors.newCachedThreadPool();

        // 10 görev gönder (hepsi kısa süreli)
        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task-" + taskId + " - Thread: "
                    + Thread.currentThread().getName());
                sleep(500);
            });
        }

        executor.shutdown();
        awaitTermination(executor);
        System.out.println("\n✓ İhtiyaç kadar thread oluşturuldu\n");
    }

    /**
     * 4. SCHEDULED THREAD POOL
     * =========================
     * Zamanlı görevler için
     * - schedule(): Belirli süre sonra çalıştır
     * - scheduleAtFixedRate(): Sabit aralıklarla çalıştır
     * - scheduleWithFixedDelay(): Bir önceki bitince belirli süre sonra çalıştır
     */
    static void example4_ScheduledThreadPool() {
        System.out.println("=== 4. ScheduledThreadPool ===\n");

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

        System.out.println("Başlangıç: " + System.currentTimeMillis());

        // 2 saniye sonra 1 kez çalış
        executor.schedule(() -> {
            System.out.println("2 saniye sonra çalıştı - " + System.currentTimeMillis());
        }, 2, TimeUnit.SECONDS);

        // 1 saniye sonra başla, her 1 saniyede tekrarla (3 kez)
        ScheduledFuture<?> periodic = executor.scheduleAtFixedRate(() -> {
            System.out.println("Periyodik görev - " + System.currentTimeMillis());
        }, 1, 1, TimeUnit.SECONDS);

        // 4 saniye sonra durdur
        executor.schedule(() -> {
            periodic.cancel(false);
            executor.shutdown();
        }, 4, TimeUnit.SECONDS);

        awaitTermination(executor);
        System.out.println("\n✓ Zamanlı görevler tamamlandı\n");
    }

    /**
     * 5. SUBMIT vs EXECUTE
     * =====================
     * execute(): Void, sonuç dönmez
     * submit(): Future döner, sonuç alabilirsin
     */
    static void example5_SubmitVsExecute() {
        System.out.println("=== 5. submit() vs execute() ===\n");

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // execute() - sonuç almıyoruz
        executor.execute(() -> {
            System.out.println("execute() ile çalıştı");
        });

        // submit() - Future ile sonuç alıyoruz
        Future<Integer> future = executor.submit(() -> {
            System.out.println("submit() ile çalıştı");
            sleep(1000);
            return 42;
        });

        try {
            Integer result = future.get();  // Bloklar, sonucu bekler
            System.out.println("Sonuç: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        executor.shutdown();
        awaitTermination(executor);
        System.out.println("\n");
    }

    /**
     * 6. SHUTDOWN vs SHUTDOWN NOW
     * ============================
     */
    static void example6_ShutdownTypes() {
        System.out.println("=== 6. shutdown() vs shutdownNow() ===\n");

        // shutdown() - Mevcut görevleri bitir, yeni görev kabul etme
        ExecutorService executor1 = Executors.newFixedThreadPool(2);
        executor1.submit(() -> {
            System.out.println("Görev çalışıyor...");
            sleep(2000);
            System.out.println("Görev bitti");
        });

        executor1.shutdown();  // Görev bitmesini bekler
        System.out.println("shutdown() çağrıldı, görev bitmesini bekliyor...");
        awaitTermination(executor1);
        System.out.println("✓ Executor kapandı\n");

        // shutdownNow() - Tüm görevleri kes, hemen kapat
        ExecutorService executor2 = Executors.newFixedThreadPool(2);
        executor2.submit(() -> {
            System.out.println("Görev başladı...");
            sleep(5000);
            System.out.println("Bu satır çalışmayacak");
        });

        sleep(500);
        executor2.shutdownNow();  // Hemen kes
        System.out.println("✓ shutdownNow() çağrıldı, görevler kesildi\n");
    }

    // Yardımcı metodlar
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void awaitTermination(ExecutorService executor) {
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        System.out.println("THREAD POOL ÖRNEKLERİ\n");
        System.out.println("==========================================\n");

        example1_SingleThreadExecutor();
        example2_FixedThreadPool();
        example3_CachedThreadPool();
        example4_ScheduledThreadPool();
        example5_SubmitVsExecute();
        example6_ShutdownTypes();

        System.out.println("==========================================");
        System.out.println("Tüm örnekler tamamlandı!");
        System.out.println("==========================================");
    }
}

/**
 * KARŞILAŞTIRMA TABLOSU
 * =====================
 *
 * | Executor Tipi          | Thread Sayısı      | Kullanım Amacı                    |
 * |------------------------|--------------------|-----------------------------------|
 * | SingleThreadExecutor   | 1                  | Sıralı işlemler                   |
 * | FixedThreadPool(N)     | N (sabit)          | Sabit sayıda paralel işlem        |
 * | CachedThreadPool       | İhtiyaca göre      | Çok sayıda kısa süreli görev      |
 * | ScheduledThreadPool    | N (sabit)          | Zamanlı/periyodik görevler        |
 *
 * NE ZAMAN HANGİSİ?
 * =================
 *
 * SingleThreadExecutor:
 * - Log yazma
 * - Queue işleme (sıralı)
 * - Event handling
 *
 * FixedThreadPool:
 * - CPU-intensive işler
 * - Thread sayısını sınırlamak istiyorsan
 * - Web server request handling
 *
 * CachedThreadPool:
 * - I/O-intensive işler
 * - Çok sayıda kısa süreli görev
 * - Thread sayısı değişken olabilir
 *
 * ScheduledThreadPool:
 * - Cron job benzeri işler
 * - Periyodik temizlik/backup
 * - Heartbeat/health check
 */
