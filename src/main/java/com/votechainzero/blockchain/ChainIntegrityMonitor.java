package com.votechainzero.blockchain;

import com.votechainzero.entity.Election;
import com.votechainzero.entity.enums.ElectionStatus;
import com.votechainzero.repository.ElectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class ChainIntegrityMonitor {

    private final ElectionRepository electionRepository;
    private final BlockchainService blockchainService;

    @Scheduled(fixedRateString = "${votechain.blockchain.integrity-check-interval-ms}")
    public void checkAllActiveChains() {
        List<Election> activeElections = electionRepository.findByStatus(ElectionStatus.ACTIVE);

        if (activeElections.isEmpty()) {
            return;
        }

        for (Election election : activeElections) {
            ChainValidationResult result = blockchainService.validateChain(election);

            if (result.isValid()) {
                log.info("Chain integrity check passed for election '{}' ({})",
                        election.getTitle(), election.getId());
            } else {
                // TODO: this is where real alerting should plug in (email the
                // admin, fire a webhook, flip a banner in the admin dashboard)
                // — logging alone is a placeholder for a "zero error" system.
                log.error("TAMPER DETECTED in election '{}' ({}): {}",
                        election.getTitle(), election.getId(), result.getErrors());
            }
        }
    }
}