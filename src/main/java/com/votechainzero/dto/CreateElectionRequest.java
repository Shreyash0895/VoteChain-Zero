package com.votechainzero.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Submitted by an admin to create a new election (starts in DRAFT status). */
@Getter
@Setter
public class CreateElectionRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;
}