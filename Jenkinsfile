pipeline {
    agent any

    tools {
        maven 'maven'
    }

    environment {
        DOCKER_REGISTRY = 'your-registry'
        DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Pulling source code...'
                checkout scm
            }
        }

        stage('Build & Test All Services') {
            parallel {
                stage('Build Eureka Server') {
                    steps {
                        dir('perpustakaan-microservices/eureka-server') {
                            sh 'mvn clean package -DskipTests=false'
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Build API Gateway') {
                    steps {
                        dir('api-gateway') {
                            sh 'mvn clean package -DskipTests=false'
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Build Service Anggota') {
                    steps {
                        dir('service-anggota') {
                            sh 'mvn clean package -DskipTests=false'
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Build Service Buku') {
                    steps {
                        dir('service-buku') {
                            sh 'mvn clean package -DskipTests=false'
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Build Service Peminjaman') {
                    steps {
                        dir('service-peminjaman') {
                            sh 'mvn clean package -DskipTests=false'
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Build Service Pengembalian') {
                    steps {
                        dir('service-pengembalian') {
                            sh 'mvn clean package -DskipTests=false'
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }

        stage('Code Quality Analysis') {
            steps {
                script {
                    echo 'Running code quality analysis...'
                    // Uncomment untuk SonarQube
                    // sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Build Docker Images') {
            parallel {
                stage('Eureka Server Image') {
                    steps {
                        dir('perpustakaan-microservices/eureka-server') {
                            script {
                                def image = docker.build("perpus/eureka-server:${env.BUILD_NUMBER}")
                                image.tag('latest')
                            }
                        }
                    }
                }
                stage('API Gateway Image') {
                    steps {
                        dir('api-gateway') {
                            script {
                                def image = docker.build("perpus/api-gateway:${env.BUILD_NUMBER}")
                                image.tag('latest')
                            }
                        }
                    }
                }
                stage('Service Anggota Image') {
                    steps {
                        dir('service-anggota') {
                            script {
                                def image = docker.build("perpus/service-anggota:${env.BUILD_NUMBER}")
                                image.tag('latest')
                            }
                        }
                    }
                }
                stage('Service Buku Image') {
                    steps {
                        dir('service-buku') {
                            script {
                                def image = docker.build("perpus/service-buku:${env.BUILD_NUMBER}")
                                image.tag('latest')
                            }
                        }
                    }
                }
                stage('Service Peminjaman Image') {
                    steps {
                        dir('service-peminjaman') {
                            script {
                                def image = docker.build("perpus/service-peminjaman:${env.BUILD_NUMBER}")
                                image.tag('latest')
                            }
                        }
                    }
                }
                stage('Service Pengembalian Image') {
                    steps {
                        dir('service-pengembalian') {
                            script {
                                def image = docker.build("perpus/service-pengembalian:${env.BUILD_NUMBER}")
                                image.tag('latest')
                            }
                        }
                    }
                }
            }
        }

        stage('Push to Registry') {
            steps {
                script {
                    docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
                        sh '''
                            docker push perpus/eureka-server:${BUILD_NUMBER}
                            docker push perpus/eureka-server:latest
                            docker push perpus/api-gateway:${BUILD_NUMBER}
                            docker push perpus/api-gateway:latest
                            docker push perpus/service-anggota:${BUILD_NUMBER}
                            docker push perpus/service-anggota:latest
                            docker push perpus/service-buku:${BUILD_NUMBER}
                            docker push perpus/service-buku:latest
                            docker push perpus/service-peminjaman:${BUILD_NUMBER}
                            docker push perpus/service-peminjaman:latest
                            docker push perpus/service-pengembalian:${BUILD_NUMBER}
                            docker push perpus/service-pengembalian:latest
                        '''
                    }
                }
            }
        }

        stage('Deploy to Environment') {
            steps {
                script {
                    echo 'Deploying services...'
                    sh 'docker-compose down'
                    sh 'docker-compose up -d'
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    echo 'Running health checks...'
                    sh '''
                        sleep 60
                        curl -f http://localhost:8761/actuator/health || exit 1
                        curl -f http://localhost:8080/actuator/health || exit 1
                        curl -f http://localhost:8081/actuator/health || exit 1
                        curl -f http://localhost:8082/actuator/health || exit 1
                        curl -f http://localhost:8083/actuator/health || exit 1
                        curl -f http://localhost:8084/actuator/health || exit 1
                    '''
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
            emailext(
                subject: "Pipeline ${currentBuild.fullDisplayName} - ${currentBuild.currentResult}",
                body: """
                    Pipeline: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                    Status: ${currentBuild.currentResult}
                    
                    Check console output at ${env.BUILD_URL}
                """,
                to: 'team@example.com'
            )
        }
        success {
            echo 'Pipeline berhasil dijalankan!'
            // Uncomment untuk Slack notification
            // slackSend(color: 'good', message: "Pipeline ${env.JOB_NAME} #${env.BUILD_NUMBER} berhasil!")
        }
        failure {
            echo 'Pipeline gagal!'
            // Uncomment untuk Slack notification
            // slackSend(color: 'danger', message: "Pipeline ${env.JOB_NAME} #${env.BUILD_NUMBER} gagal!")
        }
    }
}