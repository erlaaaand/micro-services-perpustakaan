# 💾 Database Schema Documentation

Dokumentasi lengkap schema database untuk Write Model (H2) dan Read Model (MongoDB).

## 📑 Daftar Isi

- [Database Architecture](#database-architecture)
- [Write Model (H2)](#write-model-h2)
- [Read Model (MongoDB)](#read-model-mongodb)
- [Data Migration](#data-migration)
- [Backup & Restore](#backup--restore)
- [Indexes](#indexes)

---

## Database Architecture

### CQRS Database Pattern

```
┌─────────────────────────────────────────┐
│           WRITE SIDE (Command)          │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │     H2 In-Memory Database         │ │
│  │                                   │ │
│  │  - Normalized schema              │ │
│  │  - ACID compliance                │ │
│  │  - Fast writes                    │ │
│  │  - Relational constraints         │ │
│  └───────────────────────────────────┘ │
└─────────────────┬───────────────────────┘
                  │
                  │ Events (RabbitMQ)
                  ▼
┌─────────────────────────────────────────┐
│            READ SIDE (Query)            │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │         MongoDB                    │ │
│  │                                   │ │
│  │  - Denormalized documents         │ │
│  │  - Fast queries                   │ │
│  │  - Flexible schema                │ │
│  │  - Scalable reads                 │ │
│  └───────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

---

## Write Model (H2)

### Anggota Command Table

```sql
CREATE TABLE anggota_command (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nomor_anggota VARCHAR(50) UNIQUE NOT NULL,
    nama VARCHAR(100) NOT NULL,
    alamat VARCHAR(255),
    email VARCHAR(100),
    telepon VARCHAR(15),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT uk_anggota_nomor UNIQUE (nomor_anggota),
    CONSTRAINT uk_anggota_email UNIQUE (email)
);

CREATE INDEX idx_anggota_nama ON anggota_command(nama);
CREATE INDEX idx_anggota_email ON anggota_command(email);
```

**Columns**:
| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| id | BIGINT | NO | Primary key (auto-increment) |
| nomor_anggota | VARCHAR(50) | NO | Unique member number (e.g., A001) |
| nama | VARCHAR(100) | NO | Member name |
| alamat | VARCHAR(255) | YES | Address |
| email | VARCHAR(100) | YES | Email (unique) |
| telepon | VARCHAR(15) | YES | Phone number |
| created_at | TIMESTAMP | YES | Creation timestamp |
| updated_at | TIMESTAMP | YES | Last update timestamp |

### Buku Command Table

```sql
CREATE TABLE buku_command (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kode_buku VARCHAR(50) UNIQUE NOT NULL,
    judul VARCHAR(200) NOT NULL,
    pengarang VARCHAR(100),
    penerbit VARCHAR(100),
    tahun_terbit INT,
    jumlah_halaman INT,
    isbn VARCHAR(20) UNIQUE,
    kategori VARCHAR(50),
    stok INT DEFAULT 0,
    tersedia BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT uk_buku_kode UNIQUE (kode_buku),
    CONSTRAINT uk_buku_isbn UNIQUE (isbn),
    CONSTRAINT chk_stok_positive CHECK (stok >= 0)
);

CREATE INDEX idx_buku_judul ON buku_command(judul);
CREATE INDEX idx_buku_kategori ON buku_command(kategori);
CREATE INDEX idx_buku_pengarang ON buku_command(pengarang);
```

### Peminjaman Command Table

```sql
CREATE TABLE peminjaman_command (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    anggota_id VARCHAR(50) NOT NULL,
    buku_id VARCHAR(50) NOT NULL,
    tanggal_pinjam DATE NOT NULL,
    tanggal_kembali DATE NOT NULL,
    tanggal_dikembalikan DATE,
    status VARCHAR(20) DEFAULT 'DIPINJAM',
    denda DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT chk_tanggal_kembali CHECK (tanggal_kembali >= tanggal_pinjam),
    CONSTRAINT chk_status CHECK (status IN ('DIPINJAM', 'DIKEMBALIKAN', 'TERLAMBAT'))
);

CREATE INDEX idx_peminjaman_anggota ON peminjaman_command(anggota_id);
CREATE INDEX idx_peminjaman_buku ON peminjaman_command(buku_id);
CREATE INDEX idx_peminjaman_status ON peminjaman_command(status);
CREATE INDEX idx_peminjaman_tanggal ON peminjaman_command(tanggal_pinjam, tanggal_kembali);
```

### Pengembalian Command Table

```sql
CREATE TABLE pengembalian_command (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    peminjaman_id BIGINT NOT NULL,
    tanggal_dikembalikan DATE NOT NULL,
    terlambat INT DEFAULT 0,
    denda_keterlambatan DECIMAL(10,2) DEFAULT 0.00,
    kondisi_buku VARCHAR(20),
    denda_kondisi DECIMAL(10,2) DEFAULT 0.00,
    total_denda DECIMAL(10,2) DEFAULT 0.00,
    catatan TEXT,
    created_at TIMESTAMP,
    
    CONSTRAINT fk_pengembalian_peminjaman FOREIGN KEY (peminjaman_id) 
        REFERENCES peminjaman_command(id) ON DELETE CASCADE,
    CONSTRAINT chk_kondisi_buku CHECK (kondisi_buku IN ('BAIK', 'RUSAK_RINGAN', 'RUSAK_BERAT', 'HILANG')),
    CONSTRAINT chk_terlambat CHECK (terlambat >= 0)
);

CREATE INDEX idx_pengembalian_peminjaman ON pengembalian_command(peminjaman_id);
CREATE INDEX idx_pengembalian_tanggal ON pengembalian_command(tanggal_dikembalikan);
```

### H2 Configuration

```properties
# Development - In-memory
spring.datasource.url=jdbc:h2:mem:perpustakaan_write_db
spring.jpa.hibernate.ddl-auto=create-drop

# File-based for persistence
spring.datasource.url=jdbc:h2:file:./data/perpustakaan_write_db
spring.jpa.hibernate.ddl-auto=update

# Connection pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=600000
```

---

## Read Model (MongoDB)

### Anggota Query Collection

**Collection**: `anggota_read`

```json
{
  "_id": "507f1f77bcf86cd799439011",
  "nomorAnggota": "A001",
  "nama": "John Doe",
  "alamat": "Jl. Merdeka No. 123",
  "email": "john@example.com",
  "telepon": "081234567890",
  "createdAt": ISODate("2024-01-15T10:30:00Z"),
  "updatedAt": ISODate("2024-01-15T10:30:00Z")
}
```

**Indexes**:
```javascript
db.anggota_read.createIndex({ "nomorAnggota": 1 }, { unique: true });
db.anggota_read.createIndex({ "email": 1 }, { unique: true });
db.anggota_read.createIndex({ "nama": "text" });
db.anggota_read.createIndex({ "createdAt": -1 });
```

### Buku Query Collection

**Collection**: `buku_read`

```json
{
  "_id": "507f1f77bcf86cd799439012",
  "kodeBuku": "BK-001",
  "judul": "Java Programming",
  "pengarang": "John Doe",
  "penerbit": "Erlangga",
  "tahunTerbit": 2020,
  "jumlahHalaman": 450,
  "isbn": "978-3-16-148410-0",
  "kategori": "Programming",
  "stok": 10,
  "tersedia": true,
  "createdAt": ISODate("2024-01-15T10:30:00Z"),
  "updatedAt": ISODate("2024-01-15T10:30:00Z")
}
```

**Indexes**:
```javascript
db.buku_read.createIndex({ "kodeBuku": 1 }, { unique: true });
db.buku_read.createIndex({ "isbn": 1 }, { unique: true, sparse: true });
db.buku_read.createIndex({ "judul": "text", "pengarang": "text" });
db.buku_read.createIndex({ "kategori": 1 });
db.buku_read.createIndex({ "tersedia": 1, "kategori": 1 });
```

### Peminjaman Query Collection

**Collection**: `peminjaman_read`

```json
{
  "_id": "507f1f77bcf86cd799439013",
  "anggotaId": "507f1f77bcf86cd799439011",
  "bukuId": "507f1f77bcf86cd799439012",
  "tanggalPinjam": ISODate("2024-01-15T00:00:00Z"),
  "tanggalKembali": ISODate("2024-01-29T00:00:00Z"),
  "tanggalDikembalikan": null,
  "status": "DIPINJAM",
  "denda": 0.0,
  "anggota": {
    "nomorAnggota": "A001",
    "nama": "John Doe",
    "email": "john@example.com"
  },
  "buku": {
    "kodeBuku": "BK-001",
    "judul": "Java Programming",
    "pengarang": "John Doe"
  },
  "createdAt": ISODate("2024-01-15T10:30:00Z"),
  "updatedAt": ISODate("2024-01-15T10:30:00Z")
}
```

**Indexes**:
```javascript
db.peminjaman_read.createIndex({ "anggotaId": 1 });
db.peminjaman_read.createIndex({ "bukuId": 1 });
db.peminjaman_read.createIndex({ "status": 1 });
db.peminjaman_read.createIndex({ "tanggalPinjam": -1 });
db.peminjaman_read.createIndex({ "tanggalKembali": 1, "status": 1 });
db.peminjaman_read.createIndex({ "anggota.nomorAnggota": 1 });
```

### Pengembalian Query Collection

**Collection**: `pengembalian_read`

```json
{
  "_id": "507f1f77bcf86cd799439014",
  "peminjamanId": "507f1f77bcf86cd799439013",
  "tanggalDikembalikan": ISODate("2024-01-20T14:30:00Z"),
  "terlambat": 5,
  "dendaKeterlambatan": 25000.0,
  "kondisiBuku": "BAIK",
  "dendaKondisi": 0.0,
  "totalDenda": 25000.0,
  "catatan": "Buku dalam kondisi baik",
  "peminjaman": {
    "anggotaId": "507f1f77bcf86cd799439011",
    "bukuId": "507f1f77bcf86cd799439012",
    "tanggalPinjam": ISODate("2024-01-15T00:00:00Z"),
    "tanggalKembali": ISODate("2024-01-29T00:00:00Z")
  },
  "createdAt": ISODate("2024-01-20T14:30:00Z")
}
```

**Indexes**:
```javascript
db.pengembalian_read.createIndex({ "peminjamanId": 1 }, { unique: true });
db.pengembalian_read.createIndex({ "tanggalDikembalikan": -1 });
db.pengembalian_read.createIndex({ "kondisiBuku": 1 });
db.pengembalian_read.createIndex({ "totalDenda": -1 });
```

### MongoDB Configuration

```properties
# Connection URI
spring.data.mongodb.uri=mongodb://localhost:27017/perpustakaan_read_db

# Database name
spring.data.mongodb.database=perpustakaan_read_db

# Auto-create indexes
spring.data.mongodb.auto-index-creation=true

# Connection pool
spring.data.mongodb.max-pool-size=50
spring.data.mongodb.min-pool-size=10
spring.data.mongodb.max-wait-time=5000ms
spring.data.mongodb.max-connection-idle-time=60000ms
spring.data.mongodb.max-connection-life-time=120000ms
```

---

## Data Migration

### Initial Data Setup

**Script**: `scripts/init-data.sh`

```bash
#!/bin/bash

# MongoDB - Create databases and collections
docker exec -it mongodb mongosh <<EOF
use anggota_read_db
db.createCollection("anggota_read")
db.anggota_read.createIndex({ "nomorAnggota": 1 }, { unique: true })
db.anggota_read.createIndex({ "email": 1 }, { unique: true })

use buku_read_db
db.createCollection("buku_read")
db.buku_read.createIndex({ "kodeBuku": 1 }, { unique: true })
db.buku_read.createIndex({ "isbn": 1 }, { unique: true, sparse: true })

use peminjaman_read_db
db.createCollection("peminjaman_read")
db.peminjaman_read.createIndex({ "anggotaId": 1 })
db.peminjaman_read.createIndex({ "bukuId": 1 })
db.peminjaman_read.createIndex({ "status": 1 })

use pengembalian_read_db
db.createCollection("pengembalian_read")
db.pengembalian_read.createIndex({ "peminjamanId": 1 }, { unique: true })
EOF
```

### Sample Data

**Insert sample anggota**:
```javascript
db.anggota_read.insertMany([
  {
    nomorAnggota: "A001",
    nama: "John Doe",
    alamat: "Jl. Merdeka No. 123",
    email: "john@example.com",
    telepon: "081234567890",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    nomorAnggota: "A002",
    nama: "Jane Smith",
    alamat: "Jl. Sudirman No. 456",
    email: "jane@example.com",
    telepon: "081234567891",
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);
```

**Insert sample buku**:
```javascript
db.buku_read.insertMany([
  {
    kodeBuku: "BK-001",
    judul: "Java Programming",
    pengarang: "John Doe",
    penerbit: "Erlangga",
    tahunTerbit: 2020,
    jumlahHalaman: 450,
    isbn: "978-3-16-148410-0",
    kategori: "Programming",
    stok: 10,
    tersedia: true,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    kodeBuku: "BK-002",
    judul: "Spring Boot in Action",
    pengarang: "Craig Walls",
    penerbit: "Manning",
    tahunTerbit: 2021,
    jumlahHalaman: 520,
    isbn: "978-1-617-29242-5",
    kategori: "Programming",
    stok: 5,
    tersedia: true,
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);
```

---

## Backup & Restore

### MongoDB Backup

**Backup all databases**:
```bash
#!/bin/bash
# scripts/backup-mongodb.sh

BACKUP_DIR="./backups/mongodb/$(date +%Y%m%d_%H%M%S)"

docker exec mongodb mongodump \
  --out=/backup \
  --gzip

docker cp mongodb:/backup "$BACKUP_DIR"

echo "Backup completed: $BACKUP_DIR"
```

**Backup specific database**:
```bash
docker exec mongodb mongodump \
  --db anggota_read_db \
  --out=/backup \
  --gzip
```

### MongoDB Restore

**Restore from backup**:
```bash
#!/bin/bash
# scripts/restore-mongodb.sh

BACKUP_DIR=$1

if [ -z "$BACKUP_DIR" ]; then
  echo "Usage: ./restore-mongodb.sh <backup-directory>"
  exit 1
fi

docker cp "$BACKUP_DIR" mongodb:/restore

docker exec mongodb mongorestore \
  /restore \
  --gzip \
  --drop

echo "Restore completed from: $BACKUP_DIR"
```

### H2 Backup

**Export H2 data**:
```sql
-- Connect to H2 console: http://localhost:8081/h2-console

-- Export to SQL script
SCRIPT TO 'backup.sql';

-- Export specific table
SCRIPT TO 'anggota_backup.sql' TABLE anggota_command;
```

**Automated backup script**:
```bash
#!/bin/bash
# scripts/backup-h2.sh

BACKUP_DIR="./backups/h2/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

# If using file-based H2
cp ./data/perpustakaan_write_db.mv.db "$BACKUP_DIR/"

echo "H2 backup completed: $BACKUP_DIR"
```

---

## Indexes

### Performance Indexes

**Anggota indexes for common queries**:
```javascript
// Search by name (text search)
db.anggota_read.createIndex({ "nama": "text" });

// Filter by email
db.anggota_read.createIndex({ "email": 1 });

// Sort by creation date
db.anggota_read.createIndex({ "createdAt": -1 });

// Compound index for search + sort
db.anggota_read.createIndex({ "nama": "text", "createdAt": -1 });
```

**Buku indexes for catalog queries**:
```javascript
// Text search on title and author
db.buku_read.createIndex({ "judul": "text", "pengarang": "text" });

// Filter available books by category
db.buku_read.createIndex({ "tersedia": 1, "kategori": 1 });

// Sort by newest books
db.buku_read.createIndex({ "createdAt": -1 });

// Search by ISBN
db.buku_read.createIndex({ "isbn": 1 }, { unique: true, sparse: true });
```

**Peminjaman indexes for borrowing queries**:
```javascript
// Find by member
db.peminjaman_read.createIndex({ "anggotaId": 1, "status": 1 });

// Find by book
db.peminjaman_read.createIndex({ "bukuId": 1, "status": 1 });

// Find overdue items
db.peminjaman_read.createIndex({ "tanggalKembali": 1, "status": 1 });

// Recent borrowings
db.peminjaman_read.createIndex({ "tanggalPinjam": -1 });
```

### Index Analysis

**Check index usage**:
```javascript
// Explain query execution
db.peminjaman_read.find({ status: "DIPINJAM" }).explain("executionStats");

// List all indexes
db.peminjaman_read.getIndexes();

// Check index size
db.peminjaman_read.stats().indexSizes;
```

**Remove unused indexes**:
```javascript
// Drop specific index
db.peminjaman_read.dropIndex("status_1");

// Drop all indexes except _id
db.peminjaman_read.dropIndexes();
```

---

## Schema Evolution

### Adding New Fields

**MongoDB** (flexible schema):
```javascript
// Just add the field - no migration needed
db.anggota_read.updateMany(
  { fotoProfil: { $exists: false } },
  { $set: { fotoProfil: null } }
);
```

**H2** (requires migration):
```sql
ALTER TABLE anggota_command 
ADD COLUMN foto_profil VARCHAR(255);
```

### Version Management

**Track schema versions**:
```javascript
db.schema_version.insertOne({
  version: "1.0.0",
  description: "Initial schema",
  appliedAt: new Date()
});

db.schema_version.insertOne({
  version: "1.1.0",
  description: "Added telepon field",
  appliedAt: new Date()
});
```

---

[⬅️ Back to Documentation Index](README.md)