package com.votechainzero.controller;

import com.votechainzero.dto.AddCandidateRequest;
import com.votechainzero.dto.CandidateResponse;
import com.votechainzero.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Admin-only: add candidates to an election that's still in DRAFT status. */
@RestController
@RequestMapping("/api/elections/{electionId}/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CandidateResponse> addCandidate(
            @PathVariable UUID electionId,
            @Valid @RequestBody AddCandidateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(candidateService.addCandidate(electionId, request));
    }
}