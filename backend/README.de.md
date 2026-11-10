# Memdeck – Backend

[Português](README.md) | [English](README.en.md) | **Deutsch**

REST-API von Memdeck, entwickelt mit Java 21, Spring Boot 4.1, Spring Data JPA, PostgreSQL und Flyway.

## Voraussetzungen

- Java 21
- Maven (oder der enthaltene `./mvnw`)
- Docker (lokale Datenbank und Tests)

## Lokal ausführen

1. Lege in diesem Ordner eine Datei `.env` an (sie wird nicht versioniert). Das Passwort ist erforderlich und hat keinen Standardwert:

   ```properties
   DB_USER=memdeck
   DB_PASSWORD=waehle-ein-passwort
   # Optional, mit diesen Standardwerten:
   # DB_NAME=memdeck
   # DB_PORT=5432
   # DB_URL=jdbc:postgresql://localhost:5432/memdeck
   ```

   Wenn du `DB_PORT` änderst, passe auch den Port in `DB_URL` an.

2. Starte die Datenbank und die Anwendung:

   ```bash
   docker compose up -d
   ./mvnw spring-boot:run
   ```

Die `.env`-Datei wird sowohl von Docker Compose als auch von der Anwendung gelesen.

## API-Dokumentation

Bei laufender Anwendung:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI-Dokument (JSON): http://localhost:8080/v3/api-docs

## Tests

```bash
./mvnw verify
```

Die Integrationstests verwenden Testcontainers und benötigen Docker.

## Architektur

Clean Architecture, nach Funktionalität (Feature) organisiert. Jede Funktionalität (zum Beispiel `deck`) hat diese Schichten:

- `domain`: Geschäftsregeln in reinem Java, ohne Framework
- `application`: Anwendungsfälle und die Schnittstellen (Gateways), die sie benötigen
- `infrastructure`: Persistenz mit JPA
- `presentation`: REST-Controller und DTOs

Ein ArchUnit-Test (`ArchitectureTest`) stellt die Abhängigkeitsregel zwischen den Schichten sicher.
