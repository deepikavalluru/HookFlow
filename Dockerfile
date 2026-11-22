# Stage 1: Build the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration and source code
COPY pom.xml .
COPY src ./src

# Build the executable JAR
RUN mvn -B -DskipTests clean package


# Stage 2: Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the generated JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Run as a non-root user
USER 10001

# Spring Boot application port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]