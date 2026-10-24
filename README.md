# VoteChain Zero

A tamper-evident, blockchain-backed voting system. Every vote is a transaction mined into a Proof-of-Work chain, so any change to recorded votes is detected automatically.

**Stack:** Java 21 · Spring Boot 3.3 · PostgreSQL 16 · Docker Compose · React · Vite · Tailwind CSS · Recharts

---

## Features

### Blockchain engine
- Custom blockchain written in Java: SHA-256 hashing, Merkle tree per block, Proof-of-Work mining (difficulty 4, 5 votes per block)
- One independent chain per election, starting from a genesis block
- Full chain validation: every hash and Merkle root is recomputed and cross-checked
- Background integrity monitor re-validates chains on a schedule
- Leftover pending votes are mined into a final block when an election closes (manually or automatically)
- Tamper demo: an admin-only endpoint corrupts a block so the explorer can show "chain compromised"

### Voting and privacy
- One vote per voter per election, enforced server-side
- Voter anonymity: votes store only a SHA-256 hash of the voter ID, with no link back to the voter record
- Vote receipts with transaction hash
- **Results are admin-only.** Voters never see live counts or who is leading, so results cannot influence voting. The backend strips vote counts from every response for non-admin users, and the frontend also hides the results pages

### Elections
- Lifecycle: Draft → Active → Closed
- Auto-close when the end time passes (checked on read and by a scheduled monitor)
- Ballot is disabled until the voting window is open (Scheduled / Open / Closed phases in the UI)
- Candidates with party name and symbol image (initials fallback)

### Admin panel
- Create elections, add candidates, activate and close
- Live results and analytics: total, confirmed and pending votes, lead margin, vote share per candidate, stacked bar chart
- Winner declared once the election is closed and all votes are confirmed (ties and zero-vote cases handled)
- Blockchain explorer with chain verified / compromised status

### Security
- Password-based login with JWT (stateless)
- Role-based access (ADMIN / VOTER) with method-level security
- Admin account seeded from environment variables
- Global exception handler with clean error messages (no stack traces leaked)
- CORS restricted to the frontend origin

### UI
- Custom dark "ledger" design, two-column auth pages, and a responsive layout

---

## Getting started

### Prerequisites
- Docker Desktop
- Node.js 18+

### 1. Configure environment
Create a `.env` file in the project root (it is git-ignored):

```
ADMIN_EMAIL=admin@votechain.local
ADMIN_PASSWORD=choose-a-strong-password
MAIL_USERNAME=
MAIL_APP_PASSWORD=
```

### 2. Start the backend and database
```
docker-compose up
```
The API runs on http://localhost:8080 and Swagger UI is at http://localhost:8080/swagger-ui.html.

### 3. Start the frontend
```
cd frontend
npm install
npm run dev
```
Open http://localhost:5173.

### 4. Log in
- Admin: the `ADMIN_EMAIL` and `ADMIN_PASSWORD` from `.env`
- Voters: register from the sign-up page

---

## How it works

1. **Cast vote:** the vote enters a pending pool as a transaction tied to an anonymous voter hash.
2. **Mining:** when 5 votes are pending, they are grouped into a block with a Merkle root and mined with Proof-of-Work, then linked to the previous block's hash.
3. **Closing:** when an election closes, any remaining pending votes are mined into a final block.
4. **Verification:** the explorer recomputes every hash and Merkle root. Any mismatch flags the chain as compromised.

Live counts = confirmed (mined) votes + pending votes. Only admins can see them.

---

## API overview

| Area | Endpoint | Access |
|---|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` | Public |
| Elections | `GET /api/elections`, `GET /api/elections/{id}` | Authenticated (vote counts included for admins only) |
| Elections | `POST /api/elections`, `/{id}/activate`, `/{id}/close` | Admin |
| Candidates | add candidate to an election | Admin |
| Voting | cast vote | Voter |
| Chain | view and verify chain | Authenticated |
| Chain | `POST /api/elections/{id}/chain/simulate-tamper` | Admin |

---

## Notes
- The containers run in the `Asia/Kolkata` timezone so the voting window matches what the UI shows. Change `TZ` in `docker-compose.yml` for another region.
- Elections flagged as tampered before the hashing fix remain flagged; create a new election to test a clean chain.
- This is a learning and portfolio project, not a production election system.

