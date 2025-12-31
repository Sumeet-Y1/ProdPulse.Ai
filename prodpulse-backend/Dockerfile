# Stage 1: Build the JAR using Maven
FROM maven:3.9.12-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first (for caching dependencies)
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy all source code
COPY src ./src

# Package the application without running tests
RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/target/prodpulse-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java","-jar","app.jar"]