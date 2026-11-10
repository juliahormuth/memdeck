# Memdeck - Backend

[Português](README.md) | **English**

REST API for Memdeck, built with Java 21, Spring Boot 4.1, Spring Data JPA, PostgreSQL and Flyway.

## Requirements

- Java 21
- Maven (or the included `./mvnw`)
- Docker (local database and tests)

## Running locally

1. Create a `.env` file in this folder (it is not versioned). The password is required and has no default:

   ```properties
   DB_USER=memdeck
   DB_PASSWORD=choose-a-password
   # Optional, with these defaults:
   # DB_NAME=memdeck
   # DB_PORT=5432
   # DB_URL=jdbc:postgresql://localhost:5432/memdeck
   ```

   If you change `DB_PORT`, change the port in `DB_URL` as well.

2. Start the database and the application:

   ```bash
   docker compose up -d
   ./mvnw spring-boot:run
   ```

The `.env` file is read by both Docker Compose and the application.

## API documentation

With the application running:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI document (JSON): http://localhost:8080/v3/api-docs

## Tests

```bash
./mvnw verify
```

Integration tests use Testcontainers and need Docker.

## Architecture

Clean Architecture organized by feature. Each feature (for example, `deck`) has these layers:

- `domain`: business rules, plain Java, framework-free
- `application`: use cases and the interfaces (gateways) they need
- `infrastructure`: JPA persistence
- `presentation`: REST controllers and DTOs

An ArchUnit test (`ArchitectureTest`) enforces the dependency rule between layers.
