# VoteChain Zero

A zero-error, blockchain-backed e-voting system built with Spring Boot + PostgreSQL.

## Tech Stack
- Java 21, Spring Boot 3.3.2 (Web, Data JPA, Security, Validation, WebSocket, Mail, Actuator)
- PostgreSQL 16 (via Docker)
- JWT (jjwt) for auth
- Springdoc OpenAPI (Swagger UI)
- Lombok

## Getting Started

Run everything (Postgres + pgAdmin + the app itself) with a single command —
no local Maven or JDK install required:

```bash
docker-compose up
```

- Health check: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- pgAdmin: http://localhost:5050 (login: `admin@votechain.local` / `admin`)



## How the blockchain engine works

Each `Election` owns its own independent chain of `Block`s.

1. **Genesis** — `BlockchainService.createGenesisBlock()` creates block #0 with `previousHash` = 64 zeros (the "no parent" convention).
2. **Casting a vote** — `castVote()` checks the voter hasn't already voted (`VoteTransactionRepository.existsByElectionIdAndVoterHash`), then saves an **unmined** `VoteTransaction`. It sits in the "mempool" until enough votes queue up.
3. **Mining** — once `votes-per-block` (default 5, in `application.yml`) transactions are pending, `mineBlock()` batches them, computes a `MerkleTree` root over their transaction hashes, links to the previous block's hash, and runs **Proof-of-Work**: incrementing `nonce` until the block's SHA-256 hash starts with N zeros (`mining-difficulty`, default 4).
4. **Validation** — `validateChain()` walks every block, recomputes its hash and merkle root from scratch, and compares against what's stored. Any mismatch — whether someone edited a block's data or a transaction's candidate ID directly in the DB — gets caught and reported. This powers the tamper-detection alert and the "Chain Verified ✅" badge in the UI.

## How auth works

Two-factor by design — password alone never issues a session token.

**New voter:**
1. `POST /api/auth/register` — account created (unverified), government ID hashed and never stored raw, OTP emailed
2. `POST /api/auth/verify-otp` — completes registration and returns a JWT in the same step

**Returning voter:**
1. `POST /api/auth/login` — checks email + password, emails a fresh OTP (no token yet)
2. `POST /api/auth/verify-otp` — second factor confirmed, JWT returned

All endpoints except `/api/auth/**`, Swagger, and the health check require a valid JWT (`Authorization: Bearer <token>`), enforced statelessly via `JwtAuthFilter` on every request — no server-side session storage.

Authon : Shreyash Jokare