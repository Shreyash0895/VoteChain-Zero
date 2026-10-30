# VoteChain Zero

A tamper-evident, blockchain-backed voting system. Every vote is a digitally signed transaction mined into a Proof-of-Work chain, so any change to recorded votes is detected automatically, and every voter can independently verify that their vote was counted.

**Stack:** Java 21 · Spring Boot 3.3 · PostgreSQL 16 · Docker Compose · React · Vite · Tailwind CSS · Recharts · WebCrypto (ECDSA)

---

## Features

### Blockchain engine
- Custom blockchain written in Java: SHA-256 hashing, Merkle tree per block, Proof-of-Work mining (difficulty 4, 5 votes per block)
- One independent chain per election, starting from a genesis block
- Full chain validation: every hash, Merkle root and vote signature is recomputed and cross-checked
- Background integrity monitor re-validates chains on a schedule
- Leftover pending votes are mined into a final block when an election closes (manually or automatically)
- Tamper demo: an admin-only endpoint corrupts a block so the explorer can show "chain compromised"

### Digital signatures
- Each voter's browser generates an ECDSA P-256 key pair. The private key is stored as a non-extractable key in IndexedDB and never leaves the device
- The public key is registered once per voter and cannot be replaced
- Every vote is signed in the browser over `electionId:candidateId`; the server rejects votes with a missing or invalid signature
- Chain validation re-verifies every signature, so changing a vote's candidate in the database is detected

### Vote receipt verification
- After voting, the voter gets a receipt hash
- The verify page returns a Merkle proof (block number, block hash, sibling hashes) and recomputes it in the browser against the on-chain Merkle root
- Shows signature validity and the signing key's fingerprint
- The proof never reveals who the voter chose

### Privacy and results
- Voter anonymity: votes store only a SHA-256 hash of the voter ID, with no link back to the voter record
- One vote per voter per election, enforced server-side
- **Live results are admin-only.** While an election is active, vote counts are removed from API responses for non-admin users, so results cannot influence voting
- **After the election closes, results are public.** The ballot page shows the winner, winning margin, a bar chart and standings

### Elections
- Lifecycle: Draft → Active → Closed
- Auto-close when the end time passes (checked on read and by a scheduled monitor)
- Ballot is disabled until the voting window is open (Scheduled / Open / Closed phases in the UI)
- Candidates with party name and symbol image (initials fallback)

### Admin panel
- Create elections, add candidates, activate and close
- Live analytics: total, confirmed and pending votes, lead margin, vote share per candidate, stacked bar chart
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

1. **Sign and cast:** the voter's browser signs `electionId:candidateId`. The server verifies the signature against the voter's registered public key, then queues the vote as a transaction tied to an anonymous voter hash.
2. **Mining:** when 5 votes are pending, they are grouped into a block with a Merkle root and mined with Proof-of-Work, then linked to the previous block's hash.
3. **Closing:** when an election closes, any remaining pending votes are mined into a final block, and results become public.
4. **Verification:** the explorer recomputes every hash, Merkle root and signature. Any mismatch flags the chain as compromised. Voters can verify their own vote with a Merkle proof.

Live counts = confirmed (mined) votes + pending votes. Only admins can see them until the election closes.

---

## API overview

| Area | Endpoint | Access |
|---|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` | Public |
| Elections | `GET /api/elections`, `GET /api/elections/{id}` | Authenticated (vote counts: admins always, others only after close) |
| Elections | `POST /api/elections`, `/{id}/activate`, `/{id}/close` | Admin |
| Voting | `PUT /api/voters/me/public-key` | Authenticated |
| Voting | `POST /api/elections/{id}/votes` | Voter (signed) |
| Receipts | `GET /api/elections/{id}/verify/{receiptHash}` | Authenticated |
| Chain | view and verify chain | Authenticated |
| Chain | `POST /api/elections/{id}/chain/simulate-tamper` | Admin |

---

## Notes
- A voter's signing key lives in the browser they first voted from. Voting from another browser with the same account is refused.
- Votes cast before signatures were added have no signature; they are skipped by signature checks.
- The containers run in the `Asia/Kolkata` timezone so the voting window matches what the UI shows. Change `TZ` in `docker-compose.yml` for another region.
- Elections flagged as tampered stay flagged; create a new election to test a clean chain.
- This is a learning and portfolio project, not a production election system.

