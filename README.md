# VoteChain Zero

A zero-error, blockchain-backed e-voting system built with Spring Boot + PostgreSQL.

## Tech Stack (Phase 1)
- Java 21
- Spring Boot 3.3.2 (Web, Data JPA, Security, Validation, WebSocket, Mail, Actuator)
- PostgreSQL 16 (via Docker)
- JWT (jjwt) for auth
- Springdoc OpenAPI (Swagger UI)
- Lombok



## Getting Started

### 1. Start PostgreSQL (Docker)
```bash
docker-compose up -d
```
This starts:
- PostgreSQL on `localhost:5432` (db: `votechain_zero`, user: `votechain_user`, pass: `votechain_pass`)
- pgAdmin on `localhost:5050` (login: `admin@votechain.local` / `admin`) — optional, for viewing the DB visually

### 2. Configure secrets locally
Copy the mail/JWT secrets in `src/main/resources/application.yml` into a local override file
`src/main/resources/application-local.yml` (already gitignored) and run with:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
For now, in early dev, running with the default `application.yml` is fine.

### 3. Run the app
```bash
mvn clean install
mvn spring-boot:run
```

### 4. Verify it's up
- Health check: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html

> Note: This sandbox environment doesn't have internet access to Maven Central, so
> the build hasn't been compiled/tested here. Run `mvn clean install` on your own
> machine (with internet access) to pull dependencies and verify the build.

## Roadmap
- [x] **Phase 1** — Project setup, DB config, folder structure ← *you are here*
- [ ] **Phase 2** — Entities (Voter, Election, Candidate) + Core blockchain engine (Block, Chain, Merkle tree)
- [ ] **Phase 3** — Voter auth (JWT + OTP), Election/Candidate admin APIs
- [ ] **Phase 4** — Vote casting → blockchain transaction, block mining, chain validation, verification API
- [ ] **Phase 5** — React + Tailwind frontend (voter flow, admin dashboard, blockchain explorer, live results)
- [ ] **Phase 6** — Extra features (digital signatures, QR voter slips, audit PDF export), Docker deployment

## Next Step
Phase 2: design the database schema (entities) and build the core blockchain engine
(`Block`, `Blockchain`, SHA-256 hashing, Merkle tree, Proof-of-Authority mining).
=======
# VoteChain-Zero
A blockchain-backed voting system It's zero-error friendly, fully explainable, and you don't need external blockchain infra

