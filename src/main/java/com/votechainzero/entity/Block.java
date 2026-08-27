package com.votechainzero.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "blocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Position in the chain. Genesis block is always index 0. */
    @Column(nullable = false)
    private long blockIndex;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String previousHash;

    @Column(nullable = false, unique = true)
    private String hash;

    @Column(nullable = false)
    private String merkleRoot;

    /** Proof-of-Work counter, incremented during mining until difficulty target is met. */
    @Builder.Default
    private long nonce = 0L;

   
    private String validatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL)
    @Builder.Default
    private List<VoteTransaction> transactions = new ArrayList<>();
}