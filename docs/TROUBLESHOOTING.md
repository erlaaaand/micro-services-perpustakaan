# 🔧 Troubleshooting Guide

Panduan lengkap untuk mengatasi masalah umum dalam sistem microservices perpustakaan.

## 📑 Daftar Isi

- [Service Issues](#service-issues)
- [RabbitMQ Issues](#rabbitmq-issues)
- [MongoDB Issues](#mongodb-issues)
- [Docker Issues](#docker-issues)
- [Build Issues](#build-issues)
- [Network Issues](#network-issues)
- [Monitoring Issues](#monitoring-issues)
- [Performance Issues](#performance-issues)

---

## Service Issues

### Service Not Starting

**Symptoms**:
- Service exits immediately after start
- Error in console: `Application failed to start`

**Diagnosis**:
```bash
# Check service logs
docker logs service-anggota

# Or if running locally
cd service-anggota
mvn spring-boot:run
```

**Common Causes & Solutions**:

#### 1. Port Already in Use

**Error**:
```
Web server failed to start. Port 8081 was already in use.
```

**Solution**:
```bash
# Find process using port
lsof -i :8081        # Linux/Mac
netstat -ano | findstr :8081  # Windows

# Kill process
kill -9 <PID>        # Linux/Mac
taskkill /PID <PID> /F  # Windows

# Or change port in application.properties
server.port=8091
```

#### 2. Missing Dependencies

**Error**:
```
NoClassDefFoundError: org/springframework/...
```

**Solution**:
```bash
# Clean and rebuild
mvn clean install -U

# Clear Maven cache if needed
rm -rf ~/.m2/repository
```

#### 3. Database Connection Failed

**Error**:
```
Unable to create initial connections of pool
```

**Solution**:
```bash
# Check if MongoDB is running
docker ps | grep mongodb

# Restart MongoDB
docker-compose restart mongodb

# Verify connection string in application.properties
spring.data.mongodb.uri=mongodb://localhost:27017/anggota_read_db
```

#### 4. Eureka Registration Failed

**Error**:
```
DiscoveryClient_SERVICE-ANGGOTA - was unable to refresh its cache!
```

**Solution**:
```bash
# Check if Eureka is running
curl http://localhost:8761

# Verify Eureka URL in application.properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# Wait 30-60 seconds for registration
# Check Eureka dashboard at http://localhost:8761
```

### Service Crashes After Running

**Symptoms**:
- Service starts but crashes after a few minutes
- OutOfMemoryError in logs

**Diagnosis**:
```bash
# Check memory usage
docker stats

# View full logs
docker logs --tail 100 service-anggota
```

**Solutions**:

#### 1. Increase Memory Allocation

**Docker Compose**:
```yaml
service-anggota:
  environment:
    - JAVA_OPTS=-Xmx1g -Xms512m
  deploy:
    resources:
      limits:
        memory: 1.5G
```

#### 2. Fix Memory Leak

**Enable heap dump**:
```yaml
environment:
  - JAVA_OPTS=-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp
```

**Analyze heap dump** with Eclipse MAT atau VisualVM

### Health Check Failing

**Symptoms**:
```bash
curl http://localhost:8081/actuator/health
# Returns: {"status": "DOWN"}
```

**Diagnosis**:
```bash
# Check detailed health
curl http://localhost:8081/actuator/health | jq

# Sample output:
{
  "status": "DOWN",
  "components": {
    "mongo": {
      "status": "DOWN",
      "details": {
        "error": "Connection refused"
      }
    }
  }
}
```

**Solutions**:

Check each component:

**MongoDB**:
```bash
docker exec -it mongodb mongosh --eval "db.runCommand({ ping: 1 })"
```

**RabbitMQ**:
```bash
docker exec rabbitmq rabbitmqctl status
```

**Disk Space**:
```bash
df -h
# Ensure sufficient free space
```

---

## RabbitMQ Issues

### Messages Not Being Consumed

**Symptoms**:
- Messages pile up in queue
- Ready messages count increasing
- Consumers = 0

**Diagnosis**:
```bash
# Check RabbitMQ Management UI
open http://localhost:15672

# Or via CLI
docker exec rabbitmq rabbitmqctl list_queues name messages consumers
```

**Solutions**:

#### 1. Consumer Service Not Running

```bash
# Check if service is running
docker ps | grep service-anggota

# Check service logs for listener errors
docker logs service-anggota | grep RabbitListener
```

#### 2. Queue Not Bound to Exchange

**Verify Bindings**:
1. Open RabbitMQ Management (http://localhost:15672)
2. Go to **Exchanges** tab
3. Click exchange name (e.g., `anggota-exchange`)
4. Check **Bindings** section

**Fix**:
```java
@Bean
public Binding binding(Queue queue, TopicExchange exchange) {
    return BindingBuilder
        .bind(queue)
        .to(exchange)
        .with("anggota.routing.key");  // Ensure routing key matches
}
```

#### 3. Consumer Exception

**Check Logs**:
```bash
docker logs service-anggota | grep -A 10 "ERROR"
```

**Common Exception**:
```
MessageConversionException: Cannot convert from [application/json]
```

**Fix**:
```java
@Bean
public Jackson2JsonMessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
}

@Bean
public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter());
    return template;
}
```

### Connection Refused

**Error**:
```
java.net.ConnectException: Connection refused
```

**Solutions**:

```bash
# 1. Check if RabbitMQ is running
docker ps | grep rabbitmq

# 2. Check RabbitMQ logs
docker logs rabbitmq

# 3. Verify connection settings
echo $RABBITMQ_HOST
echo $RABBITMQ_PORT

# 4. Test connection
telnet localhost 5672

# 5. Restart RabbitMQ
docker-compose restart rabbitmq
```

### Messages Being Rejected

**Symptoms**:
- Messages move to dead letter queue
- Nack/Reject in logs

**Diagnosis**:
```bash
# Check dead letter queue
docker exec rabbitmq rabbitmqctl list_queues name messages | grep dlq
```

**Solutions**:

```java
// Add error handling in listener
@RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
public void handleEvent(AnggotaEvent event) {
    try {
        processEvent(event);
    } catch (Exception e) {
        log.error("Failed to process event: {}", event.getEventId(), e);
        // Don't throw - prevents infinite retry loop
        // Handle or log to dead letter queue
    }
}
```

### Queue Not Found

**Error**:
```
Channel shutdown: channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no queue 'anggota-sync-queue'
```

**Solutions**:

```bash
# 1. Verify queue exists
docker exec rabbitmq rabbitmqctl list_queues

# 2. Check queue declaration in code
@Bean
public Queue anggotaSyncQueue() {
    return new Queue(QUEUE_NAME, true); // durable=true
}

# 3. Delete and recreate
docker exec rabbitmq rabbitmqctl delete_queue anggota-sync-queue
# Restart service to recreate
```

---

## MongoDB Issues

### Connection Failed

**Error**:
```
MongoSocketOpenException: Exception opening socket
```

**Solutions**:

```bash
# 1. Check if MongoDB is running
docker ps | grep mongodb

# 2. Test connection
docker exec -it mongodb mongosh --eval "db.version()"

# 3. Check connection string
# application.properties
spring.data.mongodb.uri=mongodb://localhost:27017/anggota_read_db

# 4. Restart MongoDB
docker-compose restart mongodb

# 5. Check MongoDB logs
docker logs mongodb
```

### Authentication Failed

**Error**:
```
MongoSecurityException: Exception authenticating
```

**Solutions**:

```bash
# 1. For development, disable auth
docker run -d --name mongodb \
  -p 27017:27017 \
  mongo:6.0

# 2. For production, use credentials
spring.data.mongodb.uri=mongodb://username:password@localhost:27017/database?authSource=admin
```

### Data Not Syncing

**Symptoms**:
- Command side updates but query side doesn't reflect changes
- MongoDB collection empty after creating records

**Diagnosis**:

```bash
# 1. Check H2 database (write side)
curl http://localhost:8081/h2-console
# Login with JDBC URL: jdbc:h2:mem:anggota_write_db

# 2. Check MongoDB (read side)
docker exec -it mongodb mongosh
use anggota_read_db
db.anggota_read.find().pretty()

# 3. Check RabbitMQ for events
# Open http://localhost:15672
# Check if events are being published
```

**Solutions**:

#### Event Not Published

```java
// Verify event publishing
@Slf4j
@Service
public class AnggotaCommandService {
    
    public AnggotaCommand createAnggota(CreateAnggotaCommand command) {
        AnggotaCommand saved = commandRepository.save(anggota);
        
        log.info("Publishing event for anggota: {}", saved.getId());
        AnggotaCreatedEvent event = new AnggotaCreatedEvent(saved);
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.ROUTING_KEY,
            event
        );
        log.info("Event published successfully");
        
        return saved;
    }
}
```

#### Event Listener Not Working

```java
// Verify listener is enabled
@EnableRabbitListeners  // Add to Application class
@SpringBootApplication
public class AnggotaApplication {
    // ...
}

// Check listener registration
@Slf4j
@Component
public class AnggotaEventListener {
    
    @PostConstruct
    public void init() {
        log.info("AnggotaEventListener initialized");
    }
    
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleAnggotaEvent(AnggotaEvent event) {
        log.info("Received event: {}", event);
        // Process event
    }
}
```

### Duplicate Key Error

**Error**:
```
E11000 duplicate key error collection: anggota_read_db.anggota_read index: nomorAnggota_1
```

**Solutions**:

```bash
# 1. Drop index and recreate
docker exec -it mongodb mongosh
use anggota_read_db
db.anggota_read.dropIndex("nomorAnggota_1")
db.anggota_read.createIndex({ "nomorAnggota": 1 }, { unique: true })

# 2. Or handle in code with upsert
// In event listener
public void handleCreatedEvent(AnggotaCreatedEvent event) {
    Query query = new Query(Criteria.where("id").is(event.getAggregateId()));
    Update update = new Update()
        .set("nomorAnggota", event.getNomorAnggota())
        .set("nama", event.getNama());
    
    mongoTemplate.upsert(query, update, AnggotaQuery.class);
}
```

---

## Docker Issues

### Container Keeps Restarting

**Symptoms**:
```bash
docker ps
# Shows container restarting continuously
```

**Diagnosis**:
```bash
# Check container logs
docker logs <container-name>

# Check exit code
docker inspect <container-name> | grep ExitCode
```

**Common Exit Codes**:
- `137`: Out of memory (OOM killed)
- `1`: Application error
- `139`: Segmentation fault

**Solutions**:

#### Exit Code 137 (OOM)

```yaml
# Increase memory limit
services:
  service-anggota:
    deploy:
      resources:
        limits:
          memory: 2G
        reservations:
          memory: 1G
```

#### Exit Code 1 (Application Error)

```bash
# View full logs
docker logs --tail 200 service-anggota

# Fix application error based on logs
```

### Volume Permission Issues

**Error**:
```
Permission denied: '/data/db'
```

**Solutions**:

```bash
# 1. Fix permissions
sudo chown -R $USER:$USER ./mongodb_data

# 2. Or use named volume
docker volume create mongodb_data

# 3. Update docker-compose.yml
services:
  mongodb:
    volumes:
      - mongodb_data:/data/db
```

### Network Issues

**Error**:
```
could not find an available, non-overlapping IPv4 address pool
```

**Solutions**:

```bash
# 1. Prune unused networks
docker network prune

# 2. Manually specify subnet
networks:
  perpustakaan-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.25.0.0/16
```

### Disk Space Issues

**Error**:
```
no space left on device
```

**Solutions**:

```bash
# 1. Check disk usage
docker system df

# 2. Clean up
docker system prune -a
docker volume prune

# 3. Remove unused images
docker image prune -a

# 4. Remove old containers
docker container prune
```

### Build Cache Issues

**Symptoms**:
- Changes not reflected in new builds
- Old code still running

**Solutions**:

```bash
# 1. Build without cache
docker-compose build --no-cache service-anggota

# 2. Or rebuild all
docker-compose build --no-cache

# 3. Clean and rebuild
docker-compose down
docker system prune -f
docker-compose up -d --build
```

---

## Build Issues

### Maven Build Fails

**Error**:
```
Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin
```

**Solutions**:

```bash
# 1. Clean and rebuild
mvn clean install

# 2. Update dependencies
mvn clean install -U

# 3. Clear Maven cache
rm -rf ~/.m2/repository
mvn clean install

# 4. Check Java version
java -version
# Should be Java 17

# 5. Set JAVA_HOME
export JAVA_HOME=/path/to/java-17
```

### Dependency Resolution Failed

**Error**:
```
Could not resolve dependencies for project
```

**Solutions**:

```bash
# 1. Force update
mvn clean install -U

# 2. Check repository connectivity
mvn dependency:resolve

# 3. Check pom.xml for conflicts
mvn dependency:tree

# 4. Use specific repository
# Add to pom.xml
<repositories>
    <repository>
        <id>central</id>
        <url>https://repo.maven.apache.org/maven2</url>
    </repository>
</repositories>
```

### Test Failures

**Error**:
```
Tests run: 5, Failures: 2, Errors: 0, Skipped: 0
```

**Solutions**:

```bash
# 1. Run tests with details
mvn test -X

# 2. Run specific test
mvn test -Dtest=AnggotaCommandServiceTest

# 3. Skip tests for quick build
mvn clean package -DskipTests

# 4. Update test dependencies
mvn clean test -U
```

### Lombok Not Working

**Error**:
```
cannot find symbol: method getNama()
```

**Solutions**:

```bash
# 1. IntelliJ IDEA
# Settings → Build → Compiler → Annotation Processors
# ✅ Enable annotation processing

# 2. VS Code
# Install Lombok Annotations Support extension

# 3. Verify Lombok in pom.xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

# 4. Rebuild project
mvn clean compile
```

---

## Network Issues

### Cannot Access Service

**Symptoms**:
- `curl http://localhost:8081` returns connection refused
- Service running but not accessible

**Diagnosis**:

```bash
# 1. Check if service is running
docker ps | grep service-anggota

# 2. Check port mapping
docker port service-anggota

# 3. Check if port is listening
netstat -tuln | grep 8081  # Linux
lsof -i :8081             # Mac
netstat -an | findstr :8081  # Windows

# 4. Test from inside container
docker exec service-anggota curl localhost:8081/actuator/health
```

**Solutions**:

#### Service Not Exposed

```yaml
# Ensure port is exposed in docker-compose.yml
services:
  service-anggota:
    ports:
      - "8081:8081"  # host:container
```

#### Firewall Blocking

```bash
# Linux - allow port
sudo ufw allow 8081

# Or disable firewall temporarily for testing
sudo ufw disable
```

#### Wrong Network

```bash
# Check container network
docker inspect service-anggota | grep NetworkMode

# Ensure all services on same network
docker network inspect perpustakaan-network
```

### Service-to-Service Communication Failed

**Error**:
```
RestTemplate: I/O error on GET request for "http://service-anggota:8081"
java.net.UnknownHostException: service-anggota
```

**Solutions**:

```bash
# 1. Check if both services on same network
docker network inspect perpustakaan-network

# 2. Use container name for hostname
RestTemplate restTemplate = new RestTemplate();
String url = "http://service-anggota:8081/api/anggota/1";

# 3. Or use Eureka for discovery
@LoadBalanced
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}

# Then use service name
String url = "http://service-anggota/api/anggota/1";
```

---

## Monitoring Issues

### Prometheus Not Scraping

**Symptoms**:
- Targets show as DOWN in Prometheus
- No metrics visible

**Diagnosis**:

```bash
# 1. Check Prometheus targets
open http://localhost:9090/targets

# 2. Test metrics endpoint manually
curl http://localhost:8081/actuator/prometheus

# 3. Check Prometheus config
docker exec prometheus cat /etc/prometheus/prometheus.yml
```

**Solutions**:

#### Endpoint Not Exposed

```properties
# application.properties
management.endpoints.web.exposure.include=*
management.metrics.export.prometheus.enabled=true
```

#### Wrong Target Configuration

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'service-anggota'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['service-anggota:8081']  # Use container name, not localhost
```

#### Restart Prometheus

```bash
docker-compose restart prometheus
```

### Grafana No Data

**Symptoms**:
- Dashboards show "No Data"
- Queries return empty

**Solutions**:

```bash
# 1. Check Prometheus datasource
# Grafana → Configuration → Data Sources → Prometheus
# Test connection

# 2. Verify Prometheus URL
# Should be: http://prometheus:9090

# 3. Check if metrics exist in Prometheus
open http://localhost:9090
# Execute query: up

# 4. Re-import dashboard
# Delete old dashboard
# Import fresh from ID or JSON
```

### Zipkin Traces Not Appearing

**Solutions**:

```properties
# 1. Verify sampling enabled
management.tracing.sampling.probability=1.0

# 2. Check Zipkin endpoint
management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans

# 3. Add dependency if missing
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

### Kibana Index Pattern Not Found

**Solutions**:

```bash
# 1. Check if logs are in Elasticsearch
curl http://localhost:9200/_cat/indices

# 2. Create index pattern
# Kibana → Management → Index Patterns → Create
# Pattern: app-logs-*
# Time field: @timestamp

# 3. Verify Logstash is receiving logs
docker logs logstash | grep "Pipeline started"
```

---

## Performance Issues

### Slow Response Times

**Diagnosis**:

```bash
# 1. Check Zipkin for slow spans
open http://localhost:9411

# 2. Check Prometheus metrics
# Query: histogram_quantile(0.95, http_server_requests_seconds_bucket)

# 3. Profile application
# Add to JAVA_OPTS: -Xprof -XX:+PrintGCDetails
```

**Solutions**:

#### Database Query Optimization

```java
// Add indexes
@Indexed
private String nomorAnggota;

// Use pagination
Page<AnggotaQuery> findAll(Pageable pageable);

// Optimize queries
@Query("{ 'nomorAnggota': ?0 }")
Optional<AnggotaQuery> findByNomorAnggota(String nomorAnggota);
```

#### Caching

```java
@Cacheable("anggota")
public AnggotaQuery getById(String id) {
    return queryRepository.findById(id).orElseThrow();
}
```

#### Connection Pool Tuning

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10
spring.rabbitmq.listener.simple.concurrency=3
spring.rabbitmq.listener.simple.max-concurrency=10
```

### High Memory Usage

**Solutions**:

```bash
# 1. Monitor memory
docker stats

# 2. Generate heap dump
docker exec service-anggota jmap -dump:live,format=b,file=/tmp/heap.bin 1

# 3. Analyze with VisualVM or Eclipse MAT

# 4. Tune JVM
JAVA_OPTS=-Xmx1g -Xms512m -XX:+UseG1GC
```

### CPU Spikes

**Solutions**:

```bash
# 1. Check thread usage
docker exec service-anggota jstack 1

# 2. Identify hot methods
# Use profiler or add timing logs

# 3. Optimize algorithms

# 4. Use async processing
@Async
public CompletableFuture<Void> processLongRunningTask() {
    // ...
}
```

---

## Emergency Procedures

### Complete System Reset

```bash
# 1. Stop everything
docker-compose down

# 2. Clean Docker
docker system prune -a --volumes

# 3. Rebuild from scratch
docker-compose build --no-cache

# 4. Start services
docker-compose up -d

# 5. Verify
./scripts/health-check.sh
```

### Data Recovery

```bash
# 1. Backup MongoDB
docker exec mongodb mongodump --out /backup

# 2. Restore MongoDB
docker exec mongodb mongorestore /backup

# 3. Export H2 (if configured for persistence)
# Access H2 console and use SCRIPT TO command
```

### Rollback Deployment

```bash
# 1. Stop current version
docker-compose down

# 2. Checkout previous version
git checkout <previous-commit>

# 3. Rebuild and deploy
docker-compose up -d --build

# 4. Verify
curl http://localhost:8080/actuator/health
```

---

## Getting Help

### Logs to Collect

When reporting issues, include:

```bash
# Service logs
docker logs service-anggota > service-anggota.log

# Docker compose logs
docker-compose logs > all-services.log

# System info
docker version
docker-compose version
java -version
mvn -version

# Container stats
docker stats --no-stream > docker-stats.txt
```

### Support Channels

- GitHub Issues: [github.com/erlaaaand/micro-services-perpustakaan/issues](https://github.com/erlaaaand/micro-services-perpustakaan/issues)
- Email: blackpenta98@gmail.com
- Documentation: [docs/](.)

---

[⬅️ Back to Documentation Index](README.md)