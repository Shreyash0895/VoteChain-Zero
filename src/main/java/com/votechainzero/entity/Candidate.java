package com.votechainzero.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * A candidate contesting in a specific Election.
 *
 * `voteCount` is a DENORMALIZED cache for fast reads (e.g. live results
 * dashboard) — it is updated only when a block containing votes for this
 * candidate is successfully mined. The real source of truth is always the
 * chain itself (count of VoteTransactions in mined blocks referencing this
 * candidate) — voteCount can be fully rebuilt from the chain at any time,
 * which is exactly what makes tampering detectable: if voteCount and the
 * chain-derived count ever disagree, something was tampered with off-chain.
 */
@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String party;

    private String symbolUrl;

    @Builder.Default
    private long voteCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;
}