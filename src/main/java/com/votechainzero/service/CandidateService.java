package com.votechainzero.service;

import com.votechainzero.dto.AddCandidateRequest;
import com.votechainzero.dto.CandidateResponse;
import com.votechainzero.entity.Candidate;
import com.votechainzero.entity.Election;
import com.votechainzero.entity.enums.ElectionStatus;
import com.votechainzero.repository.CandidateRepository;
import com.votechainzero.repository.ElectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final ElectionRepository electionRepository;

    @Transactional
    public CandidateResponse addCandidate(UUID electionId, AddCandidateRequest request) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + electionId));

        if (election.getStatus() != ElectionStatus.DRAFT) {
            throw new IllegalStateException("Candidates can only be added while the election is in DRAFT status");
        }

        Candidate candidate = Candidate.builder()
                .name(request.getName())
                .party(request.getParty())
                .symbolUrl(request.getSymbolUrl())
                .voteCount(0L)
                .election(election)
                .build();

        Candidate saved = candidateRepository.save(candidate);

        // brand new candidate — mined and pending are both 0 by definition
        return CandidateResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .party(saved.getParty())
                .symbolUrl(saved.getSymbolUrl())
                .minedVotes(0L)
                .pendingVotes(0L)
                .totalVotes(0L)
                .build();
    }
}