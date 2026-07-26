# Stage 1: Build stage using Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy root pom and code style configs for caching
COPY pom.xml checkstyle.xml ./

# Copy submodule pom files to cache dependency resolution
COPY common-library/pom.xml ./common-library/
COPY api-gateway/pom.xml ./api-gateway/
COPY auth-service/pom.xml ./auth-service/
COPY event-service/pom.xml ./event-service/
COPY seat-service/pom.xml ./seat-service/
COPY booking-service/pom.xml ./booking-service/
COPY payment-service/pom.xml ./payment-service/
COPY notification-service/pom.xml ./notification-service/
COPY analytics-service/pom.xml ./analytics-service/

# Download dependencies for offline caching
RUN mvn dependency:go-offline -pl notification-service -am -B || true

# Copy source code and build the application artifact
COPY common-library ./common-library
COPY notification-service ./notification-service
RUN mvn clean package -pl notification-service -am -DskipTests -Dcheckstyle.skip=true -Dspotless.check.skip=true -B

# Stage 2: Minimal runtime stage using Java 21 JRE
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/notification-service/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
