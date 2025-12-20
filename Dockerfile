# Giai đoạn 1: Build ứng dụng
# Sử dụng Maven và OpenJDK 21 (đúng với java.version trong pom.xml của bạn)
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy file pom.xml để tải thư viện trước (tối ưu cache)
COPY pom.xml .
# Tải toàn bộ dependency (bao gồm Cloudinary, VNPay, JWT, v.v.)
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn src
COPY src ./src

# Build ra file .jar (Bỏ qua test để chạy nhanh hơn)
RUN mvn clean package -DskipTests

# Giai đoạn 2: Chạy ứng dụng
# Sử dụng JRE 21 bản nhẹ (Alpine) để chạy
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy file .jar từ giai đoạn build sang
# File jar sẽ có tên backend-0.0.1-SNAPSHOT.jar dựa theo artifactId trong pom.xml
COPY --from=build /app/target/*.jar app.jar

# Mở cổng 8080
EXPOSE 8080

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]