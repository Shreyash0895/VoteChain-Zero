package com.votechainzero.controller;

import com.votechainzero.blockchain.SignatureUtil;
import com.votechainzero.dto.RegisterKeyRequest;
import com.votechainzero.entity.Voter;
import com.votechainzero.repository.VoterRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/voters/me")
@RequiredArgsConstructor
public class VoterKeyController {

    private final VoterRepository voterRepository;

    /**
     * Registers the voter's signing public key. Idempotent for the same key;
     * a different key is rejected, so a voter's key can never be swapped later.
     */
    @PutMapping("/public-key")
    @Transactional
    public Map<String, Object> registerKey(@AuthenticationPrincipal Voter principal,
                                           @Valid @RequestBody RegisterKeyRequest request) {
        String publicKey = request.getPublicKey().trim();
        SignatureUtil.parsePublicKey(publicKey); // rejects anything that isn't P-256

        Voter voter = voterRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Voter not found"));

        if (voter.getPublicKey() != null && !voter.getPublicKey().equals(publicKey)) {
            throw new IllegalStateException(
                    "A different signing key is already registered for this account. "
                            + "Vote from the browser you first voted with.");
        }
        voter.setPublicKey(publicKey);
        voterRepository.save(voter);

        return Map.of("registered", true, "fingerprint", SignatureUtil.fingerprint(publicKey));
    }
}