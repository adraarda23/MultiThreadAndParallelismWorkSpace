# Java.util.concurrent Paketi - Teorik ve Kavramsal Anlayış

## İçindekiler
1. [Giriş: Eşzamanlılık Probleminin Doğası](#giriş-eşzamanlılık-probleminin-doğası)
2. [Executor Framework: Görev vs Thread Paradigması](#executor-framework-görev-vs-thread-paradigması)
3. [Kuyruklar: Thread'ler Arası İletişim Modeli](#kuyruklar-threadler-arası-iletişim-modeli)
4. [Senkronizasyon Primitive'leri: Koordinasyon Mekanizmaları](#senkronizasyon-primitiveleri-koordinasyon-mekanizmaları)
5. [Concurrent Koleksiyonlar: Veri Paylaşımının Anatomisi](#concurrent-koleksiyonlar-veri-paylaşımının-anatomisi)
6. [Atomik İşlemler: Lock-Free Programlamanın Temelleri](#atomik-işlemler-lock-free-programlamanın-temelleri)
7. [Lock Mekanizmaları: Synchronized'ın Ötesi](#lock-mekanizmaları-synchronizedın-ötesi)
8. [Bellek Modeli: Görünürlük ve Sıralama Garantileri](#bellek-modeli-görünürlük-ve-sıralama-garantileri)

---

## Giriş: Eşzamanlılık Probleminin Doğası

### Temel Problem: Paylaşılan Durum ve Zaman

Eşzamanlı programlamanın temel zorluğu, **birden fazla çalışma biriminin aynı anda paylaşılan bir duruma erişmesi** durumunda ortaya çıkar. Bu problem üç temel boyuta sahiptir:

1. **Görünürlük (Visibility)**: Bir thread tarafından yapılan değişiklikler diğer thread'ler tarafından ne zaman görülür?
2. **Atomiklik (Atomicity)**: Hangi işlemler bölünemez birimler olarak gerçekleşir?
3. **Sıralama (Ordering)**: İşlemlerin gerçekleşme sırası garantili midir?

### Java Memory Model ve Happens-Before İlişkisi

Java'nın bellek modeli, modern işlemcilerin performans optimizasyonları (önbellekleme, instruction reordering) ile program doğruluğu arasında bir köprü kurar. **Happens-before** ilişkisi, bu modelin temel yapı taşıdır:

"A happens-before B" şu anlama gelir:
- A işleminin sonuçları B işlemi başlamadan önce görünür olacaktır
- A ve B arasındaki sıralama garantilidir
- Bellek tutarlılığı sağlanmıştır

### Java.util.concurrent'in Felsefi Yaklaşımı

Bu paket üç temel ilke üzerine inşa edilmiştir:

1. **Yüksek Seviyeli Soyutlamalar**: Thread'lerle doğrudan çalışmak yerine görev, executor, future gibi kavramlar
2. **Lock-Free Algoritmalar**: Mümkün olduğunda kilitleme yerine CAS (Compare-And-Swap) gibi atomik donanım işlemleri
3. **Uzmanlaşmış Veri Yapıları**: Genel amaçlı synchronized koleksiyonlar yerine özel senaryolar için optimize edilmiş yapılar

---

## Executor Framework: Görev vs Thread Paradigması

### Paradigma Değişimi: Neden Thread'lerden Uzaklaştık?

Geleneksel thread programlamasının problemleri:

**1. Kaynak Yönetimi Zorluğu**
- Her thread bir işletim sistemi kaynağıdır
- Thread oluşturma ve yok etme maliyetlidir
- Çok fazla thread context switching overhead'i yaratır

**2. Coupling (Bağımlılık) Problemi**
- İş mantığı ve çalıştırma mekanizması birbirine karışır
- Test edilebilirlik düşer
- Esneklik kaybolur

**3. Hata Yönetimi Dağınıklığı**
- Her thread kendi exception handling'ini yapmalıdır
- Sonuç toplama karmaşıktır

### Executor Abstraction: Görev Kavramı

Executor framework'ün temel fikri: **"Ne yapılacağı" ile "nasıl yapılacağını" ayırın"**

**Görev (Task)**: Çalıştırılması gereken bir iş birimi
- `Runnable`: Sonuç döndürmeyen görev
- `Callable`: Sonuç döndüren ve exception fırlatabilir görev

**Executor**: Görevlerin nasıl çalıştırılacağına karar veren mekanizma
- Hemen yeni bir thread mi oluşturulsun?
- Havuzdan bir thread mi kullanılsın?
- Sıraya mı alınsın?
- Reddedilsin mi?

### ThreadPoolExecutor: İş Gücü Havuzunun Anatomisi

Thread pool'un arkasındaki teori:

**1. Üretici-Tüketici Deseni (Producer-Consumer Pattern)**
```
[Görev Göndericiler] → [İş Kuyruğu] → [Worker Thread'ler]
```

**2. Dinamik Boyutlandırma Stratejisi**

Thread pool üç fazda çalışır:
- **Faz 1 (Core Phase)**: Thread sayısı `corePoolSize`'a ulaşana kadar her görev için yeni thread oluştur
- **Faz 2 (Queueing Phase)**: Görevleri kuyruğa ekle (havuz core size'da ve kuyruk dolmamış)
- **Faz 3 (Max Phase)**: Kuyruk doluysa `maximumPoolSize`'a kadar yeni thread oluştur
- **Faz 4 (Rejection)**: Maksimuma ulaşıldıysa görevi reddet

**Neden Bu Tasarım?**
- **Kaynak Kontrolü**: Sistem kaynakları aşılmasın
- **Latency vs Throughput**: Core size latency için, max size throughput için
- **Graceful Degradation**: Sistem yük altında yavaşlar ama çökmez

**3. Keep-Alive Mekanizması**

Core size üzerindeki idle thread'ler belirli bir süre sonra sonlandırılır:
- **Amaç**: Gereksiz kaynak tüketimini önlemek
- **Trade-off**: Thread oluşturma maliyeti vs bellek kullanımı

### ScheduledExecutorService: Zaman Boyutu

Zamanlı görevlerin iki temel problemi vardır:

**1. Fixed-Rate vs Fixed-Delay**
- **Fixed-Rate**: Görevler sabit aralıklarla başlar (drift yok, ama overlap olabilir)
- **Fixed-Delay**: Her görev bittikten sonra sabit süre bekle (overlap yok, ama drift olur)

**2. Görev Süresi > Periyot Durumu**
- Görev çalışma süresi periyottan uzunsa ne olur?
- Fixed-rate: Bir sonraki görev hemen başlar (paralel çalışma)
- Fixed-delay: Bir sonraki görev gecikir (seri çalışma)

### ForkJoinPool: Work-Stealing Paradigması

**Temel Problem: Load Balancing**

Klasik thread pool'larda her worker kendi kuyruğundan iş alır. Bu şu problemlere yol açar:
- Bazı thread'ler çok işli, bazıları boşta olabilir
- İş dağılımı başlangıçta belirlenir ve dinamik değişmez

**Work-Stealing Çözümü:**

Her worker thread'in kendi double-ended queue'su (deque) vardır:
1. Worker kendi işlerini deque'sunun bir ucundan alır (LIFO - cache locality için)
2. İşi bitince başka worker'ların deque'sunun *diğer* ucundan iş çalar (FIFO)

**Neden LIFO/FIFO Karışımı?**
- **LIFO (kendi işleri)**: En son eklenen iş muhtemelen cache'de → cache hit oranı artar
- **FIFO (çalınan işler)**: En eski işler daha büyük parçalar olabilir → daha verimli yük dengeleme

**Divide-and-Conquer ile Uyum:**

Fork-Join modeli özyinelemeli görev bölme için tasarlanmıştır:
```
Büyük Görev
    ├─ Alt Görev 1 (fork)
    │   ├─ Alt Alt Görev 1.1 (fork)
    │   └─ Alt Alt Görev 1.2 (fork)
    └─ Alt Görev 2 (compute)
        ├─ Alt Alt Görev 2.1 (fork)
        └─ Alt Alt Görev 2.2 (compute)
```

Her fork işlemi yeni bir alt görev oluşturur ve deque'ye ekler. Thread kendi görevlerini işlerken başkaları çalabilir.

### Future: Asenkron Hesaplamanın Temsili

**Future Design Pattern'in Motivasyonu:**

Senkron çağrı: `Result r = expensiveOperation()`
Problem: Caller bloklanır, başka iş yapamaz

Asenkron çağrı: `Future<Result> f = executor.submit(task)`
Avantaj: Caller başka işler yapabilir, istediğinde `f.get()` ile sonucu alır

**Get() Semantiği:**
- Sonuç hazırsa: Hemen döner
- Sonuç hazır değilse: Thread bloklanır (wait/notify mekanizması)
- Exception varsa: ExecutionException ile sarmalanır

**Cancel() Semantiği:**
- `mayInterruptIfRunning = false`: Başladıysa devam et, başlamadıysa başlatma
- `mayInterruptIfRunning = true`: Thread.interrupt() çağır (kooperatif iptal)

---

## Kuyruklar: Thread'ler Arası İletişim Modeli

### Kuyruk Teorisi ve Eşzamanlılık

**Üretici-Tüketici Problemi:**
```
Üretici Thread(ler) → [Kuyruk] → Tüketici Thread(ler)
```

Kuyruk, thread'ler arasında **dekuplaj** (decoupling) sağlar:
- Üretici tüketiciyi tanımaz
- Üretim ve tüketim hızları farklı olabilir
- Backpressure mekanizması sağlanır

### Blocking vs Non-Blocking Kuyruklar

**1. Non-Blocking (ConcurrentLinkedQueue)**

**Çalışma Prensibi**: Lock-free CAS operasyonları
```
1. Kuyruk durumunu oku
2. Yeni durumu hesapla
3. CAS ile güncellemeyi dene
4. Başarısızsa 1'e dön (spin)
```

**Avantajlar:**
- Thread bloklanmaz (context switch yok)
- Deadlock riski yok
- Scalability yüksek (çok thread aynı anda işlem yapabilir)

**Dezavantajlar:**
- Backpressure mecanizması yok (sınırsız büyüyebilir)
- CAS retry loop CPU tüketir (high contention'da)
- Producer-consumer dengeleme yok

**2. Blocking (BlockingQueue)**

**Çalışma Prensibi**: Koşul değişkenleri (condition variables)
```
put(): Kuyruk doluysa wait(), sonra ekle ve signal()
take(): Kuyruk boşsa wait(), sonra çıkar ve signal()
```

**Avantajlar:**
- Doğal backpressure (üretici yavaşlar)
- Thread park edilir (CPU tüketimi yok)
- Kapasite kontrolü

**Dezavantajlar:**
- Thread bloklanır (context switch maliyeti)
- Daha düşük throughput (kilitleme overhead'i)

### BlockingQueue Varyantları: Tasarım Kararları

**1. ArrayBlockingQueue: Sınırlı, Sabit Kapasiteli**

**İç Yapı**: Döngüsel dizi (circular array)
```
[_ _ _ X X X X X _ _]
        ↑       ↑
       head    tail
```

**Neden Array?**
- Sabit bellek ayırımı (öngörülebilir memory footprint)
- Cache locality (dizi elemanları bitişik)
- Basit index aritmetiği

**Fair vs Unfair Modu:**
- **Unfair (varsayılan)**: Yeni gelen thread kilidi kapabilir (barging)
- **Fair**: FIFO sırasına göre kilit verilir (throughput kaybı, adalet kazancı)

**2. LinkedBlockingQueue: İsteğe Bağlı Sınırlı**

**İç Yapı**: Bağlı liste (linked list)
```
[Head] → [Node] → [Node] → [Node] → [Tail]
  ↓                                    ↓
takeLock                            putLock
```

**İki Kilit Stratejisi:**
- `takeLock`: Kuyruktan çıkarma için
- `putLock`: Kuyruğa ekleme için

**Neden İki Kilit?**
- Head ve tail farklı yerlerdir
- Aynı anda bir thread çıkarırken diğeri ekleyebilir
- Contention azalır, throughput artar

**Dezavantaj**: Size hesaplama için atomik değişken (ek overhead)

**3. SynchronousQueue: Sıfır Kapasiteli**

**Kavramsal Model**: Kuyruk değil, "buluşma noktası" (rendezvous point)

**Dual Stack/Queue Algoritması:**
- Data node: Veri taşıyan üretici bekliyor
- Request node: Tüketici bekliyor

İki tür eşleşme:
1. Data node varken request gelir → transfer olur
2. Request node varken data gelir → transfer olur

**Kullanım Senaryosu**: Senkron el değiştirme gerekli (örn: güvenlik tokeni aktarımı)

**4. PriorityBlockingQueue: Öncelikli**

**Çalışma Prensibi**: Binary heap (ikili yığın)
```
        [1]           (En yüksek öncelik)
       /   \
     [3]   [2]
    /  \
  [5]  [4]
```

**Neden Heap?**
- En yüksek öncelikli eleman her zaman root'ta → O(1) peek
- Ekleme/çıkarma → O(log n)
- Dinamik boyutlandırma

**Tutarlılık Modeli**: "Weakly consistent" iterator (anlık snapshot değil)

**5. DelayQueue: Zamanlı**

**Delayed Interface**: Her eleman ne zaman "hazır" olacağını bilir

**İç Mekanizma**: PriorityQueue + delay kontrolü
- Elemanlar delay sürelerine göre sıralanır
- `take()`: En kısa delay'li elemanı kontrol et, hazırsa ver, değilse bekle

**Kullanım**: Cache expiration, scheduled task queues

**6. LinkedTransferQueue: Transfer Semantiği**

**Transfer vs Put Farkı:**
- **put()**: Kuyruğa ekle ve dön (asenkron)
- **transfer()**: Bir tüketici alana kadar bekle (senkron el değiştirme)

**Dual Queue Algoritması**: SynchronousQueue gibi ama kapasite de var

**Kullanım**: Producer'ın tüketicinin aldığından emin olması gerektiğinde

---

## Senkronizasyon Primitive'leri: Koordinasyon Mekanizmaları

### Senkronizasyon Probleminin Sınıflandırılması

Eşzamanlı sistemlerde üç temel koordinasyon problemi vardır:

1. **Mutual Exclusion**: Aynı anda sadece N thread kritik bölgeye girebilir
2. **Signaling**: Thread'lerin birbirlerine olay bildirimi
3. **Rendezvous**: Thread'lerin belirli bir noktada buluşması

### Semaphore: Dijkstra'nın Klasik Çözümü

**Tarihsel Bağlam**: Edsger Dijkstra tarafından 1965'te icat edildi

**Matematiksel Soyutlama:**
```
Semaphore S = pozitif tam sayı

P(S): // acquire, proberen (test)
    wait until S > 0
    S := S - 1

V(S): // release, verhogen (increment)
    S := S + 1
    signal waiting threads
```

**İç Yapı:**
- `permits`: Mevcut izin sayısı (atomik integer)
- `sync`: AbstractQueuedSynchronizer (AQS) - bekleme kuyruğu

**Fair vs Unfair:**
- **Unfair**: CAS ile permit kapma yarışı (barging mümkün)
- **Fair**: Bekleme kuyruğuna göre FIFO dağıtım

**Kullanım Desenleri:**

1. **Resource Pool**: `Semaphore(N)` → N kaynak havuzu
2. **Binary Semaphore**: `Semaphore(1)` → mutex (ama reentrancy yok!)
3. **Signaling**: `Semaphore(0)` → başlangıçta bloklu, signal ile açılır

**Neden Bazen Lock Yerine Semaphore?**
- Lock: Sadece kilidi alan thread açabilir (ownership var)
- Semaphore: Herhangi bir thread release yapabilir (ownership yok)

### CountDownLatch: Bir Yönlü Kapı

**Kavramsal Model**: Yarış başlangıç kapısı
- N atleti bekler (N thread)
- Starter silahı ateşlenir (countDown N kez)
- Kapı açılır (count = 0)
- Kapı bir daha kapanmaz (tek kullanımlık)

**İç Mekanizma:**
```
count = N (atomik)
await(): count == 0 ? return : wait
countDown(): count-- ; if (count == 0) signalAll()
```

**Kullanım Desenleri:**

1. **Start Signal**: Tüm thread'ler hazır, ana thread "başla" der
```
Latch(1)
Workers: await()
Main: countDown()
```

2. **Completion Signal**: Ana thread tüm worker'ları bekler
```
Latch(N)
Workers: iş yap, countDown()
Main: await()
```

**Neden Join() Değil?**
- `thread.join()`: Thread sonlanmasını bekler
- `latch.await()`: Sadece belirli bir noktaya ulaşmayı bekler (thread devam eder)

### CyclicBarrier: Çok Yönlü, Yeniden Kullanılabilir Engel

**Kavramsal Model**: Takım sporları molası
- N oyuncu sahada (N thread)
- Mola için herkes soyunma odasına gelmeli
- Herkes gelince mola başlar (barrier action)
- Sonra tekrar sahaya dönülür (yeniden kullanılabilir)

**CountDownLatch ile Karşılaştırma:**

| Özellik | CountDownLatch | CyclicBarrier |
|---------|----------------|---------------|
| Kullanım | Tek | Çok (sıfırlanabilir) |
| Semantik | Sayaç azalır | Thread'ler bekler |
| Roller | Farklı olabilir (sayıcı vs bekleyici) | Herkes eşit (await çağırır) |
| Completion Action | Yok | Var (barrier action) |

**İç Mekanizma:**
```
Generation concept: Her döngü bir "nesil"
await():
    1. Kilidi al
    2. count--
    3. count == 0 ?
        → barrier action çalıştır
        → yeni nesil başlat
        → herkesi uyandır
    : wait()
```

**Barrier Kırılması (Breaking):**
- Bekleyen thread interrupt edilirse
- Timeout oluşursa
- Barrier action exception fırlatırsa

→ Tüm bekleyen thread'ler `BrokenBarrierException` alır
→ Barrier reset edilmeden kullanılamaz

**Kullanım Senaryosu**: Paralel iteratif algoritmalar (her iterasyon bir barrier noktası)

### Phaser: Dinamik, Fazlı Koordinasyon

**Motivasyon**: CyclicBarrier'ın kısıtlamaları
- Parti sayısı sabit
- Dinamik olarak thread eklenemez/çıkarılamaz
- Sadece tek aşamalı

**Phaser'ın Gücü**: Esnek, çok fazlı, dinamik

**Temel Kavramlar:**

1. **Phase (Faz)**: Her senkronizasyon döngüsü bir faz
2. **Party (Parti)**: Senkronizasyona katılan bir thread
3. **Registration**: Yeni parti ekleme
4. **Arrival**: Bir parti mevcut fazı tamamladı
5. **Termination**: Tüm partiler çıktı veya manuel sonlandırma

**Operasyonlar:**

- `register()`: Yeni parti ekle
- `arriveAndAwaitAdvance()`: Fazı tamamla ve diğerlerini bekle (CyclicBarrier gibi)
- `arriveAndDeregister()`: Fazı tamamla ve ayrıl (bir daha katılma)
- `arrive()`: Fazı tamamla ama bekleme (asenkron)

**Hiyerarşik Phaser (Tiering):**

Büyük thread sayılarında performans için:
```
        [Root Phaser]
        /           \
[Child Phaser]   [Child Phaser]
   /    \           /    \
  T1    T2        T3    T4
```

Alt phaser'lar fazı tamamladığında parent'a bildirir → O(log N) contention

**Termination Kontrolü:**

`onAdvance(phase, registeredParties)` override edilebilir:
- `return false`: Devam et
- `return true`: Sonlan

**Kullanım**: MapReduce, multi-phase simulation, parallel streams

### Exchanger: İkili Veri Değişimi

**Kavramsal Model**: Güvenli anahtar değişimi
- İki ajan buluşur
- Aynı anda bavulları değiştirir
- Ayrılır

**İç Mekanizma:**

**Slot-Based Exchange:**
```
[Thread 1] → slot.item = X → wait
[Thread 2] → görür slot dolu → Y ile X'i değiştirir → wake up Thread 1
```

**Elimination Array** (High Contention):
- Tek slot yerine array of slots
- Thread'ler rastgele slot seçer
- Collision azalır, throughput artar

**Kullanım Senaryosu:**

1. **Pipeline Stages**: Stage A çıktısı = Stage B girdisi
2. **Genetic Algorithms**: İki birey genetic material değiştirir
3. **Buffer Swapping**: Producer ve consumer buffer değiştirir (double buffering)

---

## Concurrent Koleksiyonlar: Veri Paylaşımının Anatomisi

### Synchronized Wrapper vs Concurrent Collection

**Collections.synchronizedMap() Problemi:**

```
Map<K,V> map = Collections.synchronizedMap(new HashMap<>());
```

**Nasıl Çalışır:**
- Her metod çağrısı tek bir global lock alır
- Tüm koleksiyon kilitlenir (coarse-grained locking)

**Problemler:**
1. **Scalability**: Aynı anda sadece bir thread işlem yapabilir
2. **Compound Operations**: `putIfAbsent()` gibi kompozit işlemler atomik değil
3. **Iteration**: Iterator sırasında koleksiyon değişirse `ConcurrentModificationException`

### ConcurrentHashMap: Lock Striping ve Beyond

**Java 7 Öncesi: Segment-Based Locking**

```
[Bucket Array]
├─ Segment 0 (lock 0) → [bucket] → [bucket] → ...
├─ Segment 1 (lock 1) → [bucket] → [bucket] → ...
├─ Segment 2 (lock 2) → [bucket] → [bucket] → ...
└─ ...
```

**Strateji**: Array'i N segment'e böl, her segment'in kendi kilidi olsun
- Farklı segment'lere yazma → paralel
- Aynı segment'e yazma → seri
- Okuma → genellikle kilitsiz

**Concurrency Level**: Segment sayısı = tahmini eşzamanlı yazıcı thread sayısı

**Java 8+ Devrimi: CAS + Synchronized**

**Yeni Yaklaşım:**
- Segment yok, direkt bucket-level kilitleme
- Boş bucket: CAS ile direkt ekle (kilitsiz)
- Dolu bucket: Sadece o bucket'ı kilitle

**Tree Binning** (TREEIFY_THRESHOLD = 8):
- Collision çok olursa (8+ eleman aynı bucket)
- Linked list → Red-Black Tree dönüşümü
- O(n) → O(log n) arama

**Size Hesaplama Sorunu:**

`size()` çağrısı paralel değişikliklerde yaklaşık sonuç döner (eventually consistent)
- Neden? Tüm bucket'ları kilitlemeden tam saymak imkansız
- Çözüm: `mappingCount()` kullan, tam değil ama tutarlı

**Weakly Consistent Iterator:**
- Iterator oluşturulduğu andaki snapshot'ı yansıtabilir
- Eklenen elemanları gösterebilir veya göstermeyebilir
- Silinen elemanları atlar
- **Asla exception fırlatmaz**

**Atomic Compound Operations:**

- `putIfAbsent(key, value)`: Yoksa ekle (check-then-act atomik)
- `computeIfAbsent(key, function)`: Yoksa hesapla ve ekle
- `merge(key, value, remappingFunction)`: Varsa birleştir, yoksa ekle

**Neden Bu Metodlar Önemli?**
Klasik pattern race condition'a açıktır:
```
if (!map.containsKey(key)) {  // Check
    map.put(key, value);       // Act
}
// İki thread arasında race!
```

### ConcurrentSkipListMap: Kilit-Serbest Sıralı Koleksiyon

**Skip List Veri Yapısı:**

```
Level 3: [1] -----------------------> [50]
Level 2: [1] ---------> [25] -------> [50]
Level 1: [1] -> [10] -> [25] -> [30] -> [50]
Level 0: [1] -> [5] -> [10] -> [15] -> [25] -> [30] -> [40] -> [50]
```

**Neden Skip List?**

**Karşılaştırma: Tree vs Skip List**

| Özellik | Red-Black Tree | Skip List |
|---------|----------------|-----------|
| Arama | O(log n) | O(log n) (expected) |
| Ekleme | O(log n) + rebalancing | O(log n), no rebalancing |
| Silme | O(log n) + rebalancing | O(log n), no rebalancing |
| Concurrency | Karmaşık (tree rotations kilitleme gerektirir) | Basit (CAS yeterli) |

**Lock-Free Ekleme:**
1. Bottom level'da doğru pozisyonu bul
2. CAS ile node ekle
3. Rastgele yükseklik belirle (coin flip)
4. Üst seviyelere CAS ile pointer ekle

**Rebalancing Yok:**
- Probabilistic balancing (rastgelelik sayesinde)
- İstatistiksel olarak dengeli kalır
- Deterministik rebalancing (rotations) kilitleme gerektirir

### CopyOnWriteArrayList: Immutability ile Thread-Safety

**Radikal Yaklaşım**: Değiştirme = Kopyalama

**Çalışma Prensibi:**
```
array = [A, B, C]

add(D):
    1. Yeni array oluştur: [A, B, C, D]
    2. Atomik referans değişimi: array = yeni array
    3. Eski array garbage collection'a
```

**Iterator Semantiği**: Snapshot Isolation
```
list = [A, B, C]
iterator = list.iterator()  // [A, B, C] snapshot
list.add(D)                 // [A, B, C, D]
iterator.next()             // Hala [A, B, C] görür
```

**Neden Bu Tasarım?**

**Okuma Ağırlıklı Senaryolar:**
- Event listeners (ekleme nadir, traversal çok)
- Configuration data (güncelleme nadir, okuma çok)
- Observer pattern

**Trade-off:**
- **Avantaj**: Okuma kilitsiz, iterator safe
- **Dezavantaj**: Yazma maliyeti O(n), bellek overhead

**Fail-Safe Iterator:**
- Snapshot üzerinde iterator → değişikliklerden etkilenmez
- `ConcurrentModificationException` atmaz

---

## Atomik İşlemler: Lock-Free Programlamanın Temelleri

### Compare-And-Swap (CAS): Atomik İşlemin Kalbi

**Donanım Seviyesi Operasyon:**
```
boolean CAS(address, expectedValue, newValue):
    ATOMIC {
        if (*address == expectedValue) {
            *address = newValue
            return true
        } else {
            return false
        }
    }
```

**Neden Önemli?**

**Lock-Based vs Lock-Free:**

**Lock-Based:**
```
lock.acquire()
try {
    if (value == expected) {
        value = newValue
    }
} finally {
    lock.release()
}
```
Problemler:
- Thread bloklanabilir (context switch)
- Deadlock riski
- Priority inversion
- Thread tutulursa (death, suspend) tüm sistem bloklanır

**Lock-Free (CAS):**
```
while (!CAS(&value, expected, newValue)) {
    expected = value  // Retry
}
```
Avantajlar:
- Thread asla bloklanmaz
- Deadlock imkansız
- Progress guarantee (obstruction-free)

**Progress Guarantees:**

1. **Blocking**: Thread'ler bloklanabilir (lock-based)
2. **Obstruction-Free**: Tek başına çalışan thread sonunda ilerler
3. **Lock-Free**: Sistem genelinde ilerleme garantisi (en az bir thread ilerler)
4. **Wait-Free**: Her thread sınırlı adımda ilerler (en güçlü)

### AtomicInteger: Lock-Free Sayaç

**İç Yapı:**
```java
private volatile int value;

public final int incrementAndGet() {
    for (;;) {
        int current = value;
        int next = current + 1;
        if (compareAndSet(current, next))
            return next;
    }
}
```

**Neden Volatile + CAS?**
- **volatile**: Her thread'in en güncel değeri görmesini garantiler (visibility)
- **CAS**: Atomik güncelleme garantisi (atomicity)

**Synchronized İle Karşılaştırma:**

**Performans:**
- **Low Contention**: CAS çok daha hızlı (lock overhead yok)
- **High Contention**: CAS spin loop → CPU tüketimi, lock park → context switch

**Scalability:**
- **CAS**: Non-blocking → paralel throughput yüksek
- **Lock**: Blocking → seri execution

### ABA Problemi ve Çözümü

**Problem Senaryosu:**
```
Thread 1: A değerini oku
Thread 2: A → B değiştir
Thread 3: B → A değiştir (aynı A değeri!)
Thread 1: CAS(A, new) → başarılı (ama ara durum gözden kaçtı!)
```

**Neden Problem?**
- Pointer'larda: A nesne serbest bırakılıp yenisi aynı adreste olabilir
- Linked list'lerde: Node pointer'ları değişmiş olabilir

**Çözüm 1: AtomicStampedReference**

Değer + versiyon damgası:
```
[Value | Stamp]
CAS(expectedValue, expectedStamp, newValue, newStamp)
```

**Çözüm 2: AtomicMarkableReference**

Değer + boolean mark:
```
[Value | Mark]
```
Mark: "Bu değer değişti mi?" flag'i

### Lock-Free Algoritmaların Zorluğu

**Michael-Scott Queue** (ConcurrentLinkedQueue'nun temeli):

Basit gibi görünen linked list queue bile karmaşık:
- Head ve tail pointer'ları atomik güncelleme
- Help-along mekanizması (yarım kalan işlemleri tamamlama)
- Memory reclamation (garbage collection olmayan dillerde çok zor)

**Neden Herkes Kendi Lock-Free Yapısını Yazmasın?**
1. Doğruluğu kanıtlamak çok zor (ince race condition'lar)
2. Memory ordering semantikleri karmaşık
3. Performance tuning gerektirir (backoff strategies, padding)

---

## Lock Mekanizmaları: Synchronized'ın Ötesi

### Synchronized'ın Sınırlamaları

**Implicit Locking (synchronized):**
```java
synchronized (lock) {
    // kritik bölge
}
```

**Kısıtlamalar:**
1. **Scope-Bound**: Block bitince otomatik unlock → manuel kontrol yok
2. **No Timeout**: Kilit alınana kadar sonsuz bekler
3. **Non-Interruptible**: Interrupt'a cevap vermez
4. **No Try-Lock**: "Kilit alınıyor mu?" sorgulaması yok
5. **Single Condition**: Tek wait-set → karmaşık senkronizasyon zor

### Lock Arayüzü: Explicit Locking

**Temel Felsefe**: Locking'i first-class operation yap

**Explicit Lock Avantajları:**

1. **Timeout Desteği:**
```java
if (lock.tryLock(1, TimeUnit.SECONDS)) {
    try { ... } finally { lock.unlock(); }
} else {
    // Zaman aşımı - alternatif aksiyon
}
```

**Kullanım**: Deadlock prevention, responsive systems

2. **Interruptible Locking:**
```java
try {
    lock.lockInterruptibly();
} catch (InterruptedException e) {
    // İptal edildi
}
```

**Kullanım**: Graceful shutdown, task cancellation

3. **Non-Blocking Attempt:**
```java
if (lock.tryLock()) {
    // Kilit alındı
} else {
    // Alınamadı, başka iş yap
}
```

**Kullanım**: Optimistic concurrency

### ReentrantLock: Reentrancy ve Fairness

**Reentrancy (Yeniden Girebilirlik):**

```java
lock.lock();
try {
    method();  // method() içinde aynı lock.lock() var
} finally {
    lock.unlock();
}
```

**Neden Gerekli?**
- Recursive methods
- Nested synchronized methods
- Callback patterns

**İç Mekanizma**: Hold count
```
Lock count = 0
Thread 1: lock() → count = 1, owner = Thread 1
Thread 1: lock() → count = 2 (aynı thread, izin verilir)
Thread 1: unlock() → count = 1
Thread 1: unlock() → count = 0, owner = null
```

**Fairness (Adalet):**

**Unfair (default):**
- Thread kilidi almaya çalıştığında direkt CAS ile kapma dener (barging)
- Kuyrukta bekleyen thread varsa bile yeni thread kilidi alabilir

**Fair:**
- FIFO sırası (AbstractQueuedSynchronizer queue)
- En uzun bekleyen thread kilidi alır

**Trade-off:**
- **Unfair**: Yüksek throughput (barging thread cache'de olabilir)
- **Fair**: Adalet, starvation önleme (ama %10-50 daha yavaş)

### ReadWriteLock: Okuma/Yazma Ayrımı

**Motivasyon**: Veri yapılarında %90 okuma, %10 yazma senaryoları

**Temel Kavram:**
- **Read Lock (Shared)**: Birden fazla thread aynı anda tutabilir
- **Write Lock (Exclusive)**: Sadece bir thread, okuyucu yokken

**Karşılıklı Dışlama Matrisi:**
```
         | Read  | Write
---------|-------|-------
Read     | OK    | NO
Write    | NO    | NO
```

**İç Mekanizma (ReentrantReadWriteLock):**

Single 32-bit integer state:
```
[16 bit: read count | 16 bit: write count]
```

**Read Lock Acquisition:**
```
if (write count == 0 || current thread owns write lock)
    increment read count
else
    wait
```

**Write Lock Acquisition:**
```
if (read count == 0 && write count == 0)
    set write count = 1
else
    wait
```

**Downgrade (Write → Read):**
```
writeLock.lock()
try {
    // Yazma yap
    readLock.lock()  // Downgrade başla
} finally {
    writeLock.unlock()  // Yazma kilidi bırak, okuma kilidi kaldı
}
```

**Upgrade (Read → Write) - DEADLOCK RİSKİ:**
```
readLock.lock()
// ...
writeLock.lock()  // DİKKAT: Deadlock olabilir!
```

Neden? İki thread okuma kilidi tutarken yazma kilidi almaya çalışırsa mutual wait.

**Çözüm**: Lock upgrade desteği yok, önce unlock sonra relock.

### StampedLock: Optimistic Reading

**Java 8'in Yeniliği**: Üçüncü mod - Optimistic Read

**Üç Mod:**

1. **Writing (Exclusive)**
2. **Reading (Shared)**
3. **Optimistic Reading (No Lock!)**

**Optimistic Reading Pattern:**
```
long stamp = lock.tryOptimisticRead();  // Kilit almadan!
// Verileri oku
if (!lock.validate(stamp)) {
    // Yazma oldu, veriler geçersiz
    stamp = lock.readLock();
    try {
        // Verileri tekrar oku
    } finally {
        lock.unlockRead(stamp);
    }
}
```

**Nasıl Çalışır?**

**Stamp**: Version number (sequence lock benzeri)
- Her yazma işlemi stamp'ı artırır
- Optimistic read stamp alır
- validate(): Stamp değişti mi kontrol eder

**Avantaj**: Okuma kilidi bile almaz → max performance

**Dezavantaj**: Validation başarısız olursa retry (optimism gereksizse overhead)

**Ne Zaman Kullanılır?**
- Okuma çok baskın (>95%)
- Yazma çok nadir
- Veri yapısı küçük (okuma ucuz, retry tolere edilebilir)

**ReadWriteLock vs StampedLock:**

| Özellik | ReadWriteLock | StampedLock |
|---------|---------------|-------------|
| Read Lock | Her zaman lock | Opsiyonel (optimistic) |
| Reentrancy | Var | YOK (dikkat!) |
| Condition | Var | YOK |
| Performance | İyi | Çok iyi (optimistic case) |

### AbstractQueuedSynchronizer (AQS): Framework'ün Kalbi

**Doug Lea'nin걸작**: Çoğu synchronizer'ın temel yapısı

**Temel Fikir**: Template Method Pattern
```
Subclass defines:
- tryAcquire() / tryRelease()
- tryAcquireShared() / tryReleaseShared()

AQS provides:
- Waiting queue (CLH lock queue)
- Blocking/unblocking
- Cancellation
- Condition support
```

**CLH Lock Queue:**
```
[Head] → [Node] → [Node] → [Tail]
         (prev)    (prev)
```

Her node:
- Thread reference
- Wait status (SIGNAL, CANCELLED, CONDITION)
- Next/prev pointers

**Acquire Pattern:**
```
1. tryAcquire() çağır (subclass implement)
2. Başarılıysa dön
3. Değilse node oluştur ve queue'ya ekle
4. Park et (LockSupport.park)
5. Unpark edilince tekrar tryAcquire()
```

**Release Pattern:**
```
1. tryRelease() çağır (subclass implement)
2. Başarılıysa queue'daki successor'ı unpark et
```

**Neden Template Method?**
- Ortak queue mantığı tekrar kullanılır
- State yönetimi (CAS vs lock) subclass'a özel
- ReentrantLock, Semaphore, CountDownLatch hepsi AQS kullanır

---

## Bellek Modeli: Görünürlük ve Sıralama Garantileri

### Java Memory Model (JMM): Soyutlama ve Gerçeklik

**Problem**: Modern CPU'lar ve derleyiciler optimizasyon yapar:

1. **CPU Caches**: Her core'un kendi cache'i → değişiklikler gecikmeli görünür
2. **Store Buffers**: Yazma işlemleri buffer'da bekler
3. **Instruction Reordering**: Derleyici ve CPU komut sırasını değiştirir

**Java'nın Garantisi**: Happens-Before ilişkileri

### Happens-Before Rules

**Temel Kurallar:**

1. **Program Order**: Tek thread içinde statements program sırasına göre happens-before
2. **Monitor Lock**: Unlock happens-before sonraki lock (aynı monitör)
3. **Volatile**: Volatile yazma happens-before sonraki okuma
4. **Thread Start**: `thread.start()` happens-before thread'in içindeki her şey
5. **Thread Termination**: Thread'deki her şey happens-before `thread.join()`
6. **Transitivity**: A hb B ve B hb C ⇒ A hb C

**Concurrent Utility Guarantileri:**

7. **Executor Submission**: `submit()` happens-before task başlangıcı
8. **Future Get**: Task tamamlanması happens-before `future.get()` dönüşü
9. **Latch Countdown**: `countDown()` happens-before `await()` dönüşü
10. **Semaphore Release**: `release()` happens-before `acquire()` dönüşü

### Volatile: Lightweight Synchronization

**Volatile Garantileri:**

1. **Visibility**: Yazma tüm thread'ler tarafından hemen görülür
2. **Ordering**: Volatile yazma öncesi işlemler volatile yazma sonrası işlemlerden önce olur (memory barrier)

**Volatile vs Synchronized:**

| Özellik | Volatile | Synchronized |
|---------|----------|--------------|
| Atomicity | Sadece read/write | Compound operations |
| Mutual Exclusion | YOK | VAR |
| Happens-Before | VAR | VAR |
| Performance | Çok hafif | Orta (kilitleme) |

**Kullanım Pattern: Flag**
```java
volatile boolean shutdownRequested;

// Thread 1
shutdownRequested = true;

// Thread 2
while (!shutdownRequested) {
    // Hemen görecek (volatile sayesinde)
}
```

**Kullanılamayacağı Durum: Non-Atomic Operations**
```java
volatile int counter = 0;
counter++;  // Atomik DEĞİL! (read, increment, write üç işlem)
```

### Memory Barriers (Fences)

**Donanım Seviyesi Operasyonlar:**

1. **LoadLoad Barrier**: Load1; LoadLoad; Load2
   - Load2 ve sonraki load'lar Load1'den önce yapılamaz

2. **StoreStore Barrier**: Store1; StoreStore; Store2
   - Store2 ve sonraki store'lar Store1'den önce yapılamaz

3. **LoadStore Barrier**: Load1; LoadStore; Store2
   - Store2 ve sonraki store'lar Load1'den önce yapılamaz

4. **StoreLoad Barrier**: Store1; StoreLoad; Load2
   - Load2 ve sonraki load'lar Store1'den önce yapılamaz (en pahalı!)

**Java'da Nereye Konur?**

- **volatile write**: Öncesinde StoreStore, sonrasında StoreLoad
- **volatile read**: Sonrasında LoadLoad ve LoadStore
- **synchronized enter**: Sonrasında tüm barrier'lar
- **synchronized exit**: Öncesinde tüm barrier'lar

### False Sharing: Cache Line Problemi

**Problem:**

CPU cache line'ları (genelde 64 byte) atomic birimdir.
```
Cache Line: [Thread 1'in değişkeni | Thread 2'nin değişkeni]
```

Thread 1 kendi değişkenini değiştirdiğinde:
- Tüm cache line invalidate olur
- Thread 2'nin cache'i de invalidate olur
- Thread 2 reload yapar

**Sonuç**: Hiç paylaşılmayan veriler bile contention yaratır!

**Çözüm: Padding**
```java
class PaddedCounter {
    volatile long value;
    long p1, p2, p3, p4, p5, p6, p7;  // Padding (56 bytes)
}
```

Her counter kendi cache line'ında → false sharing yok.

**Java 8: @Contended Annotation**
```java
@Contended
class Counter {
    volatile long value;
}
```

JVM otomatik padding ekler.

### Final Field Semantics

**Special Guarantee**: Final field'lar constructor tamamlandıktan sonra tüm thread'lere görünür

**Neden Önemli?**

Immutable objects thread-safe olur:
```java
class ImmutableData {
    final int x;
    final int y;

    ImmutableData(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

// Thread 1
ImmutableData data = new ImmutableData(1, 2);
reference = data;  // Publish

// Thread 2
ImmutableData d = reference;
// d.x ve d.y garantili görünür (final sayesinde)
```

**Safe Publication**: Final field'lar sayesinde immutable objects ekstra senkronizasyon olmadan paylaşılabilir.

---

## Performans ve Scalability: Teorik Analiz

### Amdahl's Law: Paralel Performans Sınırı

**Formül:**
```
Speedup(N) = 1 / ((1 - P) + P/N)

P: Paralel yapılabilir kısım oranı
N: İşlemci sayısı
```

**Çıkarım:**
- %50 paralel → Max 2x speedup (sonsuz core'da)
- %90 paralel → Max 10x speedup
- %99 paralel → Max 100x speedup

**Eşzamanlı Programlama İçin Anlam:**
- Synchronization overhead'i minimize et (P'yi artır)
- Sequential bottleneck'leri belirle

### Contention ve Scalability

**Contention**: Birden fazla thread aynı kaynağa erişmek için yarışır

**Lock Contention Modellemesi:**
```
Throughput = N / (Sequential_Time + Contention_Time * N)
```

**Scalability Metrikleri:**

1. **Strong Scaling**: İş sabitse N arttıkça süre azalmalı
2. **Weak Scaling**: İş de N ile artarsa süre sabit kalmalı

**Contention Azaltma Stratejileri:**

1. **Lock Splitting**: Tek lock → birden fazla lock (bağımsız veri için)
2. **Lock Striping**: Veri bölümlenmiş, her bölüm kendi lock'u (ConcurrentHashMap)
3. **Lock-Free**: CAS operasyonları (AtomicInteger)
4. **Immutability**: Değiştirilemez veri paylaşılır (kilitleme gerekmez)

### Universal Scalability Law (USL)

Amdahl'ın genelleştirilmesi (contention ve coherency dahil):
```
Speedup(N) = N / (1 + α(N-1) + βN(N-1))

α: Contention coefficient
β: Coherency coefficient (cache invalidation)
```

**Çıkarım**: N arttıkça bir noktadan sonra performans düşer (retrograde)

**Concurrent Collection Tasarımının Bu Yasalara Göre Optimizasyonu:**
- ConcurrentHashMap: Düşük α (lock striping), düşük β (write nadir)
- CopyOnWriteArrayList: Yüksek α (write seri), ama read için α=0

---

## Tasarım İlkeleri ve Best Practices: Teorik Temel

### Immutability: En Güçlü Thread-Safety

**Teorem**: Immutable objects doğası gereği thread-safe'tir.

**Neden?**
- State değişmez → race condition imkansız
- Görünürlük problemi yok (final field guarantee)
- Defensive copy gerekmez

**Effectiv Immutability:**
- Constructor'da initialize et
- Final field'lar kullan
- Referansları leak etme

### Confinement: Paylaşmayı Önle

**İki Tür:**

1. **Thread Confinement**: Nesne sadece tek thread'de yaşar
   - ThreadLocal variables
   - Stack confinement (local variables)

2. **Object Confinement**: Nesne başka nesnenin içine hapsedilir
   - Private field + proper locking

**Teorem**: Shared mutable state yoksa senkronizasyon gerekmez.

### Delegation: Composition ile Thread-Safety

**Stategy**: Thread-safe bileşenlerden thread-safe yapı oluştur

**Koşul**: Bileşenler arasında invariant yoksa delegation yeterli.

**Örnek:**
```
ConcurrentMap<String, AtomicInteger> counters;
```
Her counter bağımsız → map thread-safe, counter thread-safe → sistem thread-safe

**Dikkat**: Invariant varsa (örn: counter1 + counter2 = 100) ek kilitleme gerekir.

### Trade-off Analysis: Hangi Mekanizmayı Ne Zaman?

**Decision Tree:**

```
Veri hiç değişmiyor mu?
├─ Evet → Immutable (en hızlı)
└─ Hayır
    ├─ Tek değişken atomik operasyon mu?
    │   ├─ Evet → AtomicXXX (lock-free)
    │   └─ Hayır
    │       ├─ Okuma >> Yazma mı?
    │       │   ├─ Evet
    │       │   │   ├─ Nadiren yazılıyor mu? → CopyOnWriteArrayList
    │       │   │   └─ Sık yazılıyor → ReadWriteLock veya StampedLock
    │       │   └─ Hayır
    │       │       ├─ Yüksek contention var mı?
    │       │       │   ├─ Evet → ConcurrentHashMap, Lock striping
    │       │       │   └─ Hayır → ReentrantLock veya synchronized
    │       │       └─ Koleksiyon mu?
    │       │           ├─ Map → ConcurrentHashMap
    │       │           ├─ Queue → BlockingQueue veya ConcurrentLinkedQueue
    │       │           └─ List/Set → CopyOnWrite vs Collections.synchronized
```

---

## Sonuç: Eşzamanlı Programlamanın Büyük Resmi

### Temel Prensipler

1. **Safety First**: Doğruluk performanstan önce gelir
2. **Simplicity**: Basit tasarım hata olasılığını azaltır
3. **Measured Optimization**: Profiling olmadan optimize etme
4. **Documentation**: Eşzamanlılık kararları belgelenmeli

### Java.util.concurrent'in Katkısı

**Tarihsel Perspektif:**
- Java 1.0-1.4: Sadece synchronized, wait/notify (low-level, error-prone)
- Java 5 (JSR-166): Doug Lea'nın concurrent utilities (game changer)
- Java 7+: ForkJoin, Phaser (parallelism için)
- Java 8+: CompletableFuture, StampedLock (async ve performance)

**Felsefi Değişim:**
- **Öncesi**: Thread'ler ve lock'lar (mechanism-focused)
- **Sonrası**: Görevler, queue'lar, senkronizasyon primitive'leri (problem-focused)

### Concurrent Programming Mental Model

**Düşünme Şekli:**

1. **Identify Shared State**: Ne paylaşılıyor?
2. **Define Invariants**: Hangi kurallar her zaman geçerli olmalı?
3. **Choose Coordination**: Nasıl koordine edilecek?
4. **Ensure Visibility**: Değişiklikler nasıl görünecek?
5. **Test Concurrency**: Race condition'ları nasıl yakalayacağız?

**Antipatterns'den Kaçın:**
- Double-checked locking (without volatile - broken)
- Mutable statics without synchronization
- Assuming atomicity without guarantee
- Lock ordering violations (deadlock)

---

## Kaynaklar: Derin Anlayış İçin

### Klasik Kitaplar
- **Java Concurrency in Practice** (Brian Goetz) - Endüstri standardı
- **The Art of Multiprocessor Programming** (Herlihy & Shavit) - Teorik temel
- **Concurrent Programming in Java** (Doug Lea) - Tarihsel perspektif

### Akademik Makaleler
- JSR-166 (Java Concurrency Utilities) - Doug Lea
- Java Memory Model (JSR-133) - Semantikler
- AQS Paper - AbstractQueuedSynchronizer detayları

### Online Kaynaklar
- Doug Lea's Concurrent Programming Page
- Java Language Specification (Chapter 17: Threads and Locks)
- OpenJDK Concurrent Source Code - Implementasyon detayları

---

**Doküman Özeti**: Bu dokümantasyon, java.util.concurrent paketini kod örneklerinden ziyade **kavramsal temeller, tasarım kararları, ve teorik mantık** üzerine inşa eder. Her yapının **neden** var olduğu, **nasıl** çalıştığı, ve **ne zaman** kullanılacağı anlatılır.

**Hedef**: Sadece API kullanmayı değil, **eşzamanlı programlamanın derinliklerini anlamayı** sağlamak.

---

**Son Güncelleme**: 2026-02-06
**Yaklaşım**: Teorik ve Kavramsal
**Seviye**: İleri
**Dil**: Türkçe
