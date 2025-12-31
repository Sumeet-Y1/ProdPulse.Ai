# Use official Eclipse Temurin Java 21 JDK
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first
COPY mvnw pom.xml ./
COPY .mvn/ .mvn/

# Give execution permission to Maven wrapper
RUN chmod +x mvnw

# Download dependencies offline
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the project and skip tests
RUN ./mvnw clean package -DskipTests -B

# Expose the port your Spring Boot app uses
EXPOSE 8080

# Run the built jar with exact name
CMD ["java", "-jar", "target/prodpulse-backend-0.0.1-SNAPSHOT.jar"]