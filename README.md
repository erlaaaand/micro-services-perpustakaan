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
git clone https://github.com/erlaaaand/micro-services-perpustakaan.git
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

## 📖 Dokumentasi Lengkap

Dokumentasi lengkap tersedia di folder [`docs/`](docs/):

### 📚 Dokumentasi Utama

- **[Setup Guide](docs/SETUP_GUIDE.md)** - Panduan lengkap setup dari awal
- **[Architecture](docs/ARCHITECTURE.md)** - Detail arsitektur sistem dan design patterns
- **[API Reference](docs/API_REFERENCE.md)** - Dokumentasi lengkap semua API endpoints
- **[CQRS Implementation](docs/CQRS.md)** - Penjelasan implementasi CQRS pattern
- **[Event-Driven Architecture](docs/EVENT_DRIVEN.md)** - Dokumentasi RabbitMQ dan event flow

### 🔧 Operasional

- **[Monitoring Guide](docs/MONITORING.md)** - Panduan monitoring dengan Prometheus, Grafana, Zipkin, dan ELK
- **[CI/CD Pipeline](docs/CICD.md)** - Setup dan konfigurasi Jenkins pipeline
- **[Development Guide](docs/DEVELOPMENT.md)** - Panduan development dan best practices
- **[Troubleshooting](docs/TROUBLESHOOTING.md)** - Common issues dan solusinya
- **[Production Checklist](docs/PRODUCTION.md)** - Checklist untuk production deployment

### 📊 Referensi Teknis

- **[Configuration Files](docs/CONFIGURATION.md)** - Penjelasan semua file konfigurasi
- **[Database Schema](docs/DATABASE.md)** - Schema H2 dan MongoDB
- **[Performance Tuning](docs/PERFORMANCE.md)** - Optimasi performa sistem
- **[Security Guide](docs/SECURITY.md)** - Best practices keamanan

---

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Test API endpoints
curl http://localhost:8080/api/anggota
```

Lihat [Development Guide](docs/DEVELOPMENT.md) untuk detail testing.

---

## 📂 Project Structure

```
perpustakaan-microservices/
├── 📁 eureka-server/              # Service Discovery
├── 📁 api-gateway/                # API Gateway & Routing
├── 📁 service-anggota/            # Member Management (CQRS)
├── 📁 service-buku/               # Book Catalog (CQRS)
├── 📁 service-peminjaman/         # Borrowing Service (CQRS)
├── 📁 service-pengembalian/       # Return Service (CQRS)
├── 📁 monitoring/                 # Monitoring configurations
│   ├── 📁 prometheus/
│   ├── 📁 grafana/
│   ├── 📁 kibana/
│   └── 📁 logstash/
├── 📁 docs/                       # Dokumentasi lengkap
├── 📄 docker-compose.yml         # Docker orchestration
├── 📄 Jenkinsfile                # CI/CD pipeline
├── 📄 Dockerfile-jenkins         # Custom Jenkins image
└── 📄 README.md                  # This file
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

Lihat [Development Guide](docs/DEVELOPMENT.md) untuk coding standards.

---

## 📝 License

This project is licensed under the **MIT License** - see LICENSE file for details.

---

## 📞 Support & Contact

Untuk pertanyaan atau bantuan:
- 📧 Email: blackpenta98@gmail.com
- 🐛 Issues: [GitHub Issues](https://github.com/erlaaaand/micro-services-perpustakaan/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/erlaaaand/micro-services-perpustakaan/discussions)

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

Built using Java, Spring Boot, RabbitMQ, and modern DevOps practices

**⭐ Star this repository if you find it helpful!**

</div>