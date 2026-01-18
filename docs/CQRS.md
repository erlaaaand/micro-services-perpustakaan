# 🔄 CQRS Implementation Guide

Dokumentasi lengkap implementasi Command Query Responsibility Segregation (CQRS) pattern dalam sistem microservices perpustakaan.

## 📑 Daftar Isi

- [Konsep CQRS](#konsep-cqrs)
- [Implementasi Write Model](#implementasi-write-model)
- [Implementasi Read Model](#implementasi-read-model)
- [Event Synchronization](#event-synchronization)
- [Best Practices](#best-practices)
- [Common Patterns](#common-patterns)

---

## Konsep CQRS

### Apa itu CQRS?

CQRS adalah pattern yang memisahkan operasi **Command** (Write) dan **Query** (Read) menggunakan model data yang berbeda.

```
Traditional Approach (CRUD):
┌─────────────────────┐
│   Single Model      │
│  ┌──────────────┐  │
│  │   Service    │  │
│  └──────┬───────┘  │
│         │          │
│  ┌──────▼───────┐  │
│  │   Database   │  │
│  └──────────────┘  │
└─────────────────────┘

CQRS Approach:
┌──────────────────────────────────────────┐
│           Command Side (Write)           │
│  ┌────────────────┐   ┌──────────────┐  │
│  │ Command Model  │──▶│ Write DB (H2)│  │
│  └────────────────┘   └──────────────┘  │
└──────────────┬───────────────────────────┘
               │ Events
               ▼
┌──────────────────────────────────────────┐
│            Query Side (Read)             │
│  ┌────────────────┐   ┌──────────────┐  │
│  │  Query Model   │◀──│Read DB (Mongo)│ │
│  └────────────────┘   └──────────────┘  │
└──────────────────────────────────────────┘
```

### Keuntungan CQRS

#### 1. **Performance Optimization**
- Write model dioptimasi untuk transactional consistency
- Read model dioptimasi untuk query performance
- Independent scaling untuk read dan write operations

#### 2. **Flexibility**
- Berbeda data model untuk read dan write
- Multiple read models untuk different use cases
- Easy to add new queries tanpa impact write side

#### 3. **Scalability**
- Scale read dan write independently
- Read replicas untuk high traffic queries
- Write sharding untuk high volume writes

#### 4. **Separation of Concerns**
- Business logic terpisah dari query logic
- Clear boundary antara command dan query
- Easier testing dan maintenance

#### 5. **Event Sourcing Ready**
- Natural fit dengan event-driven architecture
- Complete audit trail dari events
- Event replay untuk debugging

### Kapan Menggunakan CQRS?

✅ **Gunakan CQRS ketika**:
- Read/write ratio sangat berbeda (e.g., 10:1)
- Complex domain logic pada write operations
- Need different data models untuk read/write
- High scalability requirements
- Event-driven architecture

❌ **Jangan gunakan CQRS ketika**:
- Simple CRUD operations
- Low traffic application
- Team belum familiar dengan pattern
- Tidak ada clear benefit dari complexity

---

## Implementasi Write Model

### 1. Command Entity

Write model menggunakan JPA entities dengan H2 database:

```java
package com.perpustakaan.anggota.entity.command;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "anggota_command")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnggotaCommand {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String nomorAnggota;
    
    @Column(nullable = false, length = 100)
    private String nama;
    
    @Column(length = 255)
    private String alamat;
    
    @Column(length = 100)
    private String email;
    
    @Column(length = 15)
    private String telepon;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 2. Command Repository

```java
package com.perpustakaan.anggota.repository.command;

import com.perpustakaan.anggota.entity.command.AnggotaCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AnggotaCommandRepository extends JpaRepository<AnggotaCommand, Long> {
    
    Optional<AnggotaCommand> findByNomorAnggota(String nomorAnggota);
    
    boolean existsByNomorAnggota(String nomorAnggota);
    
    boolean existsByEmail(String email);
}
```

### 3. Command DTOs

```java
package com.perpustakaan.anggota.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateAnggotaCommand {
    
    @NotBlank(message = "Nomor anggota harus diisi")
    @Pattern(regexp = "^A\\d{3,}$", message = "Format nomor anggota: A001, A002, dst")
    private String nomorAnggota;
    
    @NotBlank(message = "Nama harus diisi")
    private String nama;
    
    private String alamat;
    
    @Email(message = "Format email tidak valid")
    private String email;
    
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Format telepon tidak valid (10-15 digit)")
    private String telepon;
}

@Data
public class UpdateAnggotaCommand {
    
    @NotBlank(message = "Nama harus diisi")
    private String nama;
    
    private String alamat;
    
    @Email(message = "Format email tidak valid")
    private String email;
    
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Format telepon tidak valid")
    private String telepon;
}
```

### 4. Command Service

```java
package com.perpustakaan.anggota.service;

import com.perpustakaan.anggota.dto.*;
import com.perpustakaan.anggota.entity.command.AnggotaCommand;
import com.perpustakaan.anggota.event.*;
import com.perpustakaan.anggota.exception.*;
import com.perpustakaan.anggota.repository.command.AnggotaCommandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnggotaCommandService {
    
    private final AnggotaCommandRepository commandRepository;
    private final RabbitTemplate rabbitTemplate;
    
    @Transactional
    public AnggotaCommand createAnggota(CreateAnggotaCommand command) {
        log.info("Creating anggota with nomor: {}", command.getNomorAnggota());
        
        // Validasi duplikasi
        if (commandRepository.existsByNomorAnggota(command.getNomorAnggota())) {
            throw new DuplicateResourceException(
                "Nomor anggota " + command.getNomorAnggota() + " sudah terdaftar"
            );
        }
        
        if (commandRepository.existsByEmail(command.getEmail())) {
            throw new DuplicateResourceException(
                "Email " + command.getEmail() + " sudah terdaftar"
            );
        }
        
        // Create entity
        AnggotaCommand anggota = new AnggotaCommand();
        anggota.setNomorAnggota(command.getNomorAnggota());
        anggota.setNama(command.getNama());
        anggota.setAlamat(command.getAlamat());
        anggota.setEmail(command.getEmail());
        anggota.setTelepon(command.getTelepon());
        
        // Save to write database
        AnggotaCommand saved = commandRepository.save(anggota);
        log.info("Anggota saved to write database with ID: {}", saved.getId());
        
        // Publish event
        publishAnggotaCreatedEvent(saved);
        
        return saved;
    }
    
    @Transactional
    public AnggotaCommand updateAnggota(Long id, UpdateAnggotaCommand command) {
        log.info("Updating anggota with ID: {}", id);
        
        AnggotaCommand anggota = commandRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Anggota dengan ID " + id + " tidak ditemukan"));
        
        // Update fields
        anggota.setNama(command.getNama());
        anggota.setAlamat(command.getAlamat());
        anggota.setEmail(command.getEmail());
        anggota.setTelepon(command.getTelepon());
        
        // Save changes
        AnggotaCommand updated = commandRepository.save(anggota);
        log.info("Anggota updated in write database");
        
        // Publish event
        publishAnggotaUpdatedEvent(updated);
        
        return updated;
    }
    
    @Transactional
    public void deleteAnggota(Long id) {
        log.info("Deleting anggota with ID: {}", id);
        
        AnggotaCommand anggota = commandRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Anggota dengan ID " + id + " tidak ditemukan"));
        
        // Delete from write database
        commandRepository.delete(anggota);
        log.info("Anggota deleted from write database");
        
        // Publish event
        publishAnggotaDeletedEvent(anggota);
    }
    
    private void publishAnggotaCreatedEvent(AnggotaCommand anggota) {
        AnggotaCreatedEvent event = new AnggotaCreatedEvent(
            String.valueOf(anggota.getId()),
            anggota.getNomorAnggota(),
            anggota.getNama(),
            anggota.getAlamat(),
            anggota.getEmail(),
            anggota.getTelepon(),
            anggota.getCreatedAt()
        );
        
        log.debug("Publishing AnggotaCreatedEvent: {}", event);
        rabbitTemplate.convertAndSend("anggota-exchange", "anggota.created", event);
        log.info("AnggotaCreatedEvent published successfully");
    }
    
    private void publishAnggotaUpdatedEvent(AnggotaCommand anggota) {
        AnggotaUpdatedEvent event = new AnggotaUpdatedEvent(
            String.valueOf(anggota.getId()),
            anggota.getNomorAnggota(),
            anggota.getNama(),
            anggota.getAlamat(),
            anggota.getEmail(),
            anggota.getTelepon(),
            anggota.getUpdatedAt()
        );
        
        log.debug("Publishing AnggotaUpdatedEvent: {}", event);
        rabbitTemplate.convertAndSend("anggota-exchange", "anggota.updated", event);
        log.info("AnggotaUpdatedEvent published successfully");
    }
    
    private void publishAnggotaDeletedEvent(AnggotaCommand anggota) {
        AnggotaDeletedEvent event = new AnggotaDeletedEvent(
            String.valueOf(anggota.getId()),
            anggota.getNomorAnggota()
        );
        
        log.debug("Publishing AnggotaDeletedEvent: {}", event);
        rabbitTemplate.convertAndSend("anggota-exchange", "anggota.deleted", event);
        log.info("AnggotaDeletedEvent published successfully");
    }
}
```

### 5. Command Controller

```java
package com.perpustakaan.anggota.controller;

import com.perpustakaan.anggota.dto.*;
import com.perpustakaan.anggota.entity.command.AnggotaCommand;
import com.perpustakaan.anggota.service.AnggotaCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anggota")
@RequiredArgsConstructor
public class AnggotaCommandController {
    
    private final AnggotaCommandService commandService;
    
    @PostMapping
    public ResponseEntity<AnggotaCommand> createAnggota(@Valid @RequestBody CreateAnggotaCommand command) {
        AnggotaCommand created = commandService.createAnggota(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AnggotaCommand> updateAnggota(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAnggotaCommand command) {
        AnggotaCommand updated = commandService.updateAnggota(id, command);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnggota(@PathVariable Long id) {
        commandService.deleteAnggota(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Implementasi Read Model

### 1. Query Entity

Read model menggunakan MongoDB documents:

```java
package com.perpustakaan.anggota.entity.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "anggota_read")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnggotaQuery {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String nomorAnggota;
    
    private String nama;
    private String alamat;
    private String email;
    private String telepon;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 2. Query Repository

```java
package com.perpustakaan.anggota.repository.query;

import com.perpustakaan.anggota.entity.query.AnggotaQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnggotaQueryRepository extends MongoRepository<AnggotaQuery, String> {
    
    Optional<AnggotaQuery> findByNomorAnggota(String nomorAnggota);
    
    List<AnggotaQuery> findByNamaContainingIgnoreCase(String nama);
    
    Page<AnggotaQuery> findByNamaContainingIgnoreCase(String nama, Pageable pageable);
    
    List<AnggotaQuery> findByEmailContainingIgnoreCase(String email);
}
```

### 3. Query Service

```java
package com.perpustakaan.anggota.service;

import com.perpustakaan.anggota.entity.query.AnggotaQuery;
import com.perpustakaan.anggota.exception.ResourceNotFoundException;
import com.perpustakaan.anggota.repository.query.AnggotaQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnggotaQueryService {
    
    private final AnggotaQueryRepository queryRepository;
    
    public Page<AnggotaQuery> getAllAnggota(Pageable pageable) {
        log.debug("Getting all anggota with pagination: {}", pageable);
        return queryRepository.findAll(pageable);
    }
    
    public AnggotaQuery getAnggotaById(String id) {
        log.debug("Getting anggota by ID: {}", id);
        return queryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Anggota dengan ID " + id + " tidak ditemukan"));
    }
    
    public AnggotaQuery getAnggotaByNomor(String nomorAnggota) {
        log.debug("Getting anggota by nomor: {}", nomorAnggota);
        return queryRepository.findByNomorAnggota(nomorAnggota)
            .orElseThrow(() -> new ResourceNotFoundException("Anggota dengan nomor " + nomorAnggota + " tidak ditemukan"));
    }
    
    public List<AnggotaQuery> searchByNama(String nama) {
        log.debug("Searching anggota by nama: {}", nama);
        return queryRepository.findByNamaContainingIgnoreCase(nama);
    }
    
    public Page<AnggotaQuery> searchByNama(String nama, Pageable pageable) {
        log.debug("Searching anggota by nama with pagination: {}", nama);
        return queryRepository.findByNamaContainingIgnoreCase(nama, pageable);
    }
}
```

### 4. Query Controller

```java
package com.perpustakaan.anggota.controller;

import com.perpustakaan.anggota.entity.query.AnggotaQuery;
import com.perpustakaan.anggota.service.AnggotaQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/anggota")
@RequiredArgsConstructor
public class AnggotaQueryController {
    
    private final AnggotaQueryService queryService;
    
    @GetMapping
    public ResponseEntity<Page<AnggotaQuery>> getAllAnggota(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nama") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<AnggotaQuery> result = queryService.getAllAnggota(pageable);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AnggotaQuery> getAnggotaById(@PathVariable String id) {
        AnggotaQuery anggota = queryService.getAnggotaById(id);
        return ResponseEntity.ok(anggota);
    }
    
    @GetMapping("/nomor/{nomorAnggota}")
    public ResponseEntity<AnggotaQuery> getAnggotaByNomor(@PathVariable String nomorAnggota) {
        AnggotaQuery anggota = queryService.getAnggotaByNomor(nomorAnggota);
        return ResponseEntity.ok(anggota);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<AnggotaQuery>> searchByNama(@RequestParam String nama) {
        List<AnggotaQuery> results = queryService.searchByNama(nama);
        return ResponseEntity.ok(results);
    }
}
```

---

## Event Synchronization

### 1. Event Models

```java
package com.perpustakaan.anggota.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnggotaCreatedEvent implements Serializable {
    private String aggregateId;
    private String nomorAnggota;
    private String nama;
    private String alamat;
    private String email;
    private String telepon;
    private LocalDateTime timestamp;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnggotaUpdatedEvent implements Serializable {
    private String aggregateId;
    private String nomorAnggota;
    private String nama;
    private String alamat;
    private String email;
    private String telepon;
    private LocalDateTime timestamp;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnggotaDeletedEvent implements Serializable {
    private String aggregateId;
    private String nomorAnggota;
}
```

### 2. Event Listener

```java
package com.perpustakaan.anggota.event;

import com.perpustakaan.anggota.entity.query.AnggotaQuery;
import com.perpustakaan.anggota.repository.query.AnggotaQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnggotaEventListener {
    
    private final AnggotaQueryRepository queryRepository;
    
    @RabbitListener(queues = "anggota-sync-queue")
    public void handleAnggotaCreatedEvent(AnggotaCreatedEvent event) {
        log.info("Received AnggotaCreatedEvent for aggregate ID: {}", event.getAggregateId());
        
        try {
            AnggotaQuery query = new AnggotaQuery();
            query.setId(event.getAggregateId());
            query.setNomorAnggota(event.getNomorAnggota());
            query.setNama(event.getNama());
            query.setAlamat(event.getAlamat());
            query.setEmail(event.getEmail());
            query.setTelepon(event.getTelepon());
            query.setCreatedAt(event.getTimestamp());
            query.setUpdatedAt(event.getTimestamp());
            
            queryRepository.save(query);
            log.info("Successfully synced AnggotaCreatedEvent to read database");
            
        } catch (Exception e) {
            log.error("Failed to sync AnggotaCreatedEvent: {}", event.getAggregateId(), e);
            throw e; // Let RabbitMQ handle retry
        }
    }
    
    @RabbitListener(queues = "anggota-sync-queue")
    public void handleAnggotaUpdatedEvent(AnggotaUpdatedEvent event) {
        log.info("Received AnggotaUpdatedEvent for aggregate ID: {}", event.getAggregateId());
        
        try {
            AnggotaQuery query = queryRepository.findById(event.getAggregateId())
                .orElse(new AnggotaQuery());
            
            query.setId(event.getAggregateId());
            query.setNomorAnggota(event.getNomorAnggota());
            query.setNama(event.getNama());
            query.setAlamat(event.getAlamat());
            query.setEmail(event.getEmail());
            query.setTelepon(event.getTelepon());
            query.setUpdatedAt(event.getTimestamp());
            
            queryRepository.save(query);
            log.info("Successfully synced AnggotaUpdatedEvent to read database");
            
        } catch (Exception e) {
            log.error("Failed to sync AnggotaUpdatedEvent: {}", event.getAggregateId(), e);
            throw e;
        }
    }
    
    @RabbitListener(queues = "anggota-sync-queue")
    public void handleAnggotaDeletedEvent(AnggotaDeletedEvent event) {
        log.info("Received AnggotaDeletedEvent for aggregate ID: {}", event.getAggregateId());
        
        try {
            queryRepository.deleteById(event.getAggregateId());
            log.info("Successfully deleted anggota from read database");
            
        } catch (Exception e) {
            log.error("Failed to sync AnggotaDeletedEvent: {}", event.getAggregateId(), e);
            throw e;
        }
    }
}
```

---

## Best Practices

### 1. Command Validation

```java
@Service
public class AnggotaCommandService {
    
    @Transactional
    public AnggotaCommand createAnggota(CreateAnggotaCommand command) {
        // Business rule validation
        validateBusinessRules(command);
        
        // Duplicate check
        if (commandRepository.existsByNomorAnggota(command.getNomorAnggota())) {
            throw new BusinessRuleViolationException("Nomor anggota sudah terdaftar");
        }
        
        // Create and save
        // ...
    }
    
    private void validateBusinessRules(CreateAnggotaCommand command) {
        // Business logic validation
        if (command.getNama().length() < 3) {
            throw new BusinessRuleViolationException("Nama minimal 3 karakter");
        }
        
        // Additional validations...
    }
}
```

### 2. Event Idempotency

```java
@Component
public class AnggotaEventListener {
    
    @RabbitListener(queues = "anggota-sync-queue")
    public void handleAnggotaCreatedEvent(AnggotaCreatedEvent event) {
        // Idempotent handling
        if (queryRepository.existsById(event.getAggregateId())) {
            log.warn("Anggota with ID {} already exists, skipping", event.getAggregateId());
            return;
        }
        
        // Process event...
    }
}
```

### 3. Error Handling

```java
@Component
public class AnggotaEventListener {
    
    private static final int MAX_RETRIES = 3;
    
    @RabbitListener(queues = "anggota-sync-queue")
    public void handleEvent(AnggotaCreatedEvent event) {
        try {
            processEvent(event);
        } catch (TransientException e) {
            // Retryable error - let RabbitMQ retry
            log.warn("Transient error, will retry: {}", e.getMessage());
            throw e;
        } catch (PermanentException e) {
            // Permanent error - log and move to DLQ
            log.error("Permanent error, moving to DLQ: {}", e.getMessage());
            // Don't rethrow - message will be acked
        }
    }
}
```

### 4. Performance Optimization

```java
@Service
public class AnggotaQueryService {
    
    // Cache frequently accessed data
    @Cacheable("anggota")
    public AnggotaQuery getAnggotaById(String id) {
        return queryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not found"));
    }
    
    // Projection for lightweight queries
    public List<AnggotaNameProjection> getAllNames() {
        return queryRepository.findAllProjectedBy();
    }
}

interface AnggotaNameProjection {
    String getId();
    String getNama();
}
```

---

## Common Patterns

### 1. Command Pattern

```java
public interface Command {
    String getAggregateId();
}

public class CreateAnggotaCommand implements Command {
    private String aggregateId;
    // ...
}

public interface CommandHandler<C extends Command, R> {
    R handle(C command);
}

@Service
public class CreateAnggotaCommandHandler 
    implements CommandHandler<CreateAnggotaCommand, AnggotaCommand> {
    
    @Override
    public AnggotaCommand handle(CreateAnggotaCommand command) {
        // Handle command
        return null;
    }
}
```

### 2. Query Pattern

```java
public interface Query<R> {
    // Marker interface
}

public class GetAnggotaByIdQuery implements Query<AnggotaQuery> {
    private final String id;
    // ...
}

public interface QueryHandler<Q extends Query<R>, R> {
    R handle(Q query);
}

@Service
public class GetAnggotaByIdQueryHandler 
    implements QueryHandler<GetAnggotaByIdQuery, AnggotaQuery> {
    
    @Override
    public AnggotaQuery handle(GetAnggotaByIdQuery query) {
        // Handle query
        return null;
    }
}
```

###