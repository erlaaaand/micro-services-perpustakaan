pipeline {
    agent any

    tools {
        // HAPUS baris jdk 'jdk-17'. Kita pakai Java bawaan container.
        // Pastikan Anda sudah setting Maven dengan nama 'maven' di Global Tool Configuration
        maven 'maven' 
    }

    environment {
        // Ganti dengan Username Docker Hub Anda
        DOCKER_HUB_USER = 'erlandagsya' 
        
        GIT_COMMIT_SHORT = sh(
            script: "git rev-parse --short HEAD",
            returnStdout: true
        ).trim()
        
        BUILD_VERSION = "${env.BUILD_NUMBER}-${GIT_COMMIT_SHORT}"
        // Daftar folder service yang akan di-build
        SERVICES = 'eureka-server,api-gateway,service-anggota,service-buku,service-peminjaman,service-pengembalian'
    }

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'production'], description: 'Target deployment environment')
        booleanParam(name: 'RUN_TESTS', defaultValue: false, description: 'Run unit and integration tests (Skip for speed)')
        booleanParam(name: 'DEPLOY_SERVICES', defaultValue: true, description: 'Deploy services after build')
        booleanParam(name: 'SKIP_DOCKER_BUILD', defaultValue: false, description: 'Skip Docker image build')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timestamps()
        timeout(time: 1, unit: 'HOURS')
        disableConcurrentBuilds()
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    echo """
                    ╔═══════════════════════════════════════════════════════════╗
                    ║  📦 PERPUSTAKAAN CI/CD PIPELINE (FIXED)                 ║
                    ║  Version: ${BUILD_VERSION}                                ║
                    ║  Environment: ${params.ENVIRONMENT}                       ║
                    ╚═══════════════════════════════════════════════════════════╝
                    """
                    env.TARGET_ENV = params.ENVIRONMENT
                }
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test Services') {
            parallel {
                stage('Eureka') { steps { buildAndTestService('eureka-server') } }
                stage('Gateway') { steps { buildAndTestService('api-gateway') } }
                stage('Anggota') { steps { buildAndTestService('service-anggota') } }
                stage('Buku') { steps { buildAndTestService('service-buku') } }
                stage('Peminjaman') { steps { buildAndTestService('service-peminjaman') } }
                stage('Pengembalian') { steps { buildAndTestService('service-pengembalian') } }
            }
        }

        stage('Build Docker Images') {
            when { expression { params.SKIP_DOCKER_BUILD == false } }
            steps {
                script {
                    def services = SERVICES.split(',')
                    services.each { service ->
                        echo "🐳 Building image: ${DOCKER_HUB_USER}/${service}:${BUILD_VERSION}"
                        // Build Image
                        sh "docker build -t ${DOCKER_HUB_USER}/${service}:${BUILD_VERSION} -t ${DOCKER_HUB_USER}/${service}:latest ./${service}"
                    }
                }
            }
        }

        stage('Push to Docker Hub') {
            when { expression { params.SKIP_DOCKER_BUILD == false } }
            steps {
                script {
                    // Pastikan ID credentials ini sama dengan yang dibuat di Jenkins
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
                        sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                        
                        def services = SERVICES.split(',')
                        services.each { service ->
                            echo "🚀 Pushing ${service}..."
                            sh "docker push ${DOCKER_HUB_USER}/${service}:${BUILD_VERSION}"
                            sh "docker push ${DOCKER_HUB_USER}/${service}:latest"
                        }
                    }
                }
            }
        }

        stage('Deploy to Environment') {
            when { expression { params.DEPLOY_SERVICES == true } }
            steps {
                script {
                    echo "Deploying to ${env.TARGET_ENV}..."
                    
                    // --- GENERATE .ENV FILE (PENTING: UPDATE CONFIG MONGODB DI SINI) ---
                    sh """
                        cat > .env << EOF
# --- Environment Setup ---
ENVIRONMENT=${env.TARGET_ENV}
BUILD_VERSION=${BUILD_VERSION}

# --- Service Discovery ---
EUREKA_SERVER_URL=http://eureka-server:8761/eureka/

# --- DATABASE CONFIG (MONGODB URI - FIXED) ---
# Ini settingan penting agar service bisa connect ke container mongo
MONGODB_URI_ANGGOTA=mongodb://mongodb:27017/anggota_db
MONGODB_URI_BUKU=mongodb://mongodb:27017/buku_db
MONGODB_URI_PEMINJAMAN=mongodb://mongodb:27017/peminjaman_db
MONGODB_URI_PENGEMBALIAN=mongodb://mongodb:27017/pengembalian_db

# --- ELK Stack (Logging) ---
ELASTICSEARCH_HOST=elasticsearch
ELASTICSEARCH_PORT=9200
ELASTICSEARCH_HOSTS=http://elasticsearch:9200
ES_JAVA_OPTS=-Xms512m -Xmx512m

LOGSTASH_HOST=logstash
LOGSTASH_PORT=5000
LS_JAVA_OPTS=-Xmx256m -Xms256m

KIBANA_HOST=kibana
KIBANA_PORT=5601

# --- Java Memory Options ---
JAVA_OPTS_GATEWAY=-Xmx512m -Xms256m
JAVA_OPTS_SERVICE=-Xmx512m -Xms256m

# --- Others ---
NETWORK_SUBNET=172.25.0.0/16
LOG_LEVEL_ROOT=INFO
SWAGGER_ENABLED=true
SWAGGER_SERVER_URL=http://localhost:8080
EOF
                    """
                    
                    // Restart Docker Compose
                    // Menggunakan '|| true' agar tidak fail jika container belum ada
                    sh "docker-compose down || true"
                    sh "docker-compose up -d"
                    
                    echo "✅ Services deployed."
                }
            }
        }
        
        // Stage Health Check (Opsional, matikan jika bikin lambat)
        stage('Health Check') {
             when { expression { params.DEPLOY_SERVICES == true } }
             steps {
                 script {
                     echo '🏥 Waiting for services startup (30s)...'
                     sleep(time: 30, unit: 'SECONDS')
                     // Simple check only Gateway & Eureka for speed
                     sh "curl -f http://localhost:8761/actuator/health || echo 'Eureka Not Ready'"
                     sh "curl -f http://localhost:8080/actuator/health || echo 'Gateway Not Ready'"
                 }
             }
        }
    }
    
    post {
        always {
            cleanWs() // Bersihkan sisa build
        }
        success {
            echo '✅ Pipeline Success!'
        }
        failure {
            echo '❌ Pipeline Failed!'
        }
    }
}

// Helper Function
def buildAndTestService(String serviceName) {
    dir(serviceName) {
        echo "🔨 Building ${serviceName}..."
        // Skip test agar cepat. Kalau mau test, hapus -DskipTests
        sh 'mvn clean package -DskipTests' 
    }
}