# Java 21 (official & stable)
FROM eclipse-temurin:21-jdk-jammy

# Set working directory
WORKDIR /app

# Copy Maven wrapper & pom
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Give execute permission
RUN chmod +x mvnw

# Download dependencies (cache layer)
RUN ./mvnw dependency:go-offline

# Copy source
COPY src ./src

# Build app
RUN ./mvnw clean package -DskipTests

# Expose app port
EXPOSE 8080

# Run app
CMD ["sh", "-c", "java -jar target/*.jar"]