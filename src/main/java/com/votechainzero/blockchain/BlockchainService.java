package com.votechainzero.blockchain;

import com.votechainzero.entity.Block;
import com.votechainzero.entity.Candidate;
import com.votechainzero.entity.Election;
import com.votechainzero.entity.VoteTransaction;
import com.votechainzero.repository.BlockRepository;
import com.votechainzero.repository.CandidateRepository;
import com.votechainzero.repository.VoteTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockchainService {

    private static final String GENESIS_PREVIOUS_HASH = "0".repeat(64);

    private final BlockRepository blockRepository;
    private final VoteTransactionRepository voteTransactionRepository;
    private final CandidateRepository candidateRepository;

    @Value("${votechain.blockchain.mining-difficulty}")
    private int miningDifficulty;

    @Value("${votechain.blockchain.votes-per-block}")
    private int votesPerBlock;

    
    @Transactional
    public Block createGenesisBlock(Election election, String validatorId) {
        if (blockRepository.countByElectionId(election.getId()) > 0) {
            throw new IllegalStateException("Genesis block already exists for election " + election.getId());
        }

        Block genesis = Block.builder()
                .blockIndex(0L)
                .timestamp(LocalDateTime.now())
                .previousHash(GENESIS_PREVIOUS_HASH)
                .merkleRoot(MerkleTree.computeMerkleRoot(List.of()))
                .nonce(0L)
                .validatorId(validatorId)
                .election(election)
                .build();

        mineProofOfWork(genesis);

        Block saved = blockRepository.save(genesis);
        log.info("Genesis block created for election {} — hash={}", election.getId(), saved.getHash());
        return saved;
    }

   
    @Transactional
    public VoteTransaction castVote(Election election, String voterHash, UUID candidateId, String signature) {
        if (voteTransactionRepository.existsByElectionIdAndVoterHash(election.getId(), voterHash)) {
            throw new IllegalStateException("This voter has already voted in this election");
        }

        LocalDateTime now = LocalDateTime.now();
        String txHash = HashUtil.sha256(voterHash + candidateId + election.getId() + now + UUID.randomUUID());

        VoteTransaction transaction = VoteTransaction.builder()
                .transactionHash(txHash)
                .voterHash(voterHash)
                .candidateId(candidateId)
                .electionId(election.getId())
                .timestamp(now)
                .signature(signature)
                .mined(false)
                .build();

        VoteTransaction saved = voteTransactionRepository.save(transaction);
        log.info("Vote transaction {} queued for election {}", saved.getTransactionHash(), election.getId());

        long pendingCount = voteTransactionRepository
                .findByElectionIdAndMinedFalseOrderByTimestampAsc(election.getId())
                .size();

        if (pendingCount >= votesPerBlock) {
            mineBlock(election, "SYSTEM");
        }

        return saved;
    }

   
    
    @Transactional
    public Optional<Block> mineBlock(Election election, String validatorId) {
        List<VoteTransaction> pending = voteTransactionRepository
                .findByElectionIdAndMinedFalseOrderByTimestampAsc(election.getId());

        if (pending.isEmpty()) {
            log.info("No pending transactions to mine for election {}", election.getId());
            return Optional.empty();
        }

        // take up to `votesPerBlock` transactions — anything beyond stays pending for the next block
        List<VoteTransaction> batch = pending.size() > votesPerBlock
                ? pending.subList(0, votesPerBlock)
                : pending;

        Block previousBlock = blockRepository
                .findTopByElectionIdOrderByBlockIndexDesc(election.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "No genesis block found for election " + election.getId() + " — create one first"));

        List<String> txHashes = batch.stream()
                .map(VoteTransaction::getTransactionHash)
                .collect(Collectors.toList());

        Block newBlock = Block.builder()
                .blockIndex(previousBlock.getBlockIndex() + 1)
                .timestamp(LocalDateTime.now())
                .previousHash(previousBlock.getHash())
                .merkleRoot(MerkleTree.computeMerkleRoot(txHashes))
                .nonce(0L)
                .validatorId(validatorId)
                .election(election)
                .build();

        mineProofOfWork(newBlock);

        Block savedBlock = blockRepository.save(newBlock);

        // link each transaction to the now-mined block and mark it mined
        for (VoteTransaction tx : batch) {
            tx.setBlock(savedBlock);
            tx.setMined(true);
            voteTransactionRepository.save(tx);
            incrementCandidateVoteCount(tx.getCandidateId());
        }

        log.info("Mined block #{} for election {} — hash={}, {} vote(s) included",
                savedBlock.getBlockIndex(), election.getId(), savedBlock.getHash(), batch.size());

        return Optional.of(savedBlock);
    }

    /** Denormalized cache used by the live results dashboard — see Candidate.voteCount javadoc. */
    private void incrementCandidateVoteCount(UUID candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalStateException("Candidate not found: " + candidateId));
        candidate.setVoteCount(candidate.getVoteCount() + 1);
        candidateRepository.save(candidate);
    }

   
    private void mineProofOfWork(Block block) {
        String hash;
        long nonce = 0L;
        do {
            block.setNonce(nonce);
            hash = computeBlockHash(block);
            nonce++;
        } while (!HashUtil.meetsDifficulty(hash, miningDifficulty));

        block.setHash(hash);
    }

    /** The single formula for "what is this block's hash" — used both when mining and when validating. */
    private String computeBlockHash(Block block) {
        String raw = block.getBlockIndex()
                + block.getTimestamp().toString()
                + block.getPreviousHash()
                + block.getMerkleRoot()
                + block.getNonce();
        return HashUtil.sha256(raw);
    }

    @Transactional(readOnly = true)
    public ChainValidationResult validateChain(Election election) {
        ChainValidationResult result = new ChainValidationResult();

        List<Block> chain = blockRepository.findByElectionIdOrderByBlockIndexAsc(election.getId());

        if (chain.isEmpty()) {
            result.addError("Chain has no genesis block");
            return result;
        }

        String expectedPreviousHash = GENESIS_PREVIOUS_HASH;

        for (Block block : chain) {
            String recomputedHash = computeBlockHash(block);

            if (!recomputedHash.equals(block.getHash())) {
                result.addError("Block #" + block.getBlockIndex()
                        + " hash mismatch — stored data does not match its recorded hash (tampered)");
            }

            if (!HashUtil.meetsDifficulty(block.getHash(), miningDifficulty)) {
                result.addError("Block #" + block.getBlockIndex()
                        + " hash no longer meets difficulty target");
            }

            if (!block.getPreviousHash().equals(expectedPreviousHash)) {
                result.addError("Block #" + block.getBlockIndex()
                        + " previousHash does not match the prior block's hash — chain link broken");
            }

            List<String> actualTxHashes = voteTransactionRepository.findByBlockId(block.getId()).stream()
                    .map(VoteTransaction::getTransactionHash)
                    .collect(Collectors.toList());
            String recomputedMerkleRoot = MerkleTree.computeMerkleRoot(actualTxHashes);

            if (!recomputedMerkleRoot.equals(block.getMerkleRoot())) {
                result.addError("Block #" + block.getBlockIndex()
                        + " merkle root mismatch — a transaction in this block was altered after mining");
            }

            expectedPreviousHash = block.getHash();
        }

        return result;
    }
}