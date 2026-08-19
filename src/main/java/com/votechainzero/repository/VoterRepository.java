package com.votechainzero.repository;

import com.votechainzero.entity.Voter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VoterRepository extends JpaRepository<Voter, UUID> {

    Optional<Voter> findByEmail(String email);

    Optional<Voter> findByVoterIdHash(String voterIdHash);

    boolean existsByEmail(String email);

    boolean existsByVoterIdHash(String voterIdHash);
}