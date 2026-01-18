# ⚡ Performance Tuning Guide

Panduan lengkap optimasi performa sistem microservices perpustakaan.

## 📑 Daftar Isi

- [Performance Metrics](#performance-metrics)
- [JVM Tuning](#jvm-tuning)
- [Database Optimization](#database-optimization)
- [Caching Strategies](#caching-strategies)
- [Connection Pooling](#connection-pooling)
- [Async Processing](#async-processing)
- [Load Testing](#load-testing)

---

## Performance Metrics

### Key Performance Indicators (KPIs)

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Response Time (p95)** | < 200ms | 95% of requests under 200ms |
| **Response Time (p99)** | < 500ms | 99% of requests under 500ms |
| **Throughput** | > 1000 req/s | Requests per second |
| **Error Rate** | < 0.1% | Percentage of failed requests |
| **CPU Usage** | < 70% | Average CPU utilization |
| **Memory Usage** | < 80% | Heap memory utilization |
| **GC Pause Time** | < 100ms | Garbage collection pause |

### Monitoring Response Times

```java
@Aspect
@Component
@Slf4j
public class PerformanceMonitoringAspect {
    
    private final MeterRegistry meterRegistry;
    
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);
        String methodName = joinPoint.getSignature().getName();
        
        try {
            Object result = joinPoint.proceed();
            
            sample.stop(Timer.builder("api.response.time")
                .tag("method", methodName)
                .tag("status", "success")
                .register(meterRegistry));
            
            return result;
            
        } catch (Exception e) {
            sample.stop(Timer.builder("api.response.time")
                .tag("method", methodName)
                .tag("status", "error")
                .register(meterRegistry));
            throw e;
        }
    }
}
```

---

## JVM Tuning

### JVM Arguments

**Development**:
```bash
JAVA_OPTS="
  -Xms512m 
  -Xmx1g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/tmp/heap-dump.hprof
"
```

**Production**:
```bash
JAVA_OPTS="
  -Xms2g 
  -Xmx4g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=100
  -XX:G1HeapRegionSize=16m
  -XX:InitiatingHeapOccupancyPercent=45
  -XX:+ParallelRefProcEnabled
  -XX:+UseStringDeduplication
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/var/log/heap-dump.hprof
  -XX:+PrintGCDetails
  -XX:+PrintGCTimeStamps
  -Xloggc:/var/log/gc.log
  -XX:+UseGCLogFileRotation
  -XX:NumberOfGCLogFiles=10
  -XX:GCLogFileSize=10M
"
```

### Garbage Collection Tuning

**G1GC Configuration** (Recommended):
```properties
# G1 Garbage Collector (Good balance)
-XX:+UseG1GC
-XX:MaxGCPauseMillis=100
-XX:G1HeapRegionSize=16m
-XX:InitiatingHeapOccupancyPercent=45
```

**ZGC Configuration** (Ultra-low latency):
```properties
# ZGC (Java 15+, very low pause times)
-XX:+UseZGC
-XX:ZCollectionInterval=120
-XX:ZAllocationSpikeTolerance=5
```

### Monitoring GC

**Enable GC logging**:
```bash
-Xlog:gc*:file=/var/log/gc.log:time,uptime:filecount=10,filesize=10M
```

**Analyze with GCViewer**:
```bash
# Download GCViewer
wget https://github.com/chewiebug/GCViewer/releases/download/1.36/gcviewer-1.36.jar

# Analyze GC log
java -jar gcviewer-1.36.jar /var/log/gc.log
```

---

## Database Optimization

### MongoDB Query Optimization

**1. Use Indexes**:
```javascript
// Bad - Full collection scan
db.anggota_read.find({ nama: "John" });

// Good - Index scan
db.anggota_read.createIndex({ nama: 1 });
db.anggota_read.find({ nama: "John" });

// Even better - Compound index
db.anggota_read.createIndex({ nama: 1, createdAt: -1 });
db.anggota_read.find({ nama: "John" }).sort({ createdAt: -1 });
```

**2. Projection (Select specific fields)**:
```java
// Bad - Fetch all fields
List<AnggotaQuery> anggota = queryRepository.findAll();

// Good - Fetch only needed fields
@Query(value = "{}", fields = "{ 'nama': 1, 'email': 1 }")
List<AnggotaProjection> findAllNamesAndEmails();

interface AnggotaProjection {
    String getNama();
    String getEmail();
}
```

**3. Pagination**:
```java
// Bad - Load all data
List<AnggotaQuery> all = queryRepository.findAll();

// Good - Paginated query
Page<AnggotaQuery> page = queryRepository.findAll(
    PageRequest.of(0, 20, Sort.by("nama").ascending())
);
```

**4. Aggregation Pipeline**:
```java
@Aggregation(pipeline = {
    "{ '$match': { 'status': 'DIPINJAM' } }",
    "{ '$group': { '_id': '$anggotaId', 'count': { '$sum': 1 } } }",
    "{ '$sort': { 'count': -1 } }",
    "{ '$limit': 10 }"
})
List<AnggotaPeminjamanCount> findTopBorrowers();
```

### H2 Query Optimization

**1. Use Prepared Statements**:
```java
// Bad - Vulnerable to SQL injection + not cached
String sql = "SELECT * FROM anggota WHERE nama = '" + nama + "'";

// Good - Safe + cached
@Query("SELECT a FROM AnggotaCommand a WHERE a.nama = :nama")
Optional<AnggotaCommand> findByNama(@Param("nama") String nama);
```

**2. Batch Operations**:
```java
// Bad - Multiple DB calls
for (AnggotaCommand anggota : anggotaList) {
    commandRepository.save(anggota);
}

// Good - Single batch operation
commandRepository.saveAll(anggotaList);
```

**3. Lazy Loading**:
```java
@OneToMany(fetch = FetchType.LAZY)
private List<Peminjaman> peminjamanList;

// Only load when accessed
anggota.getPeminjamanList().size();
```

---

## Caching Strategies

### Spring Cache Configuration

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "anggota", "buku", "peminjaman"
        );
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }
    
    Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats();
    }
}
```

### Cache Usage

```java
@Service
public class AnggotaQueryService {
    
    // Cache result
    @Cacheable(value = "anggota", key = "#id")
    public AnggotaQuery getById(String id) {
        return queryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not found"));
    }
    
    // Evict cache on update
    @CacheEvict(value = "anggota", key = "#id")
    public void updateAnggota(String id, UpdateAnggotaCommand command) {
        // Update logic
    }
    
    // Clear all cache
    @CacheEvict(value = "anggota", allEntries = true)
    public void clearCache() {
        log.info("Anggota cache cleared");
    }
}
```

### Redis Cache (Production)

```properties
# Redis configuration
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379
spring.cache.redis.time-to-live=1800000
spring.cache.redis.cache-null-values=false
```

```java
@Configuration
public class RedisCacheConfig {
    
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .disableCachingNullValues()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            );
    }
}
```

---

## Connection Pooling

### HikariCP (Default for Spring Boot)

```properties
# Connection pool size
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10

# Connection timeouts
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=600000

# Performance tuning
spring.datasource.hikari.leak-detection-threshold=60000
spring.datasource.hikari.auto-commit=true
spring.datasource.hikari.pool-name=AnggotaHikariPool
```

### MongoDB Connection Pool

```properties
# MongoDB connection pool
spring.data.mongodb.max-pool-size=50
spring.data.mongodb.min-pool-size=10
spring.data.mongodb.max-wait-time=5000ms
spring.data.mongodb.max-connection-idle-time=60000ms
spring.data.mongodb.max-connection-life-time=120000ms
```

### RabbitMQ Connection Pool

```properties
# RabbitMQ connection caching
spring.rabbitmq.cache.connection.mode=channel
spring.rabbitmq.cache.connection.size=25
spring.rabbitmq.cache.channel.size=10
spring.rabbitmq.cache.channel.checkout-timeout=0

# Connection pool
spring.rabbitmq.requested-heartbeat=60
spring.rabbitmq.connection-timeout=30000
```

---

## Async Processing

### Async Configuration

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

### Async Methods

```java
@Service
public class NotificationService {
    
    @Async("taskExecutor")
    public CompletableFuture<Void> sendWelcomeEmail(String email, String name) {
        log.info("Sending welcome email to: {}", email);
        
        // Simulate email sending
        try {
            Thread.sleep(1000);
            emailService.send(email, "Welcome", "Welcome " + name);
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
}
```

### CompletableFuture

```java
@Service
public class PeminjamanService {
    
    public PeminjamanDetailDTO getPeminjamanDetail(String id) {
        // Fetch peminjaman
        Peminjaman peminjaman = peminjamanRepository.findById(id)
            .orElseThrow();
        
        // Fetch anggota and buku in parallel
        CompletableFuture<AnggotaDTO> anggotaFuture = CompletableFuture
            .supplyAsync(() -> anggotaService.getById(peminjaman.getAnggotaId()));
        
        CompletableFuture<BukuDTO> bukuFuture = CompletableFuture
            .supplyAsync(() -> bukuService.getById(peminjaman.getBukuId()));
        
        // Wait for both to complete
        CompletableFuture.allOf(anggotaFuture, bukuFuture).join();
        
        return new PeminjamanDetailDTO(
            peminjaman,
            anggotaFuture.join(),
            bukuFuture.join()
        );
    }
}
```

---

## Load Testing

### Apache JMeter

**Test Plan**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan>
      <stringProp name="TestPlan.user_defined_variables"/>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <stringProp name="TestPlan.comments">Load Test Plan</stringProp>
    </TestPlan>
    
    <hashTree>
      <ThreadGroup>
        <stringProp name="ThreadGroup.num_threads">100</stringProp>
        <stringProp name="ThreadGroup.ramp_time">10</stringProp>
        <longProp name="ThreadGroup.duration">300</longProp>
      </ThreadGroup>
      
      <hashTree>
        <HTTPSamplerProxy>
          <stringProp name="HTTPSampler.domain">localhost</stringProp>
          <stringProp name="HTTPSampler.port">8080</stringProp>
          <stringProp name="HTTPSampler.path">/api/anggota</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
        </HTTPSamplerProxy>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

### Gatling

**Simulation Script**:
```scala
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class AnggotaLoadTest extends Simulation {
  
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
  
  val scn = scenario("Get All Anggota")
    .exec(http("Get Anggota")
      .get("/api/anggota")
      .check(status.is(200)))
  
  setUp(
    scn.inject(
      rampUsers(100) during (10 seconds),
      constantUsersPerSec(50) during (5 minutes)
    )
  ).protocols(httpProtocol)
}
```

### k6 Load Testing

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '30s', target: 20 },
    { duration: '1m', target: 100 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  let res = http.get('http://localhost:8080/api/anggota');
  
  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });
  
  sleep(1);
}
```

**Run test**:
```bash
k6 run --vus 100 --duration 5m load-test.js
```

---

## Performance Best Practices

### 1. Database Queries

✅ **Do**:
- Use indexes on frequently queried fields
- Use pagination for large datasets
- Use projection to fetch only needed fields
- Use batch operations
- Cache frequently accessed data

❌ **Don't**:
- N+1 query problem
- Fetch all data without pagination
- Use SELECT * when not needed
- Execute queries in loops

### 2. API Design

✅ **Do**:
- Implement pagination
- Support field filtering
- Use HTTP caching headers
- Implement rate limiting
- Use async endpoints for long operations

❌ **Don't**:
- Return large payloads without pagination
- Block on I/O operations
- Expose internal data structures directly

### 3. Memory Management

✅ **Do**:
- Use connection pools
- Close resources properly
- Use weak references for caches
- Monitor heap usage
- Profile memory leaks

❌ **Don't**:
- Create unnecessary objects
- Hold references to large objects
- Ignore memory warnings
- Use String concatenation in loops

### 4. Concurrency

✅ **Do**:
- Use thread pools
- Implement async processing
- Use CompletableFuture for parallel ops
- Configure proper pool sizes
- Handle thread interruptions

❌ **Don't**:
- Create threads manually
- Block threads unnecessarily
- Use synchronized everywhere
- Ignore thread safety

---

## Performance Monitoring

### Prometheus Queries

```promql
# Response time (p95)
histogram_quantile(0.95, 
  sum(rate(http_server_requests_seconds_bucket[5m])) by (le, uri)
)

# Throughput (requests per second)
sum(rate(http_server_requests_seconds_count[5m])) by (uri)

# Error rate
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) 
/ 
sum(rate(http_server_requests_seconds_count[5m]))

# Memory usage
jvm_memory_used_bytes{area="heap"} 
/ 
jvm_memory_max_bytes{area="heap"} * 100

# GC pause time
rate(jvm_gc_pause_seconds_sum[5m])
```

### Grafana Dashboards

**Import JVM Dashboard**:
- Dashboard ID: `4701`
- Name: "JVM (Micrometer)"
- Data Source: Prometheus

**Custom Panels**:
- Response Time (p95, p99)
- Throughput
- Error Rate
- CPU Usage
- Memory Usage
- GC Pause Time
- Thread Count

---

[⬅️ Back to Documentation Index](README.md)