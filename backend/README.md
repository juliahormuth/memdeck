# Memdeck backend

REST API for Memdeck, built with Java 21, Spring Boot 4.1, Spring Data JPA, PostgreSQL and Flyway.

## Requirements
- Java 21
- Maven (or use the included `./mvnw`)
- Docker (local database and tests)

## Running locally
```bash
cp .env.example .env       
docker compose up -d       
./mvnw spring-boot:run
```
The `.env` file is read by both Docker Compose and the application. It is git-ignored.

## Tests
```bash
./mvnw verify              
```

## Endpoints
| Method | Path | Description |
|---|---|---|
| POST | `/api/decks` | Create a deck (`name`, `description`, `visibility`: `PUBLIC` or `PRIVATE`) |
| GET | `/api/decks/{id}` | Get a deck by id |

