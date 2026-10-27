package com.votechainzero.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterKeyRequest {

    /** Base64 SPKI-encoded ECDSA P-256 public key. */
    @NotBlank
    private String publicKey;
}