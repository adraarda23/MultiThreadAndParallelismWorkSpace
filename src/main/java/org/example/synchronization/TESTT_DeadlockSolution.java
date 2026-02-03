package org.example.synchronization;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

/**
 * DEADLOCK ÇÖZÜMÜ ÖRNEKLERİ
 *
 * Bu dosya deadlock'tan kaçınmanın farklı yollarını gösterir:
 * 1. Lock Sıralama (Lock Ordering)
 * 2. Lock Timeout (tryLock)
 * 3. ReentrantLock kullanımı
 */
public class TESTT_DeadlockSolution {

    // ÇÖZÜM 1: LOCK SIRALAMA (LOCK ORDERING)
    // =======================================
    static class LockOrderingSolution {
        private static final Object lock1 = new Object();
        private static final Object lock2 = new Object();

        public static void demonstrate() {
            System.out.println("=== ÇÖZÜM 1: Lock Sıralama ===");
            System.out.println("Her iki thread de lock'ları aynı sırada alıyor\n");

            // Thread 1: lock1, sonra lock2
            Thread thread1 = new Thread(() -> {
                synchronized (lock1) {
                    System.out.println("Thread-1: lock1'i aldı");
                    sleep(100);

                    System.out.println("Thread-1: lock2'yi almaya çalışıyor...");
                    synchronized (lock2) {
                        System.out.println("Thread-1: lock2'yi aldı - İşlem tamamlandı!");
                    }
                }
            }, "Thread-1");

            // Thread 2: AYNI SIRA - lock1, sonra lock2
            Thread thread2 = new Thread(() -> {
                synchronized (lock1) {
                    System.out.println("Thread-2: lock1'i aldı");
                    sleep(100);

                    System.out.println("Thread-2: lock2'yi almaya çalışıyor...");
                    synchronized (lock2) {
                        System.out.println("Thread-2: lock2'yi aldı - İşlem tamamlandı!");
                    }
                }
            }, "Thread-2");

            thread1.start();
            thread2.start();

            try {
                thread1.join();
                thread2.join();
                System.out.println("✓ Her iki thread de başarıyla tamamlandı!\n");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // ÇÖZÜM 2: LOCK TIMEOUT (tryLock)
    // ================================
    static class LockTimeoutSolution {
        private static final Lock lock1 = new ReentrantLock();
        private static final Lock lock2 = new ReentrantLock();

        public static void demonstrate() {
            System.out.println("=== ÇÖZÜM 2: Lock Timeout (tryLock) ===");
            System.out.println("Thread'ler lock alırken timeout kullanıyor\n");

            Thread thread1 = new Thread(() -> {
                boolean success = false;
                int attempts = 0;

                while (!success && attempts < 5) {
                    attempts++;
                    try {
                        // Lock1'i almaya çalış (1 saniye timeout)
                        if (lock1.tryLock(1, TimeUnit.SECONDS)) {
                            try {
                                System.out.println("Thread-1: lock1'i aldı (deneme " + attempts + ")");
                                sleep(100);

                                // Lock2'yi almaya çalış (1 saniye timeout)
                                if (lock2.tryLock(1, TimeUnit.SECONDS)) {
                                    try {
                                        System.out.println("Thread-1: lock2'yi aldı - İşlem tamamlandı!");
                                        success = true;
                                    } finally {
                                        lock2.unlock();
                                    }
                                } else {
                                    System.out.println("Thread-1: lock2 alınamadı, tekrar deneniyor...");
                                }
                            } finally {
                                lock1.unlock();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!success) {
                        sleep(50); // Kısa bir süre bekle ve tekrar dene
                    }
                }
            }, "Thread-1");

            Thread thread2 = new Thread(() -> {
                boolean success = false;
                int attempts = 0;

                while (!success && attempts < 5) {
                    attempts++;
                    try {
                        // Lock2'yi almaya çalış (1 saniye timeout)
                        if (lock2.tryLock(1, TimeUnit.SECONDS)) {
                            try {
                                System.out.println("Thread-2: lock2'yi aldı (deneme " + attempts + ")");
                                sleep(100);

                                // Lock1'i almaya çalış (1 saniye timeout)
                                if (lock1.tryLock(1, TimeUnit.SECONDS)) {
                                    try {
                                        System.out.println("Thread-2: lock1'i aldı - İşlem tamamlandı!");
                                        success = true;
                                    } finally {
                                        lock1.unlock();
                                    }
                                } else {
                                    System.out.println("Thread-2: lock1 alınamadı, tekrar deneniyor...");
                                }
                            } finally {
                                lock2.unlock();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!success) {
                        sleep(50);
                    }
                }
            }, "Thread-2");

            thread1.start();
            thread2.start();

            try {
                thread1.join();
                thread2.join();
                System.out.println("✓ Her iki thread de başarıyla tamamlandı!\n");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // ÇÖZÜM 3: LOCK ID SIRALAMA
    // ==========================
    static class LockIdOrderingSolution {
        static class Resource {
            private final int id;
            private final String name;

            Resource(int id, String name) {
                this.id = id;
                this.name = name;
            }

            int getId() { return id; }
            String getName() { return name; }
        }

        private static final Resource resource1 = new Resource(1, "Resource-1");
        private static final Resource resource2 = new Resource(2, "Resource-2");

        // Lock'ları ID sırasına göre al
        private static void acquireLocks(Resource r1, Resource r2, String threadName) {
            Resource first = r1.getId() < r2.getId() ? r1 : r2;
            Resource second = r1.getId() < r2.getId() ? r2 : r1;

            synchronized (first) {
                System.out.println(threadName + ": " + first.getName() + " aldı");
                sleep(100);

                System.out.println(threadName + ": " + second.getName() + " almaya çalışıyor...");
                synchronized (second) {
                    System.out.println(threadName + ": " + second.getName() + " aldı - İşlem tamamlandı!");
                }
            }
        }

        public static void demonstrate() {
            System.out.println("=== ÇÖZÜM 3: Lock ID Sıralama ===");
            System.out.println("Lock'lar her zaman küçük ID'den büyük ID'ye doğru alınıyor\n");

            Thread thread1 = new Thread(() -> {
                acquireLocks(resource1, resource2, "Thread-1");
            }, "Thread-1");

            Thread thread2 = new Thread(() -> {
                acquireLocks(resource2, resource1, "Thread-2"); // Ters sıra veriyoruz ama metot düzeltiyor!
            }, "Thread-2");

            thread1.start();
            thread2.start();

            try {
                thread1.join();
                thread2.join();
                System.out.println("✓ Her iki thread de başarıyla tamamlandı!\n");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Yardımcı metot
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("DEADLOCK ÇÖZÜM ÖRNEKLERİ\n");

        // Çözüm 1: Lock Sıralama
        LockOrderingSolution.demonstrate();
        sleep(500);

        // Çözüm 2: Lock Timeout
        LockTimeoutSolution.demonstrate();
        sleep(500);

        // Çözüm 3: Lock ID Sıralama
        LockIdOrderingSolution.demonstrate();

        System.out.println("===========================================");
        System.out.println("Tüm çözümler başarıyla test edildi!");
        System.out.println("===========================================");
    }
}
