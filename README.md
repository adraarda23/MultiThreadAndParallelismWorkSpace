# MultiThreadAndParallelismWorkSpace

## Proje Hakkında

Bu proje, Java'da **Multi-Threading (Çok İş Parçacıklı Programlama)** ve **Parallelism (Paralelizm)** konularını öğrenmek, uygulamak ve deneyimlemek amacıyla oluşturulmuş bir çalışma alanıdır.

## Amaç

Modern yazılım geliştirmede, performans ve verimlilik için concurrency (eşzamanlılık) ve parallelism (paralelizm) kavramlarını anlamak kritik öneme sahiptir. Bu workspace:

- Thread oluşturma ve yönetimi
- Thread yaşam döngüsü (lifecycle)
- Senkronizasyon mekanizmaları
- Thread güvenliği (thread safety)
- Deadlock, livelock ve race condition problemleri
- Java Concurrency API'leri (ExecutorService, CompletableFuture, vb.)
- Parallel Streams
- Fork/Join Framework
- Thread Pool'lar ve yönetimi
- Atomic değişkenler ve CAS işlemleri
- Concurrent Collections

gibi konularda pratik yapma imkanı sunar.

## Teknolojiler

- **Java Version:** 24
- **Build Tool:** Maven
- **IDE:** IntelliJ IDEA

## Proje Yapısı

```
src/
├── main/
│   ├── java/
│   │   └── org/
│   │       └── example/
│   │           ├── basics/          # Thread temelleri
│   │           ├── synchronization/ # Senkronizasyon örnekleri
│   │           ├── executors/       # Executor framework kullanımı
│   │           ├── concurrent/      # Concurrent collections
│   │           ├── parallelism/     # Parallel streams ve fork/join
│   │           └── problems/        # Klasik concurrency problemleri
│   └── resources/
└── test/
    ├── java/
    └── resources/
```

## Başlangıç

### Gereksinimler

- JDK 24 veya üzeri
- Maven 3.6+
- IntelliJ IDEA (önerilen)

### Kurulum

```bash
# Projeyi klonlayın
git clone <repo-url>

# Proje dizinine gidin
cd MultiThreadAndParallelismWorkSpace

# Maven bağımlılıklarını yükleyin
mvn clean install

# Projeyi çalıştırın
mvn exec:java -Dexec.mainClass="org.example.Main"
```

## Çalışma Planı

1. **Temel Kavramlar**
   - Thread nedir?
   - Process vs Thread
   - Thread oluşturma yöntemleri (Thread class, Runnable interface)

2. **Senkronizasyon**
   - synchronized anahtar kelimesi
   - Lock'lar ve ReentrantLock
   - Volatile değişkenler
   - wait(), notify(), notifyAll()

3. **Executor Framework**
   - ExecutorService kullanımı
   - Thread Pool'lar
   - Callable ve Future
   - CompletableFuture

4. **Concurrent Collections**
   - ConcurrentHashMap
   - CopyOnWriteArrayList
   - BlockingQueue implementasyonları

5. **Parallelism**
   - Parallel Streams
   - Fork/Join Framework
   - RecursiveTask ve RecursiveAction

6. **Best Practices ve Anti-Patterns**
   - Thread safety garantileme
   - Deadlock'tan kaçınma
   - Performance tuning

## Notlar

Bu bir öğrenme ve pratik yapma projesidir. Kod örnekleri ve deneyler zaman içinde eklenecek ve güncellenecektir.

## Kaynaklar

- [Java Concurrency in Practice - Brian Goetz](https://jcip.net/)
- [Oracle Java Concurrency Tutorial](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
- [Java Memory Model](https://docs.oracle.com/javase/specs/jls/se17/html/jls-17.html#jls-17.4)

## Lisans

Bu proje kişisel öğrenme amaçlıdır.
