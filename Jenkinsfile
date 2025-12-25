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
        SLACK_CHANNEL = '#devops-alerts'
        SERVICES = 'eureka-server,api-gateway,service-anggota,service-buku,service-peminjaman,service-pengembalian'
    }

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'production'], description: 'Target deployment environment')
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run unit and integration tests')
        booleanParam(name: 'RUN_SONAR', defaultValue: true, description: 'Run SonarQube analysis')
        booleanParam(name: 'RUN_SECURITY_SCAN', defaultValue: true, description: 'Run OWASP security scan')
        booleanParam(name: 'DEPLOY_SERVICES', defaultValue: true, description: 'Deploy services after build')
        booleanParam(name: 'SKIP_DOCKER_BUILD', defaultValue: false, description: 'Skip Docker image build')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 2, unit: 'HOURS')
        disableConcurrentBuilds()
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    echo """
                    ╔═══════════════════════════════════════════════════════════╗
                    ║  📦 MICROSERVICES BUILD PIPELINE                         ║
                    ║  Version: ${BUILD_VERSION}                               ║
                    ║  Environment: ${params.ENVIRONMENT}                      ║
                    ║  Branch: ${env.GIT_BRANCH}                               ║
                    ╚═══════════════════════════════════════════════════════════╝
                    """
                }
            }
        }

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

        stage('Validate Maven Projects') {
            steps {
                script {
                    echo '🔍 Validating Maven projects...'
                    def services = SERVICES.split(',')
                    services.each { service ->
                        dir(service) {
                            sh 'mvn validate'
                        }
                    }
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
                        script {
                            buildAndTestService('eureka-server')
                        }
                    }
                }
                stage('API Gateway') {
                    steps {
                        script {
                            buildAndTestService('api-gateway')
                        }
                    }
                }
                stage('Service Anggota') {
                    steps {
                        script {
                            buildAndTestService('service-anggota')
                        }
                    }
                }
                stage('Service Buku') {
                    steps {
                        script {
                            buildAndTestService('service-buku')
                        }
                    }
                }
                stage('Service Peminjaman') {
                    steps {
                        script {
                            buildAndTestService('service-peminjaman')
                        }
                    }
                }
                stage('Service Pengembalian') {
                    steps {
                        script {
                            buildAndTestService('service-pengembalian')
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
                        sh """
                            mvn sonar:sonar \
                                -Dsonar.projectKey=perpustakaan-microservices \
                                -Dsonar.projectName="Perpustakaan Microservices" \
                                -Dsonar.projectVersion=${BUILD_VERSION} \
                                -Dsonar.sources=. \
                                -Dsonar.java.binaries=**/target/classes \
                                -Dsonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml \
                                -Dsonar.exclusions=**/target/**,**/test/**
                        """
                    }
                }
            }
        }

        stage('Quality Gate') {
            when {
                expression { params.RUN_SONAR == true }
            }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                    }
                }
            }
        }

        stage('Security Scan') {
            when {
                expression { params.RUN_SECURITY_SCAN == true }
            }
            steps {
                script {
                    echo '🔒 Running OWASP dependency check...'
                    try {
                        sh """
                            mvn org.owasp:dependency-check-maven:check \
                                -DfailBuildOnCVSS=7 \
                                -DsuppressionFiles=**/dependency-check-suppressions.xml \
                                -DautoUpdate=true
                        """
                    } catch (Exception e) {
                        echo "⚠️ Security scan found vulnerabilities: ${e.message}"
                        currentBuild.result = 'UNSTABLE'
                    }
                    
                    dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
                }
            }
        }

        stage('Build Docker Images') {
            when {
                expression { params.SKIP_DOCKER_BUILD == false }
            }
            parallel {
                stage('Eureka Server Image') {
                    steps {
                        script {
                            buildDockerImage('eureka-server')
                        }
                    }
                }
                stage('API Gateway Image') {
                    steps {
                        script {
                            buildDockerImage('api-gateway')
                        }
                    }
                }
                stage('Service Anggota Image') {
                    steps {
                        script {
                            buildDockerImage('service-anggota')
                        }
                    }
                }
                stage('Service Buku Image') {
                    steps {
                        script {
                            buildDockerImage('service-buku')
                        }
                    }
                }
                stage('Service Peminjaman Image') {
                    steps {
                        script {
                            buildDockerImage('service-peminjaman')
                        }
                    }
                }
                stage('Service Pengembalian Image') {
                    steps {
                        script {
                            buildDockerImage('service-pengembalian')
                        }
                    }
                }
            }
        }

        stage('Push to Registry') {
            when {
                expression { params.SKIP_DOCKER_BUILD == false }
            }
            steps {
                script {
                    echo '📤 Pushing images to registry...'
                    docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
                        def services = SERVICES.split(',')
                        services.each { service ->
                            sh """
                                docker push perpus/${service}:${BUILD_VERSION}
                                docker push perpus/${service}:latest
                                docker push perpus/${service}:${ENVIRONMENT}
                            """
                        }
                    }
                }
            }
        }

        stage('Deploy to Environment') {
            when {
                expression { params.DEPLOY_SERVICES == true }
            }
            steps {
                script {
                    echo "🚀 Deploying to ${ENVIRONMENT} environment..."
                    
                    // Stop existing containers
                    sh """
                        docker-compose -f docker-compose.yml -f docker-compose.${ENVIRONMENT}.yml down || true
                    """
                    
                    // Start new containers
                    sh """
                        docker-compose -f docker-compose.yml -f docker-compose.${ENVIRONMENT}.yml up -d
                    """
                    
                    echo "✅ Deployment completed"
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
                    
                    // Wait for services to start
                    sleep(time: 60, unit: 'SECONDS')
                    
                    def services = [
                        'Eureka Server': 8761,
                        'API Gateway': 8080,
                        'Service Anggota': 8081,
                        'Service Buku': 8082,
                        'Service Peminjaman': 8083,
                        'Service Pengembalian': 8084
                    ]
                    
                    def failedServices = []
                    
                    services.each { name, port ->
                        def healthy = checkServiceHealth(name, port)
                        if (!healthy) {
                            failedServices.add(name)
                        }
                    }
                    
                    if (failedServices.size() > 0) {
                        error "Health check failed for: ${failedServices.join(', ')}"
                    }
                    
                    echo "✅ All services are healthy"
                }
            }
        }

        stage('Smoke Tests') {
            when {
                expression { params.DEPLOY_SERVICES == true }
            }
            steps {
                script {
                    echo '🧪 Running smoke tests...'
                    
                    def tests = [
                        'Swagger UI': 'http://localhost:8081/swagger-ui.html',
                        'Actuator Health': 'http://localhost:8081/actuator/health',
                        'Eureka Dashboard': 'http://localhost:8761',
                        'API Gateway': 'http://localhost:8080/actuator/health'
                    ]
                    
                    tests.each { name, url ->
                        sh "curl -f ${url} || exit 1"
                        echo "✅ ${name} is accessible"
                    }
                }
            }
        }

        stage('Generate Reports') {
            steps {
                script {
                    echo '📚 Generating documentation and reports...'
                    
                    // Generate API documentation
                    sh '''
                        mkdir -p api-docs
                        
                        # Download OpenAPI specs
                        curl -f http://localhost:8081/api-docs -o api-docs/service-anggota-openapi.json || true
                        curl -f http://localhost:8082/api-docs -o api-docs/service-buku-openapi.json || true
                        curl -f http://localhost:8083/api-docs -o api-docs/service-peminjaman-openapi.json || true
                        curl -f http://localhost:8084/api-docs -o api-docs/service-pengembalian-openapi.json || true
                    '''
                    
                    archiveArtifacts artifacts: 'api-docs/**/*.json', fingerprint: true, allowEmptyArchive: true
                    
                    // Publish test results
                    junit '**/target/surefire-reports/*.xml'
                    
                    // Publish coverage reports
                    publishHTML([
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: '**/target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Coverage Report'
                    ])
                }
            }
        }
    }
    
    post {
        always {
            script {
                echo '🧹 Cleaning up...'
                
                // Publish reports
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                
                // Clean workspace
                cleanWs()
                
                // Send notifications
                sendNotification(currentBuild.result ?: 'SUCCESS')
            }
        }
        success {
            echo '✅ Pipeline executed successfully!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
        unstable {
            echo '⚠️ Pipeline unstable!'
        }
    }
}

// Helper Functions
def buildAndTestService(String serviceName) {
    dir(serviceName) {
        echo "🔨 Building ${serviceName}..."
        
        try {
            sh '''
                mvn clean verify -DskipTests=false \
                    -Dmaven.test.failure.ignore=false \
                    -B -V
                mvn jacoco:report
            '''
            
            junit '**/target/surefire-reports/*.xml'
            
            jacoco(
                execPattern: '**/target/jacoco.exec',
                classPattern: '**/target/classes',
                sourcePattern: '**/src/main/java'
            )
            
            echo "✅ ${serviceName} built successfully"
        } catch (Exception e) {
            echo "❌ ${serviceName} build failed: ${e.message}"
            throw e
        }
    }
}

def buildDockerImage(String serviceName) {
    dir(serviceName) {
        echo "🐳 Building Docker image for ${serviceName}..."
        
        def image = docker.build("perpus/${serviceName}:${BUILD_VERSION}")
        image.tag('latest')
        image.tag(env.ENVIRONMENT)
        
        echo "✅ Docker image built: perpus/${serviceName}:${BUILD_VERSION}"
    }
}

def checkServiceHealth(String serviceName, int port) {
    def maxAttempts = 30
    def attempt = 1
    
    while (attempt <= maxAttempts) {
        try {
            sh "curl -f http://localhost:${port}/actuator/health"
            echo "✅ ${serviceName} is healthy"
            return true
        } catch (Exception e) {
            echo "⏳ Waiting for ${serviceName} (attempt ${attempt}/${maxAttempts})..."
            sleep(time: 10, unit: 'SECONDS')
            attempt++
        }
    }
    
    echo "❌ ${serviceName} failed health check"
    return false
}

def sendNotification(String status) {
    def color = status == 'SUCCESS' ? 'good' : (status == 'UNSTABLE' ? 'warning' : 'danger')
    def emoji = status == 'SUCCESS' ? '✅' : (status == 'UNSTABLE' ? '⚠️' : '❌')
    
    emailext(
        subject: "${emoji} Build ${status}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        body: """
            <h2>Build Information</h2>
            <ul>
                <li><strong>Pipeline:</strong> ${env.JOB_NAME}</li>
                <li><strong>Build:</strong> #${env.BUILD_NUMBER}</li>
                <li><strong>Version:</strong> ${BUILD_VERSION}</li>
                <li><strong>Environment:</strong> ${params.ENVIRONMENT}</li>
                <li><strong>Status:</strong> ${status}</li>
                <li><strong>Duration:</strong> ${currentBuild.durationString}</li>
            </ul>
            
            <h3>Services:</h3>
            <ul>
                <li><a href="http://localhost:8761">Eureka Dashboard</a></li>
                <li><a href="http://localhost:8080">API Gateway</a></li>
                <li><a href="http://localhost:8081/swagger-ui.html">Service Anggota</a></li>
                <li><a href="http://localhost:8082/swagger-ui.html">Service Buku</a></li>
                <li><a href="http://localhost:8083/swagger-ui.html">Service Peminjaman</a></li>
                <li><a href="http://localhost:8084/swagger-ui.html">Service Pengembalian</a></li>
            </ul>
            
            <h3>Monitoring:</h3>
            <ul>
                <li><a href="http://localhost:9090">Prometheus</a></li>
                <li><a href="http://localhost:3000">Grafana</a></li>
                <li><a href="http://localhost:9411">Zipkin</a></li>
                <li><a href="http://localhost:5601">Kibana</a></li>
            </ul>
            
            <p><a href="${env.BUILD_URL}">View Console Output</a></p>
        """,
        to: 'team@perpustakaan.com',
        mimeType: 'text/html'
    )
    
    // Slack notification (if configured)
    try {
        slackSend(
            channel: SLACK_CHANNEL,
            color: color,
            message: """
                ${emoji} *Build ${status}*
                Job: ${env.JOB_NAME}
                Build: #${env.BUILD_NUMBER}
                Version: ${BUILD_VERSION}
                Environment: ${params.ENVIRONMENT}
                <${env.BUILD_URL}|View Details>
            """
        )
    } catch (Exception e) {
        echo "Could not send Slack notification: ${e.message}"
    }
}