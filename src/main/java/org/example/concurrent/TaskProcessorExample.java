package org.example.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.Random;

/**
 * Gerçek Dünya Örneği: Task Processing System
 *
 * Senaryo: Web sitesinden gelen istekleri işleyen bir sistem
 * - Kuyruk doluysa yeni istekler bekler
 * - Birden fazla worker thread paralel işler
 * - Rate limiting sağlar (kuyruk boyutu ile)
 */
public class TaskProcessorExample {

    static class Task {
        private final int id;
        private final String type;
        private final int processingTime;

        public Task(int id, String type, int processingTime) {
            this.id = id;
            this.type = type;
            this.processingTime = processingTime;
        }

        public void process() throws InterruptedException {
            System.out.println("    ⚙️  [" + Thread.currentThread().getName() + "] " +
                             "İşleniyor: Task-" + id + " (" + type + ")");
            Thread.sleep(processingTime);
            System.out.println("    ✅ [" + Thread.currentThread().getName() + "] " +
                             "Tamamlandı: Task-" + id);
        }

        @Override
        public String toString() {
            return "Task-" + id + "(" + type + ")";
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Maksimum 10 task bekleyebilir (rate limiting)
        ArrayBlockingQueue<Task> taskQueue = new ArrayBlockingQueue<>(10);
        Random random = new Random();

        System.out.println("=== Task Processing System Başlatılıyor ===");
        System.out.println("📦 Kuyruk Kapasitesi: 10 task");
        System.out.println("👷 Worker Sayısı: 3\n");

        // 3 Worker Thread - Task'leri işleyecek
        for (int i = 1; i <= 3; i++) {
            final int workerId = i;
            new Thread(() -> {
                System.out.println("👷 Worker-" + workerId + " başladı");
                try {
                    while (true) {
                        Task task = taskQueue.take();  // Kuyruk boşsa bekle
                        task.process();
                    }
                } catch (InterruptedException e) {
                    System.out.println("👷 Worker-" + workerId + " durduruluyor...");
                }
            }, "Worker-" + i).start();
        }

        Thread.sleep(500);  // Worker'ların başlamasını bekle

        // Producer Thread - Yeni task'ler oluşturuyor
        Thread producer = new Thread(() -> {
            String[] taskTypes = {"EMAIL", "SMS", "NOTIFICATION", "REPORT"};
            try {
                for (int i = 1; i <= 25; i++) {
                    String type = taskTypes[random.nextInt(taskTypes.length)];
                    int processingTime = 500 + random.nextInt(1000);
                    Task task = new Task(i, type, processingTime);

                    System.out.println("📥 [PRODUCER] Yeni task: " + task +
                                     " (Kuyruk: " + taskQueue.size() + "/10)");

                    if (taskQueue.size() >= 8) {
                        System.out.println("⚠️  [PRODUCER] Kuyruk dolmak üzere! Yavaşlıyorum...");
                    }

                    taskQueue.put(task);  // Kuyruk doluysa bekle
                    Thread.sleep(200);  // Task'ler arasında kısa bekleme
                }
                System.out.println("\n📥 [PRODUCER] Tüm task'ler gönderildi!");
            } catch (InterruptedException e) {
                System.out.println("📥 [PRODUCER] Durduruluyor...");
            }
        }, "Producer");

        producer.start();
        producer.join();  // Producer bitene kadar bekle

        // Worker'ların kalan task'leri tamamlamasını bekle
        System.out.println("\n⏳ Kuyruktaki kalan task'ler işleniyor...");
        while (!taskQueue.isEmpty()) {
            Thread.sleep(500);
            System.out.println("📊 Kalan task sayısı: " + taskQueue.size());
        }

        Thread.sleep(2000);  // Son task'lerin bitmesini bekle
        System.out.println("\n✅ Tüm task'ler tamamlandı!");
        System.exit(0);
    }
}
