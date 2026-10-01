package com.votechainzero.controller;

import com.votechainzero.dto.CastVoteRequest;
import com.votechainzero.dto.VoteReceiptResponse;
import com.votechainzero.entity.Voter;
import com.votechainzero.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Voting endpoints — open to any authenticated voter (no @PreAuthorize
 * role restriction, unlike the admin-only election/candidate endpoints).
 * @AuthenticationPrincipal injects the Voter set by JwtAuthFilter, so the
 * caller's identity comes from their verified JWT, not a request body
 * field — a voter can never vote "as" someone else.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("/elections/{electionId}/votes")
    public ResponseEntity<VoteReceiptResponse> castVote(
            @PathVariable UUID electionId,
            @Valid @RequestBody CastVoteRequest request,
            @AuthenticationPrincipal Voter voter
    ) {
        VoteReceiptResponse receipt = voteService.castVote(electionId, request.getCandidateId(), voter);
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }

    @GetMapping("/votes/verify/{transactionHash}")
    public ResponseEntity<VoteReceiptResponse> verifyReceipt(@PathVariable String transactionHash) {
        return ResponseEntity.ok(voteService.verifyReceipt(transactionHash));
    }
}