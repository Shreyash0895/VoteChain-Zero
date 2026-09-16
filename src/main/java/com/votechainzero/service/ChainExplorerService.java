package com.votechainzero.service;

import com.votechainzero.blockchain.BlockchainService;
import com.votechainzero.blockchain.ChainValidationResult;
import com.votechainzero.dto.BlockResponse;
import com.votechainzero.dto.ChainStatusResponse;
import com.votechainzero.entity.Block;
import com.votechainzero.entity.Election;
import com.votechainzero.repository.BlockRepository;
import com.votechainzero.repository.ElectionRepository;
import com.votechainzero.repository.VoteTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Read-only view over an election's chain, for the explorer UI. Reuses
 * BlockchainService.validateChain() directly — this is the SAME check the
 * scheduled ChainIntegrityMonitor runs in the background every minute, just
 * triggered on-demand here so a person can see the result immediately
 * instead of waiting for the next scheduled pass.
 */
@Service
@RequiredArgsConstructor
public class ChainExplorerService {

    private final ElectionRepository electionRepository;
    private final BlockRepository blockRepository;
    private final VoteTransactionRepository voteTransactionRepository;
    private final BlockchainService blockchainService;

    @Transactional(readOnly = true)
    public ChainStatusResponse getChainStatus(UUID electionId) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + electionId));

        ChainValidationResult validation = blockchainService.validateChain(election);

        List<Block> blocks = blockRepository.findByElectionIdOrderByBlockIndexAsc(electionId);
        List<BlockResponse> blockResponses = blocks.stream()
                .map(this::toBlockResponse)
                .collect(Collectors.toList());

        return ChainStatusResponse.builder()
                .electionId(electionId)
                .valid(validation.isValid())
                .errors(validation.getErrors())
                .blocks(blockResponses)
                .build();
    }

    private BlockResponse toBlockResponse(Block block) {
        int txCount = voteTransactionRepository.findByBlockId(block.getId()).size();

        return BlockResponse.builder()
                .blockIndex(block.getBlockIndex())
                .timestamp(block.getTimestamp())
                .previousHash(block.getPreviousHash())
                .hash(block.getHash())
                .merkleRoot(block.getMerkleRoot())
                .nonce(block.getNonce())
                .validatorId(block.getValidatorId())
                .transactionCount(txCount)
                .build();
    }
}