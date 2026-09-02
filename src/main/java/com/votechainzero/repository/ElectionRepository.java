package com.votechainzero.repository;

import com.votechainzero.entity.Election;
import com.votechainzero.entity.enums.ElectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ElectionRepository extends JpaRepository<Election, UUID> {

    List<Election> findByStatus(ElectionStatus status);
}