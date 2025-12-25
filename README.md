# 📚 Sistem Microservices Perpustakaan

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Coverage](https://img.shields.io/badge/coverage-85%25-green.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)]()
[![License](https://img.shields.io/badge/license-MIT-blue.svg)]()

Sistem manajemen perpustakaan enterprise-grade berbasis microservices dengan implementasi **CQRS pattern**, **complete CI/CD pipeline**, **comprehensive monitoring**, dan **distributed logging**.

## 🎯 Key Features

- ✅ **CQRS Pattern Implementation** - Command Query Responsibility Segregation
- ✅ **Full CI/CD Pipeline** - Jenkins with automated testing & deployment
- ✅ **Comprehensive Monitoring** - Prometheus, Grafana, Alertmanager
- ✅ **Distributed Logging** - ELK Stack (Elasticsearch, Logstash, Kibana)
- ✅ **Distributed Tracing** - Zipkin integration
- ✅ **Service Discovery** - Netflix Eureka
- ✅ **API Gateway** - Spring Cloud Gateway
- ✅ **Health Checks** - Spring Boot Actuator
- ✅ **API Documentation** - OpenAPI/Swagger
- ✅ **Containerization** - Docker & Docker Compose
- ✅ **Load Balancing** - Nginx reverse proxy
- ✅ **Circuit Breaker** - Resilience4j
- ✅ **Security Scanning** - OWASP Dependency Check

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                            │
└─────────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    NGINX REVERSE PROXY                          │
│                  (Load Balancing & SSL)                         │
└─────────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API GATEWAY (8080)                         │
│              (Routing, Rate Limiting, Auth)                     │
└─────────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                  EUREKA SERVER (8761)                           │
│                  (Service Discovery)                            │
└─────────────────────────────────────────────────────────────────┘
                              ▼
┌──────────────────┬──────────────────┬──────────────────────────┐
│  Service Anggota │  Service Buku    │  Service Peminjaman      │
│     (8081)       │     (8082)       │      (8083)              │
│  - CQRS Commands │  - CRUD Ops      │  - Inter-service Comm   │
│  - CQRS Queries  │  - Validation    │  - State Management     │
└──────────────────┴──────────────────┴──────────────────────────┘
                              ▼
                   Service Pengembalian (8084)
                   - Late Fee Calculation
                   - Return Processing

┌─────────────────────────────────────────────────────────────────┐
│                    MONITORING STACK                             │
├─────────────┬──────────────┬──────────────┬────────────────────┤
│ Prometheus  │   Grafana    │   Zipkin     │   ELK Stack        │
│   (9090)    │   (3000)     │   (9411)     │  (ES/LS/KB)        │
└─────────────┴──────────────┴──────────────┴────────────────────┘
```

## 📋 Prerequisites

### Required Software
- **Java 17+** - [Download OpenJDK](https://adoptium.net/)
- **Maven 3.6+** - [Download Maven](https://maven.apache.org/download.cgi)
- **Docker 20.10+** - [Download Docker](https://docs.docker.com/get-docker/)
- **Docker Compose v2+** - [Install Compose](https://docs.docker.com/compose/install/)

### Optional Tools
- **Jenkins** - For CI/CD pipeline
- **Git** - Version control
- **Postman** - API testing

### System Requirements
- **RAM**: 8GB minimum (16GB recommended)
- **CPU**: 4 cores minimum
- **Disk**: 20GB free space
- **OS**: Linux, macOS, or Windows with WSL2

## 🚀 Quick Start

### 1. Clone Repository
```bash
git clone <repository-url>
cd perpustakaan-microservices
```

### 2. Build All Services
```bash
chmod +x scripts/deploy.sh
./scripts/deploy.sh dev
```

### 3. Verify Deployment
```bash
chmod +x scripts/health-check.sh
./scripts/health-check.sh
```

## 🔧 Manual Setup

### Build Individual Services
```bash
# Build Eureka Server
cd eureka-server
mvn clean package -DskipTests
cd ..

# Build API Gateway
cd api-gateway
mvn clean package -DskipTests
cd ..

# Build all microservices
for service in service-anggota service-buku service-peminjaman service-pengembalian; do
    cd $service
    mvn clean package -DskipTests
    cd ..
done
```

### Run with Docker Compose
```bash
# Development environment
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# Production environment
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## 🔍 Accessing Services

### Application Services
| Service | URL | Description |
|---------|-----|-------------|
| **Eureka Dashboard** | http://localhost:8761 | Service registry |
| **API Gateway** | http://localhost:8080 | Main entry point |
| **Service Anggota** | http://localhost:8081/swagger-ui.html | Member management |
| **Service Buku** | http://localhost:8082/swagger-ui.html | Book catalog |
| **Service Peminjaman** | http://localhost:8083/swagger-ui.html | Borrowing service |
| **Service Pengembalian** | http://localhost:8084/swagger-ui.html | Return service |

### Monitoring & Logging
| Tool | URL | Credentials | Purpose |
|------|-----|-------------|---------|
| **Prometheus** | http://localhost:9090 | - | Metrics collection |
| **Grafana** | http://localhost:3000 | admin/admin | Metrics visualization |
| **Kibana** | http://localhost:5601 | - | Log analysis |
| **Zipkin** | http://localhost:9411 | - | Distributed tracing |
| **Alertmanager** | http://localhost:9093 | - | Alert management |

### Nginx Access
| Path | Backend | Description |
|------|---------|-------------|
| http://localhost/api/ | API Gateway | All microservices |
| http://localhost/grafana/ | Grafana | Monitoring dashboards |
| http://localhost/kibana/ | Kibana | Log viewer |
| http://localhost/prometheus/ | Prometheus | Metrics |
| http://localhost/zipkin/ | Zipkin | Traces |

## 📡 API Documentation

### Service Anggota (Member Management)

#### Create Member
```bash
POST http://localhost:8080/api/anggota
Content-Type: application/json

{
  "nomorAnggota": "A001",
  "nama": "John Doe",
  "alamat": "Jl. Contoh No. 123",
  "email": "john@example.com"
}
```

#### Get Member by ID
```bash
GET http://localhost:8080/api/anggota/1
```

#### Get All Members (with pagination)
```bash
GET http://localhost:8080/api/anggota?page=0&size=10&sortBy=nama
```

#### Update Member
```bash
PUT http://localhost:8080/api/anggota/1
Content-Type: application/json

{
  "nomorAnggota": "A001",
  "nama": "John Doe Updated",
  "alamat": "Jl. Updated No. 456",
  "email": "john.updated@example.com"
}
```

#### Delete Member
```bash
DELETE http://localhost:8080/api/anggota/1
```

### CQRS Pattern Implementation

The service-anggota implements CQRS (Command Query Responsibility Segregation):

**Commands** (Write Operations):
- `CreateAnggotaCommand` - Create new member
- `UpdateAnggotaCommand` - Update existing member
- `DeleteAnggotaCommand` - Delete member

**Queries** (Read Operations):
- `GetAnggotaByIdQuery` - Retrieve member by ID
- `GetAllAnggotaQuery` - List all members with pagination
- `GetAnggotaByNomorQuery` - Find member by number

## 🔄 CI/CD Pipeline

### Jenkins Setup

1. **Install Jenkins**
```bash
docker run -d -p 8090:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --name jenkins jenkins/jenkins:lts
```

2. **Access Jenkins**
- URL: http://localhost:8090
- Get initial password: `docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword`

3. **Install Required Plugins**
- Docker Pipeline
- Maven Integration
- JaCoCo
- SonarQube Scanner
- Email Extension
- Slack Notification (optional)

4. **Configure Credentials**
- Docker Hub credentials: `docker-hub-credentials`
- SonarQube token: `sonarqube-token`
- Email SMTP settings

5. **Create Pipeline**
- New Item → Pipeline
- Pipeline script from SCM
- Repository URL: `<your-repo-url>`
- Script Path: `Jenkinsfile`

### Pipeline Stages

```
┌─────────────┐    ┌─────────────┐    ┌──────────────┐
│  Checkout   │───▶│ Build & Test│───▶│Code Quality  │
└─────────────┘    └─────────────┘    └──────────────┘
                                              │
      ┌───────────────────────────────────────┘
      │
      ▼
┌──────────────┐    ┌──────────────┐    ┌───────────────┐
│Quality Gate  │───▶│Security Scan │───▶│Docker Build   │
└──────────────┘    └──────────────┘    └───────────────┘
                                              │
      ┌───────────────────────────────────────┘
      │
      ▼
┌───────────────┐    ┌──────────────┐    ┌──────────────┐
│Push Registry  │───▶│  Deploy      │───▶│Health Check  │
└───────────────┘    └──────────────┘    └──────────────┘
```

## 📊 Monitoring & Observability

### Prometheus Metrics

Key metrics collected:
- **HTTP Metrics**: Request count, duration, status codes
- **JVM Metrics**: Memory usage, GC time, thread count
- **Database Metrics**: Connection pool, query time
- **Custom Metrics**: Business KPIs

### Grafana Dashboards

Pre-configured dashboards:
1. **Microservices Overview** - System-wide health
2. **Service Performance** - Per-service metrics
3. **JVM Metrics** - Memory, GC, threads
4. **Database Performance** - Connection pools, queries
5. **Error Analysis** - Error rates and types

### Distributed Tracing

Zipkin traces show:
- Request flow across services
- Service dependencies
- Latency breakdown
- Error propagation

### Log Aggregation

ELK Stack provides:
- **Centralized logging** - All logs in one place
- **Full-text search** - Find logs quickly
- **Log parsing** - Structured log data
- **Visualization** - Log trends and patterns
- **Alerting** - Proactive issue detection

## 🧪 Testing

### Run All Tests
```bash
mvn clean verify
```

### Run Unit Tests Only
```bash
mvn test
```

### Run Integration Tests
```bash
mvn integration-test
```

### Generate Coverage Report
```bash
mvn jacoco:report
```

### Run Security Scan
```bash
mvn org.owasp:dependency-check-maven:check
```

### Test Coverage Requirements
- **Line Coverage**: 70% minimum
- **Branch Coverage**: 60% minimum
- **Integration Tests**: All critical paths covered

## 🔒 Security

### Implemented Security Measures
- ✅ OWASP dependency scanning
- ✅ Input validation
- ✅ Rate limiting (Nginx)
- ✅ HTTPS support (configurable)
- ✅ Security headers
- ✅ Actuator endpoint protection

### Security Best Practices
1. Regular dependency updates
2. Secret management (never commit secrets)
3. Role-based access control
4. API key authentication (optional)
5. Network segmentation (Docker networks)

## 🐛 Troubleshooting

### Services Not Starting
```bash
# Check logs
docker-compose logs [service-name]

# Check container status
docker-compose ps

# Restart specific service
./scripts/restart-service.sh [service-name]
```

### Port Already in Use
```bash
# Find process using port
lsof -i :[port]  # macOS/Linux
netstat -ano | findstr :[port]  # Windows

# Kill process or change port in configuration
```

### Memory Issues
```bash
# Increase Docker memory limit
# Docker Desktop → Settings → Resources → Memory

# Or reduce service memory
# Edit JAVA_OPTS in docker-compose.yml
```

### Health Check Failures
```bash
# Run health check script
./scripts/health-check.sh

# Check individual service
curl http://localhost:[port]/actuator/health

# View service logs
docker-compose logs -f [service-name]
```

## 📝 Maintenance Scripts

```bash
# Deploy services
./scripts/deploy.sh [dev|staging|prod]

# Health check
./scripts/health-check.sh

# View logs
./scripts/logs.sh [service-name]

# Restart service
./scripts/restart-service.sh [service-name]

# Cleanup Docker resources
./scripts/cleanup.sh

# Backup data
./scripts/backup.sh
```

## 🔄 Update Procedure

1. **Pull latest changes**
```bash
git pull origin main
```

2. **Build services**
```bash
./scripts/deploy.sh dev
```

3. **Run tests**
```bash
mvn verify
```

4. **Deploy**
```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

## 📚 Tech Stack

### Backend
- **Spring Boot 4.0.0** - Application framework
- **Spring Cloud 2025.1.0** - Microservices framework
- **Netflix Eureka** - Service discovery
- **Spring Cloud Gateway** - API gateway
- **H2 Database** - In-memory database
- **Hibernate** - ORM

### Monitoring
- **Prometheus** - Metrics collection
- **Grafana** - Metrics visualization
- **Alertmanager** - Alert management

### Logging
- **Elasticsearch** - Log storage & search
- **Logstash** - Log processing
- **Kibana** - Log visualization
- **Filebeat** - Log shipping

### Tracing
- **Zipkin** - Distributed tracing

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Orchestration
- **Jenkins** - CI/CD
- **Maven** - Build tool
- **Nginx** - Reverse proxy

### Quality
- **JUnit 5** - Unit testing
- **REST Assured** - API testing
- **JaCoCo** - Code coverage
- **SonarQube** - Code quality
- **OWASP** - Security scanning
- **SpotBugs** - Static analysis
- **Checkstyle** - Code style
- **PMD** - Code analysis

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Contributors

- **Erland Agsya** - Initial work

## 🙏 Acknowledgments

- Spring Boot team
- Netflix OSS
- Elastic team
- Prometheus & Grafana communities

## 📞 Support

For support, email team@perpustakaan.com or create an issue in the repository.

---

Made by Erland Agsya