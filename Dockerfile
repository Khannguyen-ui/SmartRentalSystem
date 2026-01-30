# ===== Stage 1: Build =====
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom trước để cache dependency
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source
COPY src ./src

# Build
RUN mvn clean package -DskipTests


# ===== Stage 2: Run =====
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Thêm giới hạn RAM (ví dụ Max 80% RAM của container hoặc fix cứng 512m)
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
