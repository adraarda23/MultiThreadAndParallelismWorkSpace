# Java.util.concurrent Paketi - Kapsamlı Türkçe Dokümantasyon

## İçindekiler
1. [Paket Özeti](#paket-özeti)
2. [Executor Framework](#executor-framework)
3. [Kuyruk Koleksiyonları](#kuyruk-koleksiyonları)
4. [Senkronizasyon Araçları](#senkronizasyon-araçları)
5. [Eşzamanlı Koleksiyonlar](#eşzamanlı-koleksiyonlar)
6. [Atomik Değişkenler](#atomik-değişkenler)
7. [Locks (Kilitler)](#locks-kilitler)
8. [Yardımcı Sınıflar](#yardımcı-sınıflar)
9. [İstisnalar](#istisnalar)
10. [Bellek Tutarlılığı](#bellek-tutarlılığı)

---

## Paket Özeti

`java.util.concurrent` paketi, **eşzamanlı programlamada yaygın olarak kullanılan yardımcı sınıfları** sağlar. Bu paket, thread yönetimi, görev çalıştırma ve senkronizasyon için standartlaştırılmış framework'ler ve implementasyonlar içerir.

### Paketin Temel Hedefleri:
- Thread-safe veri yapıları sağlamak
- Yüksek performanslı eşzamanlı işlemler desteklemek
- Thread yönetimini ve görev çalıştırmayı basitleştirmek
- Yaygın senkronizasyon desenlerini standartlaştırmak

---

## Executor Framework

Executor Framework, thread yaratma ve yönetimini soyutlayan güçlü bir yapıdır. Manuel thread yönetimi yerine görev tabanlı bir yaklaşım sunar.

### Temel Arayüzler

#### 1. Executor
En temel arayüzdür. Görevlerin nasıl çalıştırılacağını tanımlar.

```java
public interface Executor {
    void execute(Runnable command);
}
```

**Kullanım Amacı:** Özel thread alt sistemleri tanımlamak için temel bir yapı.

#### 2. ExecutorService
`Executor`'ı genişletir ve görev yönetimi, kuyruklama, zamanlama ve kontrollü kapanma özellikleri ekler.

**Temel Metodlar:**
- `submit(Callable<T> task)`: Sonuç döndüren görev gönderir
- `submit(Runnable task)`: Sonuç döndürmeyen görev gönderir
- `invokeAll(Collection<Callable<T>> tasks)`: Birden fazla görevi çalıştırır
- `invokeAny(Collection<Callable<T>> tasks)`: İlk tamamlanan görevin sonucunu döndürür
- `shutdown()`: Yeni görev kabul etmeyi durdurur
- `shutdownNow()`: Çalışan görevleri durdurmaya çalışır

#### 3. ScheduledExecutorService
Gecikmeli ve periyodik görev çalıştırma desteği ekler.

**Temel Metodlar:**
- `schedule(Callable<V> callable, long delay, TimeUnit unit)`: Belirtilen gecikmeden sonra çalışır
- `scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)`: Sabit oranlı periyodik çalışma
- `scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)`: Sabit gecikmeli periyodik çalışma

#### 4. Callable<V>
`Runnable`'a alternatif olarak sonuç döndürebilen ve istisna fırlatabililen görev arayüzü.

```java
public interface Callable<V> {
    V call() throws Exception;
}
```

#### 5. Future<V>
Asenkron hesaplamaların sonuçlarını temsil eder ve iptal işlemleri sağlar.

**Temel Metodlar:**
- `get()`: Sonuç hazır olana kadar bloklar
- `get(long timeout, TimeUnit unit)`: Zamanaşımı ile sonuç bekler
- `cancel(boolean mayInterruptIfRunning)`: Görevi iptal eder
- `isDone()`: Görevin tamamlanıp tamamlanmadığını kontrol eder
- `isCancelled()`: Görevin iptal edilip edilmediğini kontrol eder

### İmplementasyonlar

#### 1. ThreadPoolExecutor
Ayarlanabilir, esnek thread pool'ları sağlar.

**Temel Parametreler:**
- `corePoolSize`: Havuzda tutulacak minimum thread sayısı
- `maximumPoolSize`: Havuzdaki maksimum thread sayısı
- `keepAliveTime`: Boşta kalan threadlerin ne kadar süre hayatta kalacağı
- `workQueue`: Görevlerin saklandığı kuyruk
- `threadFactory`: Yeni threadler oluşturmak için factory
- `rejectedExecutionHandler`: Görev reddedildiğinde yapılacak işlem

**Örnek Kullanım:**
```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    5,                      // corePoolSize
    10,                     // maximumPoolSize
    60L,                    // keepAliveTime
    TimeUnit.SECONDS,       // time unit
    new LinkedBlockingQueue<Runnable>()
);
```

#### 2. ScheduledThreadPoolExecutor
Gecikmeli ve periyodik görevler için zamanlanmış thread pool'u.

#### 3. ForkJoinPool
`ForkJoinTask` örneklerini işlemek için tasarlanmış, **work-stealing** zamanlama algoritması kullanan özel bir executor.

**Kullanım Alanları:**
- Büyük görevleri küçük alt görevlere bölebilen (divide-and-conquer) algoritmalar
- Paralel işleme gerektiren operasyonlar

**Örnek:**
```java
ForkJoinPool pool = new ForkJoinPool();
RecursiveTask<Integer> task = new MyRecursiveTask(data);
Integer result = pool.invoke(task);
```

#### 4. Executors (Factory Sınıfı)
Yaygın executor konfigürasyonları için fabrika metodları sağlar.

**Popüler Metodlar:**
- `newFixedThreadPool(int nThreads)`: Sabit sayıda thread içeren pool
- `newCachedThreadPool()`: Gerektiğinde thread oluşturan, boştakileri yeniden kullanan pool
- `newSingleThreadExecutor()`: Tek thread ile çalışan executor
- `newScheduledThreadPool(int corePoolSize)`: Zamanlanmış görevler için pool
- `newWorkStealingPool()`: Paralel işleme için work-stealing pool

---

## Kuyruk Koleksiyonları

### 1. ConcurrentLinkedQueue<E>
Verimli, ölçeklenebilir, thread-safe, **bloklamayan FIFO kuyruğu**.

**Özellikler:**
- Lock-free algoritma kullanır
- Sınırsız kapasiteye sahiptir
- CAS (Compare-And-Swap) operasyonları ile çalışır

**Örnek:**
```java
ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
queue.offer("Görev 1");
String task = queue.poll();
```

### 2. ConcurrentLinkedDeque<E>
`ConcurrentLinkedQueue`'ya benzer ancak Deque (çift uçlu kuyruk) operasyonlarını destekler.

### 3. BlockingQueue Implementasyonları

Blocking queue'lar, kuyruk dolu olduğunda ekleme ve kuyruk boş olduğunda çıkarma işlemlerinde bloklanabilen özel kuyruklardır.

#### LinkedBlockingQueue<E>
- İsteğe bağlı sınırlı kapasiteli bağlı liste tabanlı blocking queue
- Varsayılan olarak `Integer.MAX_VALUE` kapasitelidir

```java
BlockingQueue<String> queue = new LinkedBlockingQueue<>(100);
queue.put("Item");      // Kuyruk doluysa bloklar
String item = queue.take();  // Kuyruk boşsa bloklar
```

#### ArrayBlockingQueue<E>
- Sabit boyutlu dizi tabanlı blocking queue
- FIFO sıralama
- Adil (fair) veya adaletsiz (unfair) kilitleme seçeneği

```java
BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10, true); // fair
```

#### SynchronousQueue<E>
- Kapasite bulunmayan özel bir blocking queue
- Her ekleme işlemi bir çıkarma işlemiyle eşleşmelidir
- Producer-consumer senaryoları için idealdir

```java
SynchronousQueue<String> queue = new SynchronousQueue<>();
// Producer thread
queue.put("data");  // Consumer take() yapana kadar bloklar
// Consumer thread
String data = queue.take();  // Producer put() yapana kadar bloklar
```

#### PriorityBlockingQueue<E>
- Sınırsız kapasiteli öncelik sıralı blocking queue
- Elemanlar doğal sıralama veya Comparator ile sıralanır

```java
PriorityBlockingQueue<Task> queue = new PriorityBlockingQueue<>();
```

#### DelayQueue<E extends Delayed>
- Elemanların belirli bir gecikme süresi sonra erişilebilir olduğu sınırsız blocking queue
- Zamanlanmış görevler için kullanılır

```java
DelayQueue<DelayedTask> queue = new DelayQueue<>();
```

### 4. LinkedTransferQueue<E>
- `BlockingQueue` ve `TransferQueue` arayüzlerini implement eder
- **Senkron transfer** operasyonları sunar

**Transfer Metodları:**
- `transfer(E e)`: Eleman bir consumer tarafından alınana kadar bloklar
- `tryTransfer(E e)`: Hemen bir consumer varsa transfer eder, yoksa false döner

---

## Senkronizasyon Araçları

Java.util.concurrent, yaygın senkronizasyon desenlerini çözen beş önemli yardımcı sınıf sağlar.

### 1. Semaphore
Klasik eşzamanlılık kontrol mekanizması. Belirli sayıda kaynağa erişimi kontrol eder.

**Kullanım:**
```java
Semaphore semaphore = new Semaphore(3); // 3 permit

// Thread içinde
semaphore.acquire();  // Permit al
try {
    // Kritik bölge - maksimum 3 thread girebilir
} finally {
    semaphore.release();  // Permit iade et
}
```

**Metodlar:**
- `acquire()`: Permit alır, yoksa bloklar
- `tryAcquire()`: Varsa permit alır, yoksa false döner
- `release()`: Permit iade eder
- `availablePermits()`: Mevcut permit sayısını döner

### 2. CountDownLatch
Bir veya daha fazla thread'in, belirtilen sayıda sinyal, olay veya koşul gerçekleşene kadar beklemesini sağlar.

**Kullanım Senaryoları:**
- Birden fazla threadın başlaması için bir başlangıç sinyali beklemesi
- Birden fazla işlemin tamamlanmasını beklemek

```java
CountDownLatch latch = new CountDownLatch(3);

// Worker threadler
new Thread(() -> {
    // İş yap
    latch.countDown();  // Sayacı azalt
}).start();

// Ana thread
latch.await();  // Sayaç 0 olana kadar bekle
System.out.println("Tüm işler tamamlandı!");
```

**Önemli:** `CountDownLatch` **tek kullanımlıktır**, sıfırlandıktan sonra tekrar kullanılamaz.

### 3. CyclicBarrier
Yeniden kullanılabilir, çok yönlü senkronizasyon noktası. Belirli sayıda thread bir noktada buluşana kadar bekler.

**Farkları:**
- `CountDownLatch`'ten farklı olarak **yeniden kullanılabilir**
- Tüm threadler barrier'a ulaştığında opsiyonel bir barrier action çalıştırılabilir

```java
CyclicBarrier barrier = new CyclicBarrier(3, () -> {
    System.out.println("Tüm threadler barrier'a ulaştı!");
});

// Her thread içinde
barrier.await();  // Diğer threadleri bekle
```

**Kullanım Alanları:**
- Paralel algoritmalarda faz senkronizasyonu
- Çok aşamalı işlemlerde threadlerin koordinasyonu

### 4. Phaser
Fazlanmış hesaplama için esnek bir barrier.

**Özellikler:**
- Dinamik olarak thread eklenip çıkarılabilir
- Birden fazla faz desteklenir
- `CyclicBarrier` ve `CountDownLatch`'in daha gelişmiş versiyonu

```java
Phaser phaser = new Phaser(1); // Ana thread ile başla

// Yeni parti ekle
phaser.register();

// Bir fazı tamamla ve sonrakini bekle
phaser.arriveAndAwaitAdvance();

// Parti çıkar
phaser.arriveAndDeregister();
```

**Kullanım Senaryoları:**
- Çok fazlı paralel algoritmalar
- MapReduce benzeri işlemler

### 5. Exchanger<V>
İki thread'in bir buluşma noktasında nesne alışverişi yapmasını sağlar.

```java
Exchanger<String> exchanger = new Exchanger<>();

// Thread 1
String data1 = "Thread 1 verisi";
String received = exchanger.exchange(data1);

// Thread 2
String data2 = "Thread 2 verisi";
String received = exchanger.exchange(data2);
```

**Kullanım Alanları:**
- Producer-consumer senaryolarında buffer değişimi
- Pipeline işlemlerinde veri transferi
- Genetik algoritmalarda çaprazlama (crossover)

---

## Eşzamanlı Koleksiyonlar

Standart koleksiyonların thread-safe versiyonları, yüksek performanslı eşzamanlı erişim sağlar.

### 1. ConcurrentHashMap<K,V>
Thread-safe hash tablosu. Eşzamanlı okuma ve ayarlanabilir eşzamanlı yazma işlemlerine izin verir.

**Avantajları:**
- `Hashtable`'a göre çok daha yüksek performans
- Tüm tabloyu kilitlemek yerine segment bazlı kilitleme kullanır
- Okuma işlemleri genellikle kilitlemeden gerçekleşir

**Önemli Metodlar:**
- `putIfAbsent(K key, V value)`: Anahtar yoksa ekler
- `remove(Object key, Object value)`: Belirtilen key-value çiftini siler
- `replace(K key, V oldValue, V newValue)`: Atomik değiştirme
- `computeIfAbsent(K key, Function mappingFunction)`: Yoksa hesapla ve ekle
- `merge(K key, V value, BiFunction remappingFunction)`: Birleştirme işlemi

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("key", 1);
map.computeIfAbsent("newKey", k -> 42);
map.merge("key", 1, Integer::sum);  // Değeri artır
```

**Iterator Davranışı:** "Weakly consistent" - eşzamanlı değişiklikleri yansıtabilir, `ConcurrentModificationException` fırlatmaz.

### 2. ConcurrentSkipListMap<K,V> ve ConcurrentSkipListSet<E>
Sıralı eşzamanlı koleksiyonlar. Skip list veri yapısı kullanır.

**Özellikler:**
- Logaritmik zaman karmaşıklığı: O(log n)
- Sıralı iterasyon sağlar
- `TreeMap` ve `TreeSet`'in eşzamanlı alternatifleri

```java
ConcurrentSkipListMap<Integer, String> map = new ConcurrentSkipListMap<>();
ConcurrentSkipListSet<Integer> set = new ConcurrentSkipListSet<>();
```

### 3. CopyOnWriteArrayList<E> ve CopyOnWriteArraySet<E>
Okuma ağırlıklı iş yükleri için optimize edilmiş koleksiyonlar.

**Çalışma Prensibi:**
- Her değişiklikte (ekleme, silme, güncelleme) altta yatan dizinin yeni bir kopyası oluşturulur
- Okuma işlemleri hiç kilitlenmeden gerçekleşir
- Iterator'lar snapshot semantiği sağlar

**Kullanım Alanları:**
- Event listener listeleri
- Nadiren değişen, sık okunan veri setleri

```java
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
list.add("Item");

// Iterator değişikliklerden etkilenmez
Iterator<String> it = list.iterator();
list.add("Yeni Item");  // Iterator'ı etkilemez
```

**Dikkat:** Yazma işlemleri pahalıdır, bu nedenle sadece okuma ağırlıklı senaryolarda kullanın.

---

## Atomik Değişkenler

`java.util.concurrent.atomic` paketi, lock-free, thread-safe tek değişken operasyonları için sınıflar sağlar.

### Temel Sınıflar

#### 1. AtomicInteger, AtomicLong, AtomicBoolean
Primitive türler için atomik wrapper'lar.

```java
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();  // Atomik artırma
counter.addAndGet(5);       // Atomik ekleme
counter.compareAndSet(5, 10);  // CAS operasyonu
```

**Önemli Metodlar:**
- `get()`: Mevcut değeri al
- `set(int newValue)`: Değeri ayarla
- `getAndSet(int newValue)`: Önceki değeri döndür ve yeni değeri ayarla
- `incrementAndGet()`: Artır ve yeni değeri döndür
- `getAndIncrement()`: Önceki değeri döndür ve artır
- `compareAndSet(int expect, int update)`: Beklenen değerse güncelle

#### 2. AtomicReference<V>
Nesne referansları için atomik wrapper.

```java
AtomicReference<User> userRef = new AtomicReference<>(new User());
User newUser = new User();
userRef.compareAndSet(currentUser, newUser);
```

#### 3. AtomicIntegerArray, AtomicLongArray, AtomicReferenceArray<E>
Dizi elemanları üzerinde atomik işlemler.

```java
AtomicIntegerArray array = new AtomicIntegerArray(10);
array.incrementAndGet(5);  // 5. indeksi atomik artır
```

#### 4. AtomicStampedReference<V> ve AtomicMarkableReference<V>
**ABA problemi** için çözümler sağlar.

### Updater Sınıfları
Mevcut sınıflara reflection ile atomik güncelleme yeteneği ekler:
- `AtomicIntegerFieldUpdater<T>`
- `AtomicLongFieldUpdater<T>`
- `AtomicReferenceFieldUpdater<T,V>`

---

## Locks (Kilitler)

`java.util.concurrent.locks` paketi, `synchronized` anahtar kelimesine göre daha esnek kilitleme mekanizmaları sağlar.

### 1. Lock Arayüzü
Temel kilitleme arayüzü.

```java
Lock lock = new ReentrantLock();
lock.lock();
try {
    // Kritik bölge
} finally {
    lock.unlock();  // Mutlaka finally'de unlock!
}
```

**Synchronized'a Göre Avantajları:**
- Kilidin alınması için zaman aşımı ayarlanabilir
- Kesintiye uğratılabilir kilitleme
- Deneme kilidi (try-lock)
- Adil (fair) kilitleme seçeneği
- Çoklu koşul değişkenleri

### 2. ReentrantLock
En yaygın kullanılan `Lock` implementasyonu. Aynı thread tarafından birden fazla kez alınabilir.

**Önemli Metodlar:**
- `lock()`: Kilidi al
- `unlock()`: Kilidi bırak
- `tryLock()`: Varsa kilidi al, yoksa false döner
- `tryLock(long timeout, TimeUnit unit)`: Belirtilen süre boyunca kilidi almayı dene
- `lockInterruptibly()`: Kesintiye uğratılabilir kilitleme

**Fair vs Unfair:**
```java
ReentrantLock fairLock = new ReentrantLock(true);  // Adil - FIFO sırası
ReentrantLock unfairLock = new ReentrantLock();    // Adaletsiz - daha hızlı
```

### 3. ReadWriteLock
Okuma ve yazma kilitleri ayırır.

**Özellikler:**
- Birden fazla thread aynı anda okuyabilir
- Yazma kilidi özeldir (exclusive)
- Okuma ağırlıklı senaryolarda performans artışı

```java
ReadWriteLock rwLock = new ReentrantReadWriteLock();

// Okuma kilidi
rwLock.readLock().lock();
try {
    // Veriyi oku - birden fazla thread girebilir
} finally {
    rwLock.readLock().unlock();
}

// Yazma kilidi
rwLock.writeLock().lock();
try {
    // Veriyi yaz - sadece bir thread girebilir
} finally {
    rwLock.writeLock().unlock();
}
```

### 4. StampedLock (Java 8+)
Daha gelişmiş bir okuma-yazma kilidi. İyimser (optimistic) okuma modu sunar.

**Üç Mod:**
- **Writing:** Özel yazma kilidi
- **Reading:** Paylaşılan okuma kilidi
- **Optimistic Reading:** Kilitlemeden okuma, sonra doğrulama

```java
StampedLock lock = new StampedLock();

// İyimser okuma
long stamp = lock.tryOptimisticRead();
// Veriyi oku
if (!lock.validate(stamp)) {
    // Yazma olmuş, tam kilit al
    stamp = lock.readLock();
    try {
        // Veriyi tekrar oku
    } finally {
        lock.unlockRead(stamp);
    }
}
```

### 5. Condition
Koşul değişkenleri. `Object`'in `wait()`/`notify()` metodlarına alternatif.

```java
Lock lock = new ReentrantLock();
Condition condition = lock.newCondition();

// Bekleme
lock.lock();
try {
    while (!koşul) {
        condition.await();  // wait() gibi
    }
} finally {
    lock.unlock();
}

// Sinyal gönderme
lock.lock();
try {
    condition.signal();  // notify() gibi
    condition.signalAll();  // notifyAll() gibi
} finally {
    lock.unlock();
}
```

---

## Yardımcı Sınıflar

### 1. TimeUnit
Zaman granülerleri ve dönüşüm metodları sağlayan enum.

**Değerler:**
- `NANOSECONDS`, `MICROSECONDS`, `MILLISECONDS`, `SECONDS`, `MINUTES`, `HOURS`, `DAYS`

**Kullanım:**
```java
TimeUnit.SECONDS.sleep(5);  // 5 saniye bekle
long millis = TimeUnit.MINUTES.toMillis(5);  // 5 dakika = ? milisaniye
```

**Avantajlar:**
- Okunabilirlik artışı
- Tip güvenliği
- Zaman dönüşümleri kolaylaştırır

### 2. ThreadLocalRandom
Mevcut thread'e özgü rastgele sayı üreteci.

**Neden Kullanılır:**
- `Random` sınıfı thread-safe ancak çekişme (contention) yaratır
- `ThreadLocalRandom` her thread için ayrı instance kullanır
- Çok thread'li ortamlarda çok daha hızlıdır

```java
int randomNumber = ThreadLocalRandom.current().nextInt(1, 101);  // 1-100 arası
double randomDouble = ThreadLocalRandom.current().nextDouble(0.0, 1.0);
```

### 3. CompletableFuture<T> (Java 8+)
Asenkron programlama için gelişmiş `Future` implementasyonu.

**Özellikler:**
- Zincirleme (chaining) desteği
- Birden fazla future'ı birleştirme
- Exception handling
- Callback'ler

```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    // Asenkron işlem
    return "Sonuç";
});

future.thenApply(result -> result.toUpperCase())
      .thenAccept(System.out::println)
      .exceptionally(ex -> {
          System.err.println("Hata: " + ex);
          return null;
      });
```

**Birleştirme Metodları:**
- `thenCombine()`: İki future'ı birleştir
- `thenCompose()`: Future'ları zincirleme
- `allOf()`: Tüm future'ların tamamlanmasını bekle
- `anyOf()`: Herhangi birinin tamamlanmasını bekle

---

## İstisnalar

### 1. ExecutionException
`Future.get()` çağrıldığında görev içinde fırlatılan bir istisna varsa sarmalanır.

```java
try {
    Future<String> future = executor.submit(callable);
    String result = future.get();
} catch (ExecutionException e) {
    Throwable cause = e.getCause();  // Asıl istisna
}
```

### 2. CancellationException
İptal edilmiş bir görevin sonucunu almaya çalışıldığında fırlatılır.

### 3. RejectedExecutionException
Executor bir görevi çalıştıramadığında (kapalıysa veya kapasite doluysa) fırlatılır.

### 4. TimeoutException
Zaman aşımı olan bir işlemde belirtilen süre aşıldığında fırlatılır.

```java
try {
    String result = future.get(5, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    System.out.println("İşlem zaman aşımına uğradı!");
}
```

### 5. BrokenBarrierException
`CyclicBarrier` kullanırken barrier bozulduğunda fırlatılır.

**Barrier Bozulma Nedenleri:**
- Bekleyen threadlerden biri interrupt edilirse
- Barrier action istisna fırlatırsa
- Timeout oluşursa

### 6. CompletionException
`CompletableFuture` içinde asenkron işlem sırasında oluşan istisnaları sarmalar.

---

## Bellek Tutarlılığı

### Happens-Before İlişkisi

Java.util.concurrent paketi, Java'nın **happens-before** garantilerini genişletir ve thread'ler arasında bellek görünürlüğü için senkronizasyon noktaları oluşturur.

**Temel Garantiler:**

1. **Concurrent koleksiyonlara nesne yerleştirme**, başka bir thread'in o nesneyi koleksiyondan erişmesinden önce happens-before ilişkisi kurar.

2. **Executor'a görev gönderme**, görevin başlamasından önce happens-before ilişkisi kurar.

3. **Future.get()**, asenkron hesaplamanın tamamlanmasından sonra happens-before ilişkisi kurar.

4. **CountDownLatch.countDown()**, `await()` metodunun dönmesinden önce happens-before ilişkisi kurar.

5. **Semaphore release()**, sonraki bir `acquire()` işleminden önce happens-before ilişkisi kurar.

6. **Exchanger.exchange()**, iki thread arasında happens-before ilişkisi kurar.

### Weakly Consistent Iterator'lar

Çoğu concurrent koleksiyon, **zayıf tutarlı** (weakly consistent) iterator'lar sağlar:

**Özellikler:**
- Iterator oluşturulduktan sonra yapılan değişikliklerle eşzamanlı olarak ilerleyebilir
- Asla `ConcurrentModificationException` fırlatmaz
- Elemanlar en fazla bir kez gösterilir
- Iterator oluşturulduğu andaki koleksiyonun tüm elemanlarını gösterebilir veya göstermeyebilir (değişikliklere bağlı)

**Örnek:**
```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("a", 1);
map.put("b", 2);

Iterator<String> iterator = map.keySet().iterator();
map.put("c", 3);  // Iterator başladıktan sonra ekleme

while (iterator.hasNext()) {
    String key = iterator.next();
    // "c" görülebilir veya görülmeyebilir
}
```

---

## En İyi Uygulamalar

### 1. Executor Kullanımı
```java
ExecutorService executor = Executors.newFixedThreadPool(10);
try {
    // Görevleri gönder
    Future<String> future = executor.submit(task);
} finally {
    executor.shutdown();  // Mutlaka kapat
    executor.awaitTermination(1, TimeUnit.MINUTES);
}
```

### 2. Lock Kullanımı
```java
Lock lock = new ReentrantLock();
lock.lock();
try {
    // Kritik bölge
} finally {
    lock.unlock();  // Mutlaka finally'de unlock
}
```

### 3. Doğru Koleksiyon Seçimi
- **Yüksek çekişme:** `ConcurrentHashMap`
- **Sıralı erişim:** `ConcurrentSkipListMap`
- **Producer-consumer:** `BlockingQueue`
- **Okuma ağırlıklı:** `CopyOnWriteArrayList`
- **Eleman alışverişi:** `SynchronousQueue`

### 4. Thread Havuzu Boyutlandırma
- **CPU-yoğun görevler:** `Runtime.getRuntime().availableProcessors()`
- **I/O-yoğun görevler:** Daha fazla thread (CPU sayısı × 2 veya daha fazla)

### 5. Ölü Kilitleri (Deadlock) Önleme
- Her zaman aynı sırada kilit al
- `tryLock()` ile timeout kullan
- Lock-free veri yapıları tercih et (mümkünse)

---

## Performans Optimizasyonları

### 1. ConcurrentHashMap Segment Sayısı
```java
// Varsayılan: 16 segment
ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>(
    initialCapacity,
    loadFactor,
    concurrencyLevel  // Tahmini eşzamanlı güncelleyen thread sayısı
);
```

### 2. ForkJoinPool Paralellik Seviyesi
```java
// Varsayılan: CPU çekirdek sayısı
ForkJoinPool pool = new ForkJoinPool(
    Runtime.getRuntime().availableProcessors()
);
```

### 3. Atomik Sınıflar vs Synchronized
Basit sayaç gibi tek değişken işlemlerinde atomik sınıflar çok daha hızlıdır:

```java
// Yavaş
private int counter = 0;
public synchronized void increment() {
    counter++;
}

// Hızlı
private AtomicInteger counter = new AtomicInteger(0);
public void increment() {
    counter.incrementAndGet();
}
```

---

## Yaygın Kullanım Senaryoları

### 1. Thread Pool ile Toplu Görev İşleme
```java
ExecutorService executor = Executors.newFixedThreadPool(10);
List<Future<Result>> futures = new ArrayList<>();

for (Task task : tasks) {
    Future<Result> future = executor.submit(() -> processTask(task));
    futures.add(future);
}

for (Future<Result> future : futures) {
    Result result = future.get();
    // Sonucu işle
}

executor.shutdown();
```

### 2. Producer-Consumer Pattern
```java
BlockingQueue<WorkItem> queue = new ArrayBlockingQueue<>(100);

// Producer
new Thread(() -> {
    while (true) {
        WorkItem item = produceItem();
        queue.put(item);
    }
}).start();

// Consumer
new Thread(() -> {
    while (true) {
        WorkItem item = queue.take();
        processItem(item);
    }
}).start();
```

### 3. Paralel Veri İşleme
```java
ForkJoinPool pool = new ForkJoinPool();
List<Integer> numbers = // büyük liste

RecursiveTask<Long> task = new RecursiveTask<Long>() {
    @Override
    protected Long compute() {
        if (numbers.size() <= THRESHOLD) {
            return numbers.stream().mapToLong(i -> i).sum();
        } else {
            // Listeyi böl ve alt görevler oluştur
            ForkJoinTask<Long> left = leftTask.fork();
            Long rightResult = rightTask.compute();
            Long leftResult = left.join();
            return leftResult + rightResult;
        }
    }
};

Long result = pool.invoke(task);
```

### 4. Timeout ile Görev Çalıştırma
```java
ExecutorService executor = Executors.newSingleThreadExecutor();
Future<String> future = executor.submit(callable);

try {
    String result = future.get(5, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    future.cancel(true);
    System.out.println("Görev zaman aşımına uğradı ve iptal edildi");
}
```

### 5. Koordine Thread'ler
```java
int threadCount = 5;
CyclicBarrier barrier = new CyclicBarrier(threadCount, () -> {
    System.out.println("Tüm thread'ler hazır, işleme başlıyor!");
});

for (int i = 0; i < threadCount; i++) {
    new Thread(() -> {
        // Hazırlık yap
        System.out.println(Thread.currentThread().getName() + " hazırlandı");
        barrier.await();  // Diğerlerini bekle
        // Asıl işi yap
    }).start();
}
```

---

## Özet Karşılaştırma Tablosu

| Sınıf/Arayüz | Amaç | Kullanım Durumu |
|--------------|------|-----------------|
| `Executor` | Görev çalıştırma soyutlaması | Thread yönetimini soyutlamak |
| `ExecutorService` | Görev yönetimi ve kapanış | Çoğu thread pool senaryosu |
| `ThreadPoolExecutor` | Yapılandırılabilir thread pool | Özel thread pool gereksinimleri |
| `ForkJoinPool` | Work-stealing parallelism | Divide-and-conquer algoritmaları |
| `ConcurrentHashMap` | Thread-safe hash map | Yüksek çekişmeli map erişimi |
| `BlockingQueue` | Producer-consumer kuyruğu | Thread'ler arası veri transferi |
| `CountDownLatch` | Tek kullanımlık sayaç | İşlemlerin tamamlanmasını beklemek |
| `CyclicBarrier` | Yeniden kullanılabilir barrier | Fazlanmış paralel işlemler |
| `Semaphore` | Kaynak sayısı kontrolü | Sınırlı kaynağa erişim kontrolü |
| `ReentrantLock` | Esnek kilitleme | Gelişmiş kilitleme gereksinimleri |
| `ReadWriteLock` | Okuma-yazma ayrımı | Okuma ağırlıklı veri yapıları |
| `AtomicInteger` | Lock-free sayaç | Basit atomik işlemler |
| `CompletableFuture` | Asenkron programlama | Reactive/asenkron kod akışları |

---

## Kaynaklar ve İleri Okuma

- [Oracle Java Concurrency Tutorial](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
- [Java Concurrency in Practice](https://jcip.net/) - Brian Goetz (Klasik kaynak)
- [Doug Lea's Concurrent Programming in Java](http://gee.cs.oswego.edu/dl/cpj/)

---

**Son Güncelleme:** 2026-02-06
**Java Versiyonu:** Java SE 8 ve üzeri
**Doküman Dili:** Türkçe
