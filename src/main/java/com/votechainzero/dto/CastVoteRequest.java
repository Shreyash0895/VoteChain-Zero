package com.votechainzero.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CastVoteRequest {

    @NotNull
    private UUID candidateId;
}