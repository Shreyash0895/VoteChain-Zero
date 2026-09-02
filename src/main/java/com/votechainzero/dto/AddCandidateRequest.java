package com.votechainzero.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Submitted by an admin to add a candidate to an election still in DRAFT status. */
@Getter
@Setter
public class AddCandidateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String party;

    private String symbolUrl;
}