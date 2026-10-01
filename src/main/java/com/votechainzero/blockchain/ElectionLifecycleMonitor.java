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

/**
 * Background safety net for closing expired elections. ElectionService
 * already auto-closes an election the moment anyone views it (list or
 * detail) — this job exists for the gap that leaves: an election whose
 * endTime has passed but that nobody happens to be looking at right now
 * would otherwise sit labeled ACTIVE indefinitely until someone does.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ElectionLifecycleMonitor {

    private final ElectionRepository electionRepository;

    @Scheduled(fixedRate = 60000) // every 60s — reuses the same cadence as the chain integrity check
    @Transactional
    public void closeExpiredElections() {
        List<Election> active = electionRepository.findByStatus(ElectionStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();

        for (Election election : active) {
            if (election.getEndTime().isBefore(now)) {
                election.setStatus(ElectionStatus.CLOSED);
                electionRepository.save(election);
                log.info("Auto-closed expired election '{}' ({})", election.getTitle(), election.getId());
            }
        }
    }
}