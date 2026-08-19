package com.votechainzero.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single vote, modeled as a blockchain "transaction".
 *
 * Anonymity design: we deliberately do NOT store a foreign key to Voter here.
 * Instead we store `voterHash` — the same one-way SHA-256 hash from
 * Voter.voterIdHash. This lets us:
 *   1. Prove a specific registered voter cast a vote (by re-hashing their ID
 *      and checking it matches).
 *   2. Prevent double-voting (check if voterHash already has a transaction
 *      in this election).
 *   3. NEVER let anyone join voter identity directly to candidate choice in
 *      a single query/table — you'd need the raw ID to even compute the hash.
 *
 * Lifecycle: created with mined=false and block=null the instant a vote is
 * cast. It sits in the "mempool" (unmined pool) until BlockchainService
 * batches it into a Block once `votes-per-block` threshold is hit.
 */
@Entity
@Table(name = "vote_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * SHA-256(blockIndex-agnostic content) of this transaction's own data —
     * this is the "transaction hash" a voter is given as their receipt,
     * letting them verify later that this exact vote is in the chain.
     */
    @Column(nullable = false, unique = true)
    private String transactionHash;

    /** One-way hash of the voter's ID — see class javadoc for why. */
    @Column(nullable = false)
    private String voterHash;

    @Column(nullable = false)
    private UUID candidateId;

    @Column(nullable = false)
    private UUID electionId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Placeholder for the voter's digital signature over this transaction's
     * data (signed with their private key). Proves the vote wasn't forged
     * or altered in transit. Wired up fully once we add key-pair issuance
     * during voter registration (Tier 1 security feature).
     */
    private String signature;

    @Builder.Default
    private boolean mined = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    private Block block;
}