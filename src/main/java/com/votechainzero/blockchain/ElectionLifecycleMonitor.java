package com.votechainzero.blockchain;

import com.votechainzero.entity.Election;
import com.votechainzero.entity.enums.ElectionStatus;
import com.votechainzero.repository.ElectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElectionLifecycleMonitor {

    private final ElectionRepository electionRepository;
    private final BlockchainService blockchainService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void closeExpiredElections() {
        List<Election> active = electionRepository.findByStatus(ElectionStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();

        for (Election election : active) {
            if (election.getEndTime().isBefore(now)) {
                election.setStatus(ElectionStatus.CLOSED);
                electionRepository.save(election);

                // Same reasoning as ElectionService.closeElection(): flush
                // whatever's left in the mempool so it doesn't stay
                // unconfirmed forever just because voting ended before it
                // hit the votes-per-block threshold.
                blockchainService.mineAllPending(election, "SYSTEM-AUTOCLOSE");

                log.info("Auto-closed expired election '{}' ({})", election.getTitle(), election.getId());
            }
        }
    }
}