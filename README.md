# 📚 Sistem Microservices Perpustakaan

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=for-the-badge&logo=spring-boot)
![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![MongoDB](https://img.shields.io/badge/MongoDB-6.0-green?style=for-the-badge&logo=mongodb)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange?style=for-the-badge&logo=rabbitmq)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=for-the-badge&logo=docker)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**Sistem manajemen perpustakaan enterprise-grade dengan arsitektur microservices, implementasi CQRS pattern, Event-Driven Architecture menggunakan RabbitMQ, CI/CD pipeline, monitoring & tracing terdistribusi**

[Fitur](#-fitur-utama) • [Arsitektur](#-arsitektur-sistem) • [Quick Start](#-quick-start) • [Dokumentasi](#-dokumentasi-api) • [Monitoring](#-monitoring--observability)

</div>

---

## 🎯 Fitur Utama

<table>
<tr>
<td width="50%">

### 🏗️ **Architecture & Patterns**
- ✅ **CQRS Pattern** - Command Query Responsibility Segregation
- ✅ **Event-Driven Architecture** - Asynchronous messaging dengan RabbitMQ
- ✅ **Message Broker** - RabbitMQ untuk inter-service communication
- ✅ **Service Discovery** - Netflix Eureka
- ✅ **API Gateway** - Spring Cloud Gateway dengan routing
- ✅ **Circuit Breaker** - Resilience4j untuk fault tolerance
- ✅ **Load Balancing** - Client-side load balancing

</td>
<td width="50%">

### 🔧 **DevOps & Operations**
- ✅ **CI/CD Pipeline** - Jenkins automation
- ✅ **Containerization** - Docker & Docker Compose
- ✅ **Distributed Logging** - ELK Stack (Elasticsearch, Logstash, Kibana)
- ✅ **Metrics Monitoring** - Prometheus + Grafana
- ✅ **Distributed Tracing** - Zipkin
- ✅ **Message Queue Monitoring** - RabbitMQ Management UI
- ✅ **Health Monitoring** - Spring Boot Actuator
- ✅ **API Documentation** - OpenAPI/Swagger aggregation
- ✅ **Graceful Shutdown** - Zero-downtime deployments

</td>
</tr>
</table>

---

## 🏛️ Arsitektur Sistem

```mermaid
%%{init: {
  'theme': 'base',
  'themeVariables': {
    'background': '#FFFFFF',
    'mainBkg': '#FFFFFF',
    'primaryColor': '#FFFFFF',
    'primaryTextColor': '#0f172a',
    'lineColor': '#334155',
    'tertiaryColor': '#FFFFFF',
    'clusterBkg': '#f8fafc',
    'edgeLabelBackground': '#ffffff'
  },
  'flowchart': {
    'curve': 'basis',
    'nodeSpacing': 120,
    'rankSpacing': 100,
    'padding': 20
  }
}}%%

graph TB
    %% --- STYLE DEFINITIONS ---
    classDef client fill:#1e293b,stroke:#0f172a,stroke-width:2px,color:#fff,font-weight:bold,rx:5;
    classDef gateway fill:#059669,stroke:#047857,stroke-width:2px,color:#fff,font-weight:bold,rx:5;
    classDef service fill:#2563eb,stroke:#1d4ed8,stroke-width:2px,color:#fff,rx:5;
    classDef db fill:#475569,stroke:#334155,stroke-width:2px,color:#fff,rx:5;
    classDef msg fill:#ea580c,stroke:#c2410c,stroke-width:2px,color:#fff,rx:5;
    classDef monitor fill:#7c3aed,stroke:#6d28d9,stroke-width:2px,color:#fff,rx:5;
    classDef infra fill:#0891b2,stroke:#0e7490,stroke-width:2px,color:#fff,rx:5;

    %% --- NODES ---
    Client[Client App]:::client
    Gateway[API Gateway<br/>:8080]:::gateway
    Eureka[Eureka Server<br/>:8761]:::infra

    %% GROUP: MICROSERVICES
    subgraph Services ["🔷 Microservices Layer"]
        direction LR
        SA[Service<br/>Anggota<br/>:8081]:::service
        SB[Service<br/>Buku<br/>:8082]:::service
        SP[Service<br/>Peminjaman<br/>:8083]:::service
        SR[Service<br/>Pengembalian<br/>:8084]:::service
    end

    %% GROUP: DATABASE
    subgraph Data ["💾 Persistence Layer"]
        direction LR
        WriteDB[(H2<br/>Write Model)]:::db
        ReadDB[(MongoDB<br/>Read Model)]:::db
    end

    %% GROUP: MESSAGING
    subgraph Bus ["📨 Event Bus Layer"]
        direction TB
        RMQ[RabbitMQ<br/>:5672]:::msg
        RMQMgmt[Management UI<br/>:15672]:::msg
    end

    %% GROUP: OBSERVABILITY
    subgraph Obs ["📊 Observability Stack"]
        direction TB
        ELK[ELK Stack<br/>Logging]:::monitor
        Prom[Prometheus<br/>:9090]:::monitor
        Graf[Grafana<br/>:3000]:::monitor
        Zip[Zipkin<br/>:9411]:::monitor
    end

    %% --- RELATIONS ---
    
    %% Main Flow
    Client -->|HTTP| Gateway
    Gateway -.Service Discovery.-> Eureka
    
    %% Gateway to Services
    Gateway ==>|Route| SA
    Gateway ==>|Route| SB
    Gateway ==>|Route| SP
    Gateway ==>|Route| SR

    %% Inter-service
    SP -.REST Call.-> SA
    SP -.REST Call.-> SB
    SR -.REST Call.-> SP

    %% Database Operations
    SA ---|Write| WriteDB
    SA ---|Read| ReadDB
    SB ---|Write| WriteDB
    SB ---|Read| ReadDB
    SP ---|Write| WriteDB
    SP ---|Read| ReadDB
    SR ---|Write| WriteDB
    SR ---|Read| ReadDB

    %% Event Publishing
    SA ==>|Publish Event| RMQ
    SB ==>|Publish Event| RMQ
    SP ==>|Publish Event| RMQ
    SR ==>|Publish Event| RMQ

    %% Event Subscribing
    RMQ -.->|Subscribe| SA
    RMQ -.->|Subscribe| SB
    RMQ -.->|Subscribe| SP
    RMQ -.->|Subscribe| SR
    
    %% Event Sync
    RMQ ==>|Event Sync| ReadDB
    RMQ ---|Admin| RMQMgmt

    %% Monitoring & Observability
    Gateway -.Logs.-> ELK
    SA -.Logs.-> ELK
    SB -.Logs.-> ELK
    SP -.Logs.-> ELK
    SR -.Logs.-> ELK
    
    Gateway -.Metrics.-> Prom
    SA -.Metrics.-> Prom
    SB -.Metrics.-> Prom
    SP -.Metrics.-> Prom
    SR -.Metrics.-> Prom
    
    Prom -->|Data Source| Graf
    
    Gateway -.Traces.-> Zip
    SA -.Traces.-> Zip
    SB -.Traces.-> Zip
    SP -.Traces.-> Zip
    SR -.Traces.-> Zip

    %% --- STYLING FIX ---
    style Services fill:#eff6ff,stroke:#bfdbfe,stroke-width:2px,rx:10,color:#1e3a8a
    style Data fill:#f1f5f9,stroke:#cbd5e1,stroke-width:2px,rx:10,color:#334155
    style Bus fill:#fff7ed,stroke:#fed7aa,stroke-width:2px,rx:10,color:#9a3412
    style Obs fill:#f5f3ff,stroke:#ddd6fe,stroke-width:2px,rx:10,color:#5b21b6
    
    linkStyle default stroke:#334155,stroke-width:1px
```

### 📦 Komponen Utama

| Komponen | Port | Teknologi | Fungsi |
|----------|------|-----------|--------|
| **Eureka Server** | 8761 | Spring Cloud Netflix | Service Registry & Discovery |
| **API Gateway** | 8080 | Spring Cloud Gateway | Routing, Load Balancing, Circuit Breaker |
| **Service Anggota** | 8081 | Spring Boot + CQRS | Manajemen data anggota perpustakaan |
| **Service Buku** | 8082 | Spring Boot + CQRS | Manajemen katalog buku |
| **Service Peminjaman** | 8083 | Spring Boot + CQRS | Transaksi peminjaman buku |
| **Service Pengembalian** | 8084 | Spring Boot + CQRS | Proses pengembalian & denda |
| **RabbitMQ** | 5672 | RabbitMQ 3.13 | Message Broker untuk Event-Driven Architecture |
| **RabbitMQ Management** | 15672 | RabbitMQ Management | Web UI untuk monitoring queue & exchange |
| **MongoDB** | 27017 | MongoDB 6.0 | Read Model Database (CQRS) |
| **Elasticsearch** | 9200 | Elastic 8.11 | Log storage & indexing |
| **Logstash** | 5000 | Logstash 8.11 | Log processing pipeline |
| **Kibana** | 5601 | Kibana 8.11 | Log visualization dashboard |
| **Prometheus** | 9090 | Prometheus Latest | Metrics collection & storage |
| **Grafana** | 3000 | Grafana Latest | Metrics visualization & dashboards |
| **Zipkin** | 9411 | Zipkin Latest | Distributed tracing system |
| **Jenkins** | 9000 | Jenkins LTS | CI/CD Automation |

---

## 🚀 Quick Start

### Prerequisites

```bash
# Required software
- Java 17 or higher
- Maven 3.9+
- Docker 20.10+
- Docker Compose v2+

# System requirements
- RAM: 8GB minimum (16GB recommended)
- CPU: 4 cores minimum
- Disk: 20GB free space
```

### 🔥 One-Command Setup

```bash
# Clone repository
git clone <repository-url>
cd perpustakaan-microservices

# Start semua services dengan Docker Compose
docker-compose up -d
```

### 📊 Verification

Setelah startup (tunggu ~2-3 menit), akses:

**Core Services:**
- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Swagger UI Gateway**: http://localhost:8080/swagger-ui.html

**Monitoring & Observability:**
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (username: `admin`, password: `admin`)
- **Zipkin Tracing**: http://localhost:9411
- **RabbitMQ Management**: http://localhost:15672 (username: `guest`, password: `guest`)
- **Kibana Logs**: http://localhost:5601

---

## 🎨 CQRS Pattern Implementation

Sistem ini mengimplementasikan **CQRS (Command Query Responsibility Segregation)** untuk memisahkan operasi write dan read:

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
│ - Update        │──RabbitMQ───▶│ - Get All      │
│ - Delete        │   Events     │ - Search        │
└─────────────────┘              └─────────────────┘
```

**Keuntungan CQRS:**
- ✅ Scalability: Read & Write dapat di-scale independent
- ✅ Performance: Optimasi query untuk read operations
- ✅ Flexibility: Model berbeda untuk Command & Query
- ✅ Event Sourcing Ready: Event-driven synchronization via RabbitMQ

---

## 🐰 RabbitMQ Event-Driven Architecture

### Event Flow

Sistem menggunakan RabbitMQ untuk asynchronous event publishing dan consuming:

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

Setiap service memiliki konfigurasi exchange dan queue sendiri:

**Service Anggota**
- Exchange: `anggota-exchange` (Topic)
- Queue: `anggota-sync-queue`
- Routing Key: `anggota.routing.key`

**Service Buku**
- Exchange: `buku-exchange` (Topic)
- Queue: `buku-sync-queue`
- Routing Key: `buku.routing.key`

**Service Peminjaman**
- Exchange: `peminjaman-exchange` (Topic)
- Queue: `peminjaman-sync-queue`
- Routing Key: `peminjaman.routing.key`

**Service Pengembalian**
- Exchange: `pengembalian-exchange` (Topic)
- Queue: `pengembalian-sync-queue`
- Routing Key: `pengembalian.routing.key`

### Event Structure

Contoh event yang dipublikasikan ke RabbitMQ:

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

### RabbitMQ Management UI

Akses **RabbitMQ Management Console** di http://localhost:15672

**Default Credentials:**
- Username: `guest`
- Password: `guest`

**Features:**
- 📊 Monitor exchanges dan queues
- 📈 View message rates dan statistics
- 🔍 Inspect messages dalam queue
- ⚙️ Configure bindings dan policies
- 📉 Performance metrics

---

## 📖 Dokumentasi API

### 🔹 Service Anggota (Member Management)

**Base URL**: `http://localhost:8080/api/anggota`

#### Create Member
```bash
POST /api/anggota
Content-Type: application/json

{
  "nomorAnggota": "A001",
  "nama": "John Doe",
  "alamat": "Jl. Merdeka No. 123",
  "email": "john@example.com"
}

# Response: 201 Created
# Event Published: anggota.created → RabbitMQ
```

#### Get All Members
```bash
GET /api/anggota?page=0&size=10&sortBy=nama

# Data source: MongoDB (Read Model)
```

#### Get Member by ID
```bash
GET /api/anggota/{id}

# Data source: MongoDB (Read Model)
```

#### Update Member
```bash
PUT /api/anggota/{id}
Content-Type: application/json

{
  "nomorAnggota": "A001",
  "nama": "John Doe Updated",
  "alamat": "Jl. Updated No. 456",
  "email": "john.updated@example.com"
}

# Response: 200 OK
# Event Published: anggota.updated → RabbitMQ
```

#### Delete Member
```bash
DELETE /api/anggota/{id}

# Response: 204 No Content
# Event Published: anggota.deleted → RabbitMQ
```

---

### 🔹 Service Buku (Book Catalog)

**Base URL**: `http://localhost:8080/api/buku`

#### Create Book
```bash
POST /api/buku
Content-Type: application/json

{
  "kodeBuku": "BK-001",
  "judul": "Java Programming",
  "pengarang": "John Doe",
  "penerbit": "Erlangga",
  "tahunTerbit": 2020
}

# Event Published: buku.created → RabbitMQ
```

#### Get All Books
```bash
GET /api/buku?page=0&size=10&sortBy=judul

# Data source: MongoDB (Read Model)
```

---

### 🔹 Service Peminjaman (Borrowing)

**Base URL**: `http://localhost:8080/api/peminjaman`

#### Create Borrowing Transaction
```bash
POST /api/peminjaman
Content-Type: application/json

{
  "anggotaId": "uuid-anggota",
  "bukuId": "uuid-buku",
  "tanggalPinjam": "2024-01-01",
  "tanggalKembali": "2024-01-15",
  "status": "DIPINJAM"
}

# Event Published: peminjaman.created → RabbitMQ
```

#### Get Borrowing with Details (Inter-service call)
```bash
GET /api/peminjaman/{id}

# Response includes aggregated data:
{
  "peminjaman": { ... },
  "anggota": { "nama": "John Doe", ... },
  "buku": { "judul": "Java Programming", ... }
}

# Data source: MongoDB (Read Model)
# Additional data fetched via RestTemplate from other services
```

---

### 🔹 Service Pengembalian (Return & Fines)

**Base URL**: `http://localhost:8080/api/pengembalian`

#### Create Return Transaction
```bash
POST /api/pengembalian
Content-Type: application/json

{
  "peminjamanId": "uuid-peminjaman",
  "tanggalDikembalikan": "2024-01-20",
  "terlambat": 5,
  "denda": 25000.0
}

# Event Published: pengembalian.created → RabbitMQ
```

---

## 🔍 Monitoring & Observability

### 📊 Prometheus Metrics

**Prometheus UI**: http://localhost:9090

#### Available Metrics
- **JVM Metrics**: `jvm_memory_used_bytes`, `jvm_threads_live_threads`, `jvm_gc_pause_seconds`
- **HTTP Metrics**: `http_server_requests_seconds_count`, `http_server_requests_seconds_sum`
- **System Metrics**: `system_cpu_usage`, `system_load_average_1m`, `process_uptime_seconds`
- **Custom Metrics**: Application-specific business metrics

#### Query Examples
```promql
# Request rate per service
rate(http_server_requests_seconds_count{application="service-anggota"}[5m])

# Memory usage percentage
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100

# Error rate
rate(http_server_requests_seconds_count{status=~"5.."}[5m])
```

### 📈 Grafana Dashboards

**Grafana UI**: http://localhost:3000
- Username: `admin`
- Password: `admin`

#### Pre-configured Dashboards
1. **JVM Micrometer Dashboard**
   - Heap & Non-Heap memory usage
   - GC pause times & collections
   - Thread states
   - CPU usage & system load
   - HTTP request rates & errors

#### Creating Custom Dashboards
1. Login to Grafana
2. Click **+** → **Dashboard**
3. Add panels with Prometheus queries
4. Configure visualizations (Graph, Gauge, Table, etc.)
5. Save dashboard

### 🔍 Zipkin Distributed Tracing

**Zipkin UI**: http://localhost:9411

#### Features
- **Trace Visualization**: View complete request flow across services
- **Dependency Graph**: Visualize service dependencies
- **Performance Analysis**: Identify slow operations and bottlenecks
- **Error Tracking**: Find failed requests and error patterns

#### Trace Example
```
GET /api/peminjaman/123
├─ [api-gateway] 2ms
├─ [service-peminjaman] 15ms
│  ├─ [MongoDB Read] 3ms
│  ├─ [service-anggota] 5ms
│  └─ [service-buku] 4ms
└─ Total: 17ms
```

#### Search Traces
1. Open Zipkin UI
2. Select service name
3. Set time range
4. Add optional filters (minDuration, tags)
5. Click **RUN QUERY**

### 🐰 RabbitMQ Monitoring

**RabbitMQ Management UI**: http://localhost:15672

#### Monitor Queue Activity
1. Login dengan credentials: `guest` / `guest`
2. Klik **Queues** tab
3. Monitor metrics:
   - **Ready**: Messages ready untuk diproses
   - **Unacked**: Messages sedang diproses
   - **Total**: Total messages dalam queue
   - **Publish/Deliver Rate**: Message throughput

#### View Exchange Bindings
1. Klik **Exchanges** tab
2. Pilih exchange (contoh: `anggota-exchange`)
3. View bindings ke queues
4. Monitor message routing

#### Inspect Messages
1. Klik queue name (contoh: `anggota-sync-queue`)
2. Scroll ke **Get messages** section
3. Klik **Get Message(s)** untuk preview message content

### 📊 ELK Stack (Logging)

**Kibana Dashboard**: http://localhost:5601

#### Setup Index Pattern
1. Buka Kibana → Management → Stack Management
2. Pilih **Index Patterns** → **Create index pattern**
3. Masukkan pattern: `app-logs-*`
4. Pilih timestamp field: `@timestamp`
5. Klik **Create index pattern**

#### View Logs
1. Buka **Discover** menu
2. Filter berdasarkan service:
   ```
   app_name: "service-anggota"
   app_name: "service-buku"
   ```
3. Gunakan KQL query untuk searching

#### Search Examples
```
# RabbitMQ Events
message: "Publishing event to RabbitMQ"
message: "Received event from RabbitMQ"

# API Requests
message: "API REQUEST"

# Errors
level: "ERROR"
```

### 🩺 Health Checks

```bash
# Individual service health
curl http://localhost:8761/actuator/health  # Eureka
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # Service Anggota
curl http://localhost:8082/actuator/health  # Service Buku
curl http://localhost:8083/actuator/health  # Service Peminjaman
curl http://localhost:8084/actuator/health  # Service Pengembalian

# Prometheus metrics endpoint
curl http://localhost:8081/actuator/prometheus

# Check RabbitMQ health
curl http://localhost:15672/api/health/checks/alarms  # Requires auth
```

---

## 🔧 CI/CD Pipeline

### Jenkins Setup

1. **Akses Jenkins**: http://localhost:9000

2. **Get Initial Password**:
```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

3. **Install Required Plugins**:
   - Docker Pipeline
   - Maven Integration
   - Git Plugin

4. **Configure Credentials**:
   - Docker Hub: `docker-hub-credentials`
   - Username & Password

5. **Create Pipeline**:
   - New Item → Pipeline
   - Pipeline script from SCM
   - Repository URL: `<your-repo-url>`
   - Script Path: `Jenkinsfile`

### Pipeline Stages

```
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  Initialize  │──▶│   Checkout   │──▶│ Build JARs   │
└──────────────┘   └──────────────┘   └──────────────┘
                                              │
                                              ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ Health Check │◀──│    Deploy    │◀──│ Build Docker │
└──────────────┘   └──────────────┘   └──────────────┘
                                              │
                                              ▼
                                      ┌──────────────┐
                                      │ Push to Hub  │
                                      └──────────────┘
```

### Pipeline Features
- ✅ Sequential JAR building (skip tests untuk speed)
- ✅ Docker multi-stage builds
- ✅ Automatic versioning dengan Git commit hash
- ✅ Environment-specific deployments (dev/staging/production)
- ✅ Health check verification
- ✅ Automatic rollback pada failure

---

## 🛠️ Development Guide

### Build Individual Service

```bash
cd service-anggota
mvn clean package -DskipTests
```

### Run Service Locally

```bash
# Start infrastructure services first
docker-compose up -d mongodb rabbitmq prometheus grafana zipkin

# Start Eureka
cd eureka-server
mvn spring-boot:run

# Start other services
cd service-anggota
mvn spring-boot:run
```

### Environment Variables

Buat file `.env` di root project:

```properties
# Eureka
EUREKA_SERVER_URL=http://localhost:8761/eureka/

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# MongoDB
MONGODB_URI_ANGGOTA=mongodb://localhost:27017/anggota_db
MONGODB_URI_BUKU=mongodb://localhost:27017/buku_db
MONGODB_URI_PEMINJAMAN=mongodb://localhost:27017/peminjaman_db
MONGODB_URI_PENGEMBALIAN=mongodb://localhost:27017/pengembalian_db

# Monitoring
ZIPKIN_ENDPOINT=http://localhost:9411/api/v2/spans

# ELK Stack
ELASTICSEARCH_HOSTS=http://localhost:9200
LOGSTASH_HOST=localhost
LOGSTASH_PORT=5000
```

### RabbitMQ Configuration Example

```yaml
# application.yml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    listener:
      simple:
        acknowledge-mode: auto
        prefetch: 1
        retry:
          enabled: true
          initial-interval: 3000
          max-attempts: 3
          multiplier: 2
```

---

## 🐛 Troubleshooting

### Services Not Starting

```bash
# Check logs
docker-compose logs -f [service-name]

# Restart specific service
docker-compose restart [service-name]

# Clean rebuild
docker-compose down
docker-compose up -d --build
```

### RabbitMQ Connection Issues

```bash
# Check RabbitMQ status
docker ps | grep rabbitmq

# View RabbitMQ logs
docker logs rabbitmq

# Access RabbitMQ container
docker exec -it rabbitmq bash

# Check if queues are created
rabbitmqctl list_queues

# Check exchanges
rabbitmqctl list_exchanges
```

### Message Not Being Consumed

1. Check RabbitMQ Management UI (http://localhost:15672)
2. Verify queue has consumers: **Queues** tab → Check **Consumers** column
3. Check message count: **Ready** dan **Unacked** messages
4. View service logs untuk error messages:
   ```bash
   docker-compose logs -f service-anggota
   ```
5. Verify exchange-queue binding di RabbitMQ UI

### Prometheus Not Collecting Metrics

```bash
# Check Prometheus targets
curl http://localhost:9090/api/v1/targets

# Check if actuator endpoint is accessible
curl http://localhost:8081/actuator/prometheus

# Verify prometheus.yml configuration
docker exec prometheus cat /etc/prometheus/prometheus.yml

# Restart Prometheus
docker-compose restart prometheus
```

### Zipkin Traces Not Appearing

```bash
# Check if services are sending traces
curl http://localhost:8081/actuator/health

# Verify Zipkin endpoint configuration
# In application.properties:
management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans

# Check Zipkin logs
docker logs zipkin

# Restart Zipkin
docker-compose restart zipkin
```

### Grafana Dashboard Issues

```bash
# Reset Grafana admin password
docker exec -it grafana grafana-cli admin reset-admin-password admin

# Check Grafana logs
docker logs grafana

# Verify Prometheus datasource
curl http://localhost:3000/api/datasources

# Restart Grafana
docker-compose restart grafana
```

### Port Already in Use

```bash
# Find process using port (Linux/Mac)
lsof -i :8080

# Find process using port (Windows)
netstat -ano | findstr :8080

# Kill process
kill -9 <PID>  # Linux/Mac
taskkill /PID <PID> /F  # Windows
```

### MongoDB Connection Issues

```bash
# Verify MongoDB is running
docker ps | grep mongodb

# Check MongoDB logs
docker logs mongodb

# Connect to MongoDB shell
docker exec -it mongodb mongosh

# List databases
show dbs

# Check specific database
use anggota_read_db
show collections
```

### Eureka Registration Issues

1. Tunggu 30-60 detik untuk service registration
2. Check Eureka dashboard: http://localhost:8761
3. Verify `eureka.client.register-with-eureka=true` di application.properties
4. Check network connectivity: `docker network inspect perpustakaan-network`

### ELK Stack Issues

```bash
# Check Elasticsearch health
curl http://localhost:9200/_cluster/health

# Check Logstash pipeline
docker logs logstash

# Verify Kibana connectivity
curl http://localhost:5601/api/status

# Restart ELK Stack
docker-compose restart elasticsearch logstash kibana
```

---

## 📂 Project Structure

```
perpustakaan-microservices/
├── 📁 eureka-server/              # Service Discovery
├── 📁 api-gateway/                # API Gateway & Routing
├── 📁 service-anggota/            # Member Management (CQRS)
│   ├── 📁 cqrs/
│   │   ├── command/              # Write operations
│   │   ├── query/                # Read operations
│   │   └── handler/              # Command/Query handlers
│   ├── 📁 entity/
│   │   ├── command/              # Write model (H2)
│   │   └── query/                # Read model (MongoDB)
│   ├── 📁 repository/
│   │   ├── command/              # JPA Repository
│   │   └── query/                # MongoDB Repository
│   ├── 📁 event/                 # Event definitions
│   ├── 📁 dto/                   # Data Transfer Objects
│   ├── 📁 controller/            # REST Controllers
│   ├── 📁 exception/             # Global Exception Handlers
│   └── 📁 config/
│       ├── RabbitMQConfig.java   # RabbitMQ configuration
│       └── SwaggerConfig.java    # OpenAPI configuration
├── 📁 service-buku/               # Book Catalog (CQRS)
├── 📁 service-peminjaman/         # Borrowing Service (CQRS)
│   └── 📁 vo/                    # Value Objects for inter-service data
├── 📁 service-pengembalian/       # Return Service (CQRS)
├── 📁 monitoring/
│   ├── 📁 prometheus/
│   │   └── prometheus.yml        # Prometheus scrape config
│   ├── 📁 grafana/
│   │   └── provisioning/
│   │       ├── datasources/      # Auto-provisioned datasources
│   │       └── dashboards/       # Pre-configured dashboards
│   ├── 📁 kibana/
│   │   └── kibana.yml            # Kibana configuration
│   └── 📁 logstash/
│       ├── config/               # Logstash config
│       └── pipeline/             # Log processing pipeline
├── 📄 docker-compose.yml         # Docker orchestration
├── 📄 Jenkinsfile                # CI/CD pipeline
├── 📄 Dockerfile-jenkins         # Custom Jenkins image
└── 📄 .env.example               # Environment template
```

---

## 🧪 Testing

### Unit Tests

```bash
# Run tests for specific service
cd service-anggota
mvn test

# Run all tests
mvn clean verify
```

### Integration Tests

```bash
# With coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Testing RabbitMQ Events

```bash
# Test event publishing
curl -X POST http://localhost:8080/api/anggota \
  -H "Content-Type: application/json" \
  -d '{"nomorAnggota":"A001","nama":"Test User","alamat":"Test Address","email":"test@example.com"}'

# Check RabbitMQ Management UI
# Verify message appears in queue and gets consumed
```

### Testing Distributed Tracing

```bash
# Make API call
curl http://localhost:8080/api/peminjaman/123

# Open Zipkin UI (http://localhost:9411)
# Search for trace by service name or trace ID
# Verify complete request flow across services
```

### API Testing with Postman

1. Import collection: `postman/perpustakaan-api.json`
2. Set environment variables
3. Run test suite

---

## 📊 Performance Monitoring

### Key Metrics to Monitor

#### Application Metrics
- **Request Rate**: `rate(http_server_requests_seconds_count[5m])`
- **Error Rate**: `rate(http_server_requests_seconds_count{status=~"5.."}[5m])`
- **Response Time**: `http_server_requests_seconds_sum / http_server_requests_seconds_count`

#### JVM Metrics
- **Heap Usage**: `jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}`
- **GC Pause Time**: `jvm_gc_pause_seconds_sum`
- **Thread Count**: `jvm_threads_live_threads`

#### RabbitMQ Metrics
- **Message Publish Rate**: Monitor via RabbitMQ Management UI
- **Queue Depth**: Check "Ready" messages count
- **Consumer Count**: Verify active consumers

#### System Metrics
- **CPU Usage**: `system_cpu_usage`
- **Memory Usage**: `process_memory_rss_bytes`
- **Disk I/O**: Monitor via system tools

### Setting Up Alerts

#### Prometheus Alert Rules
Create `prometheus/alerts.yml`:

```yaml
groups:
  - name: service_alerts
    interval: 30s
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Service {{ $labels.application }} has error rate > 5%"
      
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage"
          description: "Service {{ $labels.application }} using > 90% heap"
```

#### Grafana Alerts
1. Open dashboard panel
2. Click **Alert** tab
3. Configure alert conditions
4. Set notification channels (Email, Slack, etc.)

---

## 🔐 Security Notes

⚠️ **Development Mode**: Konfigurasi saat ini untuk development/testing

**Production Checklist**:
- [ ] Enable Spring Security
- [ ] Configure JWT authentication
- [ ] Setup HTTPS/SSL certificates
- [ ] Use secrets management (Vault, AWS Secrets Manager)
- [ ] Enable Actuator security
- [ ] Configure CORS properly
- [ ] Setup rate limiting
- [ ] Enable audit logging
- [ ] Secure RabbitMQ with strong credentials
- [ ] Enable RabbitMQ SSL/TLS
- [ ] Configure RabbitMQ virtual hosts per service
- [ ] Secure Prometheus/Grafana with authentication
- [ ] Use network policies for container isolation
- [ ] Implement API Gateway authentication
- [ ] Enable database encryption at rest

---

## 📈 Performance Tuning

### JVM Options

Edit `JAVA_OPTS` di docker-compose.yml:

```yaml
environment:
  - JAVA_OPTS=-Xmx1g -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

### RabbitMQ Tuning

```bash
# Edit docker-compose.yml
rabbitmq:
  environment:
    - RABBITMQ_VM_MEMORY_HIGH_WATERMARK=512MB
    - RABBITMQ_CHANNEL_MAX=2048
    - RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS=-rabbit disk_free_limit 2147483648
```

### MongoDB Indexing

```javascript
// Connect to MongoDB
docker exec -it mongodb mongosh

// Create indexes for better query performance
use anggota_read_db
db.anggota_read.createIndex({ "nomorAnggota": 1 })
db.anggota_read.createIndex({ "email": 1 })

use buku_read_db
db.buku_read.createIndex({ "kodeBuku": 1 })
db.buku_read.createIndex({ "judul": "text" })

use peminjaman_read_db
db.peminjaman_read.createIndex({ "anggotaId": 1, "status": 1 })
db.peminjaman_read.createIndex({ "bukuId": 1 })

use pengembalian_read_db
db.pengembalian_read.createIndex({ "peminjamanId": 1 })
```

### Message Processing Optimization

Adjust prefetch count di application.properties:

```properties
spring.rabbitmq.listener.simple.prefetch=10
spring.rabbitmq.listener.simple.concurrency=3
spring.rabbitmq.listener.simple.max-concurrency=10
```

### Database Connection Pooling

```properties
# HikariCP settings
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

---

## 🚀 Scaling Strategies

### Horizontal Scaling

```bash
# Scale specific service
docker-compose up -d --scale service-anggota=3

# Scale multiple services
docker-compose up -d \
  --scale service-anggota=3 \
  --scale service-buku=3 \
  --scale service-peminjaman=2
```

### Load Balancing
- Client-side load balancing via Eureka
- Gateway automatically distributes requests
- Monitor load distribution in Grafana

### Database Scaling
- **MongoDB**: Setup replica set for read scaling
- **H2 (Production)**: Replace with PostgreSQL/MySQL with replication

### Message Queue Scaling
- **RabbitMQ Cluster**: Setup cluster for high availability
- **Message Partitioning**: Use consistent hashing for message distribution

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

### Coding Standards
- Follow Java Code Conventions
- Write unit tests for new features
- Update documentation
- Use meaningful commit messages

---

## 📝 License

This project is licensed under the **MIT License** - see LICENSE file for details.

---

## 🎓 Learning Resources

### Architecture & Patterns
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [Event-Driven Architecture](https://martinfowler.com/articles/201701-event-driven.html)
- [Microservices Architecture](https://microservices.io/)

### Messaging & Events
- [RabbitMQ Tutorials](https://www.rabbitmq.com/getstarted.html)
- [RabbitMQ Best Practices](https://www.cloudamqp.com/blog/part1-rabbitmq-best-practice.html)

### Monitoring & Observability
- [Prometheus Documentation](https://prometheus.io/docs/introduction/overview/)
- [Grafana Tutorials](https://grafana.com/tutorials/)
- [Zipkin Documentation](https://zipkin.io/pages/quickstart.html)
- [ELK Stack Guide](https://www.elastic.co/guide/index.html)

### Containerization
- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/)

### Database
- [MongoDB Best Practices](https://docs.mongodb.com/manual/)
- [H2 Database Documentation](https://www.h2database.com/html/main.html)

---

## 📞 Support & Contact

Untuk pertanyaan atau bantuan:
- 📧 Email: blackpenta98@gmail.com
- 🐛 Issues: [GitHub Issues](https://github.com/username/repo/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/username/repo/discussions)

---

## 🔄 Changelog

### Version 2.0.0 (Latest)
- ✨ Added Prometheus metrics collection
- ✨ Added Grafana dashboards with JVM monitoring
- ✨ Implemented Zipkin distributed tracing
- ✨ Enhanced logging with correlation IDs
- 🐛 Fixed RabbitMQ event listener issues
- 📝 Updated documentation with monitoring guides

### Version 1.0.0
- 🎉 Initial release
- ✅ CQRS pattern implementation
- ✅ RabbitMQ event-driven architecture
- ✅ ELK Stack logging
- ✅ Jenkins CI/CD pipeline
- ✅ Docker containerization

---

## 🗺️ Roadmap

### Q1 2026
- [ ] Implement API Gateway authentication (OAuth2/JWT)
- [ ] Add Redis caching layer
- [ ] Setup Kubernetes deployment
- [ ] Implement saga pattern for distributed transactions

### Q2 2026
- [ ] Add Spring Cloud Config Server
- [ ] Implement API versioning
- [ ] Setup disaster recovery procedures
- [ ] Add automated performance testing

### Q3 2026
- [ ] Migrate to reactive programming (WebFlux)
- [ ] Implement GraphQL API
- [ ] Add machine learning for book recommendations
- [ ] Setup multi-region deployment

---

## 🙏 Acknowledgments

- Spring Boot team for amazing framework
- Netflix OSS for Eureka
- RabbitMQ team for reliable messaging
- Elastic for ELK Stack
- Prometheus & Grafana communities
- Docker & containerization ecosystem

---

<div align="center">

**[⬆ Back to Top](#-sistem-microservices-perpustakaan)**

Built with using Java, Spring Boot, RabbitMQ, and modern DevOps practices

**⭐ Star this repository if you find it helpful!**

</div>