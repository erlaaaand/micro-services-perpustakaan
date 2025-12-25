pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'jdk-17'
    }

    environment {
        DOCKER_REGISTRY = credentials('docker-registry-url')
        DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
        SONARQUBE_SERVER = 'SonarQube'
        SONARQUBE_SCANNER = 'SonarQubeScanner'
        GIT_COMMIT_SHORT = sh(
            script: "git rev-parse --short HEAD",
            returnStdout: true
        ).trim()
        BUILD_VERSION = "${env.BUILD_NUMBER}-${GIT_COMMIT_SHORT}"
    }

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'production'], description: 'Target deployment environment')
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run unit and integration tests')
        booleanParam(name: 'RUN_SONAR', defaultValue: true, description: 'Run SonarQube analysis')
        booleanParam(name: 'DEPLOY_SERVICES', defaultValue: true, description: 'Deploy services after build')
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📦 Pulling source code...'
                checkout scm
                script {
                    env.GIT_BRANCH = sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
                    echo "Building branch: ${env.GIT_BRANCH}"
                }
            }
        }

        stage('Build & Test Services') {
            when {
                expression { params.RUN_TESTS == true }
            }
            parallel {
                stage('Eureka Server') {
                    steps {
                        dir('eureka-server') {
                            echo '🔨 Building Eureka Server...'
                            sh '''
                                mvn clean verify -DskipTests=false
                                mvn jacoco:report
                            '''
                            junit '**/target/surefire-reports/*.xml'
                            jacoco(
                                execPattern: '**/target/jacoco.exec',
                                classPattern: '**/target/classes',
                                sourcePattern: '**/src/main/java'
                            )
                        }
                    }
                }
                stage('API Gateway') {
                    steps {
                        dir('api-gateway') {
                            echo '🔨 Building API Gateway...'
                            sh '''
                                mvn clean verify -DskipTests=false
                                mvn jacoco:report
                            '''
                            junit '**/target/surefire-reports/*.xml'
                            jacoco(
                                execPattern: '**/target/jacoco.exec',
                                classPattern: '**/target/classes',
                                sourcePattern: '**/src/main/java'
                            )
                        }
                    }
                }
                stage('Service Anggota') {
                    steps {
                        dir('service-anggota') {
                            echo '🔨 Building Service Anggota...'
                            sh '''
                                mvn clean verify -DskipTests=false
                                mvn jacoco:report
                            '''
                            junit '**/target/surefire-reports/*.xml'
                            jacoco(
                                execPattern: '**/target/jacoco.exec',
                                classPattern: '**/target/classes',
                                sourcePattern: '**/src/main/java'
                            )
                        }
                    }
                }
                stage('Service Buku') {
                    steps {
                        dir('service-buku') {
                            echo '🔨 Building Service Buku...'
                            sh '''
                                mvn clean verify -DskipTests=false
                                mvn jacoco:report
                            '''
                            junit '**/target/surefire-reports/*.xml'
                            jacoco(
                                execPattern: '**/target/jacoco.exec',
                                classPattern: '**/target/classes',
                                sourcePattern: '**/src/main/java'
                            )
                        }
                    }
                }
                stage('Service Peminjaman') {
                    steps {
                        dir('service-peminjaman') {
                            echo '🔨 Building Service Peminjaman...'
                            sh '''
                                mvn clean verify -DskipTests=false
                                mvn jacoco:report
                            '''
                            junit '**/target/surefire-reports/*.xml'
                            jacoco(
                                execPattern: '**/target/jacoco.exec',
                                classPattern: '**/target/classes',
                                sourcePattern: '**/src/main/java'
                            )
                        }
                    }
                }
                stage('Service Pengembalian') {
                    steps {
                        dir('service-pengembalian') {
                            echo '🔨 Building Service Pengembalian...'
                            sh '''
                                mvn clean verify -DskipTests=false
                                mvn jacoco:report
                            '''
                            junit '**/target/surefire-reports/*.xml'
                            jacoco(
                                execPattern: '**/target/jacoco.exec',
                                classPattern: '**/target/classes',
                                sourcePattern: '**/src/main/java'
                            )
                        }
                    }
                }
            }
        }

        stage('Code Quality Analysis') {
            when {
                expression { params.RUN_SONAR == true }
            }
            steps {
                script {
                    echo '📊 Running SonarQube analysis...'
                    withSonarQubeEnv(SONARQUBE_SERVER) {
                        sh '''
                            mvn sonar:sonar \
                                -Dsonar.projectKey=perpustakaan-microservices \
                                -Dsonar.projectName="Perpustakaan Microservices" \
                                -Dsonar.projectVersion=${BUILD_VERSION} \
                                -Dsonar.sources=. \
                                -Dsonar.java.binaries=**/target/classes \
                                -Dsonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml
                        '''
                    }
                }
            }
        }

        stage('Quality Gate') {
            when {
                expression { params.RUN_SONAR == true }
            }
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Security Scan') {
            steps {
                echo '🔒 Running security scan...'
                sh '''
                    mvn org.owasp:dependency-check-maven:check \
                        -DfailBuildOnCVSS=7 \
                        -DsuppressionFiles=dependency-check-suppressions.xml
                '''
                dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
            }
        }

        stage('Build Docker Images') {
            parallel {
                stage('Eureka Server Image') {
                    steps {
                        dir('eureka-server') {
                            script {
                                echo '🐳 Building Eureka Server Docker image...'
                                def image = docker.build("perpus/eureka-server:${BUILD_VERSION}")
                                image.tag('latest')
                                image.tag(env.ENVIRONMENT)
                            }
                        }
                    }
                }
                stage('API Gateway Image') {
                    steps {
                        dir('api-gateway') {
                            script {
                                echo '🐳 Building API Gateway Docker image...'
                                def image = docker.build("perpus/api-gateway:${BUILD_VERSION}")
                                image.tag('latest')
                                image.tag(env.ENVIRONMENT)
                            }
                        }
                    }
                }
                stage('Service Anggota Image') {
                    steps {
                        dir('service-anggota') {
                            script {
                                echo '🐳 Building Service Anggota Docker image...'
                                def image = docker.build("perpus/service-anggota:${BUILD_VERSION}")
                                image.tag('latest')
                                image.tag(env.ENVIRONMENT)
                            }
                        }
                    }
                }
                stage('Service Buku Image') {
                    steps {
                        dir('service-buku') {
                            script {
                                echo '🐳 Building Service Buku Docker image...'
                                def image = docker.build("perpus/service-buku:${BUILD_VERSION}")
                                image.tag('latest')
                                image.tag(env.ENVIRONMENT)
                            }
                        }
                    }
                }
                stage('Service Peminjaman Image') {
                    steps {
                        dir('service-peminjaman') {
                            script {
                                echo '🐳 Building Service Peminjaman Docker image...'
                                def image = docker.build("perpus/service-peminjaman:${BUILD_VERSION}")
                                image.tag('latest')
                                image.tag(env.ENVIRONMENT)
                            }
                        }
                    }
                }
                stage('Service Pengembalian Image') {
                    steps {
                        dir('service-pengembalian') {
                            script {
                                echo '🐳 Building Service Pengembalian Docker image...'
                                def image = docker.build("perpus/service-pengembalian:${BUILD_VERSION}")
                                image.tag('latest')
                                image.tag(env.ENVIRONMENT)
                            }
                        }
                    }
                }
            }
        }

        stage('Push to Registry') {
            steps {
                script {
                    echo '📤 Pushing images to registry...'
                    docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
                        sh """
                            docker push perpus/eureka-server:${BUILD_VERSION}
                            docker push perpus/eureka-server:latest
                            docker push perpus/eureka-server:${ENVIRONMENT}
                            
                            docker push perpus/api-gateway:${BUILD_VERSION}
                            docker push perpus/api-gateway:latest
                            docker push perpus/api-gateway:${ENVIRONMENT}
                            
                            docker push perpus/service-anggota:${BUILD_VERSION}
                            docker push perpus/service-anggota:latest
                            docker push perpus/service-anggota:${ENVIRONMENT}
                            
                            docker push perpus/service-buku:${BUILD_VERSION}
                            docker push perpus/service-buku:latest
                            docker push perpus/service-buku:${ENVIRONMENT}
                            
                            docker push perpus/service-peminjaman:${BUILD_VERSION}
                            docker push perpus/service-peminjaman:latest
                            docker push perpus/service-peminjaman:${ENVIRONMENT}
                            
                            docker push perpus/service-pengembalian:${BUILD_VERSION}
                            docker push perpus/service-pengembalian:latest
                            docker push perpus/service-pengembalian:${ENVIRONMENT}
                        """
                    }
                }
            }
        }

        stage('Deploy Services') {
            when {
                expression { params.DEPLOY_SERVICES == true }
            }
            steps {
                script {
                    echo "🚀 Deploying to ${ENVIRONMENT} environment..."
                    sh """
                        docker-compose -f docker-compose.yml -f docker-compose.${ENVIRONMENT}.yml down
                        docker-compose -f docker-compose.yml -f docker-compose.${ENVIRONMENT}.yml up -d
                    """
                }
            }
        }

        stage('Health Check') {
            when {
                expression { params.DEPLOY_SERVICES == true }
            }
            steps {
                script {
                    echo '🏥 Running health checks...'
                    sh '''
                        sleep 60
                        
                        # Health check function
                        check_health() {
                            local service=$1
                            local port=$2
                            local max_attempts=30
                            local attempt=1
                            
                            while [ $attempt -le $max_attempts ]; do
                                if curl -f http://localhost:$port/actuator/health > /dev/null 2>&1; then
                                    echo "✅ $service is healthy"
                                    return 0
                                fi
                                echo "⏳ Waiting for $service (attempt $attempt/$max_attempts)..."
                                sleep 10
                                attempt=$((attempt + 1))
                            done
                            
                            echo "❌ $service failed health check"
                            return 1
                        }
                        
                        # Check all services
                        check_health "Eureka Server" 8761
                        check_health "API Gateway" 8080
                        check_health "Service Anggota" 8081
                        check_health "Service Buku" 8082
                        check_health "Service Peminjaman" 8083
                        check_health "Service Pengembalian" 8084
                    '''
                }
            }
        }

        stage('API Testing') {
            when {
                expression { params.DEPLOY_SERVICES == true }
            }
            steps {
                script {
                    echo '🧪 Running API tests...'
                    sh '''
                        # Test Swagger endpoints
                        curl -f http://localhost:8081/swagger-ui.html || exit 1
                        curl -f http://localhost:8082/swagger-ui.html || exit 1
                        curl -f http://localhost:8083/swagger-ui.html || exit 1
                        curl -f http://localhost:8084/swagger-ui.html || exit 1
                        
                        # Test API endpoints via Gateway
                        curl -f http://localhost:8080/api/anggota || exit 1
                        curl -f http://localhost:8080/api/buku || exit 1
                        curl -f http://localhost:8080/api/peminjaman || exit 1
                        curl -f http://localhost:8080/api/pengembalian || exit 1
                    '''
                }
            }
        }

        stage('Generate Documentation') {
            steps {
                script {
                    echo '📚 Generating API documentation...'
                    sh '''
                        mkdir -p api-docs
                        
                        # Download OpenAPI specs
                        curl http://localhost:8081/api-docs -o api-docs/service-anggota-openapi.json
                        curl http://localhost:8082/api-docs -o api-docs/service-buku-openapi.json
                        curl http://localhost:8083/api-docs -o api-docs/service-peminjaman-openapi.json
                        curl http://localhost:8084/api-docs -o api-docs/service-pengembalian-openapi.json
                    '''
                    
                    archiveArtifacts artifacts: 'api-docs/**/*.json', fingerprint: true
                }
            }
        }
    }
    
    post {
        always {
            echo '🧹 Cleaning up...'
            cleanWs()
            
            // Publish test results
            junit '**/target/surefire-reports/*.xml'
            
            // Publish coverage reports
            publishHTML([
                allowMissing: false,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo Coverage Report'
            ])
            
            // Send email notification
            emailext(
                subject: "Pipeline ${currentBuild.fullDisplayName} - ${currentBuild.currentResult}",
                body: """
                    <h2>Build Information</h2>
                    <ul>
                        <li>Pipeline: ${env.JOB_NAME}</li>
                        <li>Build Number: ${env.BUILD_NUMBER}</li>
                        <li>Version: ${BUILD_VERSION}</li>
                        <li>Environment: ${params.ENVIRONMENT}</li>
                        <li>Status: ${currentBuild.currentResult}</li>
                        <li>Duration: ${currentBuild.durationString}</li>
                    </ul>
                    
                    <h3>Swagger Documentation:</h3>
                    <ul>
                        <li><a href="http://localhost:8081/swagger-ui.html">Service Anggota</a></li>
                        <li><a href="http://localhost:8082/swagger-ui.html">Service Buku</a></li>
                        <li><a href="http://localhost:8083/swagger-ui.html">Service Peminjaman</a></li>
                        <li><a href="http://localhost:8084/swagger-ui.html">Service Pengembalian</a></li>
                    </ul>
                    
                    <p>Check console output at <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                """,
                to: 'team@perpustakaan.com',
                mimeType: 'text/html'
            )
        }
        success {
            echo '✅ Pipeline executed successfully!'
            slackSend(
                color: 'good',
                message: """
                    ✅ Pipeline Success!
                    Job: ${env.JOB_NAME}
                    Build: #${env.BUILD_NUMBER}
                    Version: ${BUILD_VERSION}
                    Environment: ${params.ENVIRONMENT}
                """
            )
        }
        failure {
            echo '❌ Pipeline failed!'
            slackSend(
                color: 'danger',
                message: """
                    ❌ Pipeline Failed!
                    Job: ${env.JOB_NAME}
                    Build: #${env.BUILD_NUMBER}
                    Version: ${BUILD_VERSION}
                    Environment: ${params.ENVIRONMENT}
                    Check: ${env.BUILD_URL}
                """
            )
        }
        unstable {
            echo '⚠️ Pipeline unstable!'
            slackSend(
                color: 'warning',
                message: """
                    ⚠️ Pipeline Unstable!
                    Job: ${env.JOB_NAME}
                    Build: #${env.BUILD_NUMBER}
                    Version: ${BUILD_VERSION}
                """
            )
        }
    }
}