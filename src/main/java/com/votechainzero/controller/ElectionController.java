package com.votechainzero.controller;

import com.votechainzero.dto.CreateElectionRequest;
import com.votechainzero.dto.ElectionResponse;
import com.votechainzero.entity.Voter;
import com.votechainzero.service.ElectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/elections")
@RequiredArgsConstructor
public class ElectionController {

    private final ElectionService electionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ElectionResponse> createElection(@Valid @RequestBody CreateElectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(electionService.createElection(request));
    }

    @PostMapping("/{electionId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ElectionResponse> activateElection(
            @PathVariable UUID electionId,
            @AuthenticationPrincipal Voter admin
    ) {
        return ResponseEntity.ok(electionService.activateElection(electionId, admin.getId().toString()));
    }

    @PostMapping("/{electionId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ElectionResponse> closeElection(@PathVariable UUID electionId) {
        return ResponseEntity.ok(electionService.closeElection(electionId));
    }

    @GetMapping("/{electionId}")
    public ResponseEntity<ElectionResponse> getElection(@PathVariable UUID electionId) {
        return ResponseEntity.ok(electionService.getElection(electionId));
    }

    @GetMapping
    public ResponseEntity<List<ElectionResponse>> listElections() {
        return ResponseEntity.ok(electionService.listElections());
    }
}