pipeline {
    agent any

    tools {
        // Pastikan Maven sudah terinstall di Jenkins Global Tools dengan nama 'maven'
        maven 'maven' 
    }

    stages {
        stage('Checkout') {
            steps {
                // Jenkins otomatis checkout dari Git repository
                echo 'Pulling source code...'
            }
        }

        stage('Build & Test All Services') {
            steps {
                script {
                    // Build Eureka Server
                    dir('perpustakaan-microservices/eureka-server') {
                        sh 'mvn clean package -DskipTests=false'
                    }
                    // Build Service Anggota
                    dir('service-anggota') {
                        sh 'mvn clean package -DskipTests=false'
                    }
                    // Build Service Buku
                    dir('service-buku') {
                        sh 'mvn clean package -DskipTests=false'
                    }
                    // Build Service Peminjaman
                    dir('service-peminjaman') {
                        sh 'mvn clean package -DskipTests=false'
                    }
                    // Build Service Pengembalian
                    dir('service-pengembalian') {
                        sh 'mvn clean package -DskipTests=false'
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    // Contoh build image (Pastikan plugin Docker Pipeline terinstall di Jenkins)
                    dir('perpustakaan-microservices/eureka-server') {
                        sh 'docker build -t perpus/eureka-server:latest .'
                    }
                    dir('service-anggota') {
                        sh 'docker build -t perpus/service-anggota:latest .'
                    }
                    dir('service-buku') {
                        sh 'docker build -t perpus/service-buku:latest .'
                    }
                    dir('service-peminjaman') {
                        sh 'docker build -t perpus/service-peminjaman:latest .'
                    }
                    dir('service-pengembalian') {
                        sh 'docker build -t perpus/service-pengembalian:latest .'
                    }
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline berhasil dijalankan!'
        }
        failure {
            echo 'Pipeline gagal.'
        }
    }
}