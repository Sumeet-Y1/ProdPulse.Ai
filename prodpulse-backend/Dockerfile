# Use official OpenJDK 21 from Eclipse Temurin (works on Render)
FROM eclipse-temurin:21-jdk-focal

# Set working directory inside container
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for dependency caching)
COPY mvnw pom.xml ./
COPY .mvn/ .mvn/

# Download dependencies only
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the project and skip tests for faster builds
RUN ./mvnw clean package -DskipTests -B

# Expose the port your app will run on
EXPOSE 8080

# Run the jar file (any jar in target folder)
CMD ["sh", "-c", "java -jar target/*.jar"]