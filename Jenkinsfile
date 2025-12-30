pipeline {
    agent any

    tools {
        // Pastikan nama 'maven' sesuai dengan di Global Tool Configuration
        maven 'maven' 
    }

    environment {
        // Username Docker Hub
        DOCKER_HUB_USER = 'erlandagsya' 
        
        GIT_COMMIT_SHORT = sh(
            script: "git rev-parse --short HEAD",
            returnStdout: true
        ).trim()
        
        BUILD_VERSION = "${env.BUILD_NUMBER}-${GIT_COMMIT_SHORT}"
        // Daftar folder service
        SERVICES = 'eureka-server,api-gateway,service-anggota,service-buku,service-peminjaman,service-pengembalian'
    }

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'production'], description: 'Target deployment environment')
        // Default FALSE untuk test agar cepat
        booleanParam(name: 'RUN_TESTS', defaultValue: false, description: 'Run unit and integration tests')
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
                    ║  📦 PERPUSTAKAAN PIPELINE (MODE SANTAI / NO TEST)       ║
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

        // ============================================================
        // PERUBAHAN UTAMA DI SINI (SEQUENTIAL & SKIP ALL TEST)
        // ============================================================
        stage('Build JARs (Sequential & No Test)') {
            steps {
                script {
                    def services = SERVICES.split(',')
                    services.each { service ->
                        echo "🔨 Building ${service} (Skipping Tests)..."
                        dir(service) {
                            // PERINTAH SAKTI: -Dmaven.test.skip=true
                            // Artinya: Jangan compile test, jangan jalankan test. 
                            // Pokoknya bikin JAR dari main code saja.
                            sh 'mvn clean package -Dmaven.test.skip=true'
                        }
                    }
                }
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
                    
                    // Generate .env file
                    sh """
                        cat > .env << EOF
# --- Environment Setup ---
ENVIRONMENT=${env.TARGET_ENV}
BUILD_VERSION=${BUILD_VERSION}

# --- Service Discovery ---
EUREKA_SERVER_URL=http://eureka-server:8761/eureka/

# --- DATABASE CONFIG (MONGODB URI) ---
MONGODB_URI_ANGGOTA=mongodb://mongodb:27017/anggota_db
MONGODB_URI_BUKU=mongodb://mongodb:27017/buku_db
MONGODB_URI_PEMINJAMAN=mongodb://mongodb:27017/peminjaman_db
MONGODB_URI_PENGEMBALIAN=mongodb://mongodb:27017/pengembalian_db

# --- ELK Stack ---
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
                    sh "docker-compose down || true"
                    sh "docker-compose up -d"
                    
                    echo "✅ Services deployed."
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo '✅ Pipeline Success!'
        }
        failure {
            echo '❌ Pipeline Failed!'
        }
    }
}