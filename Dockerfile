# Build stage using Maven 3.9.6 with Eclipse Temurin JDK 21 Alpine
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application jar file (skipping tests as databases are not active during image build)
RUN mvn clean package -DskipTests

# Run stage using Eclipse Temurin JRE 21 Alpine for a lightweight runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/fundoonotes-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8086
EXPOSE 8086

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
