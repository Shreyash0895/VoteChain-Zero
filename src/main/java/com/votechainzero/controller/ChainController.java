package com.votechainzero.controller;

import com.votechainzero.dto.ChainStatusResponse;
import com.votechainzero.dto.TamperSimulationResponse;
import com.votechainzero.service.ChainExplorerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/elections/{electionId}/chain")
@RequiredArgsConstructor
public class ChainController {

    private final ChainExplorerService chainExplorerService;

    @GetMapping
    public ResponseEntity<ChainStatusResponse> getChainStatus(@PathVariable UUID electionId) {
        return ResponseEntity.ok(chainExplorerService.getChainStatus(electionId));
    }

    /** DEMO ONLY, admin-gated — see ChainExplorerService.simulateTamper() javadoc. */
    @PostMapping("/simulate-tamper")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TamperSimulationResponse> simulateTamper(@PathVariable UUID electionId) {
        return ResponseEntity.ok(chainExplorerService.simulateTamper(electionId));
    }
}