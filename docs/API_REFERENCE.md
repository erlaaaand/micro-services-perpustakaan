# 📖 API Reference

Dokumentasi lengkap semua API endpoints dalam sistem microservices perpustakaan.

## 📑 Daftar Isi

- [Base URLs](#base-urls)
- [Service Anggota](#service-anggota)
- [Service Buku](#service-buku)
- [Service Peminjaman](#service-peminjaman)
- [Service Pengembalian](#service-pengembalian)
- [Error Responses](#error-responses)
- [Pagination](#pagination)

---

## Base URLs

### Via API Gateway (Recommended)
```
http://localhost:8080/api/
```

### Direct Service Access
```
Service Anggota:      http://localhost:8081/api/anggota
Service Buku:         http://localhost:8082/api/buku
Service Peminjaman:   http://localhost:8083/api/peminjaman
Service Pengembalian: http://localhost:8084/api/pengembalian
```

---

## Service Anggota

### 1. Create Anggota

**Command Operation** - Writes to H2, publishes event to RabbitMQ

```http
POST /api/anggota
Content-Type: application/json
```

**Request Body:**
```json
{
  "nomorAnggota": "A001",
  "nama": "John Doe",
  "alamat": "Jl. Merdeka No. 123",
  "email": "john@example.com"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "nomorAnggota": "A001",
  "nama": "John Doe",
  "alamat": "Jl. Merdeka No. 123",
  "email": "john@example.com",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Event Published:**
```json
{
  "eventType": "ANGGOTA_CREATED",
  "aggregateId": "1",
  "timestamp": "2024-01-15T10:30:00Z",
  "payload": {
    "nomorAnggota": "A001",
    "nama": "John Doe",
    "alamat": "Jl. Merdeka No. 123",
    "email": "john@example.com"
  }
}
```

### 2. Get All Anggota

**Query Operation** - Reads from MongoDB

```http
GET /api/anggota?page=0&size=10&sortBy=nama&direction=ASC
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | integer | No | 0 | Page number (0-indexed) |
| size | integer | No | 10 | Items per page |
| sortBy | string | No | id | Field untuk sorting |
| direction | string | No | ASC | Sort direction (ASC/DESC) |

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": "uuid-1",
      "nomorAnggota": "A001",
      "nama": "John Doe",
      "alamat": "Jl. Merdeka No. 123",
      "email": "john@example.com",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    },
    {
      "id": "uuid-2",
      "nomorAnggota": "A002",
      "nama": "Jane Smith",
      "alamat": "Jl. Sudirman No. 456",
      "email": "jane@example.com",
      "createdAt": "2024-01-15T11:00:00Z",
      "updatedAt": "2024-01-15T11:00:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 2,
  "size": 10,
  "number": 0,
  "empty": false
}
```

### 3. Get Anggota by ID

**Query Operation** - Reads from MongoDB

```http
GET /api/anggota/{id}
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | string | UUID anggota |

**Response:** `200 OK`
```json
{
  "id": "uuid-1",
  "nomorAnggota": "A001",
  "nama": "John Doe",
  "alamat": "Jl. Merdeka No. 123",
  "email": "john@example.com",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

### 4. Search Anggota by Name

**Query Operation** - Reads from MongoDB

```http
GET /api/anggota/search?nama=John
```

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| nama | string | Yes | Search term (case-insensitive) |

**Response:** `200 OK`
```json
[
  {
    "id": "uuid-1",
    "nomorAnggota": "A001",
    "nama": "John Doe",
    "alamat": "Jl. Merdeka No. 123",
    "email": "john@example.com",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
]
```

### 5. Update Anggota

**Command Operation** - Updates H2, publishes event

```http
PUT /api/anggota/{id}
Content-Type: application/json
```

**Request Body:**
```json
{
  "nomorAnggota": "A001",
  "nama": "John Doe Updated",
  "alamat": "Jl. Updated No. 456",
  "email": "john.updated@example.com"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "nomorAnggota": "A001",
  "nama": "John Doe Updated",
  "alamat": "Jl. Updated No. 456",
  "email": "john.updated@example.com",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T12:00:00Z"
}
```

**Event Published:** `ANGGOTA_UPDATED`

### 6. Delete Anggota

**Command Operation** - Deletes from H2, publishes event

```http
DELETE /api/anggota/{id}
```

**Response:** `204 No Content`

**Event Published:** `ANGGOTA_DELETED`

---

## Service Buku

### 1. Create Buku

**Command Operation**

```http
POST /api/buku
Content-Type: application/json
```

**Request Body:**
```json
{
  "kodeBuku": "BK-001",
  "judul": "Java Programming",
  "pengarang": "John Doe",
  "penerbit": "Erlangga",
  "tahunTerbit": 2020,
  "jumlahHalaman": 450,
  "isbn": "978-3-16-148410-0",
  "kategori": "Programming",
  "stok": 10
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
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
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

### 2. Get All Buku

**Query Operation**

```http
GET /api/buku?page=0&size=10&sortBy=judul&direction=ASC
```

**Response:** Similar pagination structure as Anggota

### 3. Get Buku by ID

```http
GET /api/buku/{id}
```

**Response:** `200 OK`
```json
{
  "id": "uuid-1",
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
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

### 4. Search Buku by Title

```http
GET /api/buku/search?judul=Java
```

### 5. Get Buku by Category

```http
GET /api/buku/kategori/{kategori}
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| kategori | string | Kategori buku (Programming, Novel, History, etc.) |

### 6. Update Buku

```http
PUT /api/buku/{id}
Content-Type: application/json
```

### 7. Delete Buku

```http
DELETE /api/buku/{id}
```

**Response:** `204 No Content`

### 8. Update Stok

```http
PATCH /api/buku/{id}/stok
Content-Type: application/json
```

**Request Body:**
```json
{
  "jumlah": 5,
  "tipe": "TAMBAH"  // atau "KURANG"
}
```

---

## Service Peminjaman

### 1. Create Peminjaman

**Command Operation** - Calls service-anggota and service-buku untuk validasi

```http
POST /api/peminjaman
Content-Type: application/json
```

**Request Body:**
```json
{
  "anggotaId": "uuid-anggota",
  "bukuId": "uuid-buku",
  "tanggalPinjam": "2024-01-15",
  "tanggalKembali": "2024-01-29",
  "status": "DIPINJAM"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "anggotaId": "uuid-anggota",
  "bukuId": "uuid-buku",
  "tanggalPinjam": "2024-01-15",
  "tanggalKembali": "2024-01-29",
  "tanggalDikembalikan": null,
  "status": "DIPINJAM",
  "denda": 0.0,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Status Values:**
- `DIPINJAM` - Buku sedang dipinjam
- `DIKEMBALIKAN` - Buku sudah dikembalikan
- `TERLAMBAT` - Peminjaman terlambat

### 2. Get Peminjaman with Details

**Query Operation** - Aggregates data dari multiple services

```http
GET /api/peminjaman/{id}
```

**Response:** `200 OK`
```json
{
  "peminjaman": {
    "id": "uuid-1",
    "anggotaId": "uuid-anggota",
    "bukuId": "uuid-buku",
    "tanggalPinjam": "2024-01-15",
    "tanggalKembali": "2024-01-29",
    "status": "DIPINJAM"
  },
  "anggota": {
    "id": "uuid-anggota",
    "nomorAnggota": "A001",
    "nama": "John Doe",
    "email": "john@example.com"
  },
  "buku": {
    "id": "uuid-buku",
    "kodeBuku": "BK-001",
    "judul": "Java Programming",
    "pengarang": "John Doe",
    "tersedia": false
  }
}
```

### 3. Get All Peminjaman

```http
GET /api/peminjaman?page=0&size=10&status=DIPINJAM
```

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| status | string | No | Filter by status |
| anggotaId | string | No | Filter by anggota |
| bukuId | string | No | Filter by buku |

### 4. Get Peminjaman by Anggota

```http
GET /api/peminjaman/anggota/{anggotaId}
```

### 5. Get Active Peminjaman

```http
GET /api/peminjaman/active
```

Returns all peminjaman dengan status `DIPINJAM`

### 6. Get Overdue Peminjaman

```http
GET /api/peminjaman/overdue
```

Returns peminjaman yang melewati tanggal kembali

### 7. Update Peminjaman Status

```http
PATCH /api/peminjaman/{id}/status
Content-Type: application/json
```

**Request Body:**
```json
{
  "status": "TERLAMBAT"
}
```

---

## Service Pengembalian

### 1. Create Pengembalian

**Command Operation** - Calls service-peminjaman untuk validasi

```http
POST /api/pengembalian
Content-Type: application/json
```

**Request Body:**
```json
{
  "peminjamanId": "uuid-peminjaman",
  "tanggalDikembalikan": "2024-01-20",
  "terlambat": 5,
  "denda": 25000.0,
  "kondisiBuku": "BAIK",
  "catatan": "Buku dalam kondisi baik"
}
```

**Kondisi Buku:**
- `BAIK` - Tidak ada kerusakan
- `RUSAK_RINGAN` - Ada kerusakan ringan
- `RUSAK_BERAT` - Kerusakan berat
- `HILANG` - Buku hilang

**Response:** `201 Created`
```json
{
  "id": 1,
  "peminjamanId": "uuid-peminjaman",
  "tanggalDikembalikan": "2024-01-20",
  "terlambat": 5,
  "denda": 25000.0,
  "dendaKondisi": 0.0,
  "totalDenda": 25000.0,
  "kondisiBuku": "BAIK",
  "catatan": "Buku dalam kondisi baik",
  "createdAt": "2024-01-20T14:30:00Z"
}
```

**Calculation:**
- Denda keterlambatan: 5000/hari
- Denda rusak ringan: 50000
- Denda rusak berat: 150000
- Denda hilang: Harga buku

### 2. Get Pengembalian by ID

```http
GET /api/pengembalian/{id}
```

### 3. Get All Pengembalian

```http
GET /api/pengembalian?page=0&size=10
```

### 4. Get Pengembalian by Peminjaman

```http
GET /api/pengembalian/peminjaman/{peminjamanId}
```

### 5. Calculate Denda

**Utility endpoint untuk menghitung denda sebelum create**

```http
POST /api/pengembalian/calculate-denda
Content-Type: application/json
```

**Request Body:**
```json
{
  "tanggalPinjam": "2024-01-01",
  "tanggalKembali": "2024-01-15",
  "tanggalDikembalikan": "2024-01-20",
  "kondisiBuku": "BAIK"
}
```

**Response:** `200 OK`
```json
{
  "terlambat": 5,
  "dendaKeterlambatan": 25000.0,
  "dendaKondisi": 0.0,
  "totalDenda": 25000.0
}
```

---

## Error Responses

### Standard Error Response Format

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/anggota",
  "errors": [
    {
      "field": "email",
      "message": "Email tidak valid"
    },
    {
      "field": "nomorAnggota",
      "message": "Nomor anggota sudah terdaftar"
    }
  ]
}
```

### HTTP Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 204 | No Content | Resource deleted successfully |
| 400 | Bad Request | Invalid request data |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource conflict (duplicate) |
| 500 | Internal Server Error | Server error |
| 503 | Service Unavailable | Service down (circuit breaker open) |

### Common Error Scenarios

**1. Validation Error**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Email format is invalid"
}
```

**2. Not Found**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Anggota with id 'uuid-123' not found"
}
```

**3. Duplicate Resource**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Nomor anggota 'A001' already exists"
}
```

**4. Circuit Breaker Open**
```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "Service anggota is currently unavailable. Please try again later."
}
```

---

## Pagination

### Request Parameters

```http
GET /api/anggota?page=0&size=10&sortBy=nama&direction=ASC
```

### Response Structure

```json
{
  "content": [...],           // Array of items
  "pageable": {
    "pageNumber": 0,          // Current page (0-indexed)
    "pageSize": 10,           // Items per page
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 100,       // Total items across all pages
  "totalPages": 10,           // Total number of pages
  "last": false,              // Is this the last page?
  "first": true,              // Is this the first page?
  "numberOfElements": 10,     // Items in current page
  "size": 10,                 // Requested page size
  "number": 0,                // Current page number
  "empty": false              // Is page empty?
}
```

### Navigation

```javascript
// First page
GET /api/anggota?page=0&size=10

// Next page
GET /api/anggota?page=1&size=10

// Last page (if totalPages = 10)
GET /api/anggota?page=9&size=10

// Sorted by name descending
GET /api/anggota?page=0&size=10&sortBy=nama&direction=DESC
```

---

## Testing with cURL

### Create Anggota
```bash
curl -X POST http://localhost:8080/api/anggota \
  -H "Content-Type: application/json" \
  -d '{
    "nomorAnggota": "A001",
    "nama": "John Doe",
    "alamat": "Jl. Merdeka No. 123",
    "email": "john@example.com"
  }'
```

### Get All Anggota
```bash
curl http://localhost:8080/api/anggota?page=0&size=10
```

### Create Peminjaman
```bash
curl -X POST http://localhost:8080/api/peminjaman \
  -H "Content-Type: application/json" \
  -d '{
    "anggotaId": "uuid-anggota",
    "bukuId": "uuid-buku",
    "tanggalPinjam": "2024-01-15",
    "tanggalKembali": "2024-01-29",
    "status": "DIPINJAM"
  }'
```

---

## Postman Collection

Import collection dari: `postman/perpustakaan-api.json`

**Environment Variables:**
```json
{
  "gateway_url": "http://localhost:8080",
  "anggota_url": "http://localhost:8081",
  "buku_url": "http://localhost:8082",
  "peminjaman_url": "http://localhost:8083",
  "pengembalian_url": "http://localhost:8084"
}
```

---

[⬅️ Back to Main Documentation](../README.md)