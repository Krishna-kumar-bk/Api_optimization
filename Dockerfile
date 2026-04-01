# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# go into correct folder
COPY api-observability-platform ./api-observability-platform
WORKDIR /app/api-observability-platform

RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=build /app/api-observability-platform/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]