package com.votechainzero.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** One row in the blockchain explorer — a mined block, exactly as recorded. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockResponse {
    private long blockIndex;
    private LocalDateTime timestamp;
    private String previousHash;
    private String hash;
    private String merkleRoot;
    private long nonce;
    private String validatorId;
    private int transactionCount;
}