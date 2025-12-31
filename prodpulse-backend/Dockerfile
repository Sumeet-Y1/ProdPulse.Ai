# Use official OpenJDK 21 (guaranteed to exist)
FROM openjdk:21-jdk

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first
COPY mvnw pom.xml ./
COPY .mvn/ .mvn/

# Download dependencies offline
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the project and skip tests
RUN ./mvnw clean package -DskipTests -B

# Expose the port
EXPOSE 8080

# Run the jar file
CMD ["sh", "-c", "java -jar target/*.jar"]