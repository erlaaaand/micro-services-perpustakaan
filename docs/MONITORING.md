# 📊 Monitoring & Observability Guide

Panduan lengkap untuk monitoring dan observability sistem microservices perpustakaan.

## 📑 Daftar Isi

- [Overview](#overview)
- [Prometheus](#prometheus)
- [Grafana](#grafana)
- [Zipkin Distributed Tracing](#zipkin-distributed-tracing)
- [ELK Stack Logging](#elk-stack-logging)
- [RabbitMQ Monitoring](#rabbitmq-monitoring)
- [Health Checks](#health-checks)
- [Alerting](#alerting)

---

## Overview

### Monitoring Stack Architecture

```
┌────────────────────────────────────────────────┐
│              Applications                      │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐         │
│  │ Svc1 │ │ Svc2 │ │ Svc3 │ │ Svc4 │         │
│  └───┬──┘ └───┬──┘ └───┬──┘ └───┬──┘         │
└──────┼────────┼────────┼────────┼─────────────┘
       │        │        │        │
       │ Logs   │ Metrics│ Traces │ Health
       │        │        │        │
┌──────▼────────▼────────▼────────▼─────────────┐
│         Observability Layer                   │
│                                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │   ELK    │  │Prometheus│  │  Zipkin  │   │
│  │  Stack   │  │    +     │  │          │   │
│  │          │  │ Grafana  │  │          │   │
│  └──────────┘  └──────────┘  └──────────┘   │
└───────────────────────────────────────────────┘
```

### Access URLs

| Tool | URL | Credentials | Purpose |
|------|-----|-------------|---------|
| **Prometheus** | http://localhost:9090 | - | Metrics collection & queries |
| **Grafana** | http://localhost:3000 | admin/admin | Metrics visualization |
| **Zipkin** | http://localhost:9411 | - | Distributed tracing |
| **Kibana** | http://localhost:5601 | - | Log analysis |
| **RabbitMQ Management** | http://localhost:15672 | guest/guest | Message queue monitoring |

---

## Prometheus

### Configuration

Prometheus dikonfigurasi di `monitoring/prometheus/prometheus.yml`:

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: 'perpustakaan-microservices'

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
  
  - job_name: 'eureka-server'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['eureka-server:8761']
  
  - job_name: 'api-gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['api-gateway:8080']
  
  - job_name: 'service-anggota'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['service-anggota:8081']
  
  - job_name: 'service-buku'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['service-buku:8082']
  
  - job_name: 'service-peminjaman'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['service-peminjaman:8083']
  
  - job_name: 'service-pengembalian'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['service-pengembalian:8084']
```

### Available Metrics

#### JVM Metrics
```promql
# Memory usage
jvm_memory_used_bytes
jvm_memory_max_bytes
jvm_memory_committed_bytes

# Garbage collection
jvm_gc_pause_seconds_count
jvm_gc_pause_seconds_sum
jvm_gc_memory_allocated_bytes_total
jvm_gc_memory_promoted_bytes_total

# Threads
jvm_threads_live_threads
jvm_threads_daemon_threads
jvm_threads_peak_threads
jvm_threads_states_threads
```

#### HTTP Metrics
```promql
# Request count
http_server_requests_seconds_count

# Request duration
http_server_requests_seconds_sum
http_server_requests_seconds_max

# Active requests
http_server_requests_active_seconds_active_count
```

#### System Metrics
```promql
# CPU usage
system_cpu_usage
process_cpu_usage

# System load
system_load_average_1m

# Uptime
process_uptime_seconds
process_start_time_seconds
```

#### Custom Business Metrics
```promql
# Total anggota registered
anggota_total_count

# Total buku in catalog
buku_total_count

# Active peminjaman
peminjaman_active_count

# Overdue peminjaman
peminjaman_overdue_count
```

### Useful Queries

#### 1. Request Rate per Service
```promql
rate(http_server_requests_seconds_count{application="service-anggota"}[5m])
```

#### 2. Average Response Time
```promql
rate(http_server_requests_seconds_sum{application="service-anggota"}[5m]) 
/ 
rate(http_server_requests_seconds_count{application="service-anggota"}[5m])
```

#### 3. Error Rate
```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) 
/ 
sum(rate(http_server_requests_seconds_count[5m]))
```

#### 4. Memory Usage Percentage
```promql
jvm_memory_used_bytes{area="heap"} 
/ 
jvm_memory_max_bytes{area="heap"} * 100
```

#### 5. GC Pause Time
```promql
rate(jvm_gc_pause_seconds_sum[5m])
```

#### 6. CPU Usage
```promql
system_cpu_usage * 100
```

#### 7. Top 5 Slowest Endpoints
```promql
topk(5, 
  rate(http_server_requests_seconds_sum[5m]) 
  / 
  rate(http_server_requests_seconds_count[5m])
)
```

### Exploring Metrics

1. Open Prometheus UI: http://localhost:9090
2. Go to **Graph** tab
3. Enter PromQL query dalam expression browser
4. Click **Execute**
5. Switch antara **Table** dan **Graph** view

### Checking Targets

1. Go to **Status** → **Targets**
2. Verify all services show **State: UP**
3. Check **Last Scrape** timestamp
4. View **Errors** jika ada scrape failures

---

## Grafana

### Initial Setup

1. **Access Grafana**: http://localhost:3000
2. **Login** dengan default credentials:
   - Username: `admin`
   - Password: `admin`
3. **Change password** saat diminta

### Pre-configured Datasource

Prometheus datasource sudah auto-configured via provisioning:

```yaml
# monitoring/grafana/provisioning/datasources/prometheus.yml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
```

### Creating Dashboards

#### 1. Import JVM Micrometer Dashboard

1. Click **+** → **Import**
2. Enter dashboard ID: `4701` (JVM Micrometer)
3. Select **Prometheus** datasource
4. Click **Import**

#### 2. Create Custom Dashboard

**Service Overview Dashboard**:

```json
{
  "dashboard": {
    "title": "Service Overview",
    "panels": [
      {
        "title": "Request Rate",
        "targets": [{
          "expr": "sum(rate(http_server_requests_seconds_count[5m])) by (application)"
        }],
        "type": "graph"
      },
      {
        "title": "Error Rate",
        "targets": [{
          "expr": "sum(rate(http_server_requests_seconds_count{status=~\"5..\"}[5m])) by (application)"
        }],
        "type": "graph"
      },
      {
        "title": "Avg Response Time",
        "targets": [{
          "expr": "rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])"
        }],
        "type": "graph"
      },
      {
        "title": "Memory Usage %",
        "targets": [{
          "expr": "jvm_memory_used_bytes{area=\"heap\"} / jvm_memory_max_bytes{area=\"heap\"} * 100"
        }],
        "type": "gauge"
      }
    ]
  }
}
```

### Recommended Panels

#### Panel 1: HTTP Request Rate
```promql
sum(rate(http_server_requests_seconds_count[5m])) by (application)
```
**Visualization**: Time series graph

#### Panel 2: HTTP Error Rate
```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (application)
```
**Visualization**: Time series graph

#### Panel 3: Response Time (95th Percentile)
```promql
histogram_quantile(0.95, 
  sum(rate(http_server_requests_seconds_bucket[5m])) by (application, le)
)
```
**Visualization**: Time series graph

#### Panel 4: JVM Heap Usage
```promql
jvm_memory_used_bytes{area="heap"}
```
**Visualization**: Time series graph with max threshold

#### Panel 5: GC Pause Time
```promql
rate(jvm_gc_pause_seconds_sum[5m])
```
**Visualization**: Time series graph

#### Panel 6: Thread Count
```promql
jvm_threads_live_threads
```
**Visualization**: Stat panel

#### Panel 7: CPU Usage
```promql
system_cpu_usage * 100
```
**Visualization**: Gauge (0-100%)

#### Panel 8: System Load Average
```promql
system_load_average_1m
```
**Visualization**: Stat panel

### Dashboard Variables

Add variables untuk dynamic filtering:

```
Name: service
Query: label_values(http_server_requests_seconds_count, application)
```

Use in queries:
```promql
rate(http_server_requests_seconds_count{application="$service"}[5m])
```

---

## Zipkin Distributed Tracing

### Overview

Zipkin melacak request flow across multiple services.

### Trace Structure

```
Trace ID: abc123
│
├─ Span 1: api-gateway (2ms)
│  │
│  ├─ Span 2: service-peminjaman (15ms)
│  │  │
│  │  ├─ Span 3: MongoDB Read (3ms)
│  │  │
│  │  ├─ Span 4: service-anggota (5ms)
│  │  │  └─ Span 5: MongoDB Read (2ms)
│  │  │
│  │  └─ Span 6: service-buku (4ms)
│  │     └─ Span 7: MongoDB Read (2ms)
│  │
│  └─ Total: 17ms
```

### Using Zipkin UI

#### 1. Search Traces

1. Open Zipkin: http://localhost:9411
2. Click **Search** (ikon magnifying glass)
3. Select **Service Name** (e.g., api-gateway)
4. Set **Time Range**
5. Optional filters:
   - **Min Duration**: Find slow requests
   - **Limit**: Number of results
6. Click **RUN QUERY**

#### 2. View Trace Details

Click on a trace to see:
- **Service Dependency**: Visual service call graph
- **Timeline**: Span durations
- **Span Details**: Tags, annotations, logs

#### 3. Dependencies Graph

1. Click **Dependencies** icon
2. View service interaction graph
3. See request counts and error rates

### Trace Analysis

#### Find Slow Requests
```
Service: service-peminjaman
Min Duration: 500ms
```

#### Find Errors
Look for spans with tags:
```
error: true
http.status_code: 500
```

#### Analyze Performance Bottlenecks
1. Sort traces by duration
2. Identify longest spans
3. Drill down into slow operations

### Configuration in Services

```properties
# application.properties
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans
```

**Sampling Rates**:
- `1.0` = 100% (development)
- `0.1` = 10% (production, high traffic)
- `0.01` = 1% (production, very high traffic)

---

## ELK Stack Logging

### Architecture

```
Applications
    │
    ├─ Logback JSON encoder
    │
    ▼
Logstash (TCP:5000)
    │
    ├─ Parse logs
    ├─ Filter/Transform
    │
    ▼
Elasticsearch
    │
    ├─ Index logs
    ├─ Store & search
    │
    ▼
Kibana (UI)
    │
    └─ Visualize & query
```

### Logstash Pipeline

**monitoring/logstash/pipeline/logstash.conf**:
```conf
input {
  tcp {
    port => 5000
    codec => json
  }
}

filter {
  if [logger_name] =~ "com.perpustakaan" {
    mutate {
      add_field => { "[@metadata][index]" => "app-logs" }
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "%{[@metadata][index]}-%{+YYYY.MM.dd}"
  }
  
  stdout {
    codec => rubydebug
  }
}
```

### Kibana Setup

#### 1. Create Index Pattern

1. Open Kibana: http://localhost:5601
2. Go to **Management** → **Stack Management**
3. Click **Index Patterns**
4. Click **Create index pattern**
5. Enter pattern: `app-logs-*`
6. Select timestamp field: `@timestamp`
7. Click **Create index pattern**

#### 2. Discover Logs

1. Go to **Discover** menu
2. Select index pattern: `app-logs-*`
3. View recent logs

### Searching Logs

#### Basic Search

**By Application**:
```
app_name: "service-anggota"
```

**By Log Level**:
```
level: "ERROR"
```

**By Message**:
```
message: "Publishing event to RabbitMQ"
```

#### KQL (Kibana Query Language)

**AND Query**:
```
app_name: "service-anggota" AND level: "ERROR"
```

**OR Query**:
```
level: "ERROR" OR level: "WARN"
```

**Wildcard**:
```
message: *RabbitMQ*
```

**Range**:
```
response_time > 1000
```

**Time Range**:
```
@timestamp >= "2024-01-15" AND @timestamp <= "2024-01-16"
```

### Useful Log Queries

#### 1. Find All Errors
```
level: "ERROR"
```

#### 2. RabbitMQ Events
```
message: "Publishing event" OR message: "Received event"
```

#### 3. API Requests
```
message: "API REQUEST"
```

#### 4. Slow Queries
```
query_time > 1000
```

#### 5. Failed Transactions
```
message: *failed* OR message: *error*
```

### Creating Visualizations

#### 1. Error Rate Over Time

**Visualization Type**: Line Chart
**Query**: `level: "ERROR"`
**Metrics**: Count
**Buckets**: Date Histogram on @timestamp

#### 2. Logs by Service

**Visualization Type**: Pie Chart
**Metrics**: Count
**Buckets**: Terms on `app_name`

#### 3. Log Levels Distribution

**Visualization Type**: Vertical Bar
**Metrics**: Count
**Buckets**: Terms on `level`

### Creating Dashboards

1. Go to **Dashboard** → **Create dashboard**
2. Click **Add** → Select visualizations
3. Arrange panels
4. Click **Save**

---

## RabbitMQ Monitoring

### RabbitMQ Management UI

Access: http://localhost:15672
Login: guest/guest

### Overview Dashboard

Shows:
- **Total Queues**
- **Total Connections**
- **Total Channels**
- **Message Rate** (publish/deliver)
- **Unacked Messages**

### Monitoring Queues

1. Click **Queues** tab
2. View all queues:
   - `anggota-sync-queue`
   - `buku-sync-queue`
   - `peminjaman-sync-queue`
   - `pengembalian-sync-queue`

**Key Metrics**:
- **Ready**: Messages waiting to be delivered
- **Unacked**: Messages being processed
- **Total**: Total messages in queue
- **Publish Rate**: Messages/second published
- **Deliver Rate**: Messages/second delivered

### Monitoring Exchanges

1. Click **Exchanges** tab
2. View exchanges and bindings
3. Check message rates

### Inspecting Messages

1. Click queue name
2. Scroll to **Get messages** section
3. Set:
   - **Messages**: Number to retrieve
   - **Ackmode**: automatic/manual
4. Click **Get Message(s)**
5. View message payload

### Connection & Channels

**Connections**:
- Shows all active connections
- Client IP addresses
- Channels per connection

**Channels**:
- Active message channels
- Consumer counts
- Prefetch settings

### Troubleshooting

#### Messages Piling Up
```
Ready messages > 100
```
**Actions**:
- Check consumer health
- Verify consumer count
- Check processing time

#### No Consumers
```
Consumers = 0
```
**Actions**:
- Restart consumer service
- Check service logs
- Verify queue bindings

---

## Health Checks

### Actuator Health Endpoints

All services expose health endpoints:

```bash
# Eureka
curl http://localhost:8761/actuator/health

# API Gateway
curl http://localhost:8080/actuator/health

# Service Anggota
curl http://localhost:8081/actuator/health

# Service Buku
curl http://localhost:8082/actuator/health

# Service Peminjaman
curl http://localhost:8083/actuator/health

# Service Pengembalian
curl http://localhost:8084/actuator/health
```

### Health Response

```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 123456789,
        "threshold": 10485760,
        "exists": true
      }
    },
    "mongo": {
      "status": "UP",
      "details": {
        "version": "6.0.0"
      }
    },
    "rabbit": {
      "status": "UP",
      "details": {
        "version": "3.13.0"
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Automated Health Check Script

```bash
#!/bin/bash

services=(
  "eureka-server:8761"
  "api-gateway:8080"
  "service-anggota:8081"
  "service-buku:8082"
  "service-peminjaman:8083"
  "service-pengembalian:8084"
)

echo "=== Health Check Report ==="
echo "Timestamp: $(date)"
echo ""

for service in "${services[@]}"; do
  name=$(echo $service | cut -d: -f1)
  url="http://$service/actuator/health"
  
  response=$(curl -s -o /dev/null -w "%{http_code}" $url)
  
  if [ $response -eq 200 ]; then
    echo "✅ $name: UP"
  else
    echo "❌ $name: DOWN (HTTP $response)"
  fi
done
```

---

## Alerting

### Prometheus Alert Rules

**monitoring/prometheus/alerts.yml**:

```yaml
groups:
  - name: service_alerts
    interval: 30s
    rules:
      - alert: HighErrorRate
        expr: |
          sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (application)
          /
          sum(rate(http_server_requests_seconds_count[5m])) by (application)
          > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate on {{ $labels.application }}"
          description: "{{ $labels.application }} has error rate > 5% for 5 minutes"
      
      - alert: HighMemoryUsage
        expr: |
          jvm_memory_used_bytes{area="heap"}
          /
          jvm_memory_max_bytes{area="heap"}
          > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage on {{ $labels.application }}"
          description: "{{ $labels.application }} using > 90% heap memory"
      
      - alert: ServiceDown
        expr: up == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Service {{ $labels.job }} is down"
          description: "{{ $labels.job }} has been down for more than 1 minute"
      
      - alert: SlowResponseTime
        expr: |
          histogram_quantile(0.95,
            sum(rate(http_server_requests_seconds_bucket[5m])) by (application, le)
          ) > 2
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Slow response time on {{ $labels.application }}"
          description: "95th percentile response time > 2s for 10 minutes"
```

### Grafana Alerts

#### 1. Create Alert from Panel

1. Edit panel
2. Go to **Alert** tab
3. Click **Create Alert**
4. Configure:
   - **Condition**: When avg() OF query(A) IS ABOVE 0.9
   - **Evaluate every**: 1m
   - **For**: 5m
5. Add notification channel
6. Save

#### 2. Notification Channels

**Email**:
```yaml
apiVersion: 1
notifiers:
  - name: email-alerts
    type: email
    settings:
      addresses: admin@example.com
```

**Slack**:
```yaml
notifiers:
  - name: slack-alerts
    type: slack
    settings:
      url: https://hooks.slack.com/services/YOUR/WEBHOOK/URL
      recipient: '#alerts'
```

---

[⬅️ Back to Main Documentation](../README.md)