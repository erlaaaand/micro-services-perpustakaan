# 📚 Sistem Microservices Perpustakaan

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=for-the-badge&logo=spring-boot)
![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![MongoDB](https://img.shields.io/badge/MongoDB-6.0-green?style=for-the-badge&logo=mongodb)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange?style=for-the-badge&logo=rabbitmq)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=for-the-badge&logo=docker)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**Sistem manajemen perpustakaan enterprise-grade dengan arsitektur microservices, implementasi CQRS pattern, Event-Driven Architecture menggunakan RabbitMQ, CI/CD pipeline, dan monitoring terdistribusi**

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
graph TB
    Client[Client Application]
    
    Client --> Gateway[API Gateway :8080]
    
    Gateway --> Eureka[Eureka Server :8761<br/>Service Discovery]
    
    Gateway --> SA[Service Anggota :8081<br/>H2 + MongoDB]
    Gateway --> SB[Service Buku :8082<br/>H2 + MongoDB]
    Gateway --> SP[Service Peminjaman :8083<br/>H2 + MongoDB]
    Gateway --> SR[Service Pengembalian :8084<br/>H2 + MongoDB]
    
    SP -.Inter-service.-> SA
    SP -.Inter-service.-> SB
    SR -.Inter-service.-> SP
    
    SA --> WriteDB[(H2 Database<br/>Write Model)]
    SA --> ReadDB[(MongoDB<br/>Read Model)]
    
    SA -->|Publish Event| RMQ[RabbitMQ :5672<br/>Message Broker]
    SB -->|Publish Event| RMQ
    SP -->|Publish Event| RMQ
    SR -->|Publish Event| RMQ
    
    RMQ -->|Subscribe Event| SA
    RMQ -->|Subscribe Event| SB
    RMQ -->|Subscribe Event| SP
    RMQ -->|Subscribe Event| SR
    
    RMQ -.Sync.-> ReadDB
    
    Gateway --> ELK[ELK Stack<br/>Elasticsearch + Logstash + Kibana]
    SA --> ELK
    SB --> ELK
    SP --> ELK
    SR --> ELK
    
    RMQMgmt[RabbitMQ Management :15672]
    RMQ --> RMQMgmt
    
    style Gateway fill:#4CAF50
    style Eureka fill:#2196F3
    style SA fill:#FF9800
    style SB fill:#FF9800
    style SP fill:#FF9800
    style SR fill:#FF9800
    style RMQ fill:#FF6600
    style RMQMgmt fill:#FF6600
    style ELK fill:#9C27B0
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

# Build semua services
./build-all.sh

# Start semua services dengan Docker Compose
docker-compose up -d

# Verifikasi health status
./deploy.sh health
```

### 📊 Verification

Setelah startup (tunggu ~2-3 menit), akses:

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Swagger UI Gateway**: http://localhost:8080/swagger-ui.html
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
│                    EVENT FLOW                             │
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
- Exchange: `anggota.exchange` (Topic)
- Queue: `anggota.queue`
- Routing Keys: 
  - `anggota.created`
  - `anggota.updated`
  - `anggota.deleted`

**Service Buku**
- Exchange: `buku.exchange` (Topic)
- Queue: `buku.queue`
- Routing Keys:
  - `buku.created`
  - `buku.updated`
  - `buku.deleted`

**Service Peminjaman**
- Exchange: `peminjaman.exchange` (Topic)
- Queue: `peminjaman.queue`
- Routing Keys:
  - `peminjaman.created`
  - `peminjaman.updated`
  - `peminjaman.deleted`

**Service Pengembalian**
- Exchange: `pengembalian.exchange` (Topic)
- Queue: `pengembalian.queue`
- Routing Keys:
  - `pengembalian.created`
  - `pengembalian.updated`
  - `pengembalian.deleted`

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
  "anggotaId": 1,
  "bukuId": 1,
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
```

---

### 🔹 Service Pengembalian (Return & Fines)

**Base URL**: `http://localhost:8080/api/pengembalian`

#### Create Return Transaction
```bash
POST /api/pengembalian
Content-Type: application/json

{
  "peminjamanId": 1,
  "tanggalDikembalikan": "2024-01-20",
  "terlambat": 5,
  "denda": 25000.0
}

# Event Published: pengembalian.created → RabbitMQ
```

---

## 🔍 Monitoring & Observability

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
2. Pilih exchange (contoh: `anggota.exchange`)
3. View bindings ke queues
4. Monitor message routing

#### Inspect Messages
1. Klik queue name (contoh: `anggota.queue`)
2. Scroll ke **Get messages** section
3. Klik **Get Message(s)** untuk preview message content

### 📊 ELK Stack (Logging)

**Kibana Dashboard**: http://localhost:5601

#### Setup Index Pattern
1. Buka Kibana → Management → Stack Management
2. Pilih **Index Patterns** → **Create index pattern**
3. Masukkan pattern: `logs-*`
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

#### Search RabbitMQ Events
```
message: "Publishing event to RabbitMQ"
message: "Received event from RabbitMQ"
```

### 🩺 Health Checks

```bash
# Check all services
./deploy.sh health

# Individual service health
curl http://localhost:8761/actuator/health  # Eureka
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # Service Anggota
curl http://localhost:8082/actuator/health  # Service Buku
curl http://localhost:8083/actuator/health  # Service Peminjaman
curl http://localhost:8084/actuator/health  # Service Pengembalian

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
│  Initialize  │──▶│   Checkout   │──▶│ Build & Test │
└──────────────┘   └──────────────┘   └──────────────┘
                                              │
                                              ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ Health Check │◀──│    Deploy    │◀──│  Push Docker │
└──────────────┘   └──────────────┘   └──────────────┘
```

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
docker-compose up -d mongodb rabbitmq

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
```

### Eureka Registration Issues

1. Tunggu 30-60 detik untuk service registration
2. Check Eureka dashboard: http://localhost:8761
3. Verify `eureka.client.register-with-eureka=true` di application.properties
4. Check network connectivity: `docker network inspect perpustakaan-network`

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
│   ├── 📁 messaging/
│   │   ├── publisher/            # RabbitMQ publishers
│   │   └── consumer/             # RabbitMQ consumers
│   └── 📁 config/
│       └── RabbitMQConfig.java   # RabbitMQ configuration
├── 📁 service-buku/               # Book Catalog (CQRS)
├── 📁 service-peminjaman/         # Borrowing Service (CQRS)
├── 📁 service-pengembalian/       # Return Service (CQRS)
├── 📁 monitoring/
│   ├── 📁 kibana/                # Kibana config
│   └── 📁 logstash/              # Logstash pipeline
├── 📄 docker-compose.yml         # Docker orchestration
├── 📄 Jenkinsfile                # CI/CD pipeline
├── 📄 .env.example               # Environment template
├── 📄 build-all.sh               # Build automation
└── 📄 deploy.sh                  # Deployment script
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
  -d '{"nomorAnggota":"A001","nama":"Test User",...}'

# Check RabbitMQ Management UI
# Verify message appears in queue and gets consumed
```

### API Testing with Postman

1. Import collection: `postman/perpustakaan-api.json`
2. Set environment variables
3. Run test suite

---

## 🔐 Security Notes

⚠️ **Development Mode**: Konfigurasi saat ini untuk development/testing

**Production Checklist**:
- [ ] Enable Spring Security
- [ ] Configure JWT authentication
- [ ] Setup HTTPS/SSL certificates
- [ ] Use secrets management (Vault)
- [ ] Enable Actuator security
- [ ] Configure CORS properly
- [ ] Setup rate limiting
- [ ] Enable audit logging
- [ ] Secure RabbitMQ with strong credentials
- [ ] Enable RabbitMQ SSL/TLS
- [ ] Configure RabbitMQ virtual hosts per service

---

## 📈 Performance Tuning

### JVM Options

Edit `JAVA_OPTS` di docker-compose.yml:

```yaml
environment:
  - JAVA_OPTS=-Xmx1g -Xms512m -XX:+UseG1GC
```

### RabbitMQ Tuning

```bash
# Edit docker-compose.yml
rabbitmq:
  environment:
    - RABBITMQ_VM_MEMORY_HIGH_WATERMARK=512MB
    - RABBITMQ_CHANNEL_MAX=2048
```

### MongoDB Indexing

```javascript
// Connect to MongoDB
docker exec -it mongodb mongosh

// Create indexes
use anggota_read_db
db.anggota_read.createIndex({ "nomorAnggota": 1 })
db.anggota_read.createIndex({ "email": 1 })

use buku_read_db
db.buku_read.createIndex({ "kodeBuku": 1 })
db.buku_read.createIndex({ "judul": "text" })
```

### Message Processing Optimization

Adjust prefetch count di application.yml:

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        prefetch: 10  # Process 10 messages concurrently
        concurrency: 3  # 3 concurrent consumers
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## 📝 License

This project is licensed under the **MIT License** - see LICENSE file for details.

---

## 🎓 Learning Resources

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/getstarted.html)
- [Event-Driven Architecture](https://martinfowler.com/articles/201701-event-driven.html)
- [Microservices Architecture](https://microservices.io/)
- [Docker Documentation](https://docs.docker.com/)
- [MongoDB Best Practices](https://docs.mongodb.com/manual/)

---

## 📞 Support & Contact

Untuk pertanyaan atau bantuan:
- 📧 Email: blackpenta98@gmail.com
- 🐛 Issues: [GitHub Issues](https://github.com/username/repo/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/username/repo/discussions)

---

<div align="center">

**[⬆ Back to Top](#-sistem-microservices-perpustakaan)**

Built using Java, Spring Boot, RabbitMQ, and passion for clean architecture

</div>