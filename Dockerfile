# Use Gradle image for building
FROM gradle:8.5.0-jdk21 AS build

# Set working directory
WORKDIR /app

# Copy gradle files first for better Docker layer caching
COPY build.gradle settings.gradle ./

# Copy gradle wrapper
COPY gradle gradle
COPY gradlew ./

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy source code
COPY src ./src

# Build the application (skip dependencies step as it will be resolved during build)
RUN ./gradlew build -x test --no-daemon

# Use Eclipse Temurin JRE runtime image
FROM eclipse-temurin:21-jre

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the built JAR file from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "app.jar"]
