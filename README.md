# 📚 Microservices Perpustakaan - CI/CD, Monitoring & Logging

Sistem manajemen perpustakaan berbasis microservices dengan implementasi lengkap CI/CD Pipeline, Monitoring, dan Distributed Tracing.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     External Clients                         │
└───────────────────────┬─────────────────────────────────────┘
                        │
        ┌───────────────┴──────────────┐
        │      Eureka Server           │
        │   (Service Discovery)        │
        └───────────────┬──────────────┘
                        │
        ┌───────────────┴──────────────────────┐
        │                                      │
   ┌────▼────┐  ┌────────┐  ┌─────────┐  ┌───────────┐
   │Anggota  │  │  Buku  │  │Peminjaman│  │Pengembalian│
   │Service  │  │Service │  │ Service │  │  Service   │
   │ :8081   │  │ :8082  │  │  :8083  │  │   :8084    │
   └─────────┘  └────────┘  └─────────┘  └───────────┘
        │           │            │              │
        └───────────┴────────────┴──────────────┘
                        │
        ┌───────────────┴──────────────────────┐
        │                                      │
   ┌────▼────┐  ┌─────────┐  ┌────────┐  ┌────────┐
   │ Zipkin  │  │Prometheus│  │Grafana │  │Jenkins │
   │ :9411   │  │  :9090   │  │ :3000  │  │ :8080  │
   └─────────┘  └──────────┘  └────────┘  └────────┘
```

## 🎯 Features

### Microservices
- **Eureka Server**: Service Discovery & Registration
- **Service Anggota**: Manajemen data anggota perpustakaan
- **Service Buku**: Manajemen katalog buku
- **Service Peminjaman**: Transaksi peminjaman buku
- **Service Pengembalian**: Transaksi pengembalian buku

### Infrastructure
- **Docker & Docker Compose**: Containerization
- **Jenkins**: CI/CD Pipeline automation
- **Prometheus**: Metrics collection
- **Grafana**: Metrics visualization
- **Zipkin**: Distributed tracing

## 🚀 Quick Start

### Prerequisites
```bash
- Java 17
- Maven 3.9+
- Docker & Docker Compose
- Git
- Jenkins (optional for CI/CD)
```

### Installation

1. **Clone Repository**
```bash
git clone <repository-url>
cd micro-services-perpustakaan
```

2. **Build All Services**
```bash
./build-all.sh
# atau manual:
mvn clean package -DskipTests
```

3. **Start Infrastructure**
```bash
docker-compose up -d
```

4. **Verify Services**
```bash
# Run automated test
chmod +x test-microservices.sh
./test-microservices.sh
```

## 📦 Service Endpoints

### Eureka Server
- Dashboard: http://localhost:8761

### Service Anggota (Port 8081)
```bash
# Create
POST http://localhost:8081/api/anggota
Content-Type: application/json
{
  "nomorAnggota": "A001",
  "nama": "John Doe",
  "alamat": "Jl. Test",
  "email": "john@test.com"
}

# Get All
GET http://localhost:8081/api/anggota

# Get by ID
GET http://localhost:8081/api/anggota/{id}
```

### Service Buku (Port 8082)
```bash
# Create
POST http://localhost:8082/api/buku
Content-Type: application/json
{
  "kodeBuku": "B001",
  "judul": "Spring Boot",
  "pengarang": "Author Name",
  "penerbit": "Publisher",
  "tahunTerbit": 2024
}

# Get All
GET http://localhost:8082/api/buku

# Get by ID
GET http://localhost:8082/api/buku/{id}
```

### Service Peminjaman (Port 8083)
```bash
# Create
POST http://localhost:8083/api/peminjaman
Content-Type: application/json
{
  "anggotaId": 1,
  "bukuId": 1,
  "tanggalPinjam": "2024-12-01",
  "tanggalKembali": "2024-12-15",
  "status": "DIPINJAM"
}

# Get with Details (includes Anggota & Buku data)
GET http://localhost:8083/api/peminjaman/{id}
```

### Service Pengembalian (Port 8084)
```bash
# Create
POST http://localhost:8084/api/pengembalian
Content-Type: application/json
{
  "peminjamanId": 1,
  "tanggalDikembalikan": "2024-12-16",
  "terlambat": 1,
  "denda": 5000
}

# Get with Details (includes Peminjaman data)
GET http://localhost:8084/api/pengembalian/{id}
```

## 📊 Monitoring & Observability

### Prometheus (Port 9090)
- Access: http://localhost:9090
- Metrics endpoint: http://localhost:808x/actuator/prometheus
- Query examples:
  - `up` - Check services status
  - `http_server_requests_seconds_count` - HTTP request count
  - `jvm_memory_used_bytes` - JVM memory usage

### Grafana (Port 3000)
- Access: http://localhost:3000
- Default credentials: admin/admin
- Add Prometheus datasource: http://prometheus:9090
- Import dashboard ID: 4701 (JVM Micrometer)

### Zipkin (Port 9411)
- Access: http://localhost:9411
- View distributed traces
- Analyze inter-service communication

## 🔄 CI/CD Pipeline

### Jenkins Setup

1. **Install Jenkins**
```bash
# Docker
docker run -p 8080:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts

# Access: http://localhost:8080
```

2. **Configure Pipeline**
- Create new Pipeline job
- SCM: Git (your repository)
- Script Path: Jenkinsfile

3. **Required Plugins**
- Docker Pipeline
- Maven Integration
- Email Extension

### Pipeline Stages
1. **Checkout**: Pull source code
2. **Build & Test**: Compile and test all services
3. **Code Quality**: SonarQube analysis (optional)
4. **Build Docker Images**: Create container images
5. **Push to Registry**: Push images to Docker registry
6. **Deploy**: Deploy using docker-compose
7. **Health Check**: Verify all services are healthy

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Automated Full Test
```bash
./test-microservices.sh
```

### Manual Testing
```bash
# Import Postman collection
postman-collection.json
```

## 📈 Performance Monitoring

### Health Checks
```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

### Metrics
```bash
curl http://localhost:8081/actuator/metrics
curl http://localhost:8081/actuator/prometheus
```

## 🛠️ Configuration

### Application Properties
Each service has its own configuration:
- Database: H2 (in-memory)
- Eureka client configuration
- Actuator endpoints
- Zipkin tracing

### Environment Variables
```yaml
# docker-compose.yml
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka/
MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
```

## 🐛 Troubleshooting

### Service tidak register ke Eureka
```bash
# Check Eureka logs
docker-compose logs eureka-server

# Restart service
docker-compose restart service-anggota
```

### Health check failed
```bash
# Check service logs
docker-compose logs -f service-anggota

# Check network
docker exec service-anggota ping eureka-server
```

### Prometheus tidak scraping
```bash
# Verify prometheus.yml
cat prometheus.yml

# Check targets
curl http://localhost:9090/api/v1/targets
```

## 📝 Development

### Add New Service
1. Create Spring Boot project
2. Add dependencies (Eureka Client, Actuator, etc.)
3. Configure application.properties
4. Create Dockerfile
5. Update docker-compose.yml
6. Update Jenkinsfile
7. Update prometheus.yml

### Update Service
```bash
# Build
mvn clean package

# Rebuild image
docker-compose build service-name

# Restart
docker-compose restart service-name
```

## 🔒 Security Considerations

- Implement Spring Security for authentication
- Use JWT for stateless authentication
- Add API Gateway for centralized security
- Enable HTTPS/TLS
- Secure Eureka dashboard
- Implement rate limiting

## 🚀 Production Deployment

### Kubernetes
```bash
# Coming soon
kubectl apply -f k8s/
```

### Production Checklist
- [ ] Replace H2 with production database (PostgreSQL/MySQL)
- [ ] Configure external configuration server
- [ ] Set up load balancers
- [ ] Implement circuit breakers
- [ ] Configure backup and recovery
- [ ] Set up alerting
- [ ] Implement log aggregation (ELK)

## 📚 Documentation

- [API Documentation](docs/api.md)
- [Architecture Guide](docs/architecture.md)
- [Deployment Guide](docs/deployment.md)
- [Monitoring Guide](docs/monitoring.md)

## 👥 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see LICENSE file for details.

## 🙏 Acknowledgments

- Spring Boot
- Spring Cloud Netflix
- Micrometer
- Zipkin
- Prometheus
- Grafana

## 📞 Contact

- Email: blackpenta98@gmail.com

---