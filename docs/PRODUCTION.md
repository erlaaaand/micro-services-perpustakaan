# 🚀 Production Deployment Checklist

Panduan lengkap untuk deployment ke production environment.

## 📑 Daftar Isi

- [Pre-Deployment Checklist](#pre-deployment-checklist)
- [Infrastructure Setup](#infrastructure-setup)
- [Database Migration](#database-migration)
- [Deployment Strategy](#deployment-strategy)
- [Post-Deployment Verification](#post-deployment-verification)
- [Rollback Procedure](#rollback-procedure)
- [Maintenance](#maintenance)

---

## Pre-Deployment Checklist

### Code Quality

- [ ] All unit tests passing (`mvn test`)
- [ ] All integration tests passing (`mvn verify`)
- [ ] Code coverage > 75% ([JaCoCo report](target/site/jacoco/index.html))
- [ ] No critical SonarQube issues
- [ ] No high/critical security vulnerabilities (OWASP Dependency Check)
- [ ] Code reviewed and approved
- [ ] Documentation updated

### Configuration

- [ ] Environment variables configured
- [ ] Secrets stored securely (Vault/AWS Secrets Manager)
- [ ] Database credentials rotated
- [ ] API keys validated
- [ ] External service endpoints verified
- [ ] Logging levels set (INFO/WARN for production)
- [ ] Debug mode disabled

### Performance

- [ ] Load testing completed (target: 1000 req/s)
- [ ] Response time (p95) < 200ms
- [ ] Memory leaks checked (heap dump analysis)
- [ ] Database indexes optimized
- [ ] Connection pools tuned
- [ ] Caching strategy implemented

### Security

- [ ] HTTPS/TLS configured
- [ ] Security headers enabled
- [ ] Authentication/Authorization tested
- [ ] Rate limiting configured
- [ ] CORS properly configured
- [ ] Firewall rules applied
- [ ] Container images scanned (Trivy/Docker Scout)
- [ ] Penetration testing completed

### Monitoring

- [ ] Prometheus metrics exposed
- [ ] Grafana dashboards created
- [ ] Zipkin tracing configured
- [ ] ELK stack ready for logs
- [ ] Alerts configured (PagerDuty/Slack)
- [ ] Health check endpoints working
- [ ] Uptime monitoring setup (UptimeRobot/Pingdom)

---

## Infrastructure Setup

### Server Requirements

**Minimum Specifications**:

| Component | Development | Production |
|-----------|-------------|------------|
| **CPU** | 4 cores | 8+ cores |
| **RAM** | 8 GB | 16+ GB |
| **Storage** | 50 GB | 200+ GB SSD |
| **Network** | 100 Mbps | 1 Gbps |

### Cloud Infrastructure

**AWS Setup**:

```bash
# VPC Configuration
aws ec2 create-vpc --cidr-block 10.0.0.0/16

# Create subnets
aws ec2 create-subnet --vpc-id <vpc-id> --cidr-block 10.0.1.0/24 --availability-zone us-east-1a
aws ec2 create-subnet --vpc-id <vpc-id> --cidr-block 10.0.2.0/24 --availability-zone us-east-1b

# Create security groups
aws ec2 create-security-group \
  --group-name api-gateway-sg \
  --description "API Gateway Security Group" \
  --vpc-id <vpc-id>

# Allow HTTPS
aws ec2 authorize-security-group-ingress \
  --group-id <sg-id> \
  --protocol tcp \
  --port 443 \
  --cidr 0.0.0.0/0

# Create EC2 instances
aws ec2 run-instances \
  --image-id ami-0c55b159cbfafe1f0 \
  --count 2 \
  --instance-type t3.large \
  --key-name production-key \
  --security-group-ids <sg-id> \
  --subnet-id <subnet-id>
```

**Docker Swarm Setup** (Alternative):

```bash
# Initialize swarm on manager node
docker swarm init --advertise-addr <manager-ip>

# Join worker nodes
docker swarm join --token <token> <manager-ip>:2377

# Deploy stack
docker stack deploy -c docker-compose.prod.yml perpustakaan
```

### Load Balancer

**Nginx Configuration**:

```nginx
upstream api_servers {
    least_conn;
    server api-gateway-1:8080 max_fails=3 fail_timeout=30s;
    server api-gateway-2:8080 max_fails=3 fail_timeout=30s;
    server api-gateway-3:8080 max_fails=3 fail_timeout=30s;
}

server {
    listen 443 ssl http2;
    server_name api.perpustakaan.com;
    
    ssl_certificate /etc/letsencrypt/live/api.perpustakaan.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.perpustakaan.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    location / {
        proxy_pass http://api_servers;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Connection pooling
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        
        # Timeouts
        proxy_connect_timeout 5s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Health check
        health_check interval=10s fails=3 passes=2;
    }
}
```

---

## Database Migration

### MongoDB Production Setup

**Replica Set Configuration**:

```bash
# Initialize replica set
docker exec -it mongodb mongosh --eval "
  rs.initiate({
    _id: 'rs0',
    members: [
      { _id: 0, host: 'mongodb-1:27017' },
      { _id: 1, host: 'mongodb-2:27017' },
      { _id: 2, host: 'mongodb-3:27017' }
    ]
  })
"

# Verify status
docker exec -it mongodb mongosh --eval "rs.status()"
```

**Connection String**:
```properties
spring.data.mongodb.uri=mongodb://mongodb-1:27017,mongodb-2:27017,mongodb-3:27017/perpustakaan_read_db?replicaSet=rs0&authSource=admin
```

**Backup Strategy**:

```bash
#!/bin/bash
# scripts/backup-mongodb-prod.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/mongodb/$DATE"

# Backup with mongodump
mongodump \
  --uri="mongodb://user:pass@mongodb-1:27017,mongodb-2:27017,mongodb-3:27017/perpustakaan_read_db?replicaSet=rs0" \
  --out="$BACKUP_DIR" \
  --gzip

# Upload to S3
aws s3 sync "$BACKUP_DIR" s3://perpustakaan-backups/mongodb/$DATE/

# Retain last 30 days
find /backups/mongodb -mtime +30 -exec rm -rf {} \;
```

### H2 to PostgreSQL Migration

**Production uses PostgreSQL instead of H2**:

```properties
# application-prod.properties
spring.datasource.url=jdbc:postgresql://postgres:5432/perpustakaan_write_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate

# Connection pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
```

**Flyway Migration**:

```sql
-- V1__initial_schema.sql
CREATE TABLE anggota_command (
    id BIGSERIAL PRIMARY KEY,
    nomor_anggota VARCHAR(50) UNIQUE NOT NULL,
    nama VARCHAR(100) NOT NULL,
    alamat VARCHAR(255),
    email VARCHAR(100) UNIQUE,
    telepon VARCHAR(15),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_anggota_nama ON anggota_command(nama);
CREATE INDEX idx_anggota_email ON anggota_command(email);
```

```properties
# Enable Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

---

## Deployment Strategy

### Blue-Green Deployment

**Setup**:

```yaml
# docker-compose.blue.yml
services:
  api-gateway-blue:
    image: perpustakaan/api-gateway:${VERSION}
    environment:
      - SPRING_PROFILES_ACTIVE=prod,blue
    deploy:
      replicas: 3

# docker-compose.green.yml
services:
  api-gateway-green:
    image: perpustakaan/api-gateway:${VERSION}
    environment:
      - SPRING_PROFILES_ACTIVE=prod,green
    deploy:
      replicas: 3
```

**Deployment Process**:

```bash
#!/bin/bash
# scripts/deploy-blue-green.sh

VERSION=$1
CURRENT_ENV=$2  # blue or green

if [ "$CURRENT_ENV" = "blue" ]; then
    TARGET_ENV="green"
else
    TARGET_ENV="blue"
fi

echo "Deploying version $VERSION to $TARGET_ENV environment"

# Deploy to target environment
docker-compose -f docker-compose.$TARGET_ENV.yml up -d

# Wait for health checks
echo "Waiting for services to be healthy..."
sleep 60

# Verify health
./scripts/health-check.sh $TARGET_ENV

if [ $? -eq 0 ]; then
    echo "Health check passed. Switching traffic to $TARGET_ENV"
    
    # Update load balancer to point to new environment
    ./scripts/switch-traffic.sh $TARGET_ENV
    
    echo "Traffic switched successfully"
    
    # Wait for connections to drain
    sleep 30
    
    # Stop old environment
    docker-compose -f docker-compose.$CURRENT_ENV.yml down
    
    echo "Deployment completed successfully"
else
    echo "Health check failed. Rolling back..."
    docker-compose -f docker-compose.$TARGET_ENV.yml down
    exit 1
fi
```

### Rolling Update

**Docker Swarm**:

```bash
# Update service with rolling update
docker service update \
  --image perpustakaan/service-anggota:${VERSION} \
  --update-parallelism 1 \
  --update-delay 30s \
  perpustakaan_service-anggota
```

**Kubernetes**:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: service-anggota
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    spec:
      containers:
      - name: service-anggota
        image: perpustakaan/service-anggota:${VERSION}
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 30
```

### Canary Deployment

**Nginx Configuration**:

```nginx
upstream api_stable {
    server api-gateway-stable:8080;
}

upstream api_canary {
    server api-gateway-canary:8080;
}

split_clients "${remote_addr}" $backend {
    10%     api_canary;  # 10% to canary
    *       api_stable;   # 90% to stable
}

server {
    location / {
        proxy_pass http://$backend;
    }
}
```

---

## Post-Deployment Verification

### Smoke Tests

```bash
#!/bin/bash
# scripts/smoke-test.sh

BASE_URL="https://api.perpustakaan.com"

echo "Running smoke tests..."

# Test health endpoint
echo "Checking health..."
curl -f $BASE_URL/actuator/health || exit 1

# Test Eureka
echo "Checking Eureka..."
curl -f $BASE_URL:8761/eureka/apps || exit 1

# Test API endpoints
echo "Testing API endpoints..."
curl -f $BASE_URL/api/anggota || exit 1
curl -f $BASE_URL/api/buku || exit 1

echo "All smoke tests passed!"
```

### Monitoring Validation

```bash
# Check Prometheus targets
curl -s http://prometheus:9090/api/v1/targets | jq '.data.activeTargets[] | select(.health != "up")'

# Check Grafana dashboards
curl -s -u admin:admin http://grafana:3000/api/dashboards/home | jq

# Verify logs in Elasticsearch
curl -s http://elasticsearch:9200/_cat/indices | grep app-logs
```

### Load Testing

```bash
# Run load test with k6
k6 run --vus 100 --duration 5m load-test.js

# Expected results:
# - p95 response time < 200ms
# - Error rate < 0.1%
# - Throughput > 1000 req/s
```

---

## Rollback Procedure

### Quick Rollback

```bash
#!/bin/bash
# scripts/rollback.sh

PREVIOUS_VERSION=$1

echo "Rolling back to version: $PREVIOUS_VERSION"

# Pull previous images
docker pull perpustakaan/api-gateway:$PREVIOUS_VERSION
docker pull perpustakaan/service-anggota:$PREVIOUS_VERSION
docker pull perpustakaan/service-buku:$PREVIOUS_VERSION
docker pull perpustakaan/service-peminjaman:$PREVIOUS_VERSION
docker pull perpustakaan/service-pengembalian:$PREVIOUS_VERSION

# Update services
export VERSION=$PREVIOUS_VERSION
docker-compose -f docker-compose.prod.yml up -d

# Verify health
./scripts/health-check.sh

if [ $? -eq 0 ]; then
    echo "Rollback completed successfully"
else
    echo "Rollback failed! Manual intervention required"
    exit 1
fi
```

### Database Rollback

```bash
# Restore MongoDB from backup
mongorestore \
  --uri="mongodb://user:pass@mongodb-1:27017/perpustakaan_read_db" \
  --gzip \
  --drop \
  /backups/mongodb/20240115_120000/
```

---

## Maintenance

### Scheduled Maintenance Window

**Notification Template**:

```
Subject: Scheduled Maintenance - Library System

Dear Users,

We will be performing scheduled maintenance on the Library System:

Date: January 20, 2024
Time: 02:00 - 04:00 AM (UTC+7)
Duration: Approximately 2 hours

During this time, the system will be unavailable.

Activities:
- Database optimization
- Security updates
- Performance improvements

We apologize for any inconvenience.

Best regards,
IT Team
```

### Maintenance Tasks

**Monthly**:
```bash
#!/bin/bash
# scripts/monthly-maintenance.sh

# Optimize MongoDB indexes
docker exec mongodb mongosh --eval "
  use perpustakaan_read_db;
  db.anggota_read.reIndex();
  db.buku_read.reIndex();
  db.peminjaman_read.reIndex();
"

# Clean old logs
find /var/log/perpustakaan -name "*.log" -mtime +30 -delete

# Update Docker images
docker-compose pull
docker-compose up -d

# Clean unused Docker resources
docker system prune -a -f --volumes
```

**Weekly**:
```bash
# Database backup
./scripts/backup-mongodb-prod.sh

# Check disk space
df -h

# Review security alerts
cat /var/log/auth.log | grep "Failed password"

# Check service health
./scripts/health-check.sh
```

### Scaling

**Horizontal Scaling**:
```bash
# Scale up service instances
docker-compose -f docker-compose.prod.yml up -d --scale service-anggota=5

# Or with Docker Swarm
docker service scale perpustakaan_service-anggota=5
```

**Vertical Scaling**:
```yaml
# Increase resource limits
services:
  service-anggota:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 4G
        reservations:
          cpus: '1.0'
          memory: 2G
```

---

## Production Configuration

### Docker Compose Production

**docker-compose.prod.yml**:

```yaml
version: '3.8'

services:
  api-gateway:
    image: perpustakaan/api-gateway:${VERSION}
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - JAVA_OPTS=-Xmx2g -Xms1g -XX:+UseG1GC
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 2.5G
          cpus: '1.5'
        reservations:
          memory: 1G
          cpus: '0.5'
      restart_policy:
        condition: on-failure
        max_attempts: 3
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  service-anggota:
    image: perpustakaan/service-anggota:${VERSION}
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - JAVA_OPTS=-Xmx2g -Xms1g -XX:+UseG1GC
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 2.5G
        reservations:
          memory: 1G
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### Environment Variables

**Production .env**:

```bash
# Application Version
VERSION=1.0.0

# Database
DB_USERNAME=${VAULT_DB_USER}
DB_PASSWORD=${VAULT_DB_PASS}
MONGODB_URI=mongodb://user:pass@mongodb-1:27017,mongodb-2:27017/perpustakaan?replicaSet=rs0

# RabbitMQ
RABBITMQ_HOST=rabbitmq-cluster
RABBITMQ_USERNAME=${VAULT_RABBITMQ_USER}
RABBITMQ_PASSWORD=${VAULT_RABBITMQ_PASS}

# Security
JWT_SECRET=${VAULT_JWT_SECRET}
ENCRYPTION_KEY=${VAULT_ENCRYPTION_KEY}

# Monitoring
PROMETHEUS_ENDPOINT=http://prometheus:9090
GRAFANA_ENDPOINT=http://grafana:3000
ZIPKIN_ENDPOINT=http://zipkin:9411/api/v2/spans

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=${VAULT_SMTP_USER}
SMTP_PASSWORD=${VAULT_SMTP_PASS}
```

---

## Disaster Recovery

### Backup Strategy

**Daily**:
- MongoDB full backup
- Configuration files backup
- Docker volumes backup

**Weekly**:
- System state snapshot
- Full infrastructure backup

**Monthly**:
- Archive old backups
- Test restore procedure

### Recovery Procedure

```bash
#!/bin/bash
# scripts/disaster-recovery.sh

echo "Starting disaster recovery..."

# 1. Restore infrastructure
terraform apply -var-file=production.tfvars

# 2. Restore databases
./scripts/restore-mongodb.sh /backups/mongodb/latest

# 3. Deploy services
export VERSION=stable
docker-compose -f docker-compose.prod.yml up -d

# 4. Verify
./scripts/health-check.sh

echo "Disaster recovery completed"
```

---

## Production Checklist Summary

### Before Deployment

- [ ] All tests passing
- [ ] Security scan completed
- [ ] Performance testing done
- [ ] Documentation updated
- [ ] Backup verified
- [ ] Rollback plan ready

### During Deployment

- [ ] Maintenance notification sent
- [ ] Blue-green switch executed
- [ ] Health checks passing
- [ ] Smoke tests completed
- [ ] Monitoring active

### After Deployment

- [ ] Verify all services UP
- [ ] Check error logs
- [ ] Monitor performance metrics
- [ ] Validate user functionality
- [ ] Update deployment log
- [ ] Close maintenance window

---

[⬅️ Back to Documentation Index](README.md)