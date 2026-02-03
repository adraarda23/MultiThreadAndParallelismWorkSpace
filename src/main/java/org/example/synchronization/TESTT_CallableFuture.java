package org.example.synchronization;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * CALLABLE VE FUTURE ÖRNEKLERİ
 *
 * Runnable vs Callable:
 * - Runnable: void run() → Sonuç dönmez, exception fırlatamaz
 * - Callable: V call() → Sonuç döner, exception fırlatabilir
 *
 * Future:
 * - Asenkron bir işlemin sonucunu temsil eder
 * - get() ile sonucu alabilirsin (bloklar, bekler)
 * - cancel() ile işlemi iptal edebilirsin
 * - isDone() ile bitip bitmediğini kontrol edebilirsin
 */
public class TESTT_CallableFuture {

    /**
     * 1. RUNNABLE vs CALLABLE
     */
    static void example1_RunnableVsCallable() {
        System.out.println("=== 1. Runnable vs Callable ===\n");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Runnable: Sonuç dönmez
        executor.execute(() -> {
            System.out.println("Runnable: Sonuç dönmüyor");
        });

        // Callable: Sonuç döner
        Future<String> future = executor.submit(() -> {
            System.out.println("Callable: Hesaplama yapılıyor...");
            Thread.sleep(1000);
            return "Sonuç: 42";
        });

        try {
            String result = future.get();  // Bloklar, sonuç gelene kadar bekler
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        executor.shutdown();
        System.out.println("\n");
    }

    /**
     * 2. FUTURE.GET() - BLOKLAMA
     */
    static void example2_FutureBlocking() {
        System.out.println("=== 2. Future.get() Bloklaması ===\n");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Integer> future = executor.submit(() -> {
            System.out.println("Task başladı, 3 saniye sürecek...");
            Thread.sleep(3000);
            System.out.println("Task bitti!");
            return 100;
        });

        System.out.println("Main thread devam ediyor...");
        System.out.println("Şimdi sonucu bekliyorum (get() bloklar)...");

        try {
            Integer result = future.get();  // 3 saniye BLOKLAR!
            System.out.println("Sonuç: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        executor.shutdown();
        System.out.println("\n");
    }

    /**
     * 3. FUTURE.GET() TIMEOUT
     */
    static void example3_FutureTimeout() {
        System.out.println("=== 3. Future.get() ile Timeout ===\n");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<String> future = executor.submit(() -> {
            System.out.println("Uzun işlem başladı...");
            Thread.sleep(5000);  // 5 saniye
            return "Sonuç";
        });

        try {
            // Maksimum 2 saniye bekle
            String result = future.get(2, TimeUnit.SECONDS);
            System.out.println("Sonuç: " + result);
        } catch (TimeoutException e) {
            System.out.println("⚠️  Timeout! İşlem 2 saniyede bitmedi.");
            future.cancel(true);  // İşlemi iptal et
        } catch (Exception e) {
            e.printStackTrace();
        }

        executor.shutdownNow();
        System.out.println("\n");
    }

    /**
     * 4. FUTURE.CANCEL() - İPTAL ETME
     */
    static void example4_FutureCancel() {
        System.out.println("=== 4. Future.cancel() - İptal ===\n");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Integer> future = executor.submit(() -> {
            System.out.println("Task başladı...");
            for (int i = 1; i <= 10; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Task iptal edildi!");
                    return -1;
                }
                System.out.println("İterasyon: " + i);
                Thread.sleep(500);
            }
            return 100;
        });

        // 2 saniye bekle, sonra iptal et
        try {
            Thread.sleep(2000);
            System.out.println("\n🛑 İptal ediyorum...\n");
            future.cancel(true);  // true: interrupt et

            if (future.isCancelled()) {
                System.out.println("✓ Task iptal edildi");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        executor.shutdownNow();
        System.out.println("\n");
    }

    /**
     * 5. BİRDEN FAZLA CALLABLE - PARALEL İŞLEMLER
     */
    static void example5_MultipleCallables() {
        System.out.println("=== 5. Birden Fazla Callable - Paralel ===\n");

        ExecutorService executor = Executors.newFixedThreadPool(3);

        // 5 görev oluştur
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            tasks.add(() -> {
                System.out.println("Task-" + taskId + " başladı - Thread: "
                    + Thread.currentThread().getName());
                Thread.sleep(1000);
                int result = taskId * 10;
                System.out.println("Task-" + taskId + " bitti, sonuç: " + result);
                return result;
            });
        }

        try {
            // invokeAll(): Tüm görevleri çalıştır, hepsi bitene kadar bekle
            List<Future<Integer>> futures = executor.invokeAll(tasks);

            System.out.println("\n✓ Tüm görevler bitti, sonuçları topluyorum:\n");

            int total = 0;
            for (int i = 0; i < futures.size(); i++) {
                Integer result = futures.get(i).get();
                System.out.println("Task-" + (i + 1) + " sonucu: " + result);
                total += result;
            }

            System.out.println("\nToplam: " + total);

        } catch (Exception e) {
            e.printStackTrace();
        }

        executor.shutdown();
        System.out.println("\n");
    }

    /**
     * 6. INVOKE ANY - İLK BİTEN KAZANIR
     */
    static void example6_InvokeAny() {
        System.out.println("=== 6. invokeAny() - İlk Biten Kazanır ===\n");

        ExecutorService executor = Executors.newFixedThreadPool(3);

        List<Callable<String>> tasks = new ArrayList<>();

        // Task 1: 3 saniye
        tasks.add(() -> {
            System.out.println("Task-1 başladı (3 saniye)");
            Thread.sleep(3000);
            return "Task-1 Sonuç";
        });

        // Task 2: 1 saniye (EN HIZLI)
        tasks.add(() -> {
            System.out.println("Task-2 başladı (1 saniye) ⚡");
            Thread.sleep(1000);
            return "Task-2 Sonuç";
        });

        // Task 3: 2 saniye
        tasks.add(() -> {
            System.out.println("Task-3 başladı (2 saniye)");
            Thread.sleep(2000);
            return "Task-3 Sonuç";
        });

        try {
            // invokeAny(): İlk biten görevin sonucunu döner
            String result = executor.invokeAny(tasks);
            System.out.println("\n✓ İlk biten: " + result);

        } catch (Exception e) {
            e.printStackTrace();
        }

        executor.shutdownNow();
        System.out.println("\n");
    }

    /**
     * 7. EXCEPTION HANDLING
     */
    static void example7_ExceptionHandling() {
        System.out.println("=== 7. Exception Handling ===\n");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Integer> future = executor.submit(() -> {
            System.out.println("Task başladı...");
            Thread.sleep(1000);
            throw new RuntimeException("Bir hata oluştu!");
        });

        try {
            Integer result = future.get();  // Exception burda fırlatılır!
            System.out.println("Sonuç: " + result);
        } catch (ExecutionException e) {
            System.out.println("⚠️  Task içinde exception oluştu!");
            System.out.println("Sebep: " + e.getCause().getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        executor.shutdown();
        System.out.println("\n");
    }

    /**
     * 8. IS DONE - POLLING
     */
    static void example8_IsDone() {
        System.out.println("=== 8. isDone() - Polling ===\n");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<String> future = executor.submit(() -> {
            Thread.sleep(3000);
            return "Tamamlandı!";
        });

        // Polling: Sürekli kontrol et (kötü pratik!)
        while (!future.isDone()) {
            System.out.println("Task henüz bitmedi, bekliyor...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            String result = future.get();  // Artık bitti, hemen dönecek
            System.out.println("Sonuç: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        executor.shutdown();
        System.out.println("\n");
    }

    public static void main(String[] args) {
        System.out.println("CALLABLE VE FUTURE ÖRNEKLERİ\n");
        System.out.println("==========================================\n");

        example1_RunnableVsCallable();
        example2_FutureBlocking();
        example3_FutureTimeout();
        example4_FutureCancel();
        example5_MultipleCallables();
        example6_InvokeAny();
        example7_ExceptionHandling();
        example8_IsDone();

        System.out.println("==========================================");
        System.out.println("Tüm örnekler tamamlandı!");
        System.out.println("==========================================");
    }
}

/**
 * ÖZET TABLOSU
 * ============
 *
 * | Metod | Açıklama |
 * |-------|----------|
 * | get() | Bloklar, sonuç gelene kadar bekler |
 * | get(timeout) | Belirli süre bekler, timeout olursa TimeoutException |
 * | cancel(mayInterrupt) | İşlemi iptal et |
 * | isCancelled() | İptal edildi mi? |
 * | isDone() | Bitti mi? (başarılı, iptal veya exception) |
 * | invokeAll() | Tüm görevleri çalıştır, hepsi bitene kadar bekle |
 * | invokeAny() | İlk biten görevin sonucunu döner |
 *
 * FUTURE SINIRLAMALARI
 * ====================
 *
 * 1. get() BLOKLAR - Main thread durur
 * 2. Birden fazla Future'ı birleştiremezsin (chaining yok)
 * 3. Callback mekanizması yok
 * 4. Exception handling karmaşık
 *
 * ÇÖZÜM: CompletableFuture
 * ========================
 *
 * Future'ın tüm sorunlarını çözer:
 * - Non-blocking: thenApply(), thenAccept()
 * - Chaining: f1.thenCompose(f2)
 * - Combining: f1.thenCombine(f2)
 * - Exception handling: exceptionally()
 * - Callback: whenComplete()
 *
 * Ama önce Future'ı anlamak önemli! Temel kavramları öğrenmek için.
 */
