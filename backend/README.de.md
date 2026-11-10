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
   # Geheimnis ("Pepper") für das Passwort-Hashing. Erzeugen mit: openssl rand -base64 48
   PASSWORD_PEPPER=erzeugten-wert-einfuegen
   # Optional, mit diesen Standardwerten:
   # DB_NAME=memdeck
   # DB_PORT=5432
   # DB_URL=jdbc:postgresql://localhost:5432/memdeck
   # SESSION_COOKIE_SECURE=true   (nur auf false setzen, wenn der Browser das Cookie auf http://localhost ablehnt)
   ```

   Wenn du `DB_PORT` änderst, passe auch den Port in `DB_URL` an.

   `PASSWORD_PEPPER` ist erforderlich (die Anwendung startet sonst nicht) und muss mindestens 32 Bytes in Base64 haben. Geht er verloren oder wird er geändert, funktionieren bestehende Passwörter nicht mehr.

2. Starte die Datenbank und die Anwendung:

   ```bash
   docker compose up -d
   ./mvnw spring-boot:run
   ```

Die `.env`-Datei wird sowohl von Docker Compose als auch von der Anwendung gelesen.

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
