package com.votechainzero.entity.enums;

/**
 * Lifecycle state of an election.
 * DRAFT   -> being configured by admin, not visible to voters yet
 * ACTIVE  -> voting is open, votes can be cast
 * CLOSED  -> voting has ended, results are final and chain is locked
 */
public enum ElectionStatus {
    DRAFT,
    ACTIVE,
    CLOSED
}