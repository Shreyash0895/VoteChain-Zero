package com.votechainzero.service;

import com.votechainzero.blockchain.BlockchainService;
import com.votechainzero.blockchain.ChainValidationResult;
import com.votechainzero.blockchain.HashUtil;
import com.votechainzero.dto.BlockResponse;
import com.votechainzero.dto.ChainStatusResponse;
import com.votechainzero.dto.TamperSimulationResponse;
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

    /**
     * DEMO ONLY — deliberately corrupts the genesis block's merkleRoot
     * directly in the database, WITHOUT re-mining a valid hash to match.
     * This simulates exactly what the whole chain design defends against:
     * an attacker who can edit a database row directly, but can't redo the
     * computationally expensive Proof-of-Work to produce a hash that's
     * still internally consistent with the tampered data.
     *
     * Deliberately bypasses BlockchainService entirely — going through it
     * would just mine a new, valid block, which defeats the point. This
     * writes directly via the repository, the same way a real attacker
     * with raw database access would.
     */
    @Transactional
    public TamperSimulationResponse simulateTamper(UUID electionId) {
        List<Block> blocks = blockRepository.findByElectionIdOrderByBlockIndexAsc(electionId);

        if (blocks.isEmpty()) {
            throw new IllegalStateException("This election has no blocks yet to tamper with");
        }

        Block genesis = blocks.get(0);
        String fakeMerkleRoot = HashUtil.sha256("TAMPERED-" + System.currentTimeMillis());
        genesis.setMerkleRoot(fakeMerkleRoot);
        blockRepository.save(genesis);

        return TamperSimulationResponse.builder()
                .blockIndex(genesis.getBlockIndex())
                .message("Block #" + genesis.getBlockIndex() + "'s data was altered directly in the database, "
                        + "without re-mining. Check the chain explorer to see validation catch it.")
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