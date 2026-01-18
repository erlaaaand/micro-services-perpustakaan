# 📨 Event-Driven Architecture Guide

Dokumentasi lengkap implementasi Event-Driven Architecture menggunakan RabbitMQ dalam sistem microservices perpustakaan.

## 📑 Daftar Isi

- [Konsep Event-Driven Architecture](#konsep-event-driven-architecture)
- [RabbitMQ Setup](#rabbitmq-setup)
- [Event Design](#event-design)
- [Publishing Events](#publishing-events)
- [Consuming Events](#consuming-events)
- [Error Handling](#error-handling)
- [Best Practices](#best-practices)

---

## Konsep Event-Driven Architecture

### Apa itu Event-Driven Architecture?

Event-Driven Architecture (EDA) adalah pattern dimana services berkomunikasi melalui events asynchronous.

```
Traditional Synchronous (REST):
Service A ──HTTP Request──▶ Service B
          ◀──HTTP Response──

Event-Driven Asynchronous:
Service A ──Publish Event──▶ Message Broker ──Event──▶ Service B
                                   │
                                   └──Event──▶ Service C
```

### Keuntungan EDA

#### 1. **Loose Coupling**
- Services tidak perlu tahu tentang consumers
- Easy to add/remove consumers
- Changes dalam satu service tidak affect others

#### 2. **Scalability**
- Async processing
- Load leveling dengan message queue
- Independent scaling

#### 3. **Resilience**
- Service failures tidak propagate
- Message persistence untuk retry
- Circuit breaker pattern

#### 4. **Flexibility**
- Easy to add new features
- Multiple consumers per event
- Event replay untuk debugging

---

## RabbitMQ Setup

### 1. RabbitMQ Configuration

```java
package com.perpustakaan.anggota.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    // Exchange Configuration
    public static final String EXCHANGE_NAME = "anggota-exchange";
    public static final String QUEUE_NAME = "anggota-sync-queue";
    public static final String ROUTING_KEY_CREATED = "anggota.created";
    public static final String ROUTING_KEY_UPDATED = "anggota.updated";
    public static final String ROUTING_KEY_DELETED = "anggota.deleted";
    
    // Dead Letter Queue
    public static final String DLQ_EXCHANGE = "anggota-dlx";
    public static final String DLQ_QUEUE = "anggota-dlq";
    
    @Bean
    public TopicExchange anggotaExchange() {
        return ExchangeBuilder
            .topicExchange(EXCHANGE_NAME)
            .durable(true)
            .build();
    }
    
    @Bean
    public Queue anggotaSyncQueue() {
        return QueueBuilder
            .durable(QUEUE_NAME)
            .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", "dlq")
            .withArgument("x-message-ttl", 86400000) // 24 hours
            .build();
    }
    
    @Bean
    public Binding bindingCreated() {
        return BindingBuilder
            .bind(anggotaSyncQueue())
            .to(anggotaExchange())
            .with(ROUTING_KEY_CREATED);
    }
    
    @Bean
    public Binding bindingUpdated() {
        return BindingBuilder
            .bind(anggotaSyncQueue())
            .to(anggotaExchange())
            .with(ROUTING_KEY_UPDATED);
    }
    
    @Bean
    public Binding bindingDeleted() {
        return BindingBuilder
            .bind(anggotaSyncQueue())
            .to(anggotaExchange())
            .with(ROUTING_KEY_DELETED);
    }
    
    // Dead Letter Queue Configuration
    @Bean
    public TopicExchange deadLetterExchange() {
        return ExchangeBuilder
            .topicExchange(DLQ_EXCHANGE)
            .durable(true)
            .build();
    }
    
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
            .durable(DLQ_QUEUE)
            .build();
    }
    
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
            .bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with("dlq");
    }
    
    // Message Converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    // RabbitTemplate Configuration
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        
        // Enable publisher confirms
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("Message not acked: " + cause);
            }
        });
        
        // Enable publisher returns
        template.setMandatory(true);
        template.setReturnsCallback(returned -> {
            System.err.println("Message returned: " + returned.getMessage());
        });
        
        return template;
    }
}
```

### 2. Application Properties

```properties
# RabbitMQ Configuration
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USERNAME:guest}
spring.rabbitmq.password=${RABBITMQ_PASSWORD:guest}
spring.rabbitmq.virtual-host=/

# Connection Pool
spring.rabbitmq.cache.connection.mode=channel
spring.rabbitmq.cache.connection.size=25
spring.rabbitmq.cache.channel.size=10
spring.rabbitmq.cache.channel.checkout-timeout=0

# Publisher Confirms
spring.rabbitmq.publisher-confirm-type=correlated
spring.rabbitmq.publisher-returns=true

# Consumer Configuration
spring.rabbitmq.listener.simple.acknowledge-mode=auto
spring.rabbitmq.listener.simple.prefetch=1
spring.rabbitmq.listener.simple.concurrency=3
spring.rabbitmq.listener.simple.max-concurrency=10
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.initial-interval=1000
spring.rabbitmq.listener.simple.retry.max-attempts=3
spring.rabbitmq.listener.simple.retry.multiplier=2.0
spring.rabbitmq.listener.simple.default-requeue-rejected=false
```

---

## Event Design

### 1. Event Base Class

```java
package com.perpustakaan.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent implements Serializable {
    
    private String eventId;
    private String eventType;
    private String aggregateId;
    private LocalDateTime timestamp;
    private String userId; // Who triggered the event
    private String correlationId; // For tracing
    
    public BaseEvent(String eventType, String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.timestamp = LocalDateTime.now();
    }
}
```

### 2. Domain Events

```java
package com.perpustakaan.anggota.event;

import com.perpustakaan.common.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AnggotaCreatedEvent extends BaseEvent {
    
    private String nomorAnggota;
    private String nama;
    private String alamat;
    private String email;
    private String telepon;
    
    public AnggotaCreatedEvent(String aggregateId, String nomorAnggota, 
                               String nama, String alamat, String email, String telepon) {
        super("ANGGOTA_CREATED", aggregateId);
        this.nomorAnggota = nomorAnggota;
        this.nama = nama;
        this.alamat = alamat;
        this.email = email;
        this.telepon = telepon;
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AnggotaUpdatedEvent extends BaseEvent {
    
    private String nomorAnggota;
    private String nama;
    private String alamat;
    private String email;
    private String telepon;
    private Map<String, Object> changes; // What fields changed
    
    public AnggotaUpdatedEvent(String aggregateId, String nomorAnggota,
                               String nama, String alamat, String email, String telepon) {
        super("ANGGOTA_UPDATED", aggregateId);
        this.nomorAnggota = nomorAnggota;
        this.nama = nama;
        this.alamat = alamat;
        this.email = email;
        this.telepon = telepon;
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AnggotaDeletedEvent extends BaseEvent {
    
    private String nomorAnggota;
    private String reason; // Why deleted
    
    public AnggotaDeletedEvent(String aggregateId, String nomorAnggota) {
        super("ANGGOTA_DELETED", aggregateId);
        this.nomorAnggota = nomorAnggota;
    }
}
```

### 3. Event Naming Conventions

**Pattern**: `{Entity}{PastTenseAction}Event`

Examples:
- ✅ `AnggotaCreatedEvent`
- ✅ `BukuUpdatedEvent`
- ✅ `PeminjamanCompletedEvent`
- ❌ `AnggotaCreateEvent` (not past tense)
- ❌ `CreateAnggotaEvent` (action first)

---

## Publishing Events

### 1. Event Publisher Service

```java
package com.perpustakaan.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    public <T extends BaseEvent> void publish(String exchange, String routingKey, T event) {
        log.info("Publishing event: {} with ID: {} to exchange: {}", 
            event.getEventType(), event.getEventId(), exchange);
        
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event, message -> {
                // Add custom headers
                message.getMessageProperties().setHeader("event-type", event.getEventType());
                message.getMessageProperties().setHeader("aggregate-id", event.getAggregateId());
                message.getMessageProperties().setHeader("timestamp", event.getTimestamp().toString());
                
                // Add correlation ID for tracing
                if (event.getCorrelationId() != null) {
                    message.getMessageProperties().setCorrelationId(event.getCorrelationId());
                }
                
                return message;
            });
            
            log.info("Event published successfully: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getEventId(), e);
            throw new EventPublishException("Failed to publish event", e);
        }
    }
    
    public <T extends BaseEvent> void publishWithRetry(String exchange, String routingKey, 
                                                       T event, int maxRetries) {
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < maxRetries) {
            try {
                publish(exchange, routingKey, event);
                return; // Success
            } catch (Exception e) {
                lastException = e;
                attempts++;
                log.warn("Publish attempt {} failed, retrying...", attempts);
                
                try {
                    Thread.sleep(1000 * attempts); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        log.error("Failed to publish event after {} attempts", maxRetries);
        throw new EventPublishException("Failed to publish event after retries", lastException);
    }
}
```

### 2. Using Event Publisher

```java
@Service
@RequiredArgsConstructor
public class AnggotaCommandService {
    
    private final AnggotaCommandRepository commandRepository;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public AnggotaCommand createAnggota(CreateAnggotaCommand command) {
        // Create and save entity
        AnggotaCommand saved = commandRepository.save(anggota);
        
        // Publish event
        AnggotaCreatedEvent event = new AnggotaCreatedEvent(
            String.valueOf(saved.getId()),
            saved.getNomorAnggota(),
            saved.getNama(),
            saved.getAlamat(),
            saved.getEmail(),
            saved.getTelepon()
        );
        
        event.setUserId(getCurrentUserId());
        event.setCorrelationId(getCorrelationId());
        
        eventPublisher.publish(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.ROUTING_KEY_CREATED,
            event
        );
        
        return saved;
    }
}
```

### 3. Transactional Outbox Pattern

Untuk ensure event publishing dengan database transaction:

```java
@Entity
@Table(name = "outbox_events")
@Data
public class OutboxEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String aggregateId;
    private String eventType;
    
    @Column(columnDefinition = "TEXT")
    private String payload;
    
    private LocalDateTime createdAt;
    private boolean processed;
    private LocalDateTime processedAt;
}

@Service
@RequiredArgsConstructor
public class OutboxEventService {
    
    private final OutboxEventRepository outboxRepository;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public void saveEvent(BaseEvent event) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateId(event.getAggregateId());
        outboxEvent.setEventType(event.getEventType());
        outboxEvent.setPayload(serializeEvent(event));
        outboxEvent.setCreatedAt(LocalDateTime.now());
        outboxEvent.setProcessed(false);
        
        outboxRepository.save(outboxEvent);
    }
    
    @Scheduled(fixedDelay = 5000) // Every 5 seconds
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> unprocessedEvents = outboxRepository
            .findByProcessedFalseOrderByCreatedAtAsc(PageRequest.of(0, 100));
        
        for (OutboxEvent outboxEvent : unprocessedEvents) {
            try {
                BaseEvent event = deserializeEvent(outboxEvent.getPayload());
                eventPublisher.publish("exchange", "routing-key", event);
                
                outboxEvent.setProcessed(true);
                outboxEvent.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(outboxEvent);
                
            } catch (Exception e) {
                log.error("Failed to process outbox event: {}", outboxEvent.getId(), e);
            }
        }
    }
}
```

---

## Consuming Events

### 1. Event Listener

```java
package com.perpustakaan.anggota.event;

import com.perpustakaan.anggota.entity.query.AnggotaQuery;
import com.perpustakaan.anggota.repository.query.AnggotaQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnggotaEventListener {
    
    private final AnggotaQueryRepository queryRepository;
    
    @RabbitListener(queues = "${rabbitmq.queue.anggota-sync}")
    public void handleAnggotaCreatedEvent(
            @Payload AnggotaCreatedEvent event,
            @Header("event-type") String eventType) {
        
        log.info("Received {} event with ID: {}", eventType, event.getEventId());
        
        try {
            // Idempotency check
            if (queryRepository.existsById(event.getAggregateId())) {
                log.warn("Anggota already exists, skipping: {}", event.getAggregateId());
                return;
            }
            
            // Create read model
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
            
            log.info("Successfully processed AnggotaCreatedEvent: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to process AnggotaCreatedEvent: {}", event.getEventId(), e);
            throw e; // Let RabbitMQ handle retry
        }
    }
    
    @RabbitListener(queues = "${rabbitmq.queue.anggota-sync}")
    public void handleAnggotaUpdatedEvent(@Payload AnggotaUpdatedEvent event) {
        log.info("Received AnggotaUpdatedEvent with ID: {}", event.getEventId());
        
        try {
            AnggotaQuery query = queryRepository.findById(event.getAggregateId())
                .orElseThrow(() -> new IllegalStateException(
                    "Anggota not found for update: " + event.getAggregateId()));
            
            // Update read model
            query.setNama(event.getNama());
            query.setAlamat(event.getAlamat());
            query.setEmail(event.getEmail());
            query.setTelepon(event.getTelepon());
            query.setUpdatedAt(event.getTimestamp());
            
            queryRepository.save(query);
            
            log.info("Successfully processed AnggotaUpdatedEvent: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to process AnggotaUpdatedEvent: {}", event.getEventId(), e);
            throw e;
        }
    }
    
    @RabbitListener(queues = "${rabbitmq.queue.anggota-sync}")
    public void handleAnggotaDeletedEvent(@Payload AnggotaDeletedEvent event) {
        log.info("Received AnggotaDeletedEvent with ID: {}", event.getEventId());
        
        try {
            queryRepository.deleteById(event.getAggregateId());
            
            log.info("Successfully processed AnggotaDeletedEvent: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to process AnggotaDeletedEvent: {}", event.getEventId(), e);
            throw e;
        }
    }
}
```

### 2. Multiple Consumers

```java
@Component
public class NotificationEventListener {
    
    @RabbitListener(queues = "notification-queue")
    public void handleAnggotaCreatedEvent(AnggotaCreatedEvent event) {
        // Send welcome email
        sendWelcomeEmail(event.getEmail(), event.getNama());
    }
}

@Component
public class AuditEventListener {
    
    @RabbitListener(queues = "audit-queue")
    public void handleAnggotaCreatedEvent(AnggotaCreatedEvent event) {
        // Log to audit trail
        logAuditEvent("ANGGOTA_CREATED", event.getAggregateId(), event.getUserId());
    }
}
```

---

## Error Handling

### 1. Retry Configuration

```java
@Configuration
public class RabbitMQRetryConfig {
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        
        SimpleRabbitListenerContainerFactory factory = 
            new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        
        // Retry configuration
        RetryOperationsInterceptor interceptor = RetryInterceptorBuilder
            .stateless()
            .maxAttempts(3)
            .backOffOptions(1000, 2.0, 10000) // Initial, multiplier, max
            .recoverer(new RejectAndDontRequeueRecoverer())
            .build();
        
        factory.setAdviceChain(interceptor);
        
        return factory;
    }
}
```

### 2. Dead Letter Queue Handling

```java
@Component
@Slf4j
public class DeadLetterQueueListener {
    
    @RabbitListener(queues = "anggota-dlq")
    public void handleDeadLetter(Message message) {
        log.error("Message moved to DLQ:");
        log.error("  Body: {}", new String(message.getBody()));
        log.error("  Headers: {}", message.getMessageProperties().getHeaders());
        
        // Alert operations team
        alertOperations(message);
        
        // Store for manual intervention
        storeForManualProcessing(message);
    }
    
    private void alertOperations(Message message) {
        // Send alert via email/Slack
    }
    
    private void storeForManualProcessing(Message message) {
        // Store in database for manual review
    }
}
```

### 3. Exception Handling Strategies

```java
@Component
public class AnggotaEventListener {
    
    @RabbitListener(queues = "anggota-sync-queue")
    public void handleEvent(AnggotaCreatedEvent event) {
        try {
            processEvent(event);
            
        } catch (TransientException e) {
            // Retryable error - let RabbitMQ retry
            log.warn("Transient error, will retry: {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Transient error", e);
            
        } catch (DuplicateException e) {
            // Idempotent - already processed
            log.info("Event already processed, acknowledging: {}", event.getEventId());
            // Don't throw - will be acked
            
        } catch (ValidationException e) {
            // Permanent error - move to DLQ
            log.error("Validation error, moving to DLQ: {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Validation failed", e);
            
        } catch (Exception e) {
            // Unknown error - retry then DLQ
            log.error("Unknown error processing event: {}", event.getEventId(), e);
            throw e;
        }
    }
}
```

---

## Best Practices

### 1. Event Versioning

```java
public class AnggotaCreatedEventV2 extends BaseEvent {
    
    private static final int VERSION = 2;
    
    // New fields in V2
    private String phoneNumber;
    private Address address; // Complex type instead of String
    
    public int getVersion() {
        return VERSION;
    }
}

@Component
public class VersionedEventListener {
    
    @RabbitListener(queues = "anggota-sync-queue")
    public void handleEvent(@Payload BaseEvent event, 
                           @Header("version") Integer version) {
        
        if (version == null || version == 1) {
            handleV1Event((AnggotaCreatedEvent) event);
        } else if (version == 2) {
            handleV2Event((AnggotaCreatedEventV2) event);
        }
    }
}
```

### 2. Event Idempotency

```java
@Component
public class IdempotentEventListener {
    
    private final Set<String> processedEvents = new ConcurrentHashMap().newKeySet();
    
    @RabbitListener(queues = "anggota-sync-queue")
    public void handleEvent(AnggotaCreatedEvent event) {
        // Check if already processed
        if (!processedEvents.add(event.getEventId())) {
            log.info("Event already processed: {}", event.getEventId());
            return;
        }
        
        try {
            processEvent(event);
        } catch (Exception e) {
            processedEvents.remove(event.getEventId());
            throw e;
        }
    }
}
```

### 3. Event Correlation

```java
@Service
public class EventCorrelationService {
    
    private static final ThreadLocal<String> correlationId = new ThreadLocal<>();
    
    public void setCorrelationId(String id) {
        correlationId.set(id);
        MDC.put("correlationId", id);
    }
    
    public String getCorrelationId() {
        return correlationId.get();
    }
    
    public void clearCorrelationId() {
        correlationId.remove();
        MDC.remove("correlationId");
    }
}

@Component
public class CorrelatedEventListener {
    
    @RabbitListener(queues = "anggota-sync-queue")
    public void handleEvent(AnggotaCreatedEvent event) {
        try {
            correlationService.setCorrelationId(event.getCorrelationId());
            processEvent(event);
        } finally {
            correlationService.clearCorrelationId();
        }
    }
}
```

### 4. Event Monitoring

```java
@Aspect
@Component
@Slf4j
public class EventMetricsAspect {
    
    private final MeterRegistry meterRegistry;
    
    @Around("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)")
    public Object measureEventProcessing(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);
        String eventType = getEventType(joinPoint);
        
        try {
            Object result = joinPoint.proceed();
            
            sample.stop(Timer.builder("event.processing.time")
                .tag("event.type", eventType)
                .tag("status", "success")
                .register(meterRegistry));
            
            meterRegistry.counter("event.processed",
                "event.type", eventType,
                "status", "success").increment();
            
            return result;
            
        } catch (Exception e) {
            sample.stop(Timer.builder("event.processing.time")
                .tag("event.type", eventType)
                .tag("status", "error")
                .register(meterRegistry));
            
            meterRegistry.counter("event.processed",
                "event.type", eventType,
                "status", "error").increment();
            
            throw e;
        }
    }
}
```

---

[⬅️ Back to Documentation Index](README.md)