package com.votechainzero.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** Returned after successful login+OTP verification — the JWT the client uses for subsequent requests. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UUID voterId;
    private String fullName;
    private String role;
}