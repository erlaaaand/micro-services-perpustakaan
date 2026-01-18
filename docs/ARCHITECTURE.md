# 🏛️ System Architecture

Dokumentasi lengkap arsitektur sistem microservices perpustakaan.

## 📑 Daftar Isi

- [Overview](#overview)
- [CQRS Pattern](#cqrs-pattern)
- [Event-Driven Architecture](#event-driven-architecture)
- [Service Discovery](#service-discovery)
- [API Gateway Pattern](#api-gateway-pattern)
- [Circuit Breaker Pattern](#circuit-breaker-pattern)
- [Database Architecture](#database-architecture)

---

## Overview

Sistem ini dibangun dengan arsitektur microservices modern yang mengimplementasikan berbagai design patterns untuk mencapai:

- **Scalability**: Setiap service dapat di-scale independent
- **Resilience**: Fault tolerance dengan circuit breaker
- **Observability**: Comprehensive monitoring dan tracing
- **Maintainability**: Clear separation of concerns
- **Performance**: Optimized read/write operations dengan CQRS

### Key Architectural Patterns

1. **CQRS (Command Query Responsibility Segregation)**
2. **Event-Driven Architecture**
3. **Service Discovery**
4. **API Gateway**
5. **Circuit Breaker**
6. **Eventual Consistency**

---

## CQRS Pattern

### Konsep CQRS

CQRS memisahkan operasi **Command** (Write) dan **Query** (Read) menggunakan model data yang berbeda.

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT REQUEST                        │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
          ┌───────────────────────────────┐
          │      API Gateway (8080)       │
          └───────────────────────────────┘
                          │
          ┌───────────────┴────────────────┐
          │                                │
          ▼                                ▼
┌─────────────────┐              ┌─────────────────┐
│    COMMAND      │              │     QUERY       │
│   (Write/H2)    │              │  (Read/Mongo)   │
│                 │              │                 │
│ - Create        │              │ - Get by ID     │
│ - Update        │──RabbitMQ───▶│ - Get All       │
│ - Delete        │   Events     │ - Search        │
└─────────────────┘              └─────────────────┘
```

### Implementasi

#### Command Side (Write Model)

**Database**: H2 (In-Memory) - Optimized for write operations
**Responsibilities**:
- Handle CREATE, UPDATE, DELETE operations
- Validate business rules
- Persist to write database
- Publish events to RabbitMQ

**Contoh Command Handler**:
```java
@Service
public class AnggotaCommandService {
    
    @Autowired
    private AnggotaCommandRepository commandRepository;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Transactional
    public AnggotaCommand createAnggota(CreateAnggotaCommand command) {
        // 1. Validate
        validateCommand(command);
        
        // 2. Create entity
        AnggotaCommand anggota = new AnggotaCommand();
        anggota.setNomorAnggota(command.getNomorAnggota());
        anggota.setNama(command.getNama());
        // ... set other fields
        
        // 3. Save to write database
        AnggotaCommand saved = commandRepository.save(anggota);
        
        // 4. Publish event
        AnggotaCreatedEvent event = new AnggotaCreatedEvent(saved);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.ROUTING_KEY,
            event
        );
        
        return saved;
    }
}
```

#### Query Side (Read Model)

**Database**: MongoDB - Optimized for read operations
**Responsibilities**:
- Handle GET/SEARCH operations
- Denormalized data structure
- Fast query performance
- Updated via event synchronization

**Contoh Query Handler**:
```java
@Service
public class AnggotaQueryService {
    
    @Autowired
    private AnggotaQueryRepository queryRepository;
    
    public Page<AnggotaQuery> getAllAnggota(Pageable pageable) {
        return queryRepository.findAll(pageable);
    }
    
    public Optional<AnggotaQuery> getAnggotaById(String id) {
        return queryRepository.findById(id);
    }
    
    public List<AnggotaQuery> searchByNama(String nama) {
        return queryRepository.findByNamaContainingIgnoreCase(nama);
    }
}
```

### Event Synchronization

**Event Listener** untuk sinkronisasi Read Model:
```java
@Service
public class AnggotaEventListener {
    
    @Autowired
    private AnggotaQueryRepository queryRepository;
    
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleAnggotaEvent(AnggotaEvent event) {
        switch (event.getEventType()) {
            case CREATED:
                handleCreated(event);
                break;
            case UPDATED:
                handleUpdated(event);
                break;
            case DELETED:
                handleDeleted(event);
                break;
        }
    }
    
    private void handleCreated(AnggotaEvent event) {
        AnggotaQuery query = new AnggotaQuery();
        query.setId(event.getAggregateId());
        query.setNomorAnggota(event.getNomorAnggota());
        query.setNama(event.getNama());
        // ... map other fields
        
        queryRepository.save(query);
    }
}
```

### Keuntungan CQRS

✅ **Performance**
- Write optimized untuk transactional consistency
- Read optimized untuk query performance
- Independent scaling

✅ **Flexibility**
- Different data models untuk read/write
- Multiple read models untuk different use cases

✅ **Scalability**
- Scale read/write independently
- Read replicas untuk high traffic

✅ **Event Sourcing Ready**
- Event-driven architecture
- Audit trail dari events

---

## Event-Driven Architecture

### RabbitMQ Message Flow

```
┌──────────────────────────────────────────────────────────┐
│                    EVENT FLOW                            │
└──────────────────────────────────────────────────────────┘

Command Service                RabbitMQ                Read Service
┌─────────────┐              ┌─────────┐              ┌─────────────┐
│             │              │         │              │             │
│  CREATE/    │    Publish   │ Exchange│   Consume    │   Update    │
│  UPDATE/    │─────Event───▶│    +    │────Event────▶│   MongoDB   │
│  DELETE     │              │  Queue  │              │ Read Model  │
│             │              │         │              │             │
└─────────────┘              └─────────┘              └─────────────┘
```

### Exchange & Queue Configuration

Setiap service memiliki dedicated exchange dan queue:

| Service | Exchange | Queue | Routing Key |
|---------|----------|-------|-------------|
| Anggota | `anggota-exchange` | `anggota-sync-queue` | `anggota.routing.key` |
| Buku | `buku-exchange` | `buku-sync-queue` | `buku.routing.key` |
| Peminjaman | `peminjaman-exchange` | `peminjaman-sync-queue` | `peminjaman.routing.key` |
| Pengembalian | `pengembalian-exchange` | `pengembalian-sync-queue` | `pengembalian.routing.key` |

### Event Structure

```json
{
  "eventId": "uuid-v4",
  "eventType": "ANGGOTA_CREATED",
  "timestamp": "2024-01-15T10:30:00Z",
  "aggregateId": "1",
  "payload": {
    "nomorAnggota": "A001",
    "nama": "John Doe",
    "alamat": "Jl. Merdeka No. 123",
    "email": "john@example.com"
  }
}
```

### Event Types

**Anggota Events**:
- `ANGGOTA_CREATED`
- `ANGGOTA_UPDATED`
- `ANGGOTA_DELETED`

**Buku Events**:
- `BUKU_CREATED`
- `BUKU_UPDATED`
- `BUKU_DELETED`

**Peminjaman Events**:
- `PEMINJAMAN_CREATED`
- `PEMINJAMAN_UPDATED`
- `PEMINJAMAN_STATUS_CHANGED`

**Pengembalian Events**:
- `PENGEMBALIAN_CREATED`
- `DENDA_CALCULATED`

---

## Service Discovery

### Eureka Server Architecture

```
┌─────────────────────────────────────────┐
│         Eureka Server (8761)            │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │    Service Registry               │ │
│  │                                   │ │
│  │  - api-gateway (8080)             │ │
│  │  - service-anggota (8081)         │ │
│  │  - service-buku (8082)            │ │
│  │  - service-peminjaman (8083)      │ │
│  │  - service-pengembalian (8084)    │ │
│  └───────────────────────────────────┘ │
└─────────────────────────────────────────┘
          ▲              │
          │              │
   Registration    Service Discovery
          │              ▼
    ┌─────────────────────────┐
    │    Microservices        │
    └─────────────────────────┘
```

### Registration Process

1. Service starts up
2. Registers itself to Eureka dengan:
   - Service Name
   - Host/IP Address
   - Port
   - Health Check URL
3. Sends heartbeat setiap 30 detik
4. Eureka marks service sebagai DOWN jika heartbeat hilang

### Service Discovery Process

1. Client queries Eureka untuk service location
2. Eureka returns list of available instances
3. Client-side load balancing memilih instance
4. Request dikirim ke selected instance

---

## API Gateway Pattern

### Gateway Responsibilities

```
┌───────────────────────────────────────────────┐
│           API Gateway (8080)                  │
│                                               │
│  ┌─────────────────────────────────────────┐ │
│  │  Routing                                │ │
│  │  - Route requests ke services           │ │
│  │  - Path-based routing                   │ │
│  └─────────────────────────────────────────┘ │
│                                               │
│  ┌─────────────────────────────────────────┐ │
│  │  Load Balancing                         │ │
│  │  - Distribute load across instances     │ │
│  │  - Round-robin / Custom algorithms      │ │
│  └─────────────────────────────────────────┘ │
│                                               │
│  ┌─────────────────────────────────────────┐ │
│  │  Circuit Breaker                        │ │
│  │  - Fault tolerance                      │ │
│  │  - Fallback responses                   │ │
│  └─────────────────────────────────────────┘ │
│                                               │
│  ┌─────────────────────────────────────────┐ │
│  │  Security (Future)                      │ │
│  │  - Authentication                       │ │
│  │  - Authorization                        │ │
│  │  - Rate limiting                        │ │
│  └─────────────────────────────────────────┘ │
└───────────────────────────────────────────────┘
```

### Route Configuration

Routes dikonfigurasi di `application.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: service-anggota
          uri: lb://service-anggota
          predicates:
            - Path=/api/anggota/**
          filters:
            - name: CircuitBreaker
              args:
                name: anggotaCircuitBreaker
                fallbackUri: forward:/fallback/anggota
```

---

## Circuit Breaker Pattern

### Resilience4j Implementation

```
┌────────────────────────────────────────┐
│         Circuit Breaker States         │
│                                        │
│  ┌──────────┐                         │
│  │  CLOSED  │                         │
│  │ (Normal) │                         │
│  └─────┬────┘                         │
│        │                              │
│    Failures                           │
│    exceed                             │
│    threshold                          │
│        │                              │
│        ▼                              │
│  ┌──────────┐     Wait      ┌───────┐│
│  │   OPEN   │──────────────▶│ HALF  ││
│  │ (Blocked)│   duration    │ OPEN  ││
│  └──────────┘               └───┬───┘│
│        ▲                        │    │
│        │        Success calls   │    │
│        │        < threshold     │    │
│        └────────────────────────┘    │
└────────────────────────────────────────┘
```

### Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      anggotaCircuitBreaker:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
        permitted-number-of-calls-in-half-open-state: 3
```

**Parameters**:
- `sliding-window-size`: Monitor last 10 calls
- `failure-rate-threshold`: Open circuit jika >50% fails
- `wait-duration-in-open-state`: Wait 10s before trying again
- `permitted-number-of-calls-in-half-open-state`: Allow 3 test calls

---

## Database Architecture

### Two-Database Pattern

```
┌──────────────────────────────────────────────┐
│              Write Side                      │
│                                              │
│  ┌────────────────────────────────────────┐ │
│  │  H2 In-Memory Database                 │ │
│  │                                        │ │
│  │  - Optimized for ACID transactions     │ │
│  │  - Normalized schema                   │ │
│  │  - Business logic validation           │ │
│  └────────────────────────────────────────┘ │
└──────────────────────────────────────────────┘
                    │
                    │ Events via RabbitMQ
                    ▼
┌──────────────────────────────────────────────┐
│              Read Side                       │
│                                              │
│  ┌────────────────────────────────────────┐ │
│  │  MongoDB                               │ │
│  │                                        │ │
│  │  - Optimized for queries               │ │
│  │  - Denormalized schema                 │ │
│  │  - Fast read performance               │ │
│  │  - Flexible schema evolution           │ │
│  └────────────────────────────────────────┘ │
└──────────────────────────────────────────────┘
```

### Schema Design

#### H2 (Write Model)
```sql
-- Normalized, relational schema
CREATE TABLE anggota_command (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nomor_anggota VARCHAR(50) UNIQUE NOT NULL,
    nama VARCHAR(100) NOT NULL,
    alamat VARCHAR(255),
    email VARCHAR(100),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

#### MongoDB (Read Model)
```json
// Denormalized, document schema
{
  "_id": "uuid",
  "nomorAnggota": "A001",
  "nama": "John Doe",
  "alamat": "Jl. Merdeka No. 123",
  "email": "john@example.com",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

---

## Eventual Consistency

### Consistency Model

Sistem menggunakan **Eventual Consistency** untuk synchronisasi antara Write dan Read models:

```
Time ────────────────────────────────────────▶

Write DB:  [WRITE] ──────────────────────────
                    │
                    │ Event Published
                    ▼
RabbitMQ:           [QUEUE]
                            │
                            │ Event Consumed
                            ▼
Read DB:                    [UPDATE] ────────

Lag: ~100ms - 1s (typical)
```

### Handling Consistency

1. **Write Operations**: Immediately persisted to Write DB
2. **Event Publishing**: Asynchronous event sent to RabbitMQ
3. **Event Processing**: Consumer updates Read DB
4. **Consistency Window**: Usually <1 second

### Trade-offs

✅ **Benefits**:
- High availability
- Better performance
- Scalability

⚠️ **Considerations**:
- Read-after-write consistency tidak guaranteed
- Need to handle temporary inconsistencies
- Retry mechanisms untuk failed events

---

## Inter-Service Communication

### Synchronous Communication (REST)

Untuk immediate data yang diperlukan:

```java
@Service
public class PeminjamanService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public PeminjamanDetailDTO getPeminjamanDetail(String id) {
        // Get peminjaman from own database
        Peminjaman peminjaman = peminjamanRepository.findById(id);
        
        // Fetch anggota data from service-anggota
        AnggotaDTO anggota = restTemplate.getForObject(
            "http://service-anggota/api/anggota/" + peminjaman.getAnggotaId(),
            AnggotaDTO.class
        );
        
        // Fetch buku data from service-buku
        BukuDTO buku = restTemplate.getForObject(
            "http://service-buku/api/buku/" + peminjaman.getBukuId(),
            BukuDTO.class
        );
        
        // Aggregate response
        return new PeminjamanDetailDTO(peminjaman, anggota, buku);
    }
}
```

### Asynchronous Communication (Events)

Untuk eventual consistency dan decoupling:

```java
// Publisher
rabbitTemplate.convertAndSend(exchange, routingKey, event);

// Consumer
@RabbitListener(queues = "queue-name")
public void handleEvent(Event event) {
    // Process event
}
```

---

## Scalability Considerations

### Horizontal Scaling

```bash
# Scale specific service
docker-compose up -d --scale service-anggota=3

# Gateway automatically load balances
```

### Database Scaling

**Write Database (H2)**:
- Replace dengan PostgreSQL/MySQL untuk production
- Master-slave replication

**Read Database (MongoDB)**:
- Replica sets untuk read scaling
- Sharding untuk large datasets

### Message Queue Scaling

**RabbitMQ**:
- Cluster setup untuk HA
- Multiple queues untuk partitioning
- Priority queues untuk critical events

---

[⬅️ Back to Main Documentation](../README.md)