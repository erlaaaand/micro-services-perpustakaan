# ... (Stage 1 Builder SAMA) ...
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn clean package -DskipTests -B

# ... (Stage 2 Runtime) ...
# GANTI KE NON-ALPINE AGAR STABIL
FROM eclipse-temurin:17-jre 

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

# Install Curl untuk Healthcheck (karena image ini tidak bawa wget/curl default kadang)
RUN apt-get update && apt-get install -y curl

# --- PERHATIKAN PORT INI ---
EXPOSE 8761

# --- PERHATIKAN PORT INI ---
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=5 \
  CMD curl -f http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]