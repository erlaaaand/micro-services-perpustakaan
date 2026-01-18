# 📚 Dokumentasi Sistem Microservices Perpustakaan

Dokumentasi lengkap untuk sistem manajemen perpustakaan berbasis microservices.

## 📑 Daftar Dokumentasi

### 🚀 Getting Started

| Dokumen | Deskripsi |
|---------|-----------|
| **[Setup Guide](SETUP_GUIDE.md)** | Panduan lengkap setup proyek dari awal hingga production-ready |
| **[Quick Start](../README.md#-quick-start)** | Cara cepat menjalankan proyek |

### 🏗️ Architecture & Design

| Dokumen | Deskripsi |
|---------|-----------|
| **[Architecture](ARCHITECTURE.md)** | Penjelasan arsitektur sistem, design patterns, dan komponen |
| **[CQRS Implementation](CQRS.md)** | Detail implementasi CQRS pattern |
| **[Event-Driven Architecture](EVENT_DRIVEN.md)** | Dokumentasi RabbitMQ dan event flow |

### 📖 API Documentation

| Dokumen | Deskripsi |
|---------|-----------|
| **[API Reference](API_REFERENCE.md)** | Dokumentasi lengkap semua API endpoints |
| **[Error Handling](API_REFERENCE.md#error-responses)** | Format error responses dan troubleshooting |
| **[Pagination](API_REFERENCE.md#pagination)** | Cara menggunakan pagination |

### 📊 Monitoring & Operations

| Dokumen | Deskripsi |
|---------|-----------|
| **[Monitoring Guide](MONITORING.md)** | Panduan Prometheus, Grafana, Zipkin, dan ELK Stack |
| **[Health Checks](MONITORING.md#health-checks)** | Monitoring kesehatan services |
| **[Alerting](MONITORING.md#alerting)** | Setup dan konfigurasi alerts |

### 🚀 CI/CD

| Dokumen | Deskripsi |
|---------|-----------|
| **[CI/CD Pipeline](CICD.md)** | Setup Jenkins, pipeline configuration, dan deployment |
| **[Docker Guide](CICD.md#docker-integration)** | Containerization dan Docker best practices |

### 🔧 Development

| Dokumen | Deskripsi |
|---------|-----------|
| **[Development Guide](DEVELOPMENT.md)** | Local development, testing, dan debugging |
| **[Troubleshooting](TROUBLESHOOTING.md)** | Common issues dan solusinya |
| **[Configuration](CONFIGURATION.md)** | Penjelasan file-file konfigurasi |

### 🔐 Production

| Dokumen | Deskripsi |
|---------|-----------|
| **[Production Checklist](PRODUCTION.md)** | Checklist deployment ke production |
| **[Security Guide](SECURITY.md)** | Best practices keamanan |
| **[Performance Tuning](PERFORMANCE.md)** | Optimasi performa sistem |

### 📊 Database

| Dokumen | Deskripsi |
|---------|-----------|
| **[Database Schema](DATABASE.md)** | Schema H2 (Write Model) dan MongoDB (Read Model) |
| **[Data Migration](DATABASE.md#migration)** | Panduan migrasi data |

---

## 🎯 Dokumentasi Berdasarkan Peran

### Developer

1. [Setup Guide](SETUP_GUIDE.md) - Setup environment
2. [Development Guide](DEVELOPMENT.md) - Development workflow
3. [API Reference](API_REFERENCE.md) - API documentation
4. [Architecture](ARCHITECTURE.md) - System design

### DevOps Engineer

1. [CI/CD Pipeline](CICD.md) - Pipeline setup
2. [Monitoring Guide](MONITORING.md) - Observability
3. [Production Checklist](PRODUCTION.md) - Deployment
4. [Troubleshooting](TROUBLESHOOTING.md) - Issue resolution

### Technical Lead

1. [Architecture](ARCHITECTURE.md) - System architecture
2. [CQRS Implementation](CQRS.md) - Design patterns
3. [Performance Tuning](PERFORMANCE.md) - Optimization
4. [Security Guide](SECURITY.md) - Security practices

### QA/Tester

1. [API Reference](API_REFERENCE.md) - Testing endpoints
2. [Development Guide](DEVELOPMENT.md#testing) - Test execution
3. [Troubleshooting](TROUBLESHOOTING.md) - Issue debugging

---

## 📝 Dokumentasi Quick Links

### Sering Digunakan

- [Cara menjalankan proyek](SETUP_GUIDE.md#-one-command-setup)
- [Test API dengan cURL](API_REFERENCE.md#testing-with-curl)
- [View logs di Kibana](MONITORING.md#kibana-setup)
- [Monitor metrics di Grafana](MONITORING.md#grafana)
- [Troubleshoot RabbitMQ](TROUBLESHOOTING.md#rabbitmq-issues)

### Referensi Cepat

- [Port mapping](CONFIGURATION.md#port-configuration)
- [Environment variables](CONFIGURATION.md#environment-variables)
- [Health check endpoints](MONITORING.md#health-checks)
- [Docker commands](CICD.md#docker-integration)

---

## 🔄 Update Log

| Tanggal | Versi | Perubahan |
|---------|-------|-----------|
| 2024-01-15 | 2.0.0 | Initial refactored documentation |
| - | - | Added monitoring stack documentation |
| - | - | Added CI/CD pipeline guide |
| - | - | Restructured into modular docs |

---

## 🤝 Contributing to Documentation

Untuk berkontribusi pada dokumentasi:

1. Fork repository
2. Edit dokumentasi di folder `docs/`
3. Ikuti format Markdown yang konsisten
4. Submit pull request dengan deskripsi jelas
5. Dokumentasi akan direview sebelum merge

### Dokumentasi Guidelines

- Gunakan heading hierarchy yang tepat
- Include code examples yang working
- Add screenshots untuk UI guidance
- Keep bahasa konsisten (Indonesia atau English)
- Update table of contents jika menambah sections
- Test semua commands sebelum mendokumentasikan

---

## 📧 Feedback & Questions

Untuk pertanyaan tentang dokumentasi:
- Open issue di [GitHub Issues](https://github.com/erlaaaand/micro-services-perpustakaan/issues)
- Tag dengan label `documentation`
- Gunakan template yang tersedia

---

## 📜 License

Dokumentasi ini dilisensikan under MIT License - see [LICENSE](../LICENSE) file.

---

<div align="center">

**[⬆️ Back to Main README](../README.md)**

</div>