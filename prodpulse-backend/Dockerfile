# Use official OpenJDK 21 from Docker Hub (public, no auth)
FROM openjdk:21

# Set working directory inside container
WORKDIR /app

# Copy Maven wrapper and pom.xml first
COPY mvnw pom.xml ./
COPY .mvn/ .mvn/

# Download dependencies offline
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the project and skip tests
RUN ./mvnw clean package -DskipTests -B

# Expose the port your app runs on
EXPOSE 8080

# Run the built jar file
CMD ["sh", "-c", "java -jar target/*.jar"]