package com.votechainzero.controller;

import com.votechainzero.dto.ChainStatusResponse;
import com.votechainzero.service.ChainExplorerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Powers the blockchain explorer — open to any authenticated voter, since
 * transparency of the chain itself is the whole point of using one. */
@RestController
@RequestMapping("/api/elections/{electionId}/chain")
@RequiredArgsConstructor
public class ChainController {

    private final ChainExplorerService chainExplorerService;

    @GetMapping
    public ResponseEntity<ChainStatusResponse> getChainStatus(@PathVariable UUID electionId) {
        return ResponseEntity.ok(chainExplorerService.getChainStatus(electionId));
    }
}