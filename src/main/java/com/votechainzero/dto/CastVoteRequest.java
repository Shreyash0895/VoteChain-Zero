package com.votechainzero.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CastVoteRequest {

    @NotNull
    private UUID candidateId;

    /** Base64 ECDSA signature over "electionId:candidateId", made in the voter's browser. */
    @NotBlank
    private String signature;
}