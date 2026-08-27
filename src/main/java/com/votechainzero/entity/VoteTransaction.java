package com.votechainzero.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


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

    
    private String signature;

    @Builder.Default
    private boolean mined = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    private Block block;
}