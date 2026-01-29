# Giai đoạn 1: Build ứng dụng
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml và tải dependency trước để tận dụng cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy mã nguồn và build
COPY src ./src
RUN mvn clean package -DskipTests

# Giai đoạn 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy file jar từ stage build
COPY --from=build /app/target/*.jar app.jar

# Port mặc định của Spring Boot
EXPOSE 8080

# Chạy với múi giờ Việt Nam (Quan trọng cho tính năng đặt lịch)
ENTRYPOINT ["java", "-Duser.timezone=Asia/Ho_Chi_Minh", "-jar", "app.jar"]