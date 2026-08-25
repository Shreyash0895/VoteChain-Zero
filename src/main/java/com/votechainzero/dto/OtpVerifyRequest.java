package com.votechainzero.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Submitted after login to complete two-factor verification. */
@Getter
@Setter
public class OtpVerifyRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String otp;
}