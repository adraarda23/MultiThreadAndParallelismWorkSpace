package org.example.synchronization;

/**
 * DEADLOCK (KİLİTLENME) ÖRNEĞI
 *
 * Bu örnek klasik bir deadlock senaryosunu gösterir:
 * - İki thread var (Thread1 ve Thread2)
 * - İki farklı kaynak/lock var (lock1 ve lock2)
 * - Thread1: lock1'i alır, sonra lock2'yi almaya çalışır
 * - Thread2: lock2'yi alır, sonra lock1'i almaya çalışır
 *
 * Sonuç: Her iki thread de birbirinin tuttuğu lock'u bekler ve hiçbiri ilerleyemez.
 * Bu duruma DEADLOCK denir.
 */
public class TESTT_Deadlock {

    // İki farklı lock nesnesi
    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();

    public static void main(String[] args) {
        System.out.println("Deadlock örneği başlatılıyor...");
        System.out.println("DİKKAT: Bu program deadlock'a girecek ve sonsuz bekleyecek!");
        System.out.println("Programı durdurmak için Ctrl+C kullanmanız gerekecek.\n");

        // Thread 1: Önce lock1, sonra lock2
        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread-1: lock1'i aldı");

                // Biraz bekle ki Thread-2 lock2'yi alabilsin
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Thread-1: lock2'yi almaya çalışıyor...");
                synchronized (lock2) {
                    System.out.println("Thread-1: lock2'yi aldı");
                }
            }
        }, "Thread-1");

        // Thread 2: Önce lock2, sonra lock1 (TERS SIRA!)
        Thread thread2 = new Thread(() -> {
            synchronized (lock2) {
                System.out.println("Thread-2: lock2'yi aldı");

                // Biraz bekle ki Thread-1 lock1'i alabilsin
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Thread-2: lock1'i almaya çalışıyor...");
                synchronized (lock1) {
                    System.out.println("Thread-2: lock1'i aldı");
                }
            }
        }, "Thread-2");

        // Her iki thread'i başlat
        thread1.start();
        thread2.start();

        // Thread'lerin bitmesini bekle (ama asla bitmeyecekler!)
        try {
            thread1.join(5000); // 5 saniye bekle
            thread2.join(5000);

            // 5 saniye sonra hala çalışıyorlarsa deadlock olmuştur
            if (thread1.isAlive() || thread2.isAlive()) {
                System.out.println("\n⚠️  DEADLOCK TESPİT EDİLDİ!");
                System.out.println("Thread-1 durumu: " + thread1.getState());
                System.out.println("Thread-2 durumu: " + thread2.getState());
                System.out.println("\nHer iki thread de BLOCKED durumunda ve birbirini bekliyor.");
                System.exit(0);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * DEADLOCK'TAN NASIL KAÇINILIR?
 *
 * 1. Lock Sıralama (Lock Ordering):
 *    - Tüm thread'ler lock'ları aynı sırada almalı
 *    - Örnek: Her zaman önce lock1, sonra lock2
 *
 * 2. Lock Timeout:
 *    - Lock alırken zaman aşımı kullan
 *    - tryLock(timeout) ile belirli süre dene, almazsan bırak
 *
 * 3. Deadlock Detection:
 *    - Thread dump'ları ile deadlock tespit et
 *    - jstack komutu kullanılabilir
 *
 * 4. Lock-Free Algoritmalar:
 *    - Mümkünse lock kullanmadan concurrent veri yapıları kullan
 *    - AtomicInteger, ConcurrentHashMap gibi
 */
