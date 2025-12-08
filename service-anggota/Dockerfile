# Gunakan base image Java 17 (sesuai pom.xml Anda)
FROM eclipse-temurin:17-jdk-alpine

# Argument untuk nama file JAR
ARG JAR_FILE=target/*.jar

# Copy file JAR hasil build ke dalam image
COPY ${JAR_FILE} app.jar

# Jalankan aplikasi
ENTRYPOINT ["java","-jar","/app.jar"]