package com.votechainzero.service;

import com.votechainzero.blockchain.BlockchainService;
import com.votechainzero.dto.CandidateResponse;
import com.votechainzero.dto.CreateElectionRequest;
import com.votechainzero.dto.ElectionResponse;
import com.votechainzero.entity.Candidate;
import com.votechainzero.entity.Election;
import com.votechainzero.entity.enums.ElectionStatus;
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
public class ElectionService {

    private final ElectionRepository electionRepository;
    private final BlockchainService blockchainService;
    private final VoteTransactionRepository voteTransactionRepository;

    @Transactional
    public ElectionResponse createElection(CreateElectionRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Election election = Election.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ElectionStatus.DRAFT)
                .build();

        Election saved = electionRepository.save(election);
        return toResponse(saved);
    }

    
    @Transactional
    public ElectionResponse activateElection(UUID electionId, String adminId) {
        Election election = getElectionOrThrow(electionId);

        if (election.getStatus() != ElectionStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT elections can be activated");
        }
        if (election.getCandidates().size() < 2) {
            throw new IllegalStateException("An election needs at least 2 candidates before it can be activated");
        }

        election.setStatus(ElectionStatus.ACTIVE);
        Election saved = electionRepository.save(election);

        blockchainService.createGenesisBlock(saved, adminId);

        return toResponse(saved);
    }

    @Transactional
    public ElectionResponse closeElection(UUID electionId) {
        Election election = getElectionOrThrow(electionId);

        if (election.getStatus() != ElectionStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE elections can be closed");
        }

        election.setStatus(ElectionStatus.CLOSED);
        return toResponse(electionRepository.save(election));
    }

    @Transactional(readOnly = true)
    public ElectionResponse getElection(UUID electionId) {
        return toResponse(getElectionOrThrow(electionId));
    }

    @Transactional(readOnly = true)
    public List<ElectionResponse> listElections() {
        return electionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Election getElectionOrThrow(UUID electionId) {
        return electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + electionId));
    }

    private ElectionResponse toResponse(Election election) {
        List<CandidateResponse> candidates = election.getCandidates().stream()
                .map(candidate -> toCandidateResponse(election.getId(), candidate))
                .collect(Collectors.toList());

        return ElectionResponse.builder()
                .id(election.getId())
                .title(election.getTitle())
                .description(election.getDescription())
                .startTime(election.getStartTime())
                .endTime(election.getEndTime())
                .status(election.getStatus())
                .candidates(candidates)
                .build();
    }

    
    private CandidateResponse toCandidateResponse(UUID electionId, Candidate candidate) {
        long mined = candidate.getVoteCount();
        long pending = voteTransactionRepository
                .countByElectionIdAndCandidateIdAndMinedFalse(electionId, candidate.getId());

        return CandidateResponse.builder()
                .id(candidate.getId())
                .name(candidate.getName())
                .party(candidate.getParty())
                .symbolUrl(candidate.getSymbolUrl())
                .minedVotes(mined)
                .pendingVotes(pending)
                .totalVotes(mined + pending)
                .build();
    }
}