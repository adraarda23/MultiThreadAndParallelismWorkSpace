package org.example.synchronization;

/**
 * WAIT() ve NOTIFY() AÇIKLAMASI
 *
 * wait() ve notify() metodları, thread'ler arası iletişim (inter-thread communication)
 * için kullanılır. Object sınıfında tanımlıdırlar, yani her obje üzerinde kullanılabilir.
 *
 * TEMEL KURALLAR:
 * ================
 * 1. wait() ve notify() SADECE synchronized block/method içinde çağrılabilir
 * 2. wait() çağrıldığında thread LOCK'U BIRAKIR ve WAITING durumuna geçer
 * 3. notify() çağrıldığında bir thread uyandırılır (hangisi belirsiz)
 * 4. notifyAll() tüm bekleyen thread'leri uyandırır
 *
 * KLASIK SENARYO: Producer-Consumer (Üretici-Tüketici)
 * ====================================================
 * - Producer: Veri üretir ve buffer'a koyar
 * - Consumer: Buffer'dan veri alır ve işler
 * - Buffer doluysa: Producer bekler (wait)
 * - Buffer boşsa: Consumer bekler (wait)
 * - Producer veri koyunca: Consumer'ı uyandırır (notify)
 * - Consumer veri alınca: Producer'ı uyandırır (notify)
 */
public class TESTT_WaitNotify {

    /**
     * ÖRNEK 1: Basit Wait-Notify Mekanizması
     */
    static class SimpleWaitNotify {
        private static final Object lock = new Object();
        private static boolean dataReady = false;

        public static void demonstrate() {
            System.out.println("=== ÖRNEK 1: Basit Wait-Notify ===\n");

            // Consumer Thread: Veri bekliyor
            Thread consumer = new Thread(() -> {
                synchronized (lock) {
                    System.out.println("Consumer: Veri bekliyorum...");

                    try {
                        // wait() çağrıldığında:
                        // 1. Thread LOCK'U BIRAKIR (başka thread girebilir)
                        // 2. WAITING durumuna geçer
                        // 3. notify() çağrılana kadar bekler
                        lock.wait();

                        System.out.println("Consumer: Veri geldi! İşliyorum...");
                        System.out.println("Consumer: Veri = " + dataReady);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Consumer");

            // Producer Thread: Veri üretiyor
            Thread producer = new Thread(() -> {
                try {
                    Thread.sleep(2000); // 2 saniye bekle (veri üretimi simüle)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (lock) {
                    System.out.println("Producer: Veriyi hazırladım!");
                    dataReady = true;

                    // notify() çağrıldığında:
                    // 1. Bekleyen bir thread uyandırılır
                    // 2. Uyandırılan thread RUNNABLE durumuna geçer
                    // 3. Lock boşalınca synchronized block'a girer
                    lock.notify();

                    System.out.println("Producer: Consumer'ı uyandırdım!");
                }
            }, "Producer");

            consumer.start();
            producer.start();

            try {
                consumer.join();
                producer.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("\n");
        }
    }

    /**
     * ÖRNEK 2: Producer-Consumer Pattern (Tek Eleman Buffer)
     */
    static class SingleElementBuffer {
        private String data = null;

        // Producer: Veri üret ve buffer'a koy
        public synchronized void produce(String value) throws InterruptedException {
            // Buffer doluysa bekle
            while (data != null) {
                System.out.println(Thread.currentThread().getName() + ": Buffer dolu, bekliyorum...");
                wait(); // Lock'u bırak ve bekle
            }

            // Buffer boş, veriyi koy
            data = value;
            System.out.println(Thread.currentThread().getName() + ": Veri ürettim: " + data);

            // Consumer'ı uyandır
            notify();
        }

        // Consumer: Buffer'dan veri al
        public synchronized String consume() throws InterruptedException {
            // Buffer boşsa bekle
            while (data == null) {
                System.out.println(Thread.currentThread().getName() + ": Buffer boş, bekliyorum...");
                wait(); // Lock'u bırak ve bekle
            }

            // Buffer dolu, veriyi al
            String value = data;
            data = null; // Buffer'ı temizle
            System.out.println(Thread.currentThread().getName() + ": Veri tükettim: " + value);

            // Producer'ı uyandır
            notify();

            return value;
        }

        public static void demonstrate() {
            System.out.println("=== ÖRNEK 2: Producer-Consumer (Tek Eleman Buffer) ===\n");

            SingleElementBuffer buffer = new SingleElementBuffer();

            // Producer thread
            Thread producer = new Thread(() -> {
                try {
                    for (int i = 1; i <= 5; i++) {
                        buffer.produce("Data-" + i);
                        Thread.sleep(500); // Yavaşlat
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Producer");

            // Consumer thread
            Thread consumer = new Thread(() -> {
                try {
                    for (int i = 1; i <= 5; i++) {
                        buffer.consume();
                        Thread.sleep(1000); // Producer'dan daha yavaş
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Consumer");

            producer.start();
            consumer.start();

            try {
                producer.join();
                consumer.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("\n");
        }
    }

    /**
     * ÖRNEK 3: notify() vs notifyAll()
     */
    static class NotifyVsNotifyAll {
        private static final Object lock = new Object();
        private static int data = 0;

        public static void demonstrateNotify() {
            System.out.println("=== ÖRNEK 3a: notify() - Sadece BİR thread uyandırılır ===\n");

            // 3 tane consumer thread
            for (int i = 1; i <= 3; i++) {
                final int id = i;
                new Thread(() -> {
                    synchronized (lock) {
                        try {
                            System.out.println("Consumer-" + id + ": Bekliyorum...");
                            lock.wait();
                            System.out.println("Consumer-" + id + ": Uyandım! Data = " + data);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, "Consumer-" + i).start();
            }

            // Thread'lerin başlamasını bekle
            sleep(1000);

            // Producer: notify() çağır (sadece 1 thread uyanır!)
            new Thread(() -> {
                synchronized (lock) {
                    data = 100;
                    System.out.println("\nProducer: notify() çağırıyorum...");
                    lock.notify(); // SADECE 1 THREAD UYANIR
                    System.out.println("Producer: Sadece 1 thread uyandırdım!\n");
                }
            }, "Producer").start();

            sleep(2000);
            System.out.println("DİKKAT: Diğer 2 thread hala bekliyor!\n\n");
        }

        public static void demonstrateNotifyAll() {
            System.out.println("=== ÖRNEK 3b: notifyAll() - TÜM thread'ler uyandırılır ===\n");

            data = 0; // Reset

            // 3 tane consumer thread
            for (int i = 1; i <= 3; i++) {
                final int id = i;
                new Thread(() -> {
                    synchronized (lock) {
                        try {
                            System.out.println("Consumer-" + id + ": Bekliyorum...");
                            lock.wait();
                            System.out.println("Consumer-" + id + ": Uyandım! Data = " + data);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, "Consumer-" + i).start();
            }

            // Thread'lerin başlamasını bekle
            sleep(1000);

            // Producer: notifyAll() çağır (TÜM thread'ler uyanır!)
            new Thread(() -> {
                synchronized (lock) {
                    data = 200;
                    System.out.println("\nProducer: notifyAll() çağırıyorum...");
                    lock.notifyAll(); // TÜM THREAD'LER UYANIR
                    System.out.println("Producer: Tüm thread'leri uyandırdım!\n");
                }
            }, "Producer").start();

            sleep(2000);
            System.out.println("✓ Tüm thread'ler uyandı ve işini yaptı!\n\n");
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
        System.out.println("WAIT() VE NOTIFY() ÖRNEKLERİ\n");
        System.out.println("==========================================\n");

        // Örnek 1: Basit wait-notify
        SimpleWaitNotify.demonstrate();
        sleep(500);

        // Örnek 2: Producer-Consumer
        SingleElementBuffer.demonstrate();
        sleep(500);

        // Örnek 3a: notify() - tek thread
        NotifyVsNotifyAll.demonstrateNotify();
        sleep(500);

        // Örnek 3b: notifyAll() - tüm thread'ler
        NotifyVsNotifyAll.demonstrateNotifyAll();

        System.out.println("==========================================");
        System.out.println("Tüm örnekler tamamlandı!");
        System.out.println("==========================================");
    }
}

/**
 * ÖNEMLİ NOKTALAR
 * ===============
 *
 * 1. NEDEN while (condition) { wait(); } KULLANILIR?
 *    - Spurious wakeup (sahte uyanma) olabilir
 *    - Birden fazla thread bekleyebilir
 *    - Thread uyandığında koşulu tekrar kontrol etmeli
 *
 *    ❌ YANLIŞ:
 *    if (data == null) {
 *        wait();
 *    }
 *
 *    ✅ DOĞRU:
 *    while (data == null) {
 *        wait();
 *    }
 *
 * 2. wait() vs sleep()
 *    - wait(): Lock'u BIRAKIR, notify() ile uyandırılır
 *    - sleep(): Lock'u TUTMAYA DEVAM EDER, belirli süre sonra uyanır
 *
 * 3. notify() vs notifyAll()
 *    - notify(): Bir thread uyandırır (hangisi belirsiz)
 *    - notifyAll(): Tüm bekleyen thread'leri uyandırır
 *    - Genellikle notifyAll() daha güvenlidir
 *
 * 4. IllegalMonitorStateException
 *    - wait()/notify() synchronized olmadan çağrılırsa bu exception fırlatılır
 *    - Thread objenin monitor'una sahip olmalı
 *
 * 5. InterruptedException
 *    - wait() sırasında thread interrupt edilirse bu exception fırlatılır
 *    - Mutlaka handle edilmeli (try-catch)
 */
