# ===== Stage 1: Build the application =====
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# ===== Stage 2: Run the application =====
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9000
ENTRYPOINT ["java","-jar","app.jar"]