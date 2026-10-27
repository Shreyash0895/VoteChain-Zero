package com.votechainzero.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Result of verifying a vote receipt. Deliberately contains NO candidate and NO
 * voter information: it proves the vote is on the chain, not who it was for.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptVerificationResponse {

    /** "CONFIRMED" (mined into a block) or "PENDING" (not mined yet). */
    private String status;
    private UUID electionId;
    private String transactionHash;

    // The fields below are only set when status = CONFIRMED
    private Long blockIndex;
    private String blockHash;
    private String merkleRoot;
    private LocalDateTime blockTimestamp;
    private int votesInBlock;
    private List<Step> proof;
    private boolean proofValid;

    /** true/false once signed; null for older votes cast before signatures existed. */
    private Boolean signatureValid;
    /** Short fingerprint of the signing key (identifies the key, not the person). */
    private String signerFingerprint;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Step {
        private String hash;
        /** Side the sibling sits on: LEFT or RIGHT. */
        private String position;
    }
}