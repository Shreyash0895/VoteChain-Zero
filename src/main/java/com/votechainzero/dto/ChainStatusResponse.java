package com.votechainzero.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * What powers the explorer's "Chain Verified " badge. `valid` and
 * `errors` come straight from BlockchainService.validateChain() — the
 * exact same check the scheduled ChainIntegrityMonitor runs periodically
 * in the background, just triggered on-demand here for display.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChainStatusResponse {
    private UUID electionId;
    private boolean valid;
    private List<String> errors;
    private List<BlockResponse> blocks;
}