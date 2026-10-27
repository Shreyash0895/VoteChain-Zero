package com.votechainzero.service;

import com.votechainzero.blockchain.BlockchainService;
import com.votechainzero.blockchain.SignatureUtil;
import com.votechainzero.dto.VoteReceiptResponse;
import com.votechainzero.entity.Election;
import com.votechainzero.entity.Voter;
import com.votechainzero.entity.VoteTransaction;
import com.votechainzero.entity.enums.ElectionStatus;
import com.votechainzero.repository.CandidateRepository;
import com.votechainzero.repository.ElectionRepository;
import com.votechainzero.repository.VoteTransactionRepository;
import com.votechainzero.repository.VoterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class VoteService {

    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final VoteTransactionRepository voteTransactionRepository;
    private final VoterRepository voterRepository;
    private final BlockchainService blockchainService;

    @Transactional
    public VoteReceiptResponse castVote(UUID electionId, UUID candidateId, String signature, Voter voter) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + electionId));

        if (election.getStatus() != ElectionStatus.ACTIVE) {
            throw new IllegalStateException("Voting is only allowed while the election is ACTIVE");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(election.getStartTime()) || now.isAfter(election.getEndTime())) {
            throw new IllegalStateException("This election's voting window is not currently open");
        }

        boolean candidateBelongsToElection = candidateRepository.findByElectionId(electionId).stream()
                .anyMatch(c -> c.getId().equals(candidateId));
        if (!candidateBelongsToElection) {
            throw new IllegalArgumentException("This candidate is not part of this election");
        }

        // Re-load the voter so we always use the key that is actually stored in the database
        Voter registered = voterRepository.findById(voter.getId())
                .orElseThrow(() -> new IllegalArgumentException("Voter not found"));
        if (registered.getPublicKey() == null) {
            throw new IllegalStateException("No signing key is registered for this account yet");
        }
        String payload = SignatureUtil.payload(electionId, candidateId);
        if (!SignatureUtil.verify(registered.getPublicKey(), payload, signature)) {
            throw new IllegalArgumentException("Invalid vote signature — the vote was not signed by this voter's key");
        }

        VoteTransaction transaction = blockchainService.castVote(
                election,
                registered.getVoterIdHash(),
                candidateId,
                signature,
                registered.getPublicKey()
        );

        return VoteReceiptResponse.builder()
                .transactionHash(transaction.getTransactionHash())
                .electionId(electionId)
                .timestamp(transaction.getTimestamp())
                .mined(transaction.isMined())
                .build();
    }

    @Transactional(readOnly = true)
    public VoteReceiptResponse verifyReceipt(String transactionHash) {
        VoteTransaction transaction = voteTransactionRepository.findByTransactionHash(transactionHash)
                .orElseThrow(() -> new IllegalArgumentException("No vote found with this receipt hash"));

        return VoteReceiptResponse.builder()
                .transactionHash(transaction.getTransactionHash())
                .electionId(transaction.getElectionId())
                .timestamp(transaction.getTimestamp())
                .mined(transaction.isMined())
                .build();
    }
}