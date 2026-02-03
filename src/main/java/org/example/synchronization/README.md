# Java Thread Synchronization Examples

Bu modül, Java'da multi-threading ortamlarında karşılaşılan **race condition** problemlerini ve **synchronization** tekniklerini göstermektedir.

## İçindekiler

1. [TESTT.java](#1-testtjava---race-condition-problemi) - Race Condition Problemi
2. [TESTT_Synchronized.java](#2-testt_synchronizedjava---synchronized-method) - Synchronized Method
3. [TESTT_MultipleCounters.java](#3-testt_multiplecountersjava---lock-contention) - Lock Contention
4. [TESTT_TrueParallel.java](#4-testt_trueparalleljava---gerçek-paralellik) - Gerçek Paralellik
5. [TESTT_InstanceBased.java](#5-testt_instancebasedjava---instance-based-synchronization) - Instance-Based Synchronization
6. [TESTT_Deadlock.java](#6-testt_deadlockjava---deadlock-kilitlenme) - Deadlock (Kilitlenme)
7. [TESTT_DeadlockSolution.java](#7-testt_deadlocksolutionjava---deadlock-çözümleri) - Deadlock Çözümleri
8. [TESTT_WaitNotify.java](#8-testt_waitnotifyjava---wait-ve-notify) - wait() ve notify()
9. [TESTT_ProducerConsumer.java](#9-testt_producerconsumerjava---producer-consumer-pattern) - Producer-Consumer Pattern
10. [TESTT_ThreadPools.java](#10-testt_threadpoolsjava---thread-pools-executor-framework) - Thread Pools (Executor Framework)
11. [TESTT_CallableFuture.java](#11-testt_callablefuturejava---callable-ve-future) - Callable ve Future

---

## 1. TESTT.java - Race Condition Problemi

### Amaç
Synchronization olmadan birden fazla thread'in aynı kaynağa (shared variable) eriştiğinde oluşan **race condition** problemini göstermek.

### Nasıl Çalışır?
- İki thread, `counter` değişkenini 100 milyon kez artırıyor
- Beklenen sonuç: 200,000,000
- Gerçek sonuç: Her çalıştırmada farklı ve yanlış

### Race Condition Nedeni
```java
int temp = counter;    // Thread 1 okuyor: 5
counter = temp + 1;    // Thread 2 okuyor: 5 (Thread 1 yazmadan önce!)
                       // Thread 1 yazıyor: 6
                       // Thread 2 yazıyor: 6 (5+1)
                       // Sonuç: 2 artırma işlemi yapıldı ama değer sadece 1 arttı!
```

### Çalıştırma
```bash
java org.example.synchronization.TESTT
```

### Beklenen Çıktı
```
Run #1 - counter = 105832674 (Expected: 200000000) - Time: 234 ms
Run #2 - counter = 108234521 (Expected: 200000000) - Time: 198 ms
...
```

---

## 2. TESTT_Synchronized.java - Synchronized Method

### Amaç
`synchronized` anahtar kelimesi ile race condition problemini çözmek.

### Nasıl Çalışır?
- `increment()` metodu `synchronized` olarak işaretlendi
- Synchronized static method → `TESTT_Synchronized.class` objesini kilitler
- Bir thread metoda girdiğinde, diğer thread kilidi bekler

### Avantajlar
- Race condition problemi çözüldü
- Sonuçlar her zaman doğru: 200,000,000

### Dezavantajlar
- **Performans kaybı**: Thread'ler birbirlerini bekliyor
- Gerçek paralellik yok, işlemler sırayla yapılıyor

### Çalıştırma
```bash
java org.example.synchronization.TESTT_Synchronized
```

---

## 3. TESTT_MultipleCounters.java - Lock Contention

### Amaç
İki ayrı counter olmasına rağmen aynı lock kullanıldığında oluşan **lock contention** problemini göstermek.

### Problem
```java
private synchronized static void incrementCounter1() { ... }  // Class lock
private synchronized static void incrementCounter2() { ... }  // Aynı class lock!
```

Her iki metod da aynı lock'u (`TESTT_MultipleCounters.class`) kullanıyor:
- Thread 1, `counter1`'i güncelliyor
- Thread 2, `counter2`'yi güncelliyor
- Farklı counter'lar olmasına rağmen thread'ler birbirlerini bekliyor!

### Sonuç
Gereksiz bekleme → Performans kaybı

### Çalıştırma
```bash
java org.example.synchronization.TESTT_MultipleCounters
```

---

## 4. TESTT_TrueParallel.java - Gerçek Paralellik

### Amaç
Her counter için **farklı lock objeleri** kullanarak gerçek paralelliği sağlamak.

### Çözüm
```java
private static final Object lock1 = new Object();
private static final Object lock2 = new Object();

private static void incrementCounter1() {
    synchronized(lock1) { ... }  // lock1 kullanıyor
}

private static void incrementCounter2() {
    synchronized(lock2) { ... }  // lock2 kullanıyor (farklı obje!)
}
```

### Avantajlar
- Her thread kendi lock'unu kullanıyor
- Birbirlerini beklemiyorlar → **Gerçek paralellik**
- En yüksek performans

### Çalıştırma
```bash
java org.example.synchronization.TESTT_TrueParallel
```

---

## 5. TESTT_InstanceBased.java - Instance-Based Synchronization

### Amaç
Instance-level synchronization'ın **shared mutable state** durumunda nasıl çalıştığını göstermek.

### Tasarım
```java
class Counter {
    private int count = 0;

    public synchronized void increment() {  // 'this' objesini kilitler
        int temp = count;
        count = temp + 1;
    }
}
```

### Çalışma Prensibi
- **AYNI** `Counter` objesi her iki thread tarafından da kullanılıyor (shared state!)
- Thread 1 → `sharedCounter` objesine erişiyor
- Thread 2 → **AYNI** `sharedCounter` objesine erişiyor
- `synchronized` instance method, `this` objesini (sharedCounter) kilitliyor → Thread safety sağlanıyor

### Önemli Not
Instance-based synchronization, birden fazla thread'in **aynı objeye** eriştiği durumda anlamlıdır. Eğer her thread farklı instance kullanıyorsa, zaten shared state yoktur ve `synchronized` gereksizdir.

### Avantajlar
- Object-oriented tasarıma uygun
- Her instance kendi lock'unu kullanır
- Class-level lock'tan daha granular (ince taneli) kontrol

### Çalıştırma
```bash
java org.example.synchronization.TESTT_InstanceBased
```

---

## Performans Karşılaştırması

| Örnek | Thread Safety | Paralellik | Performans |
|-------|---------------|------------|------------|
| TESTT | ❌ Yok | ✅ Var (ama yanlış sonuç) | ⚡ En hızlı |
| TESTT_Synchronized | ✅ Var | ❌ Yok (lock contention) | 🐌 En yavaş |
| TESTT_MultipleCounters | ✅ Var | ❌ Yok (gereksiz lock contention) | 🐌 Yavaş |
| TESTT_TrueParallel | ✅ Var | ✅ Var (farklı lock'lar) | ⚡ Hızlı |
| TESTT_InstanceBased | ✅ Var | ❌ Yok (shared instance) | 🐌 Yavaş |

---

## Temel Kavramlar

### Race Condition
Birden fazla thread'in aynı anda shared resource'a erişmesi ve tutarsız sonuçlar oluşturması.

### Synchronization
Thread'lerin shared resource'lara güvenli erişimini sağlamak için kullanılan mekanizma.

### Lock (Kilit)
Bir thread synchronized bloğa girdiğinde aldığı özel erişim hakkı. Lock sahibi olan thread işini bitirene kadar diğer thread'ler bekler.

### Lock Contention
Birden fazla thread'in aynı lock için yarışması ve birbirlerini beklemesi durumu. Performans kaybına neden olur.

### Monitor
Java'da her obje bir **monitor**'a sahiptir. Synchronized block/method bu monitor'u kullanır:
- Synchronized instance method → `this` objesinin monitor'u
- Synchronized static method → `ClassName.class` objesinin monitor'u
- Synchronized(obj) → `obj` objesinin monitor'u

---

## Best Practices

1. **Minimize Lock Scope**: Lock'u sadece gerekli olan kod bloğunda kullanın
   ```java
   // ❌ Kötü
   public synchronized void doWork() {
       heavyComputation();
       counter++;
       anotherComputation();
   }

   // ✅ İyi
   public void doWork() {
       heavyComputation();
       synchronized(this) {
           counter++;
       }
       anotherComputation();
   }
   ```

2. **Use Separate Locks**: İlgisiz resource'lar için farklı lock'lar kullanın (TESTT_TrueParallel örneği)

3. **Prefer Higher-Level Concurrency Utilities**: Modern Java'da `java.util.concurrent` paketini kullanın:
   - `AtomicInteger` (lock-free counter)
   - `ReentrantLock` (daha esnek locking)
   - `ConcurrentHashMap` (thread-safe map)

4. **Avoid Nested Locks**: Deadlock riskini azaltmak için iç içe lock kullanımından kaçının

---

## Öğrenme Hedefleri

Bu örnekleri çalıştırarak şunları öğreneceksiniz:

- ✅ Race condition'ın nasıl oluştuğunu
- ✅ Synchronized anahtar kelimesinin nasıl çalıştığını
- ✅ Lock contention'ın performans etkisini
- ✅ Gerçek paralellik için doğru lock stratejisini
- ✅ Instance-based vs class-based synchronization farkını

---

## Notlar

- Tüm örnekler 10 kez çalıştırılıp ortalama süre hesaplanmaktadır
- Her çalıştırmada 100 milyon increment işlemi yapılmaktadır
- Süre ölçümleri nanosaniye cinsinden alınıp milisaniyeye çevrilmektedir

---

## 6. TESTT_Deadlock.java - Deadlock (Kilitlenme)

### Amaç
Klasik **deadlock (kilitlenme)** senaryosunu göstermek ve deadlock'ın nasıl oluştuğunu anlamak.

### Deadlock Nedir?
İki veya daha fazla thread'in birbirinin tuttuğu kaynakları (lock'ları) beklemesi sonucu hiçbirinin ilerleyememesi durumudur.

### Senaryo
```java
// Thread 1:
synchronized(lock1) {           // lock1'i aldı
    synchronized(lock2) {       // lock2'yi bekliyor (Thread 2'de)
        // İş yap
    }
}

// Thread 2:
synchronized(lock2) {           // lock2'yi aldı
    synchronized(lock1) {       // lock1'i bekliyor (Thread 1'de)
        // İş yap
    }
}
```

### Deadlock Koşulları (Coffman Koşulları)
Deadlock oluşması için 4 koşulun aynı anda gerçekleşmesi gerekir:

1. **Mutual Exclusion**: Kaynaklar aynı anda sadece bir thread tarafından kullanılabilir
2. **Hold and Wait**: Thread bir kaynağı tutarken başka kaynak bekleyebilir
3. **No Preemption**: Kaynaklar zorla alınamaz, thread kendi bırakmalı
4. **Circular Wait**: Thread'ler döngüsel olarak birbirini bekler

### Çalıştırma
```bash
java org.example.synchronization.TESTT_Deadlock
```

### Beklenen Çıktı
```
Deadlock örneği başlatılıyor...
DİKKAT: Bu program deadlock'a girecek ve sonsuz bekleyecek!

Thread-1: lock1'i aldı
Thread-2: lock2'yi aldı
Thread-1: lock2'yi almaya çalışıyor...
Thread-2: lock1'i almaya çalışıyor...

⚠️  DEADLOCK TESPİT EDİLDİ!
Thread-1 durumu: BLOCKED
Thread-2 durumu: BLOCKED
```

### Deadlock Tespiti
- **jstack**: JVM stack dump alarak deadlock tespiti
- **VisualVM**: GUI ile thread durumlarını izleme
- **Thread.getState()**: BLOCKED durumunu kontrol etme

---

## 7. TESTT_DeadlockSolution.java - Deadlock Çözümleri

### Amaç
Deadlock'tan kaçınmanın farklı yöntemlerini göstermek.

### Çözüm 1: Lock Sıralama (Lock Ordering)

**Prensip**: Tüm thread'ler lock'ları her zaman aynı sırada almalı.

```java
// Her iki thread de:
synchronized(lock1) {
    synchronized(lock2) {
        // İş yap
    }
}
```

**Avantajlar**:
- Basit ve etkili
- Deadlock garantili şekilde engellenir
- Performans kaybı minimal

**Dezavantajlar**:
- Lock'ların sırasını önceden bilmek gerekir
- Karmaşık sistemlerde uygulaması zor olabilir

### Çözüm 2: Lock Timeout (tryLock)

**Prensip**: Lock alırken zaman aşımı kullan. Belirli sürede alamazsan bırak ve tekrar dene.

```java
Lock lock1 = new ReentrantLock();
Lock lock2 = new ReentrantLock();

if (lock1.tryLock(1, TimeUnit.SECONDS)) {
    try {
        if (lock2.tryLock(1, TimeUnit.SECONDS)) {
            try {
                // İş yap
            } finally {
                lock2.unlock();
            }
        } else {
            // lock2 alınamadı, lock1'i bırak ve tekrar dene
        }
    } finally {
        lock1.unlock();
    }
}
```

**Avantajlar**:
- Deadlock'tan garantili şekilde kaçınır
- Esnektir, farklı timeout stratejileri kullanılabilir

**Dezavantajlar**:
- Daha karmaşık kod
- **Livelock** riski (thread'ler sürekli deneyip başarısız olabilir)
- Performans overhead

### Çözüm 3: Lock ID Sıralama

**Prensip**: Her lock'a bir ID ver, her zaman küçük ID'den büyüğe doğru kilitle.

```java
class Resource {
    private final int id;
}

void acquireLocks(Resource r1, Resource r2) {
    Resource first = r1.getId() < r2.getId() ? r1 : r2;
    Resource second = r1.getId() < r2.getId() ? r2 : r1;

    synchronized(first) {
        synchronized(second) {
            // İş yap
        }
    }
}
```

**Avantajlar**:
- Dinamik lock sıralaması
- Deadlock garantili şekilde engellenir
- Kod daha temiz ve maintainable

### Diğer Önleme Yöntemleri

4. **Lock-Free Algoritmalar**: `AtomicInteger`, `ConcurrentHashMap` gibi lock-free veri yapıları kullan
5. **Single Lock**: Mümkünse tek bir lock kullan (ama performans kaybı olabilir)
6. **Lock Hierarchy**: Lock'ları hiyerarşik yapıda organize et

### Çalıştırma
```bash
java org.example.synchronization.TESTT_DeadlockSolution
```

### Beklenen Çıktı
```
=== ÇÖZÜM 1: Lock Sıralama ===
Thread-1: lock1'i aldı
Thread-1: lock2'yi aldı - İşlem tamamlandı!
Thread-2: lock1'i aldı
Thread-2: lock2'yi aldı - İşlem tamamlandı!
✓ Her iki thread de başarıyla tamamlandı!

=== ÇÖZÜM 2: Lock Timeout ===
Thread-1: lock1'i aldı (deneme 1)
Thread-2: lock2'yi aldı (deneme 1)
Thread-1: lock2 alınamadı, tekrar deneniyor...
Thread-1: lock1'i aldı (deneme 2)
Thread-1: lock2'yi aldı - İşlem tamamlandı!
...
```

---

## Performans Karşılaştırması (Güncellenmiş)

| Örnek | Thread Safety | Paralellik | Deadlock Risk | Performans |
|-------|---------------|------------|---------------|------------|
| TESTT | ❌ Yok | ✅ Var (ama yanlış sonuç) | ❌ Yok | ⚡ En hızlı |
| TESTT_Synchronized | ✅ Var | ❌ Yok (lock contention) | ❌ Yok | 🐌 En yavaş |
| TESTT_MultipleCounters | ✅ Var | ❌ Yok (gereksiz lock contention) | ❌ Yok | 🐌 Yavaş |
| TESTT_TrueParallel | ✅ Var | ✅ Var (farklı lock'lar) | ⚠️ Var (nested locks) | ⚡ Hızlı |
| TESTT_InstanceBased | ✅ Var | ❌ Yok (shared instance) | ❌ Yok | 🐌 Yavaş |
| TESTT_Deadlock | ✅ Var | ❌ Yok | ⚠️ VAR (DEMO) | ❌ Kilitlenir |
| TESTT_DeadlockSolution | ✅ Var | ✅ Değişken | ❌ Yok | ✅ İyi |

---

## Temel Kavramlar (Güncellenmiş)

### Deadlock (Kilitlenme)
İki veya daha fazla thread'in birbirinin tuttuğu kaynakları beklemesi ve hiçbirinin ilerleyememesi durumu.

### Livelock
Thread'lerin deadlock'tan kaçınmak için sürekli durumlarını değiştirmesi ama yine de ilerleme kaydedememe durumu. Deadlock'tan farkı: thread'ler BLOCKED değil RUNNABLE durumunda, ama iş yapamıyorlar.

### Starvation (Açlık)
Bir thread'in sürekli diğer thread'ler tarafından geçilmesi ve hiç CPU zamanı alamaması durumu.

---

## Best Practices (Güncellenmiş)

1. **Minimize Lock Scope**: Lock'u sadece gerekli olan kod bloğunda kullanın

2. **Use Separate Locks**: İlgisiz resource'lar için farklı lock'lar kullanın

3. **Prefer Higher-Level Concurrency Utilities**: Modern Java'da `java.util.concurrent` paketini kullanın:
   - `AtomicInteger` (lock-free counter)
   - `ReentrantLock` (daha esnek locking)
   - `ConcurrentHashMap` (thread-safe map)

4. **Avoid Nested Locks**: Deadlock riskini azaltmak için iç içe lock kullanımından kaçının

5. **Always Use Lock Ordering**: Eğer nested lock kullanmak zorundasanız, her zaman aynı sırada kilitleyin

6. **Use Timeout with Locks**: ReentrantLock kullanıyorsanız tryLock(timeout) tercih edin

7. **Monitor Thread States**: Production'da thread dump'ları düzenli alın ve BLOCKED thread'leri izleyin

---

## Öğrenme Hedefleri (Güncellenmiş)

Bu örnekleri çalıştırarak şunları öğreneceksiniz:

- ✅ Race condition'ın nasıl oluştuğunu
- ✅ Synchronized anahtar kelimesinin nasıl çalıştığını
- ✅ Lock contention'ın performans etkisini
- ✅ Gerçek paralellik için doğru lock stratejisini
- ✅ Instance-based vs class-based synchronization farkını
- ✅ Deadlock'ın nasıl oluştuğunu ve 4 koşulunu
- ✅ Deadlock'tan kaçınmanın farklı yöntemlerini
- ✅ Lock ordering, timeout ve ID sıralama stratejilerini

---

---

## 8. TESTT_WaitNotify.java - wait() ve notify()

### Amaç
`wait()` ve `notify()` metodlarını kullanarak thread'ler arası iletişimi (inter-thread communication) göstermek.

### wait() ve notify() Nedir?

**wait()**: Bir thread'in lock'u bırakıp bekleme durumuna geçmesi
**notify()**: Bekleyen bir thread'i uyandırma
**notifyAll()**: Tüm bekleyen thread'leri uyandırma

### Temel Kurallar

1. **Synchronized block içinde çağrılmalı**: wait()/notify() sadece synchronized block/method içinde çağrılabilir
2. **Lock bırakılır**: wait() çağrıldığında thread lock'u BIRAKIR
3. **Lock tutulur**: notify() çağrıldığında lock hala tutulmaya devam eder
4. **while loop kullanılmalı**: Koşul kontrolü her zaman while ile yapılmalı (if değil!)

### Örnek Senaryolar

```java
// Consumer bekliyor
synchronized(lock) {
    while (data == null) {
        wait();  // Lock'u bırak ve bekle
    }
    // Veriyi işle
}

// Producer uyandırıyor
synchronized(lock) {
    data = "New Data";
    notify();  // Consumer'ı uyandır
}
```

### wait() vs sleep()

| Özellik | wait() | sleep() |
|---------|--------|---------|
| Lock durumu | Lock'u BIRAKIR | Lock'u TUTAR |
| Uyandırma | notify()/notifyAll() ile | Zaman aşımı ile |
| Kullanım yeri | synchronized içinde | Her yerde |
| Exception | InterruptedException | InterruptedException |

### notify() vs notifyAll()

**notify()**: Bekleyen thread'lerden rastgele BİRİNİ uyandırır
- Avantaj: Daha performanslı
- Dezavantaj: Yanlış thread'i uyandırabilir

**notifyAll()**: Bekleyen TÜM thread'leri uyandırır
- Avantaj: Daha güvenli, tüm thread'ler koşulu kontrol eder
- Dezavantaj: Daha fazla context switch

### Neden while (condition) { wait(); } ?

```java
// ❌ YANLIŞ (if kullanmak)
synchronized(lock) {
    if (data == null) {
        wait();
    }
    process(data);  // data null olabilir!
}

// ✅ DOĞRU (while kullanmak)
synchronized(lock) {
    while (data == null) {
        wait();
    }
    process(data);  // data kesinlikle null değil
}
```

**Nedenler**:
1. **Spurious wakeup**: JVM bazen thread'leri sebepsiz uyandırabilir
2. **Multiple waiters**: Birden fazla thread bekleyebilir ve ilki veriyi alabilir
3. **Safety**: Koşulun hala geçerli olduğunu garanti eder

### Çalıştırma
```bash
java org.example.synchronization.TESTT_WaitNotify
```

### Beklenen Çıktı
```
=== ÖRNEK 1: Basit Wait-Notify ===
Consumer: Veri bekliyorum...
Producer: Veriyi hazırladım!
Producer: Consumer'ı uyandırdım!
Consumer: Veri geldi! İşliyorum...

=== ÖRNEK 2: Producer-Consumer ===
Consumer: Buffer boş, bekliyorum...
Producer: Veri ürettim: Data-1
Consumer: Veri tükettim: Data-1
...
```

---

## 9. TESTT_ProducerConsumer.java - Producer-Consumer Pattern

### Amaç
Gerçekçi bir **Producer-Consumer** senaryosunu **bounded buffer** (sınırlı boyutlu buffer) ile göstermek.

### Pattern Açıklaması

**Producer-Consumer Pattern**, çok thread'li programlamada yaygın kullanılan bir tasarım desenidir:

- **Producer**: Veri üreten thread(ler)
- **Consumer**: Veri tüketen thread(ler)
- **Buffer**: Producer ve Consumer arasındaki kuyruk

### Bounded Buffer (Sınırlı Buffer)

```java
class BoundedBuffer {
    private Queue<Integer> queue = new LinkedList<>();
    private int capacity = 5;

    public synchronized void produce(int value) throws InterruptedException {
        while (queue.size() == capacity) {
            wait();  // Buffer dolu, bekle
        }
        queue.add(value);
        notifyAll();  // Consumer'ları uyandır
    }

    public synchronized int consume() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();  // Buffer boş, bekle
        }
        int value = queue.poll();
        notifyAll();  // Producer'ları uyandır
        return value;
    }
}
```

### Senaryolar

**1. Tek Producer - Tek Consumer**
- En basit senaryo
- Producer veri üretir, Consumer tüketir
- Buffer koordinasyonu sağlar

**2. Birden Fazla Producer ve Consumer**
- Daha gerçekçi senaryo
- Birden fazla üretici ve tüketici
- notifyAll() kullanımı şart

**3. Yavaş Consumer**
- Consumer yavaş çalışırsa buffer dolar
- Producer beklemek zorunda kalır
- Gerçek hayatta sık görülür (I/O işlemleri)

### Neden notifyAll() Kullanılmalı?

```java
// Senaryo: Buffer boş, 2 consumer bekliyor
// Producer veri ekler ve notify() çağırır

notify();     // ❌ Bir consumer yerine başka bir producer'ı uyandırabilir!
notifyAll();  // ✅ Tüm thread'leri uyandırır, uygun olan çalışır
```

**notifyAll() şart çünkü**:
1. Birden fazla türde thread var (producer VE consumer)
2. notify() yanlış türde thread'i uyandırabilir
3. notifyAll() tüm thread'leri uyandırır, onlar koşulu kontrol eder

### Modern Alternatifler

Wait/notify yerine `java.util.concurrent` paketi kullanılabilir:

```java
// BlockingQueue kullanımı (önerilen)
BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);

// Producer
queue.put(value);  // Buffer doluysa bekler

// Consumer
int value = queue.take();  // Buffer boşsa bekler
```

### Avantajlar
- Daha basit ve okunabilir kod
- Exception handling daha iyi
- Daha performanslı (optimize edilmiş)
- Deadlock riski düşük

### Çalıştırma
```bash
java org.example.synchronization.TESTT_ProducerConsumer
```

### Beklenen Çıktı
```
=== ÖRNEK 1: Tek Producer - Tek Consumer ===
Producer: Ürettim: 1 | Buffer: 1/5
Consumer: Tükettim: 1 | Buffer: 0/5
Producer: Ürettim: 2 | Buffer: 1/5
...

=== ÖRNEK 2: Birden Fazla Producer ve Consumer ===
Producer-1: Ürettim: 1 | Buffer: 1/3
Producer-2: Ürettim: 1 | Buffer: 2/3
Consumer-1: Tükettim: 1 | Buffer: 1/3
...
```

---

## Performans Karşılaştırması (Güncellenmiş)

| Örnek | Thread Safety | Paralellik | Deadlock Risk | Koordinasyon | Performans |
|-------|---------------|------------|---------------|--------------|------------|
| TESTT | ❌ Yok | ✅ Var (ama yanlış sonuç) | ❌ Yok | ❌ Yok | ⚡ En hızlı |
| TESTT_Synchronized | ✅ Var | ❌ Yok (lock contention) | ❌ Yok | ❌ Yok | 🐌 En yavaş |
| TESTT_MultipleCounters | ✅ Var | ❌ Yok (gereksiz lock contention) | ❌ Yok | ❌ Yok | 🐌 Yavaş |
| TESTT_TrueParallel | ✅ Var | ✅ Var (farklı lock'lar) | ⚠️ Var (nested locks) | ❌ Yok | ⚡ Hızlı |
| TESTT_InstanceBased | ✅ Var | ❌ Yok (shared instance) | ❌ Yok | ❌ Yok | 🐌 Yavaş |
| TESTT_Deadlock | ✅ Var | ❌ Yok | ⚠️ VAR (DEMO) | ❌ Yok | ❌ Kilitlenir |
| TESTT_DeadlockSolution | ✅ Var | ✅ Değişken | ❌ Yok | ❌ Yok | ✅ İyi |
| TESTT_WaitNotify | ✅ Var | ❌ Yok (sıralı çalışma) | ❌ Yok | ✅ Var | 🐌 Yavaş |
| TESTT_ProducerConsumer | ✅ Var | ✅ Var (buffer ile) | ❌ Yok | ✅ Var | ✅ İyi |

---

## Temel Kavramlar (Güncellenmiş)

### wait()
Bir thread'in lock'u bırakıp WAITING durumuna geçmesi. notify()/notifyAll() ile uyandırılana kadar bekler.

### notify()
Aynı obje üzerinde wait() ile bekleyen thread'lerden birini uyandırır. Hangisinin uyandırılacağı belirsizdir.

### notifyAll()
Aynı obje üzerinde wait() ile bekleyen TÜM thread'leri uyandırır. Genellikle notify()'dan daha güvenlidir.

### Producer-Consumer Pattern
Veri üreten thread'ler (producer) ile veri tüketen thread'ler (consumer) arasında bir buffer kullanarak koordinasyon sağlayan tasarım deseni.

### Bounded Buffer
Maksimum kapasitesi olan buffer. Dolu olduğunda producer bekler, boş olduğunda consumer bekler.

### Spurious Wakeup
JVM'in bazen thread'leri sebepsiz uyandırması. Bu yüzden koşul kontrolü while ile yapılmalı.

### Thread Coordination
Thread'lerin birbirleriyle iletişim kurması ve senkronize çalışması. wait()/notify() bunun için kullanılır.

---

## Best Practices (Güncellenmiş)

1. **Minimize Lock Scope**: Lock'u sadece gerekli olan kod bloğunda kullanın

2. **Use Separate Locks**: İlgisiz resource'lar için farklı lock'lar kullanın

3. **Prefer Higher-Level Concurrency Utilities**: Modern Java'da `java.util.concurrent` paketini kullanın:
   - `AtomicInteger` (lock-free counter)
   - `ReentrantLock` (daha esnek locking)
   - `ConcurrentHashMap` (thread-safe map)
   - `BlockingQueue` (Producer-Consumer için)

4. **Avoid Nested Locks**: Deadlock riskini azaltmak için iç içe lock kullanımından kaçının

5. **Always Use Lock Ordering**: Eğer nested lock kullanmak zorundasanız, her zaman aynı sırada kilitleyin

6. **Use Timeout with Locks**: ReentrantLock kullanıyorsanız tryLock(timeout) tercih edin

7. **Monitor Thread States**: Production'da thread dump'ları düzenli alın ve BLOCKED thread'leri izleyin

8. **Always use while with wait()**: Koşul kontrolü her zaman while ile yapın, if ile değil
   ```java
   while (condition) {
       wait();
   }
   ```

9. **Prefer notifyAll() over notify()**: Birden fazla türde thread bekleyebilir, notifyAll() daha güvenli

10. **Use BlockingQueue for Producer-Consumer**: wait/notify yerine BlockingQueue kullanın, daha basit ve güvenli

---

## Öğrenme Hedefleri (Güncellenmiş)

Bu örnekleri çalıştırarak şunları öğreneceksiniz:

- ✅ Race condition'ın nasıl oluştuğunu
- ✅ Synchronized anahtar kelimesinin nasıl çalıştığını
- ✅ Lock contention'ın performans etkisini
- ✅ Gerçek paralellik için doğru lock stratejisini
- ✅ Instance-based vs class-based synchronization farkını
- ✅ Deadlock'ın nasıl oluştuğunu ve 4 koşulunu
- ✅ Deadlock'tan kaçınmanın farklı yöntemlerini
- ✅ Lock ordering, timeout ve ID sıralama stratejilerini
- ✅ wait() ve notify() kullanımını
- ✅ Thread'ler arası iletişim tekniklerini
- ✅ Producer-Consumer pattern implementasyonunu
- ✅ Bounded buffer kullanımını
- ✅ notifyAll() vs notify() farkını
- ✅ Spurious wakeup ve while loop gerekliliğini

---

---

## 10. TESTT_ThreadPools.java - Thread Pools (Executor Framework)

### Amaç
**Thread Pool** kullanarak thread yönetimini göstermek. Her görev için yeni thread yaratmak yerine hazır thread'leri kullanmak.

### Thread Pool Nedir?

Önceden oluşturulmuş thread'lerin bulunduğu havuz. Görevler geldiğinde hazır thread'ler işleri alır.

**Neden Thread Pool?**
- Thread yaratma maliyeti yüksek (her seferinde new Thread() pahalı)
- Thread sayısını kontrol eder (binlerce thread sistem çökertir)
- Task queue ile iş yönetimi

### Executor Tipleri

#### 1. SingleThreadExecutor

**1 thread**, görevler **sırayla** işlenir.

```java
ExecutorService executor = Executors.newSingleThreadExecutor();

executor.submit(() -> System.out.println("Task 1"));
executor.submit(() -> System.out.println("Task 2"));

// Çıktı: Task 1, Task 2 (sırayla)
```

**Kullanım**: Log yazma, sıralı işlemler

#### 2. FixedThreadPool

**N thread**, görevler **paralel** işlenir.

```java
ExecutorService executor = Executors.newFixedThreadPool(3);

for (int i = 0; i < 10; i++) {
    executor.submit(() -> doWork());
}

// 10 görev, 3'er 3'er paralel işlenir
```

**Kullanım**: Web server, CPU-intensive işler

#### 3. CachedThreadPool

**İhtiyaca göre thread** oluşturur. Thread 60 saniye boşsa yok edilir.

```java
ExecutorService executor = Executors.newCachedThreadPool();

for (int i = 0; i < 100; i++) {
    executor.submit(() -> quickTask());
}

// İhtiyaç kadar thread oluşturur
```

**Kullanım**: Çok sayıda kısa süreli görev, I/O işlemleri

#### 4. ScheduledThreadPool

**Zamanlı/periyodik** görevler için.

```java
ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

// 2 saniye sonra çalıştır
executor.schedule(() -> task(), 2, TimeUnit.SECONDS);

// 1 saniye sonra başla, her 5 saniyede tekrarla
executor.scheduleAtFixedRate(() -> task(), 1, 5, TimeUnit.SECONDS);
```

**Kullanım**: Cron job, periyodik temizlik, health check

### submit() vs execute()

```java
// execute(): Void, sonuç dönmez
executor.execute(() -> doWork());

// submit(): Future döner, sonuç alabilirsin
Future<Integer> future = executor.submit(() -> {
    return 42;
});

Integer result = future.get();  // Bekler, sonucu alır
```

### shutdown() vs shutdownNow()

```java
// shutdown(): Mevcut görevleri bitir, yeni görev alma
executor.shutdown();
executor.awaitTermination(10, TimeUnit.SECONDS);

// shutdownNow(): Tüm görevleri kes, hemen kapat
executor.shutdownNow();
```

### Karşılaştırma

| Executor Tipi | Thread Sayısı | Kullanım |
|---------------|---------------|----------|
| SingleThreadExecutor | 1 | Sıralı işlemler |
| FixedThreadPool(N) | N (sabit) | Paralel işlemler, CPU-intensive |
| CachedThreadPool | İhtiyaca göre | Kısa süreli çok görev, I/O |
| ScheduledThreadPool | N (sabit) | Zamanlı/periyodik görevler |

### Manuel Thread vs Thread Pool

```java
// ❌ Manuel (her seferinde thread yarat)
for (int i = 0; i < 1000; i++) {
    new Thread(() -> doWork()).start();  // 1000 thread!
}

// ✅ Thread Pool (hazır thread'leri kullan)
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 1000; i++) {
    executor.submit(() -> doWork());  // 10 thread, 1000 görevi işler
}
```

### Çalıştırma
```bash
java org.example.synchronization.TESTT_ThreadPools
```

### Beklenen Çıktı
```
=== 1. SingleThreadExecutor ===
Task-1 başladı - Thread: pool-1-thread-1
Task-1 bitti
Task-2 başladı - Thread: pool-1-thread-1
Task-2 bitti
...

=== 2. FixedThreadPool (3 thread) ===
Task-1 başladı - Thread: pool-2-thread-1
Task-2 başladı - Thread: pool-2-thread-2
Task-3 başladı - Thread: pool-2-thread-3
(3 görev paralel)
```

---

## 11. TESTT_CallableFuture.java - Callable ve Future

### Amaç
**Callable** ve **Future** kullanarak asenkron işlemlerden **sonuç almayı** göstermek.

### Runnable vs Callable

**Runnable**: Sonuç dönmez, exception fırlatamaz
```java
Runnable task = () -> {
    System.out.println("İş yapıyorum");
    // void, sonuç yok
};
```

**Callable**: Sonuç döner, exception fırlatabilir
```java
Callable<Integer> task = () -> {
    return 42;  // Sonuç döner
};
```

### Future Nedir?

**Asenkron bir işlemin sonucunu temsil eden nesne.**

```java
ExecutorService executor = Executors.newSingleThreadExecutor();

Future<Integer> future = executor.submit(() -> {
    Thread.sleep(2000);
    return 42;
});

// Main thread devam ediyor...
System.out.println("Sonucu bekliyorum...");

Integer result = future.get();  // BLOKLAR, sonucu bekler
System.out.println("Sonuç: " + result);
```

### Future Metodları

#### get() - Bloklar

```java
Integer result = future.get();  // Sonuç gelene kadar BEKLER
```

**Sorun**: Main thread durur, bekler!

#### get(timeout) - Timeout ile Bekle

```java
try {
    Integer result = future.get(5, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    System.out.println("5 saniyede bitmedi!");
    future.cancel(true);
}
```

#### cancel() - İptal Et

```java
future.cancel(true);  // true: Thread'i interrupt et

if (future.isCancelled()) {
    System.out.println("İptal edildi");
}
```

#### isDone() - Bitti mi?

```java
while (!future.isDone()) {
    System.out.println("Henüz bitmedi...");
    Thread.sleep(500);
}

Integer result = future.get();  // Artık hemen dönecek
```

### invokeAll() - Tüm Görevler

Tüm görevleri çalıştır, **hepsi bitene kadar bekle**.

```java
List<Callable<Integer>> tasks = Arrays.asList(
    () -> task1(),
    () -> task2(),
    () -> task3()
);

List<Future<Integer>> futures = executor.invokeAll(tasks);

// Tüm görevler bitti
for (Future<Integer> f : futures) {
    Integer result = f.get();  // Hemen dönecek
}
```

### invokeAny() - İlk Biten Kazanır

İlk biten görevin sonucunu döner, **diğerlerini iptal eder**.

```java
List<Callable<String>> tasks = Arrays.asList(
    () -> slowTask(),   // 5 saniye
    () -> fastTask(),   // 1 saniye ⚡
    () -> normalTask()  // 3 saniye
);

String result = executor.invokeAny(tasks);
// fastTask() kazanır, diğerleri iptal edilir
```

### Exception Handling

```java
Future<Integer> future = executor.submit(() -> {
    throw new RuntimeException("Hata!");
});

try {
    Integer result = future.get();
} catch (ExecutionException e) {
    System.out.println("Task içinde exception: " + e.getCause());
}
```

Exception **get()** çağrıldığında fırlatılır!

### Future'ın Sınırlamaları

1. **get() bloklar**: Main thread durur
2. **Chaining yok**: Birden fazla Future'ı birleştiremezsin
3. **Callback yok**: İşlem bitince otomatik çalışacak kod yazamazsın
4. **Composition zor**: f1 bitince f2'yi çalıştır gibi senaryolar

**Çözüm**: `CompletableFuture` (Java 8+)

### Çalıştırma
```bash
java org.example.synchronization.TESTT_CallableFuture
```

### Beklenen Çıktı
```
=== 1. Runnable vs Callable ===
Runnable: Sonuç dönmüyor
Callable: Hesaplama yapılıyor...
Sonuç: 42

=== 2. Future.get() Bloklaması ===
Main thread devam ediyor...
Şimdi sonucu bekliyorum (get() bloklar)...
Task başladı, 3 saniye sürecek...
Task bitti!
Sonuç: 100
```

---

## Performans Karşılaştırması (Final)

| Örnek | Thread Safety | Paralellik | Deadlock Risk | Koordinasyon | Performans |
|-------|---------------|------------|---------------|--------------|------------|
| TESTT | ❌ Yok | ✅ Var (ama yanlış sonuç) | ❌ Yok | ❌ Yok | ⚡ En hızlı |
| TESTT_Synchronized | ✅ Var | ❌ Yok (lock contention) | ❌ Yok | ❌ Yok | 🐌 En yavaş |
| TESTT_MultipleCounters | ✅ Var | ❌ Yok (gereksiz lock contention) | ❌ Yok | ❌ Yok | 🐌 Yavaş |
| TESTT_TrueParallel | ✅ Var | ✅ Var (farklı lock'lar) | ⚠️ Var (nested locks) | ❌ Yok | ⚡ Hızlı |
| TESTT_InstanceBased | ✅ Var | ❌ Yok (shared instance) | ❌ Yok | ❌ Yok | 🐌 Yavaş |
| TESTT_Deadlock | ✅ Var | ❌ Yok | ⚠️ VAR (DEMO) | ❌ Yok | ❌ Kilitlenir |
| TESTT_DeadlockSolution | ✅ Var | ✅ Değişken | ❌ Yok | ❌ Yok | ✅ İyi |
| TESTT_WaitNotify | ✅ Var | ❌ Yok (sıralı çalışma) | ❌ Yok | ✅ Var | 🐌 Yavaş |
| TESTT_ProducerConsumer | ✅ Var | ✅ Var (buffer ile) | ❌ Yok | ✅ Var | ✅ İyi |
| TESTT_ThreadPools | ✅ Var | ✅ Var (pool'a göre) | ❌ Yok | ✅ Var | ⚡⚡ Çok hızlı |

---

## Temel Kavramlar (Final)

### Thread Pool
Önceden oluşturulmuş thread'lerin bulunduğu havuz. Thread yaratma maliyetini azaltır ve thread sayısını kontrol eder.

### Executor Framework
Java'da thread pool yönetimi için kullanılan framework. `ExecutorService`, `ScheduledExecutorService` gibi arayüzler sağlar.

### Task Queue
Thread pool'da bekleyen görevlerin tutulduğu kuyruk. Thread boşaldığında kuyruktan görev alır.

### Future
`submit()` metodunun döndürdüğü nesne. Görevin sonucunu almak için `get()` metodunu kullanırız.

---

## Best Practices (Final)

1. **Minimize Lock Scope**: Lock'u sadece gerekli olan kod bloğunda kullanın

2. **Use Separate Locks**: İlgisiz resource'lar için farklı lock'lar kullanın

3. **Prefer Higher-Level Concurrency Utilities**: Modern Java'da `java.util.concurrent` paketini kullanın:
   - `AtomicInteger` (lock-free counter)
   - `ReentrantLock` (daha esnek locking)
   - `ConcurrentHashMap` (thread-safe map)
   - `BlockingQueue` (Producer-Consumer için)
   - **`ExecutorService` (Thread pool yönetimi)**

4. **Avoid Nested Locks**: Deadlock riskini azaltmak için iç içe lock kullanımından kaçının

5. **Always Use Lock Ordering**: Eğer nested lock kullanmak zorundasanız, her zaman aynı sırada kilitleyin

6. **Use Timeout with Locks**: ReentrantLock kullanıyorsanız tryLock(timeout) tercih edin

7. **Monitor Thread States**: Production'da thread dump'ları düzenli alın ve BLOCKED thread'leri izleyin

8. **Always use while with wait()**: Koşul kontrolü her zaman while ile yapın, if ile değil

9. **Prefer notifyAll() over notify()**: Birden fazla türde thread bekleyebilir, notifyAll() daha güvenli

10. **Use BlockingQueue for Producer-Consumer**: wait/notify yerine BlockingQueue kullanın

11. **Use Thread Pools**: Manuel thread yaratmak yerine ExecutorService kullanın
    - Fixed size görevler için `FixedThreadPool`
    - Kısa süreli çok görev için `CachedThreadPool`
    - Zamanlı görevler için `ScheduledThreadPool`

12. **Always shutdown() executors**: Memory leak önlemek için executor'ları kapatın

---

## Öğrenme Hedefleri (Final)

Bu örnekleri çalıştırarak şunları öğreneceksiniz:

- ✅ Race condition'ın nasıl oluştuğunu
- ✅ Synchronized anahtar kelimesinin nasıl çalıştığını
- ✅ Lock contention'ın performans etkisini
- ✅ Gerçek paralellik için doğru lock stratejisini
- ✅ Instance-based vs class-based synchronization farkını
- ✅ Deadlock'ın nasıl oluştuğunu ve 4 koşulunu
- ✅ Deadlock'tan kaçınmanın farklı yöntemlerini
- ✅ Lock ordering, timeout ve ID sıralama stratejilerini
- ✅ wait() ve notify() kullanımını
- ✅ Thread'ler arası iletişim tekniklerini
- ✅ Producer-Consumer pattern implementasyonunu
- ✅ Bounded buffer kullanımını
- ✅ notifyAll() vs notify() farkını
- ✅ Spurious wakeup ve while loop gerekliliğini
- ✅ Thread Pool'ların nasıl çalıştığını
- ✅ Executor Framework kullanımını
- ✅ Farklı executor tiplerini ve kullanım alanlarını
- ✅ submit() vs execute() farkını
- ✅ shutdown() vs shutdownNow() farkını

---

## İleri Okuma

- [Java Concurrency Tutorial - Oracle](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
- [Java Memory Model](https://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.4)
- [Effective Java - Item 79: Avoid excessive synchronization](https://www.oreilly.com/library/view/effective-java/9780134686097/)
- [Deadlock Detection with jstack](https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/tooldescr016.html)
- [Guarded Blocks and wait/notify](https://docs.oracle.com/javase/tutorial/essential/concurrency/guardmeth.html)
- [Producer-Consumer Problem](https://en.wikipedia.org/wiki/Producer%E2%80%93consumer_problem)
- [Executor Framework Guide](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executor.html)
- [Thread Pools in Java](https://www.baeldung.com/thread-pool-java-and-guava)
