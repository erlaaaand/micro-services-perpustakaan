# 🚀 CI/CD Pipeline Guide

Panduan lengkap setup dan konfigurasi Jenkins CI/CD pipeline untuk sistem microservices perpustakaan.

## 📑 Daftar Isi

- [Overview](#overview)
- [Jenkins Setup](#jenkins-setup)
- [Pipeline Configuration](#pipeline-configuration)
- [Docker Integration](#docker-integration)
- [Deployment Stages](#deployment-stages)
- [Troubleshooting](#troubleshooting)

---

## Overview

### CI/CD Architecture

```
┌────────────────────────────────────────────────┐
│           Developer Workflow                   │
│                                                │
│  Code → Commit → Push → GitHub                 │
└─────────────────┬──────────────────────────────┘
                  │
                  │ Webhook/Poll
                  ▼
┌────────────────────────────────────────────────┐
│             Jenkins Pipeline                   │
│                                                │
│  ┌─────────────────────────────────────────┐  │
│  │  1. Checkout Code                       │  │
│  └─────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────┐  │
│  │  2. Build JARs (Maven)                  │  │
│  └─────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────┐  │
│  │  3. Build Docker Images                 │  │
│  └─────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────┐  │
│  │  4. Push to Docker Hub                  │  │
│  └─────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────┐  │
│  │  5. Deploy to Environment               │  │
│  └─────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────┐  │
│  │  6. Health Checks                       │  │
│  └─────────────────────────────────────────┘  │
└────────────────────────────────────────────────┘
                  │
                  │ Success/Failure
                  ▼
┌────────────────────────────────────────────────┐
│          Running Application                   │
└────────────────────────────────────────────────┘
```

### Pipeline Stages

| Stage | Description | Duration |
|-------|-------------|----------|
| Initialize | Setup environment & variables | ~5s |
| Checkout | Clone repository | ~10s |
| Build JARs | Maven compile & package | ~2-3min |
| Build Images | Docker image build | ~1-2min |
| Push Images | Upload to Docker Hub | ~30s-1min |
| Deploy | Start containers | ~30s |
| Health Check | Verify services | ~1min |

**Total Pipeline Time**: ~5-10 minutes

---

## Jenkins Setup

### 1. Start Jenkins Container

Jenkins sudah included di `docker-compose.yml`:

```yaml
jenkins:
  build:
    context: .
    dockerfile: Dockerfile-jenkins
  container_name: jenkins
  ports:
    - "9000:8080"
    - "50000:50000"
  volumes:
    - jenkins_data:/var/jenkins_home
    - /var/run/docker.sock:/var/run/docker.sock
  networks:
    - perpustakaan-network
```

Start Jenkins:
```bash
docker-compose up -d jenkins
```

### 2. Initial Jenkins Configuration

**Get Initial Admin Password**:
```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

**Access Jenkins**:
1. Open http://localhost:9000
2. Enter initial admin password
3. Click **Install suggested plugins**
4. Create admin user
5. Configure Jenkins URL: `http://localhost:9000`

### 3. Install Required Plugins

Go to **Manage Jenkins** → **Manage Plugins** → **Available**

Search dan install:
- ✅ **Docker Pipeline**
- ✅ **Maven Integration**
- ✅ **Git Plugin**
- ✅ **Credentials Binding Plugin**
- ✅ **Pipeline Plugin** (biasanya sudah installed)

Click **Install without restart**

### 4. Configure Maven

**Manage Jenkins** → **Global Tool Configuration**

**Maven**:
- Name: `Maven-3.9`
- Install automatically: ✅
- Version: Select latest 3.9.x

Click **Save**

### 5. Configure Docker Credentials

**Manage Jenkins** → **Manage Credentials** → **System** → **Global credentials**

Click **Add Credentials**:
- Kind: `Username with password`
- Scope: `Global`
- Username: `your-dockerhub-username`
- Password: `your-dockerhub-password`
- ID: `docker-hub-credentials`
- Description: `Docker Hub Credentials`

Click **Create**

---

## Pipeline Configuration

### Jenkinsfile

Located di root project: `Jenkinsfile`

```groovy
pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
        DOCKER_REGISTRY = 'perpustakaan' // Your Docker Hub username/org
    }
    
    stages {
        stage('Initialize') {
            steps {
                echo '=== Initializing Pipeline ==='
                echo "Build Number: ${env.BUILD_NUMBER}"
                echo "Git Commit: ${env.GIT_COMMIT}"
                echo "Docker Image Tag: ${DOCKER_IMAGE_TAG}"
            }
        }
        
        stage('Checkout') {
            steps {
                echo '=== Checking out source code ==='
                checkout scm
            }
        }
        
        stage('Build JARs') {
            parallel {
                stage('Build Eureka Server') {
                    steps {
                        echo '=== Building Eureka Server ==='
                        dir('eureka-server') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                
                stage('Build API Gateway') {
                    steps {
                        echo '=== Building API Gateway ==='
                        dir('api-gateway') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                
                stage('Build Service Anggota') {
                    steps {
                        echo '=== Building Service Anggota ==='
                        dir('service-anggota') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                
                stage('Build Service Buku') {
                    steps {
                        echo '=== Building Service Buku ==='
                        dir('service-buku') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                
                stage('Build Service Peminjaman') {
                    steps {
                        echo '=== Building Service Peminjaman ==='
                        dir('service-peminjaman') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                
                stage('Build Service Pengembalian') {
                    steps {
                        echo '=== Building Service Pengembalian ==='
                        dir('service-pengembalian') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                echo '=== Building Docker Images ==='
                script {
                    sh """
                        docker build -t ${DOCKER_REGISTRY}/eureka-server:${DOCKER_IMAGE_TAG} ./eureka-server
                        docker build -t ${DOCKER_REGISTRY}/api-gateway:${DOCKER_IMAGE_TAG} ./api-gateway
                        docker build -t ${DOCKER_REGISTRY}/service-anggota:${DOCKER_IMAGE_TAG} ./service-anggota
                        docker build -t ${DOCKER_REGISTRY}/service-buku:${DOCKER_IMAGE_TAG} ./service-buku
                        docker build -t ${DOCKER_REGISTRY}/service-peminjaman:${DOCKER_IMAGE_TAG} ./service-peminjaman
                        docker build -t ${DOCKER_REGISTRY}/service-pengembalian:${DOCKER_IMAGE_TAG} ./service-pengembalian
                        
                        docker tag ${DOCKER_REGISTRY}/eureka-server:${DOCKER_IMAGE_TAG} ${DOCKER_REGISTRY}/eureka-server:latest
                        docker tag ${DOCKER_REGISTRY}/api-gateway:${DOCKER_IMAGE_TAG} ${DOCKER_REGISTRY}/api-gateway:latest
                        docker tag ${DOCKER_REGISTRY}/service-anggota:${DOCKER_IMAGE_TAG} ${DOCKER_REGISTRY}/service-anggota:latest
                        docker tag ${DOCKER_REGISTRY}/service-buku:${DOCKER_IMAGE_TAG} ${DOCKER_REGISTRY}/service-buku:latest
                        docker tag ${DOCKER_REGISTRY}/service-peminjaman:${DOCKER_IMAGE_TAG} ${DOCKER_REGISTRY}/service-peminjaman:latest
                        docker tag ${DOCKER_REGISTRY}/service-pengembalian:${DOCKER_IMAGE_TAG} ${DOCKER_REGISTRY}/service-pengembalian:latest
                    """
                }
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                echo '=== Pushing Docker Images to Registry ==='
                script {
                    sh """
                        echo ${DOCKER_HUB_CREDENTIALS_PSW} | docker login -u ${DOCKER_HUB_CREDENTIALS_USR} --password-stdin
                        
                        docker push ${DOCKER_REGISTRY}/eureka-server:${DOCKER_IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/eureka-server:latest
                        
                        docker push ${DOCKER_REGISTRY}/api-gateway:${DOCKER_IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/api-gateway:latest
                        
                        docker push ${DOCKER_REGISTRY}/service-anggota:${DOCKER_IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/service-anggota:latest
                        
                        docker push ${DOCKER_REGISTRY}/service-buku:${DOCKER_IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/service-buku:latest
                        
                        docker push ${DOCKER_REGISTRY}/service-peminjaman:${DOCKER_IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/service-peminjaman:latest
                        
                        docker push ${DOCKER_REGISTRY}/service-pengembalian:${DOCKER_IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/service-pengembalian:latest
                    """
                }
            }
        }
        
        stage('Deploy') {
            steps {
                echo '=== Deploying Application ==='
                script {
                    sh '''
                        docker-compose down || true
                        docker-compose up -d
                    '''
                }
            }
        }
        
        stage('Health Check') {
            steps {
                echo '=== Performing Health Checks ==='
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
                    
                    def allHealthy = true
                    
                    services.each { service ->
                        def name = service.split(':')[0]
                        def url = "http://${service}/actuator/health"
                        
                        def response = sh(
                            script: "curl -s -o /dev/null -w '%{http_code}' ${url}",
                            returnStdout: true
                        ).trim()
                        
                        if (response == '200') {
                            echo "✅ ${name}: UP"
                        } else {
                            echo "❌ ${name}: DOWN (HTTP ${response})"
                            allHealthy = false
                        }
                    }
                    
                    if (!allHealthy) {
                        error "Some services failed health check"
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo '✅ Pipeline executed successfully!'
            echo "Deployed version: ${DOCKER_IMAGE_TAG}"
        }
        
        failure {
            echo '❌ Pipeline failed!'
            echo 'Rolling back deployment...'
            sh 'docker-compose down || true'
        }
        
        always {
            sh 'docker logout || true'
            echo 'Cleaning up...'
        }
    }
}
```

### Creating Jenkins Pipeline Job

1. **New Item** → Enter name: `Perpustakaan-Microservices-Pipeline`
2. Select **Pipeline**
3. Click **OK**

**Configure Pipeline**:

**General**:
- Description: `CI/CD Pipeline untuk Sistem Microservices Perpustakaan`

**Build Triggers**:
- ✅ Poll SCM: `H/5 * * * *` (every 5 minutes)
- OR ✅ GitHub hook trigger for GITScm polling

**Pipeline**:
- Definition: `Pipeline script from SCM`
- SCM: `Git`
- Repository URL: `https://github.com/erlaaaand/micro-services-perpustakaan.git`
- Credentials: (add if private repo)
- Branch: `*/main` or `*/master`
- Script Path: `Jenkinsfile`

Click **Save**

---

## Docker Integration

### Dockerfile Template

Each service has similar Dockerfile:

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Multi-Stage Build (Optional Optimization)

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Benefits**:
- Smaller final image
- No Maven dalam production image
- Better layer caching

---

## Deployment Stages

### Stage 1: Initialize

**Purpose**: Setup environment variables

```groovy
environment {
    DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')
    DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
}
```

**Variables**:
- `BUILD_NUMBER`: Jenkins build number (1, 2, 3, ...)
- `GIT_COMMIT`: Git commit SHA
- `DOCKER_IMAGE_TAG`: Combination untuk unique tag

### Stage 2: Checkout

**Purpose**: Clone repository

```groovy
stage('Checkout') {
    steps {
        checkout scm
    }
}
```

### Stage 3: Build JARs

**Purpose**: Compile dan package Spring Boot applications

```groovy
dir('service-anggota') {
    sh 'mvn clean package -DskipTests'
}
```

**Options**:
- `-DskipTests`: Skip unit tests (faster builds)
- `-U`: Force update dependencies
- `-T 4`: Use 4 threads (parallel build)

**Parallel Execution**: All services build simultaneously untuk speed

### Stage 4: Build Docker Images

**Purpose**: Create Docker images dari JARs

```groovy
docker build -t perpustakaan/service-anggota:${DOCKER_IMAGE_TAG} ./service-anggota
```

**Tagging Strategy**:
- Version tag: `service-anggota:1-abc1234`
- Latest tag: `service-anggota:latest`

### Stage 5: Push to Registry

**Purpose**: Upload images ke Docker Hub

```groovy
docker push perpustakaan/service-anggota:${DOCKER_IMAGE_TAG}
docker push perpustakaan/service-anggota:latest
```

**Private Registry** (Alternative):
```groovy
docker tag service-anggota:latest my-registry:5000/service-anggota:latest
docker push my-registry:5000/service-anggota:latest
```

### Stage 6: Deploy

**Purpose**: Start containers dengan docker-compose

```groovy
sh 'docker-compose down'
sh 'docker-compose up -d'
```

**Blue-Green Deployment** (Advanced):
```groovy
sh 'docker-compose -f docker-compose.blue.yml up -d'
// Verify health
sh 'docker-compose -f docker-compose.green.yml down'
```

### Stage 7: Health Check

**Purpose**: Verify deployment success

```groovy
def response = sh(
    script: "curl -s -o /dev/null -w '%{http_code}' http://service:8081/actuator/health",
    returnStdout: true
).trim()

if (response != '200') {
    error "Health check failed"
}
```

---

## Environment-Specific Deployments

### Multiple Environments

```groovy
parameters {
    choice(
        name: 'ENVIRONMENT',
        choices: ['dev', 'staging', 'production'],
        description: 'Target environment'
    )
}

stage('Deploy') {
    steps {
        script {
            def composeFile = "docker-compose.${params.ENVIRONMENT}.yml"
            sh "docker-compose -f ${composeFile} up -d"
        }
    }
}
```

### Environment Files

**docker-compose.dev.yml**:
```yaml
# Development - single instance, local volumes
services:
  service-anggota:
    image: perpustakaan/service-anggota:latest
    environment:
      - SPRING_PROFILES_ACTIVE=dev
```

**docker-compose.staging.yml**:
```yaml
# Staging - similar to production
services:
  service-anggota:
    image: perpustakaan/service-anggota:${VERSION}
    environment:
      - SPRING_PROFILES_ACTIVE=staging
    deploy:
      replicas: 2
```

**docker-compose.production.yml**:
```yaml
# Production - highly available, monitoring
services:
  service-anggota:
    image: perpustakaan/service-anggota:${VERSION}
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 1G
          cpus: '1.0'
```

---

## Automated Testing Integration

### Unit Tests

```groovy
stage('Run Unit Tests') {
    steps {
        dir('service-anggota') {
            sh 'mvn test'
        }
    }
    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
    }
}
```

### Integration Tests

```groovy
stage('Integration Tests') {
    steps {
        sh 'docker-compose -f docker-compose.test.yml up -d'
        sh 'mvn verify -Pintegration-tests'
    }
    post {
        always {
            sh 'docker-compose -f docker-compose.test.yml down'
        }
    }
}
```

### Code Quality (SonarQube)

```groovy
stage('Code Quality') {
    steps {
        withSonarQubeEnv('SonarQube') {
            sh 'mvn sonar:sonar'
        }
    }
}
```

---

## Troubleshooting

### Common Issues

#### 1. Maven Build Failures

**Error**: `Failed to execute goal`

**Solution**:
```bash
# Clean Maven cache
rm -rf ~/.m2/repository

# Rebuild dengan force update
mvn clean install -U
```

#### 2. Docker Permission Denied

**Error**: `permission denied while trying to connect to the Docker daemon socket`

**Solution**:
```bash
# Add Jenkins user to docker group
docker exec -u root jenkins usermod -aG docker jenkins

# Restart Jenkins
docker restart jenkins
```

#### 3. Out of Disk Space

**Error**: `no space left on device`

**Solution**:
```bash
# Clean unused Docker resources
docker system prune -a

# Remove old images
docker image prune -a

# Remove unused volumes
docker volume prune
```

#### 4. Port Already in Use

**Error**: `Bind for 0.0.0.0:8080 failed: port is already allocated`

**Solution**:
```bash
# Stop conflicting container
docker-compose down

# Or kill process using port
lsof -ti:8080 | xargs kill -9
```

#### 5. Service Health Check Fails

**Error**: `Health check failed for service-anggota`

**Solution**:
```bash
# Check service logs
docker logs service-anggota

# Check if service is running
docker ps | grep service-anggota

# Manually test health endpoint
curl http://localhost:8081/actuator/health
```

### Pipeline Debugging

**View Console Output**:
1. Click build number
2. Click **Console Output**
3. Search for errors

**Replay Pipeline**:
1. Click build number
2. Click **Replay**
3. Edit Jenkinsfile
4. Click **Run**

**Pipeline Syntax**:
- Use **Pipeline Syntax** generator untuk help
- Generate script snippets
- Test scripts before committing

---

## Best Practices

### 1. Version Control

```groovy
// Tag images dengan version
DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
```

### 2. Rollback Strategy

```groovy
post {
    failure {
        sh 'docker-compose down'
        // Deploy previous version
        sh 'docker-compose pull'
        sh 'docker-compose up -d'
    }
}
```

### 3. Notifications

**Email**:
```groovy
post {
    success {
        emailext (
            to: 'team@example.com',
            subject: "Build ${env.BUILD_NUMBER} - SUCCESS",
            body: "Deployment successful"
        )
    }
}
```

**Slack**:
```groovy
post {
    always {
        slackSend (
            channel: '#deployments',
            message: "Build ${env.BUILD_NUMBER}: ${currentBuild.result}"
        )
    }
}
```

### 4. Secrets Management

**Never commit secrets!**

Use Jenkins credentials:
```groovy
withCredentials([usernamePassword(
    credentialsId: 'db-credentials',
    usernameVariable: 'DB_USER',
    passwordVariable: 'DB_PASS'
)]) {
    sh 'deploy-with-credentials.sh'
}
```

### 5. Build Caching

**Layer caching**:
```dockerfile
# Copy dependencies first (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# Then copy source (changes frequently)
COPY src ./src
RUN mvn package
```

---

## Monitoring Pipeline

### Build Metrics

Track dalam Grafana:
- Build duration
- Success rate
- Failure rate
- Deployment frequency

### Pipeline Logs

All logs tersimpan di:
```
/var/jenkins_home/jobs/Perpustakaan-Microservices-Pipeline/builds/
```

### Audit Trail

View di Jenkins:
- Build history
- Changes per build
- User who triggered
- Build parameters

---

[⬅️ Back to Main Documentation](../README.md)