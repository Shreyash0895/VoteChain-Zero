package com.votechainzero.service;

import com.votechainzero.blockchain.BlockchainService;
import com.votechainzero.dto.VoteReceiptResponse;
import com.votechainzero.entity.Election;
import com.votechainzero.entity.Voter;
import com.votechainzero.entity.VoteTransaction;
import com.votechainzero.entity.enums.ElectionStatus;
import com.votechainzero.repository.CandidateRepository;
import com.votechainzero.repository.ElectionRepository;
import com.votechainzero.repository.VoteTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The bridge between the REST layer and BlockchainService.castVote().
 * Reuses the voter's existing voterIdHash (already a one-way hash, set
 * at registration) as the "voterHash" for the blockchain transaction —
 * this is what makes double-vote prevention work while keeping the vote
 * anonymous.
 */
@Service
@RequiredArgsConstructor
public class VoteService {

    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final VoteTransactionRepository voteTransactionRepository;
    private final BlockchainService blockchainService;

    @Transactional
    public VoteReceiptResponse castVote(UUID electionId, UUID candidateId, Voter voter) {
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

        VoteTransaction transaction = blockchainService.castVote(
                election,
                voter.getVoterIdHash(),
                candidateId,
                null
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