package com.votechainzero.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteReceiptResponse {
    private String transactionHash;
    private UUID electionId;
    private LocalDateTime timestamp;
    private boolean mined;
}