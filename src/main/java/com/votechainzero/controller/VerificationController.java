package com.votechainzero.controller;

import com.votechainzero.blockchain.ReceiptVerificationService;
import com.votechainzero.dto.ReceiptVerificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/elections")
@RequiredArgsConstructor
public class VerificationController {

    private final ReceiptVerificationService receiptVerificationService;

    @GetMapping("/{electionId}/verify/{receiptHash}")
    public ReceiptVerificationResponse verify(@PathVariable UUID electionId,
                                              @PathVariable String receiptHash) {
        return receiptVerificationService.verify(electionId, receiptHash);
    }
}