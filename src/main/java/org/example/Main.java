package org.example;

/**
 * MultiThreadAndParallelismWorkSpace - Ana Başlangıç Sınıfı
 *
 * Bu proje Java'da Multi-Threading ve Parallelism konularını öğrenmek için oluşturulmuştur.
 *
 * Farklı modüller:
 * - basics: Thread temelleri ve yaşam döngüsü
 * - synchronization: Senkronizasyon mekanizmaları
 * - executors: Executor framework ve thread pool'lar
 * - concurrent: Concurrent collections kullanımı
 * - parallelism: Parallel streams ve fork/join framework
 * - problems: Klasik concurrency problemleri ve çözümleri
 *
 * @author Arda Aydın Kılınç
 * @version 1.0
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║   Multi-Threading & Parallelism WorkSpace             ║");
        System.out.println("║   Java Concurrency Öğrenme Projesi                    ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Proje başarıyla çalışıyor!");
        System.out.println("Farklı paketlerdeki örnekleri inceleyebilirsiniz:");
        System.out.println("  • org.example.basics - Thread temelleri");
        System.out.println("  • org.example.synchronization - Senkronizasyon");
        System.out.println("  • org.example.executors - Executor framework");
        System.out.println("  • org.example.concurrent - Concurrent collections");
        System.out.println("  • org.example.parallelism - Parallel processing");
        System.out.println("  • org.example.problems - Klasik concurrency problemleri");
        System.out.println();

        // Thread bilgisi göster
        Thread currentThread = Thread.currentThread();
        System.out.println("Şu anda çalışan thread:");
        System.out.println("  → İsim: " + currentThread.getName());
        System.out.println("  → ID: " + currentThread.threadId());
        System.out.println("  → Öncelik: " + currentThread.getPriority());
        System.out.println("  → Thread Group: " + currentThread.getThreadGroup().getName());
        System.out.println("  → Aktif thread sayısı: " + Thread.activeCount());
    }
}