# FundooNotes - Spring Boot Backend

FundooNotes is a robust Spring Boot backend application featuring JWT Authentication, Google OAuth2, Redis caching, RabbitMQ message queuing, and PostgreSQL database storage.

## Features
- **User Authentication**: Secure signup, login, OTP verification, and JWT-based authentication.
- **Note Management**: CRUD operations for notes, labels, pins, archives, trash, and reminders.
- **Caching**: Performance optimized note retrieval using Redis.
- **Async Processing**: Asynchronous tasks (like emails and OTP dispatches) handled via RabbitMQ.
- **API Documentation**: Interactive API testing through Swagger/OpenAPI.

---

## Prerequisites
Before running or deploying the application, ensure you have:
- **Java**: JDK 21
- **Maven**: 3.8+ (or use the included Maven wrapper `mvnw`)
- **Docker & Docker Compose**: Required for containerized deployment.
- **Local Services (if not using Docker)**:
  - PostgreSQL (port 5432)
  - Redis (port 6379)
  - RabbitMQ (ports 5672 and 15672)
  - SMTP credentials (e.g. Gmail App Password) for sending mail

---

## Environment Setup
1. Copy the environment variables template:
   ```bash
   cp .env.example .env
   ```
2. Open `.env` and fill in the required fields, specifically:
   - `DB_PASSWORD`: PostgreSQL database password.
   - `MAIL_USERNAME` and `MAIL_PASSWORD`: Your SMTP email credentials.
   - `JWT_SECRET`: A secure 64-character hex string.

---

## Deployment Options

### Option 1: Docker Compose (Recommended)
You can deploy the entire application environment including PostgreSQL, Redis, RabbitMQ, and the Spring Boot application using a single command:

1. Build and run the stack:
   ```bash
   docker-compose up -d --build
   ```
2. Verify all services are running:
   ```bash
   docker-compose ps
   ```
3. View logs of the Spring Boot application:
   ```bash
   docker-compose logs -f app
   ```
4. Stop the services:
   ```bash
   docker-compose down
   ```

### Option 2: Local Development Run
If you have PostgreSQL, Redis, and RabbitMQ running locally:

1. Package the project:
   ```bash
   sh mvnw package -DskipTests
   ```
2. Run the application:
   ```bash
   java -jar target/fundoonotes-0.0.1-SNAPSHOT.jar
   ```
   *Alternatively, run directly with maven:*
   ```bash
   sh mvnw spring-boot:run
   ```

---

## API Documentation
Once the application is running, you can access:
- **Interactive Swagger UI**: [http://localhost:8086/swagger-ui.html](http://localhost:8086/swagger-ui.html)
- **OpenAPI v3 Docs**: [http://localhost:8086/v3/api-docs](http://localhost:8086/v3/api-docs)
