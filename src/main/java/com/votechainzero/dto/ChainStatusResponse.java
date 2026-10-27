package com.votechainzero.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChainStatusResponse {
    private UUID electionId;
    private boolean valid;
    private List<String> errors;
    private List<BlockResponse> blocks;
}