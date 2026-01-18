# 📋 Setup Guide - Sistem Microservices Perpustakaan

Panduan lengkap untuk setup proyek dari awal hingga production-ready.

## 📑 Daftar Isi

- [Persiapan Environment](#persiapan-environment)
- [Setup Infrastructure](#setup-infrastructure)
- [Setup Microservices](#setup-microservices)
- [Setup Monitoring](#setup-monitoring)
- [Setup CI/CD](#setup-cicd)
- [Verifikasi Setup](#verifikasi-setup)

---

## Persiapan Environment

### 1. Install Prerequisites

**Java Development Kit (JDK) 21**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# MacOS
brew install openjdk@21

# Windows - Download dari Oracle atau AdoptOpenJDK
# https://adoptium.net/

# Verifikasi instalasi
java -version
```

**Maven 3.9+**
```bash
# Ubuntu/Debian
sudo apt install maven

# MacOS
brew install maven

# Windows - Download binary dari Apache Maven
# https://maven.apache.org/download.cgi

# Verifikasi instalasi
mvn -version
```

**Docker & Docker Compose**
```bash
# Ubuntu/Debian
sudo apt install docker.io docker-compose

# MacOS
brew install docker docker-compose
# Atau install Docker Desktop

# Windows - Install Docker Desktop
# https://www.docker.com/products/docker-desktop

# Verifikasi instalasi
docker --version
docker-compose --version

# Start Docker daemon
sudo systemctl start docker
sudo systemctl enable docker

# Tambahkan user ke docker group
sudo usermod -aG docker $USER
newgrp docker
```

**Git**
```bash
# Ubuntu/Debian
sudo apt install git

# MacOS
brew install git

# Windows - Download dari git-scm.com
# https://git-scm.com/download/win

# Verifikasi instalasi
git --version
```

### 2. Clone Repository

```bash
# Clone project
git clone https://github.com/erlaaaand/micro-services-perpustakaan.git
cd micro-services-perpustakaan

# Buat branch development
git checkout -b development
```

### 3. Setup Environment Variables

```bash
# Salin file environment template
cp .env.example .env

# Edit file .env
nano .env
```

**.env File Content:**
```properties
# Eureka Server
EUREKA_SERVER_URL=http://localhost:8761/eureka/

# RabbitMQ Configuration
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# MongoDB Configuration
MONGODB_URI_ANGGOTA=mongodb://localhost:27017/anggota_db
MONGODB_URI_BUKU=mongodb://localhost:27017/buku_db
MONGODB_URI_PEMINJAMAN=mongodb://localhost:27017/peminjaman_db
MONGODB_URI_PENGEMBALIAN=mongodb://localhost:27017/pengembalian_db

# Zipkin Tracing
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

---

## Setup Infrastructure

### 1. Buat Docker Network

```bash
# Buat network untuk semua services
docker network create perpustakaan-network
```

### 2. Setup MongoDB

```bash
# Pull MongoDB image
docker pull mongo:6.0

# Run MongoDB container
docker run -d \
  --name mongodb \
  --network perpustakaan-network \
  -p 27017:27017 \
  -v mongodb_data:/data/db \
  mongo:6.0

# Verifikasi MongoDB running
docker ps | grep mongodb

# Test koneksi
docker exec -it mongodb mongosh --eval "db.version()"
```

### 3. Setup RabbitMQ

```bash
# Pull RabbitMQ image dengan management plugin
docker pull rabbitmq:3.13-management

# Run RabbitMQ container
docker run -d \
  --name rabbitmq \
  --network perpustakaan-network \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3.13-management

# Verifikasi RabbitMQ running
docker ps | grep rabbitmq

# Test Management UI
curl http://localhost:15672
# Atau buka di browser: http://localhost:15672
# Login: guest/guest
```

---

## Setup Microservices

### 1. Build Eureka Server

```bash
cd eureka-server

# Build JAR
mvn clean package -DskipTests

# Verifikasi JAR created
ls -lh target/*.jar

cd ..
```

**Struktur Eureka Server:**
```
eureka-server/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/perpustakaan/eureka/
│       │       └── EurekaServerApplication.java
│       └── resources/
│           └── application.properties
├── pom.xml
└── Dockerfile
```

**eureka-server/pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
    </parent>
    
    <groupId>com.perpustakaan</groupId>
    <artifactId>eureka-server</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.1</spring-cloud.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
    </dependencies>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**eureka-server/src/main/resources/application.properties:**
```properties
spring.application.name=eureka-server
server.port=8761

eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

eureka.server.enable-self-preservation=false
eureka.server.eviction-interval-timer-in-ms=10000
```

**eureka-server/Dockerfile:**
```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Build API Gateway

```bash
cd api-gateway

# Build JAR
mvn clean package -DskipTests

cd ..
```

**api-gateway/pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
    </parent>
    
    <groupId>com.perpustakaan</groupId>
    <artifactId>api-gateway</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.1</spring-cloud.version>
    </properties>
    
    <dependencies>
        <!-- Spring Cloud Gateway -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        
        <!-- Eureka Client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        
        <!-- Circuit Breaker -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
        </dependency>
        
        <!-- Actuator for monitoring -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

**api-gateway/src/main/resources/application.yml:**
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
        - id: service-anggota
          uri: lb://service-anggota
          predicates:
            - Path=/api/anggota/**
          filters:
            - name: CircuitBreaker
              args:
                name: anggotaCircuitBreaker
                fallbackUri: forward:/fallback/anggota
        
        - id: service-buku
          uri: lb://service-buku
          predicates:
            - Path=/api/buku/**
          filters:
            - name: CircuitBreaker
              args:
                name: bukuCircuitBreaker
                fallbackUri: forward:/fallback/buku
        
        - id: service-peminjaman
          uri: lb://service-peminjaman
          predicates:
            - Path=/api/peminjaman/**
          filters:
            - name: CircuitBreaker
              args:
                name: peminjamanCircuitBreaker
                fallbackUri: forward:/fallback/peminjaman
        
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

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
    register-with-eureka: true
    fetch-registry: true

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always

resilience4j:
  circuitbreaker:
    instances:
      anggotaCircuitBreaker:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
        permitted-number-of-calls-in-half-open-state: 3
```

### 3. Build Service Anggota (dengan CQRS)

**service-anggota/pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
    </parent>
    
    <groupId>com.perpustakaan</groupId>
    <artifactId>service-anggota</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.1</spring-cloud.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Data JPA (Write Model) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <!-- H2 Database (Write Model) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Spring Data MongoDB (Read Model) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
        </dependency>
        
        <!-- RabbitMQ -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        
        <!-- Eureka Client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        
        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Micrometer Prometheus -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
        
        <!-- Zipkin -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-brave</artifactId>
        </dependency>
        <dependency>
            <groupId>io.zipkin.reporter2</groupId>
            <artifactId>zipkin-reporter-brave</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Swagger/OpenAPI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.2.0</version>
        </dependency>
    </dependencies>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**service-anggota/src/main/resources/application.properties:**
```properties
spring.application.name=service-anggota
server.port=8081

# H2 Database (Write Model)
spring.datasource.url=jdbc:h2:mem:anggota_write_db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true

# MongoDB (Read Model)
spring.data.mongodb.uri=${MONGODB_URI_ANGGOTA:mongodb://localhost:27017/anggota_read_db}

# RabbitMQ
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USERNAME:guest}
spring.rabbitmq.password=${RABBITMQ_PASSWORD:guest}

# Eureka
eureka.client.service-url.defaultZone=${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
eureka.instance.prefer-ip-address=true

# Actuator
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true

# Zipkin
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint=${ZIPKIN_ENDPOINT:http://localhost:9411/api/v2/spans}
```

**Buat struktur CQRS untuk Service Anggota:**

```bash
cd service-anggota
mkdir -p src/main/java/com/perpustakaan/anggota/{cqrs/{command,query,handler},entity/{command,query},repository/{command,query},event,dto,controller,config,exception}
```

**RabbitMQ Configuration - service-anggota/src/main/java/com/perpustakaan/anggota/config/RabbitMQConfig.java:**
```java
package com.perpustakaan.anggota.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String EXCHANGE_NAME = "anggota-exchange";
    public static final String QUEUE_NAME = "anggota-sync-queue";
    public static final String ROUTING_KEY = "anggota.routing.key";
    
    @Bean
    public TopicExchange anggotaExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }
    
    @Bean
    public Queue anggotaSyncQueue() {
        return new Queue(QUEUE_NAME, true);
    }
    
    @Bean
    public Binding binding(Queue anggotaSyncQueue, TopicExchange anggotaExchange) {
        return BindingBuilder
            .bind(anggotaSyncQueue)
            .to(anggotaExchange)
            .with(ROUTING_KEY);
    }
    
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
```

Lanjutkan dengan build service lainnya dengan pola yang sama.

```bash
cd service-anggota
mvn clean package -DskipTests
cd ..
```

Ulangi untuk service-buku, service-peminjaman, dan service-pengembalian.

---

## Setup Monitoring

### 1. Setup Prometheus

Buat konfigurasi Prometheus:

```bash
mkdir -p monitoring/prometheus
```

**monitoring/prometheus/prometheus.yml:**
```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: 'perpustakaan-microservices'

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
  
  - job_name: 'eureka-server'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['eureka-server:8761']
  
  - job_name: 'api-gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['api-gateway:8080']
  
  - job_name: 'service-anggota'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['service-anggota:8081']
  
  - job_name: 'service-buku'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['service-buku:8082']
  
  - job_name: 'service-peminjaman'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['service-peminjaman:8083']
  
  - job_name: 'service-pengembalian'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['service-pengembalian:8084']
```

### 2. Setup Grafana

```bash
mkdir -p monitoring/grafana/provisioning/{datasources,dashboards}
```

**monitoring/grafana/provisioning/datasources/prometheus.yml:**
```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
```

**monitoring/grafana/provisioning/dashboards/dashboard.yml:**
```yaml
apiVersion: 1

providers:
  - name: 'Default'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    allowUiUpdates: true
    options:
      path: /etc/grafana/provisioning/dashboards
```

### 3. Setup ELK Stack

**monitoring/logstash/pipeline/logstash.conf:**
```conf
input {
  tcp {
    port => 5000
    codec => json
  }
}

filter {
  if [logger_name] =~ "com.perpustakaan" {
    mutate {
      add_field => { "[@metadata][index]" => "app-logs" }
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "%{[@metadata][index]}-%{+YYYY.MM.dd}"
  }
  
  stdout {
    codec => rubydebug
  }
}
```

**monitoring/kibana/kibana.yml:**
```yaml
server.host: "0.0.0.0"
server.port: 5601
elasticsearch.hosts: ["http://elasticsearch:9200"]
```

---

## Setup CI/CD

### 1. Setup Jenkins

**Dockerfile-jenkins:**
```dockerfile
FROM jenkins/jenkins:lts

USER root

# Install Docker CLI
RUN apt-get update && \
    apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg && \
    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    apt-get update && \
    apt-get install -y docker-ce-cli

# Install Maven
RUN apt-get install -y maven

USER jenkins

# Install Jenkins plugins
RUN jenkins-plugin-cli --plugins \
    docker-workflow \
    pipeline-maven \
    git \
    credentials-binding \
    workflow-aggregator
```

**Jenkinsfile:**
```groovy
pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
    }
    
    stages {
        stage('Initialize') {
            steps {
                echo 'Initializing Pipeline...'
                echo "Build Number: ${env.BUILD_NUMBER}"
                echo "Git Commit: ${env.GIT_COMMIT}"
            }
        }
        
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build JARs') {
            parallel {
                stage('Build Eureka') {
                    steps {
                        dir('eureka-server') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Gateway') {
                    steps {
                        dir('api-gateway') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Service Anggota') {
                    steps {
                        dir('service-anggota') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Service Buku') {
                    steps {
                        dir('service-buku') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Service Peminjaman') {
                    steps {
                        dir('service-peminjaman') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Service Pengembalian') {
                    steps {
                        dir('service-pengembalian') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                script {
                    sh """
                        docker build -t perpustakaan/eureka-server:${DOCKER_IMAGE_TAG} ./eureka-server
                        docker build -t perpustakaan/api-gateway:${DOCKER_IMAGE_TAG} ./api-gateway
                        docker build -t perpustakaan/service-anggota:${DOCKER_IMAGE_TAG} ./service-anggota
                        docker build -t perpustakaan/service-buku:${DOCKER_IMAGE_TAG} ./service-buku
                        docker build -t perpustakaan/service-peminjaman:${DOCKER_IMAGE_TAG} ./service-peminjaman
                        docker build -t perpustakaan/service-pengembalian:${DOCKER_IMAGE_TAG} ./service-pengembalian
                    """
                }
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                script {
                    sh """
                        echo ${DOCKER_HUB_CREDENTIALS_PSW} | docker login -u ${DOCKER_HUB_CREDENTIALS_USR} --password-stdin
                        docker push perpustakaan/eureka-server:${DOCKER_IMAGE_TAG}
                        docker push perpustakaan/api-gateway:${DOCKER_IMAGE_TAG}
                        docker push perpustakaan/service-anggota:${DOCKER_IMAGE_TAG}
                        docker push perpustakaan/service-buku:${DOCKER_IMAGE_TAG}
                        docker push perpustakaan/service-peminjaman:${DOCKER_IMAGE_TAG}
                        docker push perpustakaan/service-pengembalian:${DOCKER_IMAGE_TAG}
                    """
                }
            }
        }
        
        stage('Deploy') {
            steps {
                sh 'docker-compose down'
                sh 'docker-compose up -d'
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    sleep 60 // Wait for services to start
                    
                    def services = [
                        'eureka-server:8761',
                        'api-gateway:8080',
                        'service-anggota:8081',
                        'service-buku:8082',
                        'service-peminjaman:8083',
                        'service-pengembalian:8084'
                    ]
                    
                    services.each { service ->
                        sh "curl -f http://${service}/actuator/health || exit 1"
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline failed! Rolling back...'
            sh 'docker-compose down'
        }
        always {
            sh 'docker logout'
        }
    }
}
```

---

## Verifikasi Setup

### 1. Start Semua Services

```bash
# Build semua images
docker-compose build

# Start semua services
docker-compose up -d

# Monitor logs
docker-compose logs -f
```

### 2. Verifikasi Service Registration

```bash
# Check Eureka dashboard
curl http://localhost:8761

# Atau buka di browser
open http://localhost:8761
```

Pastikan semua services terdaftar:
- EUREKA-SERVER
- API-GATEWAY
- SERVICE-ANGGOTA
- SERVICE-BUKU
- SERVICE-PEMINJAMAN
- SERVICE-PENGEMBALIAN

### 3. Test API Endpoints

```bash
# Test via Gateway
curl http://localhost:8080/api/anggota

# Test direct to service
curl http://localhost:8081/api/anggota

# Test Create
curl -X POST http://localhost:8080/api/anggota \
  -H "Content-Type: application/json" \
  -d '{
    "nomorAnggota": "A001",
    "nama": "Test User",
    "alamat": "Test Address",
    "email": "test@example.com"
  }'
```

### 4. Verify RabbitMQ

```bash
# Open RabbitMQ Management
open http://localhost:15672

# Login: guest/guest
# Check Exchanges dan Queues
```

### 5. Verify Monitoring Stack

```bash
# Prometheus
open http://localhost:9090
# Check Targets → All should be UP

# Grafana
open http://localhost:3000
# Login: admin/admin

# Zipkin
open http://localhost:9411

# Kibana
open http://localhost:5601
```

### 6. Health Checks

```bash
# Script untuk check semua health endpoints
#!/bin/bash

services=(
  "eureka-server:8761"
  "api-gateway:8080"
  "service-anggota:8081"
  "service-buku:8082"
  "service-peminjaman:8083"
  "service-pengembalian:8084"
)

echo "Checking health of all services..."

for service in "${services[@]}"; do
  name=$(echo $service | cut -d: -f1)
  url="http://$service/actuator/health"
  
  status=$(curl -s -o /dev/null -w "%{http_code}" $url)
  
  if [ $status -eq 200 ]; then
    echo "✅ $name is UP"
  else
    echo "❌ $name is DOWN (HTTP $status)"
  fi
done
```

---

## Troubleshooting Setup

### Common Issues

**Issue: Port already in use**
```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>
```

**Issue: Docker daemon not running**
```bash
# Start Docker
sudo systemctl start docker

# Enable on boot
sudo systemctl enable docker
```

**Issue: Maven build fails**
```bash
# Clean Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install -U
```

**Issue: Services not registering to Eureka**
```bash
# Check network connectivity
docker network inspect perpustakaan-network

# Restart Eureka
docker-compose restart eureka-server

# Wait 30 seconds then restart other services
docker-compose restart service-anggota service-buku service-peminjaman service-pengembalian
```

---

## Next Steps

Setelah setup selesai:

1. ✅ Baca [Architecture Documentation](ARCHITECTURE.md)
2. ✅ Pelajari [API Documentation](API_REFERENCE.md)
3. ✅ Setup [Monitoring Dashboards](MONITORING.md)
4. ✅ Configure [CI/CD Pipeline](CICD.md)
5. ✅ Review [Production Checklist](PRODUCTION.md)

---

**Setup Complete! 🎉**

Sistem microservices perpustakaan Anda sekarang siap digunakan untuk development dan testing.
