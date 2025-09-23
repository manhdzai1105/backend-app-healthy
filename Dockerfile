# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run with JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Set timezone
ENV TZ=Asia/Ho_Chi_Minh

# Tạo user không phải root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy jar và thay đổi quyền sở hữu
COPY --from=builder /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

# Chạy bằng user thường
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
