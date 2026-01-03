<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistem Microservices Perpustakaan</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #333;
            line-height: 1.6;
        }

        /* Navigation Bar */
        .navbar {
            position: fixed;
            top: 0;
            width: 100%;
            background: rgba(255, 255, 255, 0.98);
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            z-index: 1000;
            padding: 15px 0;
        }

        .nav-container {
            max-width: 1200px;
            margin: 0 auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0 20px;
        }

        .nav-logo {
            font-size: 24px;
            font-weight: bold;
            color: #667eea;
        }

        .nav-links {
            display: flex;
            gap: 25px;
            list-style: none;
        }

        .nav-links a {
            text-decoration: none;
            color: #333;
            font-weight: 500;
            transition: color 0.3s;
        }

        .nav-links a:hover {
            color: #667eea;
        }

        /* Container */
        .container {
            max-width: 1200px;
            margin: 80px auto 40px;
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.2);
            overflow: hidden;
        }

        /* Header with Typing Effect */
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 60px 40px;
            text-align: center;
            color: white;
        }

        .typing-title {
            font-size: 42px;
            font-weight: bold;
            min-height: 60px;
            margin-bottom: 20px;
        }

        .typing-cursor {
            display: inline-block;
            width: 3px;
            background: white;
            animation: blink 1s infinite;
        }

        @keyframes blink {
            0%, 50% { opacity: 1; }
            51%, 100% { opacity: 0; }
        }

        .subtitle {
            font-size: 18px;
            opacity: 0.9;
            max-width: 800px;
            margin: 0 auto;
        }

        .badges {
            display: flex;
            justify-content: center;
            gap: 10px;
            margin-top: 25px;
            flex-wrap: wrap;
        }

        .badge {
            background: rgba(255,255,255,0.2);
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 14px;
            backdrop-filter: blur(10px);
        }

        /* Content */
        .content {
            padding: 40px;
        }

        .section {
            margin-bottom: 50px;
        }

        .section-title {
            font-size: 32px;
            color: #667eea;
            margin-bottom: 25px;
            padding-bottom: 10px;
            border-bottom: 3px solid #667eea;
        }

        /* Grid Layout for Features */
        .features-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
            margin-top: 25px;
        }

        .feature-card {
            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            transition: transform 0.3s;
        }

        .feature-card:hover {
            transform: translateY(-5px);
        }

        .feature-card h3 {
            color: #667eea;
            margin-bottom: 15px;
            font-size: 20px;
        }

        .feature-card ul {
            list-style: none;
            padding-left: 0;
        }

        .feature-card li {
            padding: 8px 0;
            border-bottom: 1px solid rgba(0,0,0,0.1);
        }

        .feature-card li:last-child {
            border-bottom: none;
        }

        /* Tech Stack Table */
        .tech-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            border-radius: 10px;
            overflow: hidden;
        }

        .tech-table thead {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }

        .tech-table th {
            padding: 15px;
            text-align: left;
            font-weight: 600;
        }

        .tech-table td {
            padding: 15px;
            border-bottom: 1px solid #e0e0e0;
        }

        .tech-table tbody tr:hover {
            background: #f5f7fa;
        }

        .tech-table tbody tr:last-child td {
            border-bottom: none;
        }

        /* Accordion/Collapsible */
        .accordion {
            margin: 20px 0;
        }

        .accordion-item {
            background: #f8f9fa;
            margin-bottom: 15px;
            border-radius: 10px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        .accordion-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 18px 25px;
            cursor: pointer;
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-weight: 600;
            transition: background 0.3s;
        }

        .accordion-header:hover {
            background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
        }

        .accordion-icon {
            transition: transform 0.3s;
            font-size: 20px;
        }

        .accordion-item.active .accordion-icon {
            transform: rotate(180deg);
        }

        .accordion-content {
            max-height: 0;
            overflow: hidden;
            transition: max-height 0.3s ease-out;
        }

        .accordion-item.active .accordion-content {
            max-height: 2000px;
            transition: max-height 0.5s ease-in;
        }

        .accordion-body {
            padding: 25px;
            background: white;
        }

        /* Code Blocks */
        .code-block {
            background: #2d2d2d;
            color: #f8f8f2;
            padding: 20px;
            border-radius: 8px;
            overflow-x: auto;
            margin: 15px 0;
            font-family: 'Courier New', monospace;
            font-size: 14px;
            line-height: 1.5;
        }

        .code-block pre {
            margin: 0;
        }

        /* Architecture Diagram */
        .diagram-container {
            background: #f8f9fa;
            padding: 30px;
            border-radius: 15px;
            text-align: center;
            margin: 25px 0;
        }

        .diagram-placeholder {
            background: white;
            padding: 60px;
            border-radius: 10px;
            border: 2px dashed #667eea;
            color: #667eea;
            font-size: 18px;
        }

        /* Quick Links */
        .quick-links {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-top: 25px;
        }

        .quick-link {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 10px;
            text-decoration: none;
            text-align: center;
            font-weight: 600;
            transition: transform 0.3s, box-shadow 0.3s;
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }

        .quick-link:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 25px rgba(0,0,0,0.3);
        }

        /* Footer */
        .footer {
            background: #2d2d2d;
            color: white;
            text-align: center;
            padding: 30px;
            margin-top: 40px;
        }

        /* Responsive */
        @media (max-width: 768px) {
            .nav-links {
                display: none;
            }

            .typing-title {
                font-size: 28px;
            }

            .section-title {
                font-size: 24px;
            }

            .features-grid {
                grid-template-columns: 1fr;
            }
        }

        /* Scroll to Top Button */
        .scroll-top {
            position: fixed;
            bottom: 30px;
            right: 30px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            width: 50px;
            height: 50px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            box-shadow: 0 5px 15px rgba(0,0,0,0.3);
            opacity: 0;
            transition: opacity 0.3s, transform 0.3s;
            z-index: 999;
        }

        .scroll-top.visible {
            opacity: 1;
        }

        .scroll-top:hover {
            transform: translateY(-5px);
        }
    </style>
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar">
        <div class="nav-container">
            <div class="nav-logo">📚 Perpustakaan MS</div>
            <ul class="nav-links">
                <li><a href="#features">Fitur</a></li>
                <li><a href="#architecture">Arsitektur</a></li>
                <li><a href="#quickstart">Quick Start</a></li>
                <li><a href="#api">API</a></li>
                <li><a href="#monitoring">Monitoring</a></li>
            </ul>
        </div>
    </nav>

    <!-- Main Container -->
    <div class="container">
        <!-- Header -->
        <div class="header">
            <div class="typing-title" id="typingTitle"></div>
            <p class="subtitle">Sistem manajemen perpustakaan enterprise-grade dengan arsitektur microservices, implementasi CQRS pattern, Event-Driven Architecture menggunakan RabbitMQ, CI/CD pipeline, dan monitoring terdistribusi</p>
            <div class="badges">
                <span class="badge">Spring Boot 3.2.5</span>
                <span class="badge">Java 17</span>
                <span class="badge">MongoDB 6.0</span>
                <span class="badge">RabbitMQ 3.13</span>
                <span class="badge">Docker Ready</span>
                <span class="badge">MIT License</span>
            </div>
        </div>

        <!-- Content -->
        <div class="content">
            <!-- Quick Links -->
            <section class="section" id="quicklinks">
                <h2 class="section-title">🚀 Quick Links</h2>
                <div class="quick-links">
                    <a href="#features" class="quick-link">📋 Fitur Utama</a>
                    <a href="#architecture" class="quick-link">🏛️ Arsitektur</a>
                    <a href="#quickstart" class="quick-link">⚡ Quick Start</a>
                    <a href="#api" class="quick-link">📖 Dokumentasi API</a>
                    <a href="#monitoring" class="quick-link">📊 Monitoring</a>
                    <a href="#troubleshooting" class="quick-link">🐛 Troubleshooting</a>
                </div>
            </section>

            <!-- Features -->
            <section class="section" id="features">
                <h2 class="section-title">🎯 Fitur Utama</h2>
                <div class="features-grid">
                    <div class="feature-card">
                        <h3>🏗️ Architecture & Patterns</h3>
                        <ul>
                            <li>✅ CQRS Pattern</li>
                            <li>✅ Event-Driven Architecture</li>
                            <li>✅ Message Broker - RabbitMQ</li>
                            <li>✅ Service Discovery - Eureka</li>
                            <li>✅ API Gateway - Spring Cloud</li>
                            <li>✅ Circuit Breaker - Resilience4j</li>
                            <li>✅ Load Balancing</li>
                        </ul>
                    </div>
                    <div class="feature-card">
                        <h3>🔧 DevOps & Operations</h3>
                        <ul>
                            <li>✅ CI/CD Pipeline - Jenkins</li>
                            <li>✅ Containerization - Docker</li>
                            <li>✅ Distributed Logging - ELK</li>
                            <li>✅ Message Queue Monitoring</li>
                            <li>✅ Health Monitoring</li>
                            <li>✅ API Documentation - Swagger</li>
                            <li>✅ Graceful Shutdown</li>
                        </ul>
                    </div>
                </div>
            </section>

            <!-- Tech Stack Table -->
            <section class="section" id="techstack">
                <h2 class="section-title">📦 Tech Stack & Components</h2>
                <table class="tech-table">
                    <thead>
                        <tr>
                            <th>Komponen</th>
                            <th>Port</th>
                            <th>Teknologi</th>
                            <th>Fungsi</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><strong>Eureka Server</strong></td>
                            <td>8761</td>
                            <td>Spring Cloud Netflix</td>
                            <td>Service Registry & Discovery</td>
                        </tr>
                        <tr>
                            <td><strong>API Gateway</strong></td>
                            <td>8080</td>
                            <td>Spring Cloud Gateway</td>
                            <td>Routing, Load Balancing</td>
                        </tr>
                        <tr>
                            <td><strong>Service Anggota</strong></td>
                            <td>8081</td>
                            <td>Spring Boot + CQRS</td>
                            <td>Manajemen data anggota</td>
                        </tr>
                        <tr>
                            <td><strong>Service Buku</strong></td>
                            <td>8082</td>
                            <td>Spring Boot + CQRS</td>
                            <td>Manajemen katalog buku</td>
                        </tr>
                        <tr>
                            <td><strong>Service Peminjaman</strong></td>
                            <td>8083</td>
                            <td>Spring Boot + CQRS</td>
                            <td>Transaksi peminjaman</td>
                        </tr>
                        <tr>
                            <td><strong>Service Pengembalian</strong></td>
                            <td>8084</td>
                            <td>Spring Boot + CQRS</td>
                            <td>Proses pengembalian & denda</td>
                        </tr>
                        <tr>
                            <td><strong>RabbitMQ</strong></td>
                            <td>5672</td>
                            <td>RabbitMQ 3.13</td>
                            <td>Message Broker</td>
                        </tr>
                        <tr>
                            <td><strong>RabbitMQ Management</strong></td>
                            <td>15672</td>
                            <td>RabbitMQ UI</td>
                            <td>Monitoring queue & exchange</td>
                        </tr>
                        <tr>
                            <td><strong>MongoDB</strong></td>
                            <td>27017</td>
                            <td>MongoDB 6.0</td>
                            <td>Read Model Database</td>
                        </tr>
                        <tr>
                            <td><strong>Elasticsearch</strong></td>
                            <td>9200</td>
                            <td>Elastic 8.11</td>
                            <td>Log storage & indexing</td>
                        </tr>
                        <tr>
                            <td><strong>Kibana</strong></td>
                            <td>5601</td>
                            <td>Kibana 8.11</td>
                            <td>Log visualization</td>
                        </tr>
                        <tr>
                            <td><strong>Jenkins</strong></td>
                            <td>9000</td>
                            <td>Jenkins LTS</td>
                            <td>CI/CD Automation</td>
                        </tr>
                    </tbody>
                </table>
            </section>

            <!-- Architecture -->
            <section class="section" id="architecture">
                <h2 class="section-title">🏛️ Arsitektur Sistem</h2>
                <div class="diagram-container">
                    <div class="diagram-placeholder">
                        <strong>Diagram Arsitektur Microservices</strong><br><br>
                        Client → API Gateway → Eureka<br>
                        ↓<br>
                        Services (Anggota, Buku, Peminjaman, Pengembalian)<br>
                        ↓<br>
                        RabbitMQ (Event-Driven) ↔ MongoDB (Read Model)<br>
                        ↓<br>
                        ELK Stack (Logging & Monitoring)
                    </div>
                </div>
            </section>

            <!-- Quick Start with Accordion -->
            <section class="section" id="quickstart">
                <h2 class="section-title">⚡ Quick Start</h2>
                
                <div class="accordion">
                    <div class="accordion-item">
                        <div class="accordion-header">
                            <span>Prerequisites & Requirements</span>
                            <span class="accordion-icon">▼</span>
                        </div>
                        <div class="accordion-content">
                            <div class="accordion-body">
                                <h4>Required Software:</h4>
                                <ul>
                                    <li>Java 17 or higher</li>
                                    <li>Maven 3.9+</li>
                                    <li>Docker 20.10+</li>
                                    <li>Docker Compose v2+</li>
                                </ul>
                                <h4>System Requirements:</h4>
                                <ul>
                                    <li>RAM: 8GB minimum (16GB recommended)</li>
                                    <li>CPU: 4 cores minimum</li>
                                    <li>Disk: 20GB free space</li>
                                </ul>
                            </div>
                        </div>
                    </div>

                    <div class="accordion-item">
                        <div class="accordion-header">
                            <span>🔥 One-Command Setup</span>
                            <span class="accordion-icon">▼</span>
                        </div>
                        <div class="accordion-content">
                            <div class="accordion-body">
                                <div class="code-block">
<pre># Clone repository
git clone &lt;repository-url&gt;
cd perpustakaan-microservices

# Build semua services
./build-all.sh

# Start semua services dengan Docker Compose
docker-compose up -d

# Verifikasi health status
./deploy.sh health</pre>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="accordion-item">
                        <div class="accordion-header">
                            <span>📊 Verification URLs</span>
                            <span class="accordion-icon">▼</span>
                        </div>
                        <div class="accordion-content">
                            <div class="accordion-body">
                                <ul>
                                    <li><strong>Eureka Dashboard:</strong> <a href="http://localhost:8761" target="_blank">http://localhost:8761</a></li>
                                    <li><strong>API Gateway:</strong> <a href="http://localhost:8080" target="_blank">http://localhost:8080</a></li>
                                    <li><strong>Swagger UI:</strong> <a href="http://localhost:8080/swagger-ui.html" target="_blank">http://localhost:8080/swagger-ui.html</a></li>
                                    <li><strong>RabbitMQ Management:</strong> <a href="http://localhost:15672" target="_blank">http://localhost:15672</a> (guest/guest)</li>
                                    <li><strong>Kibana Logs:</strong> <a href="http://localhost:5601" target="_blank">http://localhost:5601</a></li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- API Documentation with Accordion -->
            <section class="section" id="api">
                <h2 class="section-title">📖 Dokumentasi API</h2>
                
                <div class="accordion">
                    <div class="accordion-item">
                        <div class="accordion-header">
                            <span>🔹 Service Anggota (Member Management)</span>
                            <span class="accordion-icon">▼</span>
                        </div>
                        <div class="accordion-content">
                            <div class="accordion-body">
                                <p><strong>Base URL:</strong> <code>http://localhost:8080/api/anggota</code></p>
                                
                                <h4>Create Member</h4>
                                <div class="code-block">
<pre>POST /api/anggota
Content-Type: application/json

{
  "nomorAnggota": "A001",
  "nama": "John Doe",
  "alamat": "Jl. Merdeka No. 123",
  "email": "john@example.com"
}

Response: 201 Created
Event Published: anggota.created → RabbitMQ</pre>
                                </div>

                                <h4>Get All Members</h4>
                                <div class="code-block">
<pre>GET /api/anggota?page=0&size=10&sortBy=nama

Data source: MongoDB (Read Model)</pre>
                                </div>

                                <h4>Get Member by ID</h4>
                                <div class="code-block">
<pre>GET /api/anggota/{id}

Data source: MongoDB (Read Model)</pre>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="accordion-item">
                        <div class="accordion-header">
                            <span>🔹 Service Buku (Book Catalog)</span>
                            <span class="accordion-icon">▼</span>
                        </div>
                        <div class="accordion-content">
                            <div class="accordion-body">
                                <p><strong>Base URL:</strong> <code>http://localhost:8080/api/buku</code></p>
                                
                                <h4>Create Book</h4>
                                <div class="code-block">
<pre>POST /api/buku
Content-Type: application/json

{
  "kodeBuku": "BK-001",
  "judul": "Java Programming",
  "pengarang": "John Doe",
  "penerbit": "Erlangga",
  "tahunTerbit": 2020
}

Event Published: buku.created → RabbitMQ</pre>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="accordion-item">
                        <div class="accordion-header">
                            <span>🔹 Service Peminjaman (Borrowing)</span>
                            <span class="accordion-icon">▼</span>
                        </div>
                        <div class="accordion-content">
                            <div class="accordion-body">
                                <p><strong>Base URL:</strong> <code>http://localhost:8080/api/peminjaman</code></p>
                                
                                <h4>Create Borrowing Transaction</h4>
                                <div class="code-block">
<pre>POST /api/peminjaman
Content-Type: application/json

{
  "anggotaId": 1,
  "bukuId": 1,
  "tanggalPinjam": "2024-01-01",
  "tanggalKembali": "2024-01-15",
  "status": "DIPINJAM"
}

Event Published: peminjaman.created → RabbitMQ</pre>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="accordion-item">
                        <div class="accordion-header">
                            <span>📝 Event Structure Example</span>
                            <span class="accordion-icon">▼</span>
                        </div>
                        <div class="accordion-content">
                            <div class="accordion-body">
                                <p>Contoh event yang dipublikasikan ke RabbitMQ:</p>
                                <div class="code-block">
<pre>{
  "eventId": "uuid-v4",
  "eventType": "ANGGOTA_CREATED",
  "timestamp": "2024-01-15T10:30:00Z",
  "aggregateId": "1",
  "payload": {
    "nomorAnggota": "A001",
    "nama": "John Doe",
    "alamat": "Jl. Merdeka No. 123",
    "email": "john@example.com"
  }
}</pre>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Monitoring -->
            <section class="section" id="monitoring">
                <h2 class="section-title">🔍 Monitoring & Observability</h2>
                
                <div class="accordion">
                    <div class="accordion-item">
                        <div class="accordion-header">
                            <span>🐰 RabbitMQ Monitoring</span>
                            <span class="accordion-icon">▼</span>
                        </div>
                        <div class="accordion-content">
                            <div class="accordion-body">
                                <p><strong>RabbitMQ Management UI:</strong> <a href="http://localhost:15672" target="_blank">http://localhost:15672</a></p>
                                <p><strong>Login:</strong> guest / guest</p>
                                
                                <h4>Monitor Queue Activity:</h4>
                                <ol>
                                    <li>Klik <strong>Queues</strong> tab</li>
                                    <li>Monitor metrics: Ready, Unacked, Total messages</li>
                                    <li>Check Publish/Deliver Rate</li>
                                </ol>

                                <h4>Exchange Configuration:</h4>
                                <ul>
                                    <li><strong>anggota.exchange</strong> (Topic) → anggota.queue</li>
                                    <li><strong>buku.exchange</strong> (Topic) → buku.queue</li>
                                    <li><strong>peminjaman.exchange</strong> (Topic) → peminjaman.queue</li>
                                    <li><strong>pengembalian.exchange</strong> (Topic) → pengembalian.queue</li>
                                </ul>
                            </div>
                        </div>
                    </div>

                    <div class="accordion-item">
                        <div class="accordion-header">
                            <span>📊 ELK Stack (Logging)</span>
                            <span class="accordion-icon">▼</span>
                        </div>
                        <div class="accordion-content">
                            <div class="accordion-body">
                                <p><strong>Kibana Dashboard:</strong> <a href="http://localhost:5601" target="_blank">http://localhost:5601</a></p>
                                
                                <h4>Setup Index Pattern:</h4>
                                <ol>
                                    <li>Buka Kibana → Management → Stack Management</li>
                                    <li>Pilih <strong>Index Patterns</strong> → <strong>Create index pattern</strong></li>
                                    <li>Masukkan pattern: <code>logs-*</code></li>
                                    <li>Pilih timestamp field: <code>@timestamp</code></li>
                                    <li>Klik <strong>Create index pattern</strong></li>
                                </ol>

                                <h4>Search RabbitMQ Events:</h4>
                                <div class="code-block">
<pre>message: "Publishing event to RabbitMQ"
message: "Received event from RabbitMQ"</pre>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="accordion-item">
                        <div class="accordion-header">
                            <span>🩺 Health Checks</span>
                            <span class="accordion-icon">▼</span>
                        </div>
                        <div class="accordion-content">
                            <div class="accordion-body">
                                <div class="code-block">
<pre># Check all services
./deploy.sh health

# Individual service health
curl http://localhost:8761/actuator/health  # Eureka
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # Service Anggota
curl http://localhost:8082/actuator/health  # Service Buku

# Check RabbitMQ health
curl http://localhost:15672/api/health/checks/alarms</pre>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Troubleshooting -->
            <section class="section" id="troubleshooting">
                <h2 class="section-title">🐛 Troubleshooting</h2>
                
                <div class="accordion">
                    <div class="accordion-item">
                        <div class="accordion-header">
                            <span>Services Not Starting</span>
                            <span class="accordion-icon">▼</span>
                        </div>
                        <div class="accordion-content">
                            <div class="accordion-body">
                                <div class="code-block">
<pre># Check logs
docker-compose logs -f [service-name]

# Restart specific service
docker-compose restart [service-name]

# Clean rebuild
docker-compose down
docker-compose up -d --build</pre>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="accordion-item">
                        <div class="accordion-header">
                            <span>RabbitMQ Connection Issues</span>
                            <span class="accordion-icon">▼</span>
                        </div>
                        <div class="accordion-content">
                            <div class="accordion-body">
                                <div class="code-block">
<pre># Check RabbitMQ status
docker ps | grep rabbitmq

# View RabbitMQ logs
docker logs rabbitmq

# Check if queues are created
docker exec -it rabbitmq rabbitmqctl list_queues

# Check exchanges
docker exec -it rabbitmq rabbitmqctl list_exchanges</pre>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="accordion-item">
                        <div class="accordion-header">
                            <span>Message Not Being Consumed</span>
                            <span class="accordion-icon">▼</span>
                        </div>
                        <div class="accordion-content">
                            <div class="accordion-body">
                                <ol>
                                    <li>Check RabbitMQ Management UI (http://localhost:15672)</li>
                                    <li>Verify queue has consumers in <strong>Queues</strong> tab</li>
                                    <li>Check message count: Ready dan Unacked</li>
                                    <li>View service logs untuk error messages</li>
                                    <li>Verify exchange-queue binding</li>
                                </ol>
                                <div class="code-block">
<pre>docker-compose logs -f service-anggota</pre>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="accordion-item">
                        <div class="accordion-header">
                            <span>Port Already in Use</span>
                            <span class="accordion-icon">▼</span>
                        </div>
                        <div class="accordion-content">
                            <div class="accordion-body">
                                <div class="code-block">
<pre># Find process using port (Linux/Mac)
lsof -i :8080

# Find process using port (Windows)
netstat -ano | findstr :8080

# Kill process
kill -9 &lt;PID&gt;  # Linux/Mac
taskkill /PID &lt;PID&gt; /F  # Windows</pre>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </div>

        <!-- Footer -->
        <div class="footer">
            <p>📧 Email: blackpenta98@gmail.com</p>
            <p>Built with Java, Spring Boot, RabbitMQ, and passion for clean architecture</p>
            <p>&copy; 2024 Sistem Microservices Perpustakaan | MIT License</p>
        </div>
    </div>

    <!-- Scroll to Top Button -->
    <div class="scroll-top" id="scrollTop">↑</div>

    <script>
        // Typing Effect Animation
        const title = "📚 Sistem Microservices Perpustakaan";
        const typingElement = document.getElementById('typingTitle');
        let index = 0;

        function typeWriter() {
            if (index < title.length) {
                typingElement.innerHTML = title.substring(0, index + 1) + '<span class="typing-cursor">|</span>';
                index++;
                setTimeout(typeWriter, 100);
            } else {
                typingElement.innerHTML = title;
            }
        }

        // Start typing animation when page loads
        window.addEventListener('load', () => {
            setTimeout(typeWriter, 500);
        });

        // Accordion functionality
        document.querySelectorAll('.accordion-header').forEach(header => {
            header.addEventListener('click', () => {
                const item = header.parentElement;
                const isActive = item.classList.contains('active');
                
                // Close all accordion items
                document.querySelectorAll('.accordion-item').forEach(acc => {
                    acc.classList.remove('active');
                });
                
                // Open clicked item if it wasn't active
                if (!isActive) {
                    item.classList.add('active');
                }
            });
        });

        // Smooth scroll for navigation links
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function (e) {
                e.preventDefault();
                const target = document.querySelector(this.getAttribute('href'));
                if (target) {
                    const offsetTop = target.offsetTop - 80;
                    window.scrollTo({
                        top: offsetTop,
                        behavior: 'smooth'
                    });
                }
            });
        });

        // Scroll to top button
        const scrollTopBtn = document.getElementById('scrollTop');
        
        window.addEventListener('scroll', () => {
            if (window.pageYOffset > 300) {
                scrollTopBtn.classList.add('visible');
            } else {
                scrollTopBtn.classList.remove('visible');
            }
        });

        scrollTopBtn.addEventListener('click', () => {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });

        // Add animation on scroll
        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }
            });
        }, observerOptions);

        document.querySelectorAll('.section').forEach(section => {
            section.style.opacity = '0';
            section.style.transform = 'translateY(20px)';
            section.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            observer.observe(section);
        });
    </script>
</body>
</html>