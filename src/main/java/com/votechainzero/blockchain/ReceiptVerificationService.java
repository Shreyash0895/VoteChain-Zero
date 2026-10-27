package com.votechainzero.blockchain;

import com.votechainzero.dto.ReceiptVerificationResponse;
import com.votechainzero.entity.Block;
import com.votechainzero.entity.VoteTransaction;
import com.votechainzero.repository.VoteTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceiptVerificationService {

    private final VoteTransactionRepository voteTransactionRepository;

    @Transactional(readOnly = true)
    public ReceiptVerificationResponse verify(UUID electionId, String receiptHash) {
        String hash = receiptHash == null ? "" : receiptHash.trim();

        VoteTransaction tx = voteTransactionRepository.findByTransactionHash(hash)
                .filter(t -> t.getElectionId().equals(electionId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "No vote with that receipt was found in this election"));

        if (!tx.isMined() || tx.getBlock() == null) {
            return ReceiptVerificationResponse.builder()
                    .status("PENDING")
                    .electionId(electionId)
                    .transactionHash(hash)
                    .build();
        }

        Block block = tx.getBlock();

        // Same order the block was mined in: oldest transaction first
        List<String> leaves = voteTransactionRepository.findByBlockId(block.getId()).stream()
                .sorted(Comparator.comparing(VoteTransaction::getTimestamp))
                .map(VoteTransaction::getTransactionHash)
                .toList();

        int index = leaves.indexOf(hash);
        List<MerkleTree.ProofStep> proof = MerkleTree.buildProof(leaves, index);
        boolean valid = MerkleTree.applyProof(hash, proof).equals(block.getMerkleRoot());

        Boolean signatureValid = null;
        String fingerprint = null;
        if (tx.getSignature() != null && tx.getPublicKey() != null) {
            signatureValid = SignatureUtil.verify(tx.getPublicKey(),
                    SignatureUtil.payload(tx.getElectionId(), tx.getCandidateId()), tx.getSignature());
            fingerprint = SignatureUtil.fingerprint(tx.getPublicKey());
        }

        return ReceiptVerificationResponse.builder()
                .status("CONFIRMED")
                .electionId(electionId)
                .transactionHash(hash)
                .blockIndex(block.getBlockIndex())
                .blockHash(block.getHash())
                .merkleRoot(block.getMerkleRoot())
                .blockTimestamp(block.getTimestamp())
                .votesInBlock(leaves.size())
                .proof(proof.stream()
                        .map(s -> new ReceiptVerificationResponse.Step(s.hash(), s.position()))
                        .toList())
                .proofValid(valid)
                .signatureValid(signatureValid)
                .signerFingerprint(fingerprint)
                .build();
    }
}