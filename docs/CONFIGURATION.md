# ⚙️ Configuration Guide

Dokumentasi lengkap semua file konfigurasi dalam sistem microservices perpustakaan.

## 📑 Daftar Isi

- [Application Properties](#application-properties)
- [Docker Configuration](#docker-configuration)
- [Environment Variables](#environment-variables)
- [Spring Profiles](#spring-profiles)
- [Port Configuration](#port-configuration)
- [Database Configuration](#database-configuration)
- [RabbitMQ Configuration](#rabbitmq-configuration)
- [Monitoring Configuration](#monitoring-configuration)

---

## Application Properties

### Eureka Server

**eureka-server/src/main/resources/application.properties**:

```properties
# Application Name
spring.application.name=eureka-server

# Server Port
server.port=8761

# Eureka Server Configuration
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# Self Preservation
eureka.server.enable-self-preservation=false
eureka.server.eviction-interval-timer-in-ms=10000

# Logging
logging.level.com.netflix.eureka=INFO
logging.level.com.netflix.discovery=INFO
```

### API Gateway

**api-gateway/src/main/resources/application.yml**:

```yaml
spring:
  application:
    name: api-gateway
  
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      
      routes:
        # Service Anggota Routes
        - id: service-anggota
          uri: lb://service-anggota
          predicates:
            - Path=/api/anggota/**
          filters:
            - name: CircuitBreaker
              args:
                name: anggotaCircuitBreaker
                fallbackUri: forward:/fallback/anggota
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY,GATEWAY_TIMEOUT
                methods: GET,POST,PUT,DELETE
                backoff:
                  firstBackoff: 50ms
                  maxBackoff: 500ms
        
        # Service Buku Routes
        - id: service-buku
          uri: lb://service-buku
          predicates:
            - Path=/api/buku/**
          filters:
            - name: CircuitBreaker
              args:
                name: bukuCircuitBreaker
                fallbackUri: forward:/fallback/buku
        
        # Service Peminjaman Routes
        - id: service-peminjaman
          uri: lb://service-peminjaman
          predicates:
            - Path=/api/peminjaman/**
          filters:
            - name: CircuitBreaker
              args:
                name: peminjamanCircuitBreaker
                fallbackUri: forward:/fallback/peminjaman
        
        # Service Pengembalian Routes
        - id: service-pengembalian
          uri: lb://service-pengembalian
          predicates:
            - Path=/api/pengembalian/**
          filters:
            - name: CircuitBreaker
              args:
                name: pengembalianCircuitBreaker
                fallbackUri: forward:/fallback/pengembalian

server:
  port: 8080

# Eureka Client Configuration
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30

# Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
    gateway:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true

# Circuit Breaker Configuration
resilience4j:
  circuitbreaker:
    instances:
      anggotaCircuitBreaker:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
      bukuCircuitBreaker:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
      peminjamanCircuitBreaker:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
      pengembalianCircuitBreaker:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000

# Logging
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

### Service Anggota

**service-anggota/src/main/resources/application.properties**:

```properties
# Application Configuration
spring.application.name=service-anggota
server.port=8081

# H2 Database (Write Model)
spring.datasource.url=jdbc:h2:mem:anggota_write_db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# MongoDB (Read Model)
spring.data.mongodb.uri=${MONGODB_URI_ANGGOTA:mongodb://localhost:27017/anggota_read_db}
spring.data.mongodb.auto-index-creation=true

# RabbitMQ Configuration
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USERNAME:guest}
spring.rabbitmq.password=${RABBITMQ_PASSWORD:guest}
spring.rabbitmq.virtual-host=/
spring.rabbitmq.publisher-confirm-type=correlated
spring.rabbitmq.publisher-returns=true
spring.rabbitmq.listener.simple.acknowledge-mode=auto
spring.rabbitmq.listener.simple.prefetch=1
spring.rabbitmq.listener.simple.concurrency=3
spring.rabbitmq.listener.simple.max-concurrency=10
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.initial-interval=1000
spring.rabbitmq.listener.simple.retry.max-attempts=3

# Eureka Client
eureka.client.service-url.defaultZone=${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
eureka.instance.prefer-ip-address=true
eureka.instance.lease-renewal-interval-in-seconds=10

# Actuator
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
management.health.rabbit.enabled=true
management.health.mongo.enabled=true

# Distributed Tracing
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint=${ZIPKIN_ENDPOINT:http://localhost:9411/api/v2/spans}

# Logging
logging.level.com.perpustakaan=DEBUG
logging.level.org.springframework.amqp=INFO
logging.level.org.springframework.data.mongodb=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Jackson Configuration
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=Asia/Jakarta

# API Documentation
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
```

---

## Docker Configuration

### docker-compose.yml

**docker-compose.yml** (Root):

```yaml
version: '3.8'

services:
  # Service Discovery
  eureka-server:
    build:
      context: ./eureka-server
      dockerfile: Dockerfile
    container_name: eureka-server
    ports:
      - "8761:8761"
    networks:
      - perpustakaan-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 40s

  # API Gateway
  api-gateway:
    build:
      context: ./api-gateway
      dockerfile: Dockerfile
    container_name: api-gateway
    ports:
      - "8080:8080"
    networks:
      - perpustakaan-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
    depends_on:
      eureka-server:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5

  # Infrastructure - MongoDB
  mongodb:
    image: mongo:6.0
    container_name: mongodb
    ports:
      - "27017:27017"
    networks:
      - perpustakaan-network
    volumes:
      - mongodb_data:/data/db
    environment:
      - MONGO_INITDB_DATABASE=perpustakaan
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Infrastructure - RabbitMQ
  rabbitmq:
    image: rabbitmq:3.13-management
    container_name: rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    networks:
      - perpustakaan-network
    environment:
      - RABBITMQ_DEFAULT_USER=guest
      - RABBITMQ_DEFAULT_PASS=guest
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Microservices
  service-anggota:
    build:
      context: ./service-anggota
      dockerfile: Dockerfile
    container_name: service-anggota
    ports:
      - "8081:8081"
    networks:
      - perpustakaan-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
      - MONGODB_URI_ANGGOTA=mongodb://mongodb:27017/anggota_read_db
      - RABBITMQ_HOST=rabbitmq
      - ZIPKIN_ENDPOINT=http://zipkin:9411/api/v2/spans
    depends_on:
      eureka-server:
        condition: service_healthy
      mongodb:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy

  service-buku:
    build:
      context: ./service-buku
      dockerfile: Dockerfile
    container_name: service-buku
    ports:
      - "8082:8082"
    networks:
      - perpustakaan-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
      - MONGODB_URI_BUKU=mongodb://mongodb:27017/buku_read_db
      - RABBITMQ_HOST=rabbitmq
      - ZIPKIN_ENDPOINT=http://zipkin:9411/api/v2/spans
    depends_on:
      eureka-server:
        condition: service_healthy
      mongodb:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy

  # Monitoring - Prometheus
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    networks:
      - perpustakaan-network
    volumes:
      - ./monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
    restart: unless-stopped

  # Monitoring - Grafana
  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    networks:
      - perpustakaan-network
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning
      - grafana_data:/var/lib/grafana
    depends_on:
      - prometheus

  # Monitoring - Zipkin
  zipkin:
    image: openzipkin/zipkin:latest
    container_name: zipkin
    ports:
      - "9411:9411"
    networks:
      - perpustakaan-network
    environment:
      - STORAGE_TYPE=mem

  # Logging - Elasticsearch
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: elasticsearch
    ports:
      - "9200:9200"
      - "9300:9300"
    networks:
      - perpustakaan-network
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data

  # Logging - Logstash
  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    container_name: logstash
    ports:
      - "5000:5000"
      - "9600:9600"
    networks:
      - perpustakaan-network
    volumes:
      - ./monitoring/logstash/pipeline:/usr/share/logstash/pipeline
    environment:
      - "LS_JAVA_OPTS=-Xmx256m -Xms256m"
    depends_on:
      - elasticsearch

  # Logging - Kibana
  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    container_name: kibana
    ports:
      - "5601:5601"
    networks:
      - perpustakaan-network
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch

networks:
  perpustakaan-network:
    driver: bridge

volumes:
  mongodb_data:
  rabbitmq_data:
  prometheus_data:
  grafana_data:
  elasticsearch_data:
```

---

## Environment Variables

### Development (.env.example)

```bash
# Eureka Server
EUREKA_SERVER_URL=http://localhost:8761/eureka/

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# MongoDB
MONGODB_URI_ANGGOTA=mongodb://localhost:27017/anggota_read_db
MONGODB_URI_BUKU=mongodb://localhost:27017/buku_read_db
MONGODB_URI_PEMINJAMAN=mongodb://localhost:27017/peminjaman_read_db
MONGODB_URI_PENGEMBALIAN=mongodb://localhost:27017/pengembalian_read_db

# Zipkin
ZIPKIN_ENDPOINT=http://localhost:9411/api/v2/spans

# Elasticsearch
ELASTICSEARCH_HOSTS=http://localhost:9200

# Logstash
LOGSTASH_HOST=localhost
LOGSTASH_PORT=5000

# Prometheus
PROMETHEUS_PORT=9090

# Grafana
GRAFANA_PORT=3000
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
```

### Production (.env.production)

```bash
# Eureka Server
EUREKA_SERVER_URL=http://eureka-server.production.local:8761/eureka/

# RabbitMQ
RABBITMQ_HOST=rabbitmq.production.local
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=${RABBITMQ_USER}
RABBITMQ_PASSWORD=${RABBITMQ_PASS}

# MongoDB
MONGODB_URI_ANGGOTA=mongodb://mongodb.production.local:27017/anggota_read_db?authSource=admin
MONGODB_USERNAME=${MONGO_USER}
MONGODB_PASSWORD=${MONGO_PASS}

# Security
JWT_SECRET=${JWT_SECRET_KEY}
ENCRYPTION_KEY=${ENCRYPTION_KEY}

# Resource Limits
JAVA_OPTS=-Xmx2g -Xms1g -XX:+UseG1GC
```

---

## Spring Profiles

### Development Profile

**application-dev.properties**:

```properties
# Development specific settings
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 Console
spring.h2.console.enabled=true

# Debug logging
logging.level.com.perpustakaan=DEBUG
logging.level.org.springframework.web=DEBUG

# Disable security (for development)
management.security.enabled=false

# Tracing - Sample all requests
management.tracing.sampling.probability=1.0
```

### Docker Profile

**application-docker.properties**:

```properties
# Use containerized services
eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka/
spring.data.mongodb.uri=mongodb://mongodb:27017/anggota_read_db
spring.rabbitmq.host=rabbitmq
management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans

# Adjust for container networking
eureka.instance.prefer-ip-address=false
eureka.instance.hostname=${HOSTNAME}
```

### Production Profile

**application-prod.properties**:

```properties
# Production optimizations
spring.jpa.show-sql=false
spring.h2.console.enabled=false

# Logging
logging.level.root=WARN
logging.level.com.perpustakaan=INFO

# Security
management.security.enabled=true
management.endpoints.web.exposure.include=health,prometheus

# Tracing - Sample 10% of requests
management.tracing.sampling.probability=0.1

# Connection pooling
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10
spring.rabbitmq.cache.channel.size=25
```

---

## Port Configuration

| Service | Port | Protocol | Description |
|---------|------|----------|-------------|
| **Eureka Server** | 8761 | HTTP | Service Registry Dashboard |
| **API Gateway** | 8080 | HTTP | Main API Endpoint |
| **Service Anggota** | 8081 | HTTP | Member Service |
| **Service Buku** | 8082 | HTTP | Book Service |
| **Service Peminjaman** | 8083 | HTTP | Borrowing Service |
| **Service Pengembalian** | 8084 | HTTP | Return Service |
| **MongoDB** | 27017 | TCP | Database |
| **RabbitMQ** | 5672 | AMQP | Message Broker |
| **RabbitMQ Management** | 15672 | HTTP | RabbitMQ UI |
| **Prometheus** | 9090 | HTTP | Metrics Collection |
| **Grafana** | 3000 | HTTP | Metrics Visualization |
| **Zipkin** | 9411 | HTTP | Distributed Tracing |
| **Elasticsearch** | 9200 | HTTP | Log Storage |
| **Kibana** | 5601 | HTTP | Log Visualization |
| **Logstash** | 5000 | TCP | Log Processing |
| **Jenkins** | 9000 | HTTP | CI/CD |

---

## Database Configuration

### H2 (Write Model)

```properties
# In-memory database for development
spring.datasource.url=jdbc:h2:mem:anggota_write_db

# File-based for persistence (optional)
spring.datasource.url=jdbc:h2:file:./data/anggota_write_db

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.h2.console.settings.web-allow-others=false
```

### MongoDB (Read Model)

```properties
# Connection URI
spring.data.mongodb.uri=mongodb://localhost:27017/anggota_read_db

# With authentication
spring.data.mongodb.uri=mongodb://username:password@localhost:27017/anggota_read_db?authSource=admin

# Replica Set
spring.data.mongodb.uri=mongodb://host1:27017,host2:27017,host3:27017/anggota_read_db?replicaSet=rs0

# Connection pool
spring.data.mongodb.max-pool-size=50
spring.data.mongodb.min-pool-size=10
```

---

## RabbitMQ Configuration

### Connection Settings

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.virtual-host=/

# Connection pool
spring.rabbitmq.cache.connection.mode=channel
spring.rabbitmq.cache.connection.size=25
spring.rabbitmq.cache.channel.size=10

# Publisher configuration
spring.rabbitmq.publisher-confirm-type=correlated
spring.rabbitmq.publisher-returns=true
spring.rabbitmq.template.mandatory=true

# Consumer configuration
spring.rabbitmq.listener.simple.acknowledge-mode=auto
spring.rabbitmq.listener.simple.prefetch=1
spring.rabbitmq.listener.simple.concurrency=3
spring.rabbitmq.listener.simple.max-concurrency=10
spring.rabbitmq.listener.simple.default-requeue-rejected=false

# Retry configuration
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.initial-interval=1000
spring.rabbitmq.listener.simple.retry.max-attempts=3
spring.rabbitmq.listener.simple.retry.multiplier=2.0
spring.rabbitmq.listener.simple.retry.max-interval=10000
```

---

## Monitoring Configuration

### Prometheus

**monitoring/prometheus/prometheus.yml**:

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'service-anggota'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['service-anggota:8081']
        labels:
          service: 'anggota'
          environment: 'production'
```

### Grafana

**monitoring/grafana/provisioning/datasources/prometheus.yml**:

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
    jsonData:
      timeInterval: "15s"
```

---

[⬅️ Back to Documentation Index](README.md)