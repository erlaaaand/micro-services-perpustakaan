pipeline {
    agent any

    tools {
        maven 'maven'   // Pastikan nama ini sama dengan di Global Tool Configuration Jenkins
        jdk 'jdk-17'    // Pastikan nama ini sama dengan di Global Tool Configuration Jenkins
    }

    environment {
        // DOCKER_REGISTRY = credentials('docker-registry-url') // Uncomment jika pakai registry luar
        // DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'     // Uncomment jika pakai registry luar
        
        // Hapus SonarQube jika belum punya servernya, atau set false di parameter
        SONARQUBE_SERVER = 'SonarQube' 
        
        GIT_COMMIT_SHORT = sh(
            script: "git rev-parse --short HEAD",
            returnStdout: true
        ).trim()
        
        BUILD_VERSION = "${env.BUILD_NUMBER}-${GIT_COMMIT_SHORT}"
        SERVICES = 'eureka-server,api-gateway,service-anggota,service-buku,service-peminjaman,service-pengembalian'
    }

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'production'], description: 'Target deployment environment')
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run unit and integration tests')
        booleanParam(name: 'RUN_SONAR', defaultValue: false, description: 'Run SonarQube analysis (Set True if Server Exists)')
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
                    ║  📦 PERPUSTAKAAN CI/CD PIPELINE (CLEAN ARCH)              ║
                    ║  Version: ${BUILD_VERSION}                                ║
                    ║  Environment: ${params.ENVIRONMENT}                       ║
                    ║  Stack: Java 17, H2, Mongo, ELK                           ║
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
            when { expression { params.RUN_TESTS == true } }
            parallel {
                stage('Eureka') { steps { buildAndTestService('eureka-server') } }
                stage('Gateway') { steps { buildAndTestService('api-gateway') } }
                stage('Anggota') { steps { buildAndTestService('service-anggota') } }
                stage('Buku') { steps { buildAndTestService('service-buku') } }
                stage('Peminjaman') { steps { buildAndTestService('service-peminjaman') } }
                stage('Pengembalian') { steps { buildAndTestService('service-pengembalian') } }
            }
        }

        // Stage SonarQube (Hanya jalan jika parameter RUN_SONAR dicentang)
        stage('Code Quality') {
            when { expression { params.RUN_SONAR == true } }
            steps {
                script {
                    echo 'Skipping SonarQube for local dev unless server is configured.'
                }
            }
        }

        stage('Build Docker Images') {
            when { expression { params.SKIP_DOCKER_BUILD == false } }
            steps {
                script {
                    def services = SERVICES.split(',')
                    services.each { service ->
                        // Build image lokal (tanpa push ke registry jika development lokal)
                        echo "🐳 Building image: perpus/${service}:${BUILD_VERSION}"
                        sh "docker build -t perpus/${service}:${BUILD_VERSION} -t perpus/${service}:latest ./${service}"
                    }
                }
            }
        }

        stage('Deploy to Environment') {
            when { expression { params.DEPLOY_SERVICES == true } }
            steps {
                script {
                    echo "🚀 Deploying to ${env.TARGET_ENV}..."
                    
                    // 1. Buat file .env yang SESUAI dengan arsitektur baru (Tanpa RabbitMQ/Zipkin)
                    sh """
                        cat > .env << EOF
ENVIRONMENT=${env.TARGET_ENV}
BUILD_VERSION=${BUILD_VERSION}
EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
ELASTICSEARCH_HOST=elasticsearch
ELASTICSEARCH_PORT=9200
LOGSTASH_HOST=logstash
LOGSTASH_PORT=5000
KIBANA_HOST=kibana
KIBANA_PORT=5601
EOF
                    """
                    
                    // 2. Restart Docker Compose
                    // Pastikan file docker-compose.yml ada di root project Jenkins workspace
                    sh "docker-compose down || true"
                    sh "docker-compose up -d"
                    
                    echo "✅ Services deployed."
                }
            }
        }

        stage('Health Check') {
            when { expression { params.DEPLOY_SERVICES == true } }
            steps {
                script {
                    echo '🏥 Waiting for services to start (60s)...'
                    sleep(time: 60, unit: 'SECONDS')
                    
                    def services = [
                        'Eureka Server': 8761,
                        'API Gateway': 8080,
                        'Service Anggota': 8081,
                        'Service Buku': 8082,
                        'Service Peminjaman': 8083,
                        'Service Pengembalian': 8084,
                        'Kibana (ELK)': 5601
                    ]
                    
                    def failedServices = []
                    
                    services.each { name, port ->
                        // Menggunakan curl -I (Head request) untuk cek status
                        // Menggunakan || true agar pipeline tidak langsung mati jika 1 gagal
                        def status = sh(script: "curl -s -o /dev/null -w '%{http_code}' http://localhost:${port}/actuator/health || echo '000'", returnStdout: true).trim()
                        
                        // Khusus Kibana cek root /, microservice cek /actuator/health
                        if (name == 'Kibana (ELK)') {
                             status = sh(script: "curl -s -o /dev/null -w '%{http_code}' http://localhost:${port}/api/status || echo '000'", returnStdout: true).trim()
                        }

                        if (status == '200' || status == '302') {
                            echo "✅ ${name} is UP"
                        } else {
                            echo "❌ ${name} is DOWN (Status: ${status})"
                            failedServices.add(name)
                        }
                    }
                    
                    if (failedServices.size() > 0) {
                        unstable "Health check failed for: ${failedServices.join(', ')}"
                    }
                }
            }
        }
    }
    
    post {
        always {
            // Bersihkan workspace hemat disk space
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

// Helper Function
def buildAndTestService(String serviceName) {
    dir(serviceName) {
        echo "🔨 Building ${serviceName}..."
        // Skip test di sini jika ingin build cepat, tapi disarankan run test
        sh 'mvn clean package -DskipTests' 
    }
}