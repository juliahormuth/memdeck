# Memdeck - Backend

**Português** | [English](README.en.md) | [Deutsch](README.de.md)

API REST do Memdeck, feita com Java 21, Spring Boot 4.1, Spring Data JPA, PostgreSQL e Flyway.

## Requisitos

- Java 21
- Maven (ou o `./mvnw` incluído)
- Docker (banco local e testes)

## Como rodar localmente

1. Crie o arquivo `.env` nesta pasta (ele não é versionado). A senha é obrigatória e não tem valor padrão:

   ```properties
   DB_USER=memdeck
   DB_PASSWORD=escolha-uma-senha
   # Segredo ("pepper") usado no hash das senhas. Gere com: openssl rand -base64 48
   PASSWORD_PEPPER=cole-o-valor-gerado
   # Opcionais, com estes valores padrão:
   # DB_NAME=memdeck
   # DB_PORT=5432
   # DB_URL=jdbc:postgresql://localhost:5432/memdeck
   # SESSION_COOKIE_SECURE=true   (troque para false apenas se o navegador recusar o cookie em http://localhost)
   ```

   Se mudar o `DB_PORT`, ajuste também a porta no `DB_URL`.

   O `PASSWORD_PEPPER` é obrigatório (a aplicação não sobe sem ele) e deve ter pelo menos 32 bytes em Base64. Se ele for perdido ou trocado, as senhas já cadastradas deixam de funcionar.

2. Suba o banco e a aplicação:

   ```bash
   docker compose up -d
   ./mvnw spring-boot:run
   ```

O `.env` é lido tanto pelo Docker Compose quanto pela aplicação.

## Testes

```bash
./mvnw verify
```

Os testes de integração usam Testcontainers e precisam do Docker.

## Arquitetura

Arquitetura Limpa organizada por funcionalidade. Cada funcionalidade (por exemplo, `deck`) tem as camadas:

- `domain`: regras de negócio, em Java puro, sem framework
- `application`: casos de uso e as interfaces (gateways) que eles precisam
- `infrastructure`: persistência com JPA
- `presentation`: controllers REST e DTOs

Um teste com ArchUnit (`ArchitectureTest`) garante a regra de dependência entre as camadas.
