# 📚 Sistem Microservices Perpustakaan

Sistem manajemen perpustakaan berbasis microservices dengan implementasi CI/CD, monitoring, dan distributed tracing.

## 🏗️ Arsitektur

```
┌─────────────────┐
│   API Gateway   │ (Port 8080)
│   Load Balancer │
└────────┬────────┘
         │
    ┌────┴─────────────────────┐
    │   Eureka Server (8761)   │
    │   Service Discovery      │
    └──────────────────────────┘
         │
    ┌────┴────────────────────────────┐
    │                                 │
┌───┴────────┐  ┌──────────────┐  ┌──┴──────────┐
│  Service   │  │   Service    │  │   Service   │
│  Anggota   │  │    Buku      │  │ Peminjaman  │
│  (8081)    │  │   (8082)     │  │   (8083)    │
└────────────┘  └──────────────┘  └─────────────┘
                                       │
                                  ┌────┴─────────┐
                                  │   Service    │
                                  │ Pengembalian │
                                  │   (8084)     │
                                  └──────────────┘

Monitoring Stack:
┌──────────┐  ┌──────────┐  ┌──────────┐
│Prometheus│  │ Grafana  │  │  Zipkin  │
│  (9090)  │  │  (3000)  │  │  (9411)  │
└──────────┘  └──────────┘  └──────────┘
```

## 🚀 Services

| Service | Port | Deskripsi |
|---------|------|-----------|
| Eureka Server | 8761 | Service Discovery & Registration |
| API Gateway | 8080 | Routing & Load Balancing |
| Service Anggota | 8081 | Manajemen data anggota perpustakaan |
| Service Buku | 8082 | Manajemen katalog buku |
| Service Peminjaman | 8083 | Proses peminjaman buku |
| Service Pengembalian | 8084 | Proses pengembalian buku |
| Prometheus | 9090 | Metrics Collection |
| Grafana | 3000 | Metrics Visualization |
| Zipkin | 9411 | Distributed Tracing |

## 📋 Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- Jenkins (untuk CI/CD)

## 🛠️ Instalasi & Menjalankan

### 1. Clone Repository
```bash
git clone <repository-url>
cd perpustakaan-microservices
```

### 2. Build Semua Services
```bash
# Build Eureka Server
cd perpustakaan-microservices/eureka-server
mvn clean package -DskipTests

# Build Service Anggota
cd ../../service-anggota
mvn clean package -DskipTests

# Build Service Buku
cd ../service-buku
mvn clean package -DskipTests

# Build Service Peminjaman
cd ../service-peminjaman
mvn clean package -DskipTests

# Build Service Pengembalian
cd ../service-pengembalian
mvn clean package -DskipTests

# Build API Gateway
cd ../api-gateway
mvn clean package -DskipTests
```

### 3. Jalankan dengan Docker Compose
```bash
cd ..
docker-compose up -d
```

### 4. Cek Status Services
```bash
docker-compose ps
```

## 🔍 Akses Services

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Zipkin**: http://localhost:9411

## 📡 API Endpoints

### Via API Gateway (Port 8080)

#### Service Anggota
```bash
# Create Anggota
POST http://localhost:8080/api/anggota
{
  "nomorAnggota": "A001",
  "nama": "John Doe",
  "alamat": "Jl. Contoh No. 123",
  "email": "john@example.com"
}

# Get Anggota by ID
GET http://localhost:8080/api/anggota/{id}

# Get All Anggota
GET http://localhost:8080/api/anggota
```

#### Service Buku
```bash
# Create Buku
POST http://localhost:8080/api/buku
{
  "kodeBuku": "B001",
  "judul": "Spring Boot Microservices",
  "pengarang": "John Smith",
  "penerbit": "Tech Publisher",
  "tahunTerbit": 2024
}

# Get Buku by ID
GET http://localhost:8080/api/buku/{id}

# Get All Buku
GET http://localhost:8080/api/buku
```

#### Service Peminjaman
```bash
# Create Peminjaman
POST http://localhost:8080/api/peminjaman
{
  "anggotaId": 1,
  "bukuId": 1,
  "tanggalPinjam": "2024-12-10",
  "tanggalKembali": "2024-12-17",
  "status": "DIPINJAM"
}

# Get Peminjaman by ID (dengan detail anggota & buku)
GET http://localhost:8080/api/peminjaman/{id}
```

#### Service Pengembalian
```bash
# Create Pengembalian
POST http://localhost:8080/api/pengembalian
{
  "peminjamanId": 1,
  "tanggalDikembalikan": "2024-12-18",
  "terlambat": 1,
  "denda": 5000
}

# Get Pengembalian by ID (dengan detail peminjaman)
GET http://localhost:8080/api/pengembalian/{id}
```

## 🔧 CI/CD dengan Jenkins

### Setup Jenkins Pipeline

1. **Install Jenkins**
```bash
docker run -d -p 8090:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --name jenkins jenkins/jenkins:lts
```

2. **Install Required Plugins**
   - Docker Pipeline
   - Maven Integration
   - Email Extension
   - Slack Notification (optional)

3. **Create Pipeline Job**
   - New Item → Pipeline
   - Pipeline script from SCM
   - Select Git
   - Repository URL: <your-repo-url>
   - Script Path: Jenkinsfile

4. **Configure Credentials**
   - Docker Hub credentials (docker-hub-credentials)
   - Email SMTP settings

### Pipeline Stages

1. **Checkout**: Pull kode dari repository
2. **Build & Test**: Build semua services dengan Maven
3. **Code Quality**: Analisis kode (optional)
4. **Docker Build**: Build Docker images
5. **Push to Registry**: Push images ke registry
6. **Deploy**: Deploy dengan Docker Compose
7. **Health Check**: Verifikasi semua services berjalan

## 📊 Monitoring

### Prometheus Metrics
- Request rates
- Response times
- Error rates
- JVM metrics

### Grafana Dashboards
1. Login: http://localhost:3000
2. Username: admin
3. Password: admin
4. Import dashboards dari folder `grafana/dashboards/`

### Zipkin Tracing
- Lihat request traces di http://localhost:9411
- Monitor latency antar services
- Debug distributed transactions

## 🐳 Docker Commands

```bash
# Build ulang semua images
docker-compose build

# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f [service-name]

# Restart specific service
docker-compose restart [service-name]

# Remove all containers dan volumes
docker-compose down -v
```

## 🔄 Update Docker Images

### Jika Ada Perubahan Kode:

```bash
# 1. Build ulang JAR file
cd [service-directory]
mvn clean package -DskipTests

# 2. Build ulang Docker image
docker-compose build [service-name]

# 3. Restart service
docker-compose up -d [service-name]

# Atau rebuild semua sekaligus:
docker-compose down
docker-compose build
docker-compose up -d
```

### Update via Jenkins:
- Trigger pipeline build
- Jenkins akan otomatis build, test, dan deploy

## 🧪 Testing

```bash
# Run tests untuk semua services
cd perpustakaan-microservices/eureka-server && mvn test
cd ../../service-anggota && mvn test
cd ../service-buku && mvn test
cd ../service-peminjaman && mvn test
cd ../service-pengembalian && mvn test
```

## 📝 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE | Eureka server URL | http://eureka-server:8761/eureka/ |
| MANAGEMENT_ZIPKIN_TRACING_ENDPOINT | Zipkin endpoint | http://zipkin:9411/api/v2/spans |

## 🛡️ Best Practices

1. **Service Communication**: Gunakan Eureka untuk service discovery
2. **Circuit Breaker**: Implementasi Resilience4j untuk fault tolerance
3. **API Gateway**: Selalu akses services melalui gateway
4. **Monitoring**: Pantau metrics di Grafana secara regular
5. **Logging**: Gunakan Zipkin untuk trace distributed requests

## 🐛 Troubleshooting

### Service tidak register di Eureka
- Cek koneksi network
- Pastikan eureka.client.service-url.defaultZone benar
- Tunggu 30 detik untuk registrasi

### Docker Compose gagal start
```bash
# Cek logs
docker-compose logs

# Rebuild images
docker-compose build --no-cache
```

### Port sudah digunakan
```bash
# Cek port yang digunakan
netstat -ano | findstr :8080

# Ubah port di application.properties
```

## 📚 Tech Stack

- **Framework**: Spring Boot 4.0.0, Spring Cloud 2025.1.0
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Database**: H2 (in-memory)
- **Monitoring**: Prometheus + Grafana
- **Tracing**: Zipkin
- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **CI/CD**: Jenkins
- **Build Tool**: Maven

## 👥 Contributors

- Erland Agsya

## OPEN SOURCE
