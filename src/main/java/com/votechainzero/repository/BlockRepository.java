package com.votechainzero.repository;

import com.votechainzero.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlockRepository extends JpaRepository<Block, UUID> {

    /** Full chain for an election, in mining order — needed for validation and the explorer UI. */
    List<Block> findByElectionIdOrderByBlockIndexAsc(UUID electionId);

    /** The most recently mined block — its hash becomes the next block's previousHash. */
    Optional<Block> findTopByElectionIdOrderByBlockIndexDesc(UUID electionId);

    long countByElectionId(UUID electionId);
}