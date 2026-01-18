# 💻 Development Guide

Panduan lengkap untuk development lokal sistem microservices perpustakaan.

## 📑 Daftar Isi

- [Development Environment Setup](#development-environment-setup)
- [Running Services Locally](#running-services-locally)
- [Development Workflow](#development-workflow)
- [Testing](#testing)
- [Debugging](#debugging)
- [Code Style & Standards](#code-style--standards)
- [Git Workflow](#git-workflow)
- [Common Development Tasks](#common-development-tasks)

---

## Development Environment Setup

### Prerequisites

Pastikan sudah terinstall:

```bash
# Java 17
java -version
# Output: openjdk version "17.0.x"

# Maven 3.9+
mvn -version
# Output: Apache Maven 3.9.x

# Docker & Docker Compose
docker --version
docker-compose --version

# Git
git --version

# IDE (pilih salah satu)
# - IntelliJ IDEA (recommended)
# - Eclipse
# - VS Code dengan Java Extension Pack
```

### IDE Setup

#### IntelliJ IDEA (Recommended)

**Install Plugins**:
1. Go to **Settings** → **Plugins**
2. Install:
   - ✅ **Spring Boot**
   - ✅ **Lombok**
   - ✅ **Docker**
   - ✅ **MongoDB**
   - ✅ **Maven Helper**

**Import Project**:
1. **File** → **Open**
2. Select root folder `perpustakaan-microservices`
3. Wait for Maven import to complete
4. Enable annotation processing:
   - **Settings** → **Build, Execution, Deployment** → **Compiler** → **Annotation Processors**
   - ✅ Enable annotation processing

**Configure Lombok**:
1. Lombok plugin akan auto-detect
2. Restart IDE jika perlu

#### VS Code

**Install Extensions**:
- Extension Pack for Java
- Spring Boot Extension Pack
- Lombok Annotations Support
- Docker
- MongoDB for VS Code

**Settings**:
```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic",
  "spring-boot.ls.java.home": "/path/to/java-17"
}
```

### Clone Repository

```bash
# Clone repository
git clone https://github.com/erlaaaand/micro-services-perpustakaan.git
cd perpustakaan-microservices

# Create development branch
git checkout -b development
```

### Environment Variables

Create `.env` file di root project:

```bash
cp .env.example .env
```

Edit `.env`:
```properties
# Eureka Server
EUREKA_SERVER_URL=http://localhost:8761/eureka/

# RabbitMQ Configuration
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# MongoDB Configuration
MONGODB_URI_ANGGOTA=mongodb://localhost:27017/anggota_read_db
MONGODB_URI_BUKU=mongodb://localhost:27017/buku_read_db
MONGODB_URI_PEMINJAMAN=mongodb://localhost:27017/peminjaman_read_db
MONGODB_URI_PENGEMBALIAN=mongodb://localhost:27017/pengembalian_read_db

# Zipkin Tracing
ZIPKIN_ENDPOINT=http://localhost:9411/api/v2/spans

# Logstash
LOGSTASH_HOST=localhost
LOGSTASH_PORT=5000
```

---

## Running Services Locally

### Option 1: Full Docker Stack (Recommended for Testing)

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Option 2: Hybrid (Infrastructure + Local Services)

**Step 1: Start Infrastructure Services Only**

```bash
# Start MongoDB, RabbitMQ, Monitoring
docker-compose up -d mongodb rabbitmq prometheus grafana zipkin elasticsearch logstash kibana
```

**Step 2: Run Services Locally**

Terminal 1 - Eureka Server:
```bash
cd eureka-server
mvn spring-boot:run
```

Terminal 2 - API Gateway:
```bash
cd api-gateway
mvn spring-boot:run
```

Terminal 3 - Service Anggota:
```bash
cd service-anggota
mvn spring-boot:run
```

Terminal 4 - Service Buku:
```bash
cd service-buku
mvn spring-boot:run
```

Terminal 5 - Service Peminjaman:
```bash
cd service-peminjaman
mvn spring-boot:run
```

Terminal 6 - Service Pengembalian:
```bash
cd service-pengembalian
mvn spring-boot:run
```

### Option 3: Single Service Development

Untuk development fokus pada satu service:

```bash
# Start infrastructure
docker-compose up -d mongodb rabbitmq eureka-server

# Run your service
cd service-anggota
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Verify Services Running

```bash
# Check Eureka Dashboard
open http://localhost:8761

# Check service health
curl http://localhost:8081/actuator/health  # Service Anggota
curl http://localhost:8082/actuator/health  # Service Buku
curl http://localhost:8083/actuator/health  # Service Peminjaman
curl http://localhost:8084/actuator/health  # Service Pengembalian
```

---

## Development Workflow

### 1. Create Feature Branch

```bash
# Update main branch
git checkout main
git pull origin main

# Create feature branch
git checkout -b feature/add-anggota-validation

# Or for bugfix
git checkout -b bugfix/fix-rabbitmq-connection
```

### 2. Make Changes

**Example: Adding New Field to Anggota**

**Step 1: Update Command Entity** (`service-anggota/src/main/java/com/perpustakaan/anggota/entity/command/AnggotaCommand.java`):
```java
@Entity
@Table(name = "anggota_command")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnggotaCommand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nomorAnggota;
    private String nama;
    private String alamat;
    private String email;
    
    // NEW FIELD
    @Column(length = 15)
    private String telepon;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Step 2: Update Query Entity** (`entity/query/AnggotaQuery.java`):
```java
@Document(collection = "anggota_read")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnggotaQuery {
    @Id
    private String id;
    private String nomorAnggota;
    private String nama;
    private String alamat;
    private String email;
    private String telepon; // NEW FIELD
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Step 3: Update DTOs**:
```java
@Data
public class CreateAnggotaCommand {
    @NotBlank(message = "Nomor anggota harus diisi")
    private String nomorAnggota;
    
    @NotBlank(message = "Nama harus diisi")
    private String nama;
    
    private String alamat;
    
    @Email(message = "Format email tidak valid")
    private String email;
    
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Format telepon tidak valid")
    private String telepon; // NEW FIELD with validation
}
```

**Step 4: Update Service Logic**:
```java
@Service
public class AnggotaCommandService {
    
    public AnggotaCommand createAnggota(CreateAnggotaCommand command) {
        AnggotaCommand anggota = new AnggotaCommand();
        anggota.setNomorAnggota(command.getNomorAnggota());
        anggota.setNama(command.getNama());
        anggota.setAlamat(command.getAlamat());
        anggota.setEmail(command.getEmail());
        anggota.setTelepon(command.getTelepon()); // NEW FIELD
        anggota.setCreatedAt(LocalDateTime.now());
        anggota.setUpdatedAt(LocalDateTime.now());
        
        AnggotaCommand saved = commandRepository.save(anggota);
        
        // Publish event
        publishAnggotaCreatedEvent(saved);
        
        return saved;
    }
}
```

**Step 5: Update Event Listener**:
```java
@Service
public class AnggotaEventListener {
    
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleAnggotaEvent(AnggotaEvent event) {
        if (event.getEventType() == EventType.CREATED) {
            AnggotaQuery query = new AnggotaQuery();
            query.setId(event.getAggregateId());
            query.setNomorAnggota(event.getNomorAnggota());
            query.setNama(event.getNama());
            query.setAlamat(event.getAlamat());
            query.setEmail(event.getEmail());
            query.setTelepon(event.getTelepon()); // NEW FIELD
            query.setCreatedAt(event.getTimestamp());
            query.setUpdatedAt(event.getTimestamp());
            
            queryRepository.save(query);
        }
    }
}
```

### 3. Build and Test

```bash
# Build service
cd service-anggota
mvn clean package

# Run tests
mvn test

# Run with new changes
mvn spring-boot:run
```

### 4. Test Manually

```bash
# Test create dengan field baru
curl -X POST http://localhost:8080/api/anggota \
  -H "Content-Type: application/json" \
  -d '{
    "nomorAnggota": "A001",
    "nama": "John Doe",
    "alamat": "Jl. Merdeka No. 123",
    "email": "john@example.com",
    "telepon": "081234567890"
  }'

# Verify di MongoDB
docker exec -it mongodb mongosh
use anggota_read_db
db.anggota_read.find().pretty()
```

### 5. Commit Changes

```bash
git add .
git commit -m "feat(anggota): add telepon field

- Add telepon to Command and Query entities
- Add validation for telepon format
- Update event listener to sync telepon
- Add tests for telepon validation"

git push origin feature/add-anggota-validation
```

---

## Testing

### Unit Tests

**Example: Testing Command Service**

```java
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class AnggotaCommandServiceTest {
    
    @Mock
    private AnggotaCommandRepository commandRepository;
    
    @Mock
    private RabbitTemplate rabbitTemplate;
    
    @InjectMocks
    private AnggotaCommandService commandService;
    
    @Test
    void testCreateAnggota_Success() {
        // Arrange
        CreateAnggotaCommand command = new CreateAnggotaCommand();
        command.setNomorAnggota("A001");
        command.setNama("John Doe");
        command.setEmail("john@example.com");
        command.setTelepon("081234567890");
        
        AnggotaCommand expected = new AnggotaCommand();
        expected.setId(1L);
        expected.setNomorAnggota("A001");
        expected.setNama("John Doe");
        
        when(commandRepository.save(any(AnggotaCommand.class)))
            .thenReturn(expected);
        
        // Act
        AnggotaCommand result = commandService.createAnggota(command);
        
        // Assert
        assertNotNull(result);
        assertEquals("A001", result.getNomorAnggota());
        assertEquals("John Doe", result.getNama());
        
        verify(commandRepository, times(1)).save(any(AnggotaCommand.class));
        verify(rabbitTemplate, times(1))
            .convertAndSend(anyString(), anyString(), any());
    }
    
    @Test
    void testCreateAnggota_DuplicateNomorAnggota() {
        // Arrange
        CreateAnggotaCommand command = new CreateAnggotaCommand();
        command.setNomorAnggota("A001");
        
        when(commandRepository.existsByNomorAnggota("A001"))
            .thenReturn(true);
        
        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            commandService.createAnggota(command);
        });
    }
}
```

**Run Unit Tests**:
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AnggotaCommandServiceTest

# Run with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Integration Tests

**Example: Testing RabbitMQ Event Flow**

```java
@SpringBootTest
@Testcontainers
class AnggotaEventIntegrationTest {
    
    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management");
    
    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:6.0");
    
    @Autowired
    private AnggotaCommandService commandService;
    
    @Autowired
    private AnggotaQueryRepository queryRepository;
    
    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }
    
    @Test
    void testEventSynchronization() throws InterruptedException {
        // Create anggota (command side)
        CreateAnggotaCommand command = new CreateAnggotaCommand();
        command.setNomorAnggota("A001");
        command.setNama("John Doe");
        
        commandService.createAnggota(command);
        
        // Wait for event processing
        Thread.sleep(2000);
        
        // Verify query side updated
        Optional<AnggotaQuery> result = queryRepository.findByNomorAnggota("A001");
        
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getNama());
    }
}
```

**Run Integration Tests**:
```bash
mvn verify -P integration-tests
```

### API Tests with REST Assured

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AnggotaApiTest {
    
    @LocalServerPort
    private int port;
    
    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }
    
    @Test
    void testCreateAnggota_Returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "nomorAnggota": "A001",
                    "nama": "John Doe",
                    "email": "john@example.com"
                }
                """)
        .when()
            .post("/api/anggota")
        .then()
            .statusCode(201)
            .body("nomorAnggota", equalTo("A001"))
            .body("nama", equalTo("John Doe"));
    }
    
    @Test
    void testCreateAnggota_InvalidEmail_Returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "nomorAnggota": "A001",
                    "nama": "John Doe",
                    "email": "invalid-email"
                }
                """)
        .when()
            .post("/api/anggota")
        .then()
            .statusCode(400)
            .body("message", containsString("email"));
    }
}
```

### Test Coverage Goals

| Component | Target Coverage |
|-----------|----------------|
| Service Layer | 80%+ |
| Controller Layer | 70%+ |
| Repository Layer | 60%+ |
| Overall | 75%+ |

---

## Debugging

### Debugging in IntelliJ IDEA

**1. Set Breakpoints**:
- Click gutter next to line number
- Right-click breakpoint untuk conditional breakpoints

**2. Debug Configuration**:
- **Run** → **Edit Configurations**
- Add **Spring Boot** configuration
- Main class: `AnggotaApplication`
- Working directory: `$MODULE_DIR$`
- Environment variables: Load from `.env`

**3. Start Debug**:
- Click debug icon (🐛)
- Or press `Shift + F9`

**4. Debug Tools**:
- **F8**: Step over
- **F7**: Step into
- **Shift + F8**: Step out
- **F9**: Resume program
- **Evaluate Expression**: `Alt + F8`

### Remote Debugging Docker Container

**1. Update docker-compose.yml**:
```yaml
service-anggota:
  environment:
    - JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
  ports:
    - "8081:8081"
    - "5005:5005"  # Debug port
```

**2. IntelliJ Remote Debug Config**:
- **Run** → **Edit Configurations**
- Add **Remote JVM Debug**
- Host: `localhost`
- Port: `5005`
- Click **Debug**

### Debugging RabbitMQ Events

**1. Add Logging**:
```java
@Slf4j
@Service
public class AnggotaEventListener {
    
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleAnggotaEvent(AnggotaEvent event) {
        log.info("Received event: {}", event);
        log.debug("Event type: {}, Aggregate ID: {}", 
            event.getEventType(), event.getAggregateId());
        
        // Process event
        
        log.info("Event processed successfully");
    }
}
```

**2. View RabbitMQ Management**:
- Open http://localhost:15672
- Login: guest/guest
- Go to **Queues** tab
- Click queue name
- **Get messages** untuk inspect message content

### Debugging MongoDB Queries

**1. Enable MongoDB Query Logging**:
```properties
# application.properties
logging.level.org.springframework.data.mongodb.core=DEBUG
```

**2. MongoDB Shell**:
```bash
# Connect to MongoDB
docker exec -it mongodb mongosh

# Switch database
use anggota_read_db

# View collections
show collections

# Query data
db.anggota_read.find().pretty()

# Find specific document
db.anggota_read.find({ "nomorAnggota": "A001" }).pretty()

# Count documents
db.anggota_read.countDocuments()

# View indexes
db.anggota_read.getIndexes()
```

### Logging Best Practices

```java
@Slf4j
@Service
public class AnggotaCommandService {
    
    public AnggotaCommand createAnggota(CreateAnggotaCommand command) {
        log.info("Creating anggota with nomor: {}", command.getNomorAnggota());
        
        try {
            // Validate
            log.debug("Validating command: {}", command);
            validateCommand(command);
            
            // Save
            log.debug("Saving anggota to database");
            AnggotaCommand saved = commandRepository.save(anggota);
            log.info("Anggota saved with ID: {}", saved.getId());
            
            // Publish event
            log.debug("Publishing event to RabbitMQ");
            publishEvent(saved);
            log.info("Event published successfully");
            
            return saved;
            
        } catch (Exception e) {
            log.error("Failed to create anggota: {}", command.getNomorAnggota(), e);
            throw e;
        }
    }
}
```

**Log Levels**:
- **ERROR**: Errors yang perlu immediate attention
- **WARN**: Potential issues
- **INFO**: Important business flow
- **DEBUG**: Detailed debugging info
- **TRACE**: Very detailed trace info

---

## Code Style & Standards

### Java Code Conventions

**Naming Conventions**:
```java
// Classes: PascalCase
public class AnggotaCommandService { }

// Methods & Variables: camelCase
private String nomorAnggota;
public void createAnggota() { }

// Constants: UPPER_SNAKE_CASE
public static final String QUEUE_NAME = "anggota-sync-queue";

// Packages: lowercase
package com.perpustakaan.anggota.service;
```

### Project Structure

```
service-anggota/
└── src/main/java/com/perpustakaan/anggota/
    ├── AnggotaApplication.java        # Main class
    ├── cqrs/
    │   ├── command/                   # Commands
    │   ├── query/                     # Queries
    │   └── handler/                   # Handlers
    ├── entity/
    │   ├── command/                   # Write model entities
    │   └── query/                     # Read model entities
    ├── repository/
    │   ├── command/                   # JPA repositories
    │   └── query/                     # MongoDB repositories
    ├── service/                       # Business logic
    ├── controller/                    # REST controllers
    ├── dto/                           # Data Transfer Objects
    ├── event/                         # Events
    ├── config/                        # Configurations
    └── exception/                     # Custom exceptions
```

### Code Formatting

**IntelliJ IDEA**:
1. **Code** → **Reformat Code** (`Ctrl + Alt + L`)
2. **Code** → **Optimize Imports** (`Ctrl + Alt + O`)

**Checkstyle Configuration** (`.checkstyle.xml`):
```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">

<module name="Checker">
    <module name="TreeWalker">
        <module name="LineLength">
            <property name="max" value="120"/>
        </module>
        <module name="Indentation">
            <property name="basicOffset" value="4"/>
        </module>
    </module>
</module>
```

### Documentation

**Javadoc Example**:
```java
/**
 * Service untuk mengelola command operations pada Anggota.
 * Implements CQRS pattern untuk write operations.
 * 
 * @author Your Name
 * @since 1.0.0
 */
@Service
@Slf4j
public class AnggotaCommandService {
    
    /**
     * Create anggota baru dan publish event ke RabbitMQ.
     * 
     * @param command CreateAnggotaCommand containing anggota data
     * @return AnggotaCommand entity yang telah disimpan
     * @throws DuplicateResourceException jika nomor anggota sudah ada
     */
    public AnggotaCommand createAnggota(CreateAnggotaCommand command) {
        // Implementation
    }
}
```

---

## Git Workflow

### Branch Naming Convention

```bash
# Features
feature/add-validation
feature/implement-search

# Bug fixes
bugfix/fix-rabbitmq-connection
bugfix/fix-null-pointer

# Hotfixes
hotfix/security-patch
hotfix/critical-bug

# Refactoring
refactor/improve-service-layer
refactor/optimize-queries
```

### Commit Message Convention

Follow **Conventional Commits**:

```bash
# Format
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

**Examples**:
```bash
feat(anggota): add telepon field

- Add telepon to Command and Query entities
- Add validation for Indonesian phone number format
- Update DTOs and event synchronization

Closes #123

---

fix(rabbitmq): handle connection retry

- Implement exponential backoff for RabbitMQ reconnection
- Add connection health check
- Log connection status changes

Fixes #456

---

docs(api): update API reference for pagination

- Add pagination examples
- Document query parameters
- Include response structure
```

### Pull Request Process

**1. Create Pull Request**:
```bash
git push origin feature/add-validation
```

**2. PR Template**:
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex logic
- [ ] Documentation updated
- [ ] No new warnings generated
```

**3. Code Review**:
- At least 1 approval required
- All tests must pass
- No merge conflicts

**4. Merge**:
```bash
# Squash and merge preferred
git checkout main
git pull origin main
git merge --squash feature/add-validation
git push origin main
```

---

## Common Development Tasks

### Adding New Endpoint

**1. Define Command/Query**:
```java
@Data
public class SearchAnggotaQuery {
    private String keyword;
    private int page = 0;
    private int size = 10;
}
```

**2. Update Service**:
```java
public Page<AnggotaQuery> searchAnggota(SearchAnggotaQuery query) {
    Pageable pageable = PageRequest.of(query.getPage(), query.getSize());
    return queryRepository.findByNamaContainingOrEmailContaining(
        query.getKeyword(), 
        query.getKeyword(), 
        pageable
    );
}
```

**3. Add Controller Endpoint**:
```java
@GetMapping("/search")
public ResponseEntity<Page<AnggotaQuery>> search(
    @RequestParam String keyword,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
) {
    SearchAnggotaQuery query = new SearchAnggotaQuery();
    query.setKeyword(keyword);
    query.setPage(page);
    query.setSize(size);
    
    Page<AnggotaQuery> result = queryService.searchAnggota(query);
    return ResponseEntity.ok(result);
}
```

**4. Add Tests**:
```java
@Test
void testSearchAnggota() {
    given()
        .queryParam("keyword", "John")
        .queryParam("page", 0)
        .queryParam("size", 10)
    .when()
        .get("/api/anggota/search")
    .then()
        .statusCode(200)
        .body("content.size()", greaterThan(0));
}
```

### Adding Custom Exception

```java
public class AnggotaNotFoundException extends RuntimeException {
    public AnggotaNotFoundException(String id) {
        super("Anggota with id " + id + " not found");
    }
}
```

**Global Exception Handler**:
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AnggotaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(AnggotaNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
```

### Hot Reload Development

**Spring Boot DevTools**:

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

**IntelliJ IDEA**:
- **Settings** → **Build, Execution, Deployment** → **Compiler**
- ✅ Build project automatically
- **Registry** (`Ctrl + Shift + A` → "Registry")
- ✅ `compiler.automake.allow.when.app.running`

Changes akan auto-reload tanpa restart!

---

## Performance Tips

### 1. Use Lazy Loading
```java
@OneToMany(fetch = FetchType.LAZY)
private List<Peminjaman> peminjamanList;
```

### 2. Database Connection Pooling
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

### 3. Cache Frequently Used Data
```java
@Cacheable("anggota")
public AnggotaQuery getById(String id) {
    return queryRepository.findById(id).orElseThrow();
}
```

### 4. Async Processing
```java
@Async
public CompletableFuture<Void> sendNotification(String email) {
    // Send email
    return CompletableFuture.completedFuture(null);
}
```

---

[⬅️ Back to Documentation Index](README.md)