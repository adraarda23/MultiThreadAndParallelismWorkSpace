package org.example.synchronization;

import java.util.LinkedList;
import java.util.Queue;

/**
 * PRODUCER-CONSUMER PATTERN - GERÇEKÇİ ÖRNEK
 *
 * Bu örnek, wait() ve notify() kullanarak klasik Producer-Consumer
 * problemini çözer. Sınırlı boyutlu bir buffer (bounded buffer) kullanır.
 *
 * SENARYO:
 * - Buffer'ın maksimum kapasitesi var (örn: 5 eleman)
 * - Producer: Veri üretip buffer'a ekler
 * - Consumer: Buffer'dan veri alıp işler
 * - Buffer doluysa: Producer bekler
 * - Buffer boşsa: Consumer bekler
 */
public class TESTT_ProducerConsumer {

    /**
     * Bounded Buffer (Sınırlı Boyutlu Buffer)
     */
    static class BoundedBuffer {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int capacity;

        public BoundedBuffer(int capacity) {
            this.capacity = capacity;
        }

        /**
         * Producer: Buffer'a veri ekle
         */
        public synchronized void produce(int value) throws InterruptedException {
            // Buffer dolu mu kontrol et
            while (queue.size() == capacity) {
                System.out.println(Thread.currentThread().getName()
                        + ": Buffer DOLU (" + queue.size() + "/" + capacity + "), bekliyorum...");
                wait(); // Lock'u bırak ve bekle
            }

            // Buffer'a veri ekle
            queue.add(value);
            System.out.println(Thread.currentThread().getName()
                    + ": Ürettim: " + value + " | Buffer: " + queue.size() + "/" + capacity);

            // Consumer'ları uyandır (buffer boş değil artık)
            notifyAll();
        }

        /**
         * Consumer: Buffer'dan veri al
         */
        public synchronized int consume() throws InterruptedException {
            // Buffer boş mu kontrol et
            while (queue.isEmpty()) {
                System.out.println(Thread.currentThread().getName()
                        + ": Buffer BOŞ, bekliyorum...");
                wait(); // Lock'u bırak ve bekle
            }

            // Buffer'dan veri al
            int value = queue.poll();
            System.out.println(Thread.currentThread().getName()
                    + ": Tükettim: " + value + " | Buffer: " + queue.size() + "/" + capacity);

            // Producer'ları uyandır (buffer dolu değil artık)
            notifyAll();

            return value;
        }

        public synchronized int size() {
            return queue.size();
        }
    }

    /**
     * Producer Thread
     */
    static class Producer implements Runnable {
        private final BoundedBuffer buffer;
        private final int itemCount;

        public Producer(BoundedBuffer buffer, int itemCount) {
            this.buffer = buffer;
            this.itemCount = itemCount;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= itemCount; i++) {
                    buffer.produce(i);
                    // Rastgele bir süre bekle (üretim zamanı simülasyonu)
                    Thread.sleep((int) (Math.random() * 500));
                }
                System.out.println(Thread.currentThread().getName() + ": Üretim tamamlandı!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(Thread.currentThread().getName() + ": Kesildi!");
            }
        }
    }

    /**
     * Consumer Thread
     */
    static class Consumer implements Runnable {
        private final BoundedBuffer buffer;
        private final int itemCount;

        public Consumer(BoundedBuffer buffer, int itemCount) {
            this.buffer = buffer;
            this.itemCount = itemCount;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= itemCount; i++) {
                    int value = buffer.consume();
                    // Rastgele bir süre bekle (işleme zamanı simülasyonu)
                    Thread.sleep((int) (Math.random() * 700));
                }
                System.out.println(Thread.currentThread().getName() + ": Tüketim tamamlandı!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(Thread.currentThread().getName() + ": Kesildi!");
            }
        }
    }

    /**
     * ÖRNEK 1: Tek Producer - Tek Consumer
     */
    static void example1_SingleProducerConsumer() {
        System.out.println("=== ÖRNEK 1: Tek Producer - Tek Consumer ===\n");

        BoundedBuffer buffer = new BoundedBuffer(5); // Kapasite: 5

        Thread producer = new Thread(new Producer(buffer, 10), "Producer");
        Thread consumer = new Thread(new Consumer(buffer, 10), "Consumer");

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n✓ Örnek 1 tamamlandı!\n");
    }

    /**
     * ÖRNEK 2: Birden Fazla Producer ve Consumer
     */
    static void example2_MultipleProducersConsumers() {
        System.out.println("=== ÖRNEK 2: Birden Fazla Producer ve Consumer ===\n");

        BoundedBuffer buffer = new BoundedBuffer(3); // Küçük buffer

        // 2 Producer thread
        Thread producer1 = new Thread(new Producer(buffer, 5), "Producer-1");
        Thread producer2 = new Thread(new Producer(buffer, 5), "Producer-2");

        // 2 Consumer thread
        Thread consumer1 = new Thread(new Consumer(buffer, 5), "Consumer-1");
        Thread consumer2 = new Thread(new Consumer(buffer, 5), "Consumer-2");

        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();

        try {
            producer1.join();
            producer2.join();
            consumer1.join();
            consumer2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n✓ Örnek 2 tamamlandı!\n");
    }

    /**
     * ÖRNEK 3: Yavaş Consumer (Buffer dolacak)
     */
    static void example3_SlowConsumer() {
        System.out.println("=== ÖRNEK 3: Yavaş Consumer (Producer bekleyecek) ===\n");

        BoundedBuffer buffer = new BoundedBuffer(3); // Küçük buffer

        // Hızlı Producer
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    buffer.produce(i);
                    Thread.sleep(100); // Hızlı üretim
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Fast-Producer");

        // Yavaş Consumer
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    buffer.consume();
                    Thread.sleep(1000); // Yavaş tüketim
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Slow-Consumer");

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n✓ Örnek 3 tamamlandı!\n");
    }

    public static void main(String[] args) {
        System.out.println("PRODUCER-CONSUMER PATTERN ÖRNEKLERİ\n");
        System.out.println("==========================================\n");

        // Örnek 1
        example1_SingleProducerConsumer();
        sleep(1000);

        // Örnek 2
        example2_MultipleProducersConsumers();
        sleep(1000);

        // Örnek 3
        example3_SlowConsumer();

        System.out.println("==========================================");
        System.out.println("Tüm örnekler tamamlandı!");
        System.out.println("==========================================");
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * NOTLAR VE AÇIKLAMALAR
 * ======================
 *
 * 1. NEDEN notifyAll() KULLANIYORUZ?
 *    --------------------------------
 *    - Birden fazla producer VE consumer olabilir
 *    - notify() sadece bir thread uyandırır, ama yanlış thread'i uyandırabilir
 *    - Örnek: Buffer boş, bir consumer bekliyor, başka bir consumer da bekliyor
 *             Producer veri ekler ve notify() çağırır
 *             notify() bir consumer yerine başka bir producer'ı uyandırabilir!
 *    - notifyAll() tüm thread'leri uyandırır, onlar da koşulu kontrol eder
 *
 * 2. NEDEN while (condition) KULLANIYORUZ?
 *    --------------------------------------
 *    - Spurious wakeup: JVM bazen thread'leri sebepsiz uyandırabilir
 *    - Multiple waiters: Birden fazla thread bekleyebilir
 *    - Thread uyandığında koşulu tekrar kontrol etmeli
 *
 *    Örnek: Buffer boş, 2 consumer bekliyor
 *           Producer 1 veri ekler, notifyAll() çağırır
 *           Her iki consumer da uyanır
 *           İlk consumer lock'u alır, veriyi tüketir
 *           İkinci consumer lock'u aldığında buffer tekrar boş!
 *           Bu yüzden while ile tekrar kontrol şart!
 *
 * 3. PERFORMANS ÖNEMLİ İSE NE YAPMALI?
 *    -----------------------------------
 *    wait/notify yerine java.util.concurrent paketini kullanın:
 *    - ArrayBlockingQueue: Bounded buffer
 *    - LinkedBlockingQueue: Unbounded buffer
 *    - ReentrantLock + Condition: Daha esnek locking
 *
 * 4. GERÇEK HAYAT KULLANIMI
 *    -----------------------
 *    - Thread pool'lar
 *    - Message queue'lar
 *    - Event handling sistemleri
 *    - Data streaming uygulamaları
 */
