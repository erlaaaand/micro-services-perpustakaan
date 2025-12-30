# ... (Stage 1 Builder SAMA) ...
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn clean package -DskipTests -B

# ... (Stage 2 Runtime) ...
FROM eclipse-temurin:17-jre 

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
RUN apt-get update && apt-get install -y curl

# --- PERHATIKAN PORT INI ---
EXPOSE 8084

# --- PERHATIKAN PORT INI ---
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=5 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]