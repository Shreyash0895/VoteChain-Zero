package com.votechainzero.repository;

import com.votechainzero.entity.VoteTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoteTransactionRepository extends JpaRepository<VoteTransaction, UUID> {

    /** The unmined "mempool" for an election — pulled in batches during mining. */
    List<VoteTransaction> findByElectionIdAndMinedFalseOrderByTimestampAsc(UUID electionId);

    Optional<VoteTransaction> findByTransactionHash(String transactionHash);

    /** Double-vote prevention: has this voter already voted in this election? */
    boolean existsByElectionIdAndVoterHash(UUID electionId, String voterHash);

    List<VoteTransaction> findByBlockId(UUID blockId);

    long countByElectionIdAndCandidateIdAndMinedTrue(UUID electionId, UUID candidateId);
}