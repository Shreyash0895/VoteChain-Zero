package com.votechainzero.service;

import com.votechainzero.blockchain.HashUtil;
import com.votechainzero.dto.AuthResponse;
import com.votechainzero.dto.LoginRequest;
import com.votechainzero.dto.RegisterRequest;
import com.votechainzero.entity.Voter;
import com.votechainzero.entity.enums.Role;
import com.votechainzero.repository.VoterRepository;
import com.votechainzero.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final VoterRepository voterRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtService jwtService;

    @Transactional
    public void register(RegisterRequest request) {
        if (voterRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("An account with this email already exists");
        }

        String voterIdHash = HashUtil.sha256(request.getGovernmentId());
        if (voterRepository.existsByVoterIdHash(voterIdHash)) {
            throw new IllegalStateException("This government ID is already registered");
        }

        Voter voter = Voter.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .voterIdHash(voterIdHash)
                .role(Role.VOTER)
                .verified(false)
                .hasVoted(false)
                .build();

        voterRepository.save(voter);
        otpService.generateAndSend(request.getEmail());
    }

    public void login(LoginRequest request) {
        Voter voter = voterRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), voter.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!voter.isVerified()) {
            throw new IllegalStateException("Please complete registration by verifying the OTP sent to your email");
        }

        otpService.generateAndSend(request.getEmail());
    }

    @Transactional
    public AuthResponse verifyOtp(String email, String submittedOtp) {
        if (!otpService.verify(email, submittedOtp)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        Voter voter = voterRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Voter not found"));

        if (!voter.isVerified()) {
            voter.setVerified(true);
            voterRepository.save(voter);
        }

        String token = jwtService.generateToken(voter.getId(), voter.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .voterId(voter.getId())
                .fullName(voter.getFullName())
                .role(voter.getRole().name())
                .build();
    }
}