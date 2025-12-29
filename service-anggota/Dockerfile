# ============================================
# DOCKERFILE STANDARD (FIXED NETWORK ISSUE)
# Gunakan ini untuk: Eureka, Gateway, & Services
# ============================================

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml dan download dependencies (untuk caching)
COPY pom.xml .

# --- BAGIAN YG DIPERBAIKI ---
# Tambahkan '|| true' agar tidak error saat koneksi tidak stabil
RUN mvn dependency:go-offline -B || true

# Copy source code dan build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Add non-root user untuk security
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Change ownership to spring user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose port (Ini cuma dokumentasi, port asli diatur via docker-compose)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application with optimized JVM settings
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-jar", \
  "app.jar"]