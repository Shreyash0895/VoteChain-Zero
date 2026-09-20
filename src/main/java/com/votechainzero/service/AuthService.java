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

/**
 * Password-only auth (OTP/2FA removed for simplicity — see git history if
 * you want to bring it back later; OtpService.java is still in the project,
 * just unused).
 *
 * register() and login() both return a JWT directly — registering logs
 * you straight in, no separate verification step.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final VoterRepository voterRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
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
                .verified(true)
                .hasVoted(false)
                .build();

        Voter saved = voterRepository.save(voter);
        return buildAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        Voter voter = voterRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), voter.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return buildAuthResponse(voter);
    }

    private AuthResponse buildAuthResponse(Voter voter) {
        String token = jwtService.generateToken(voter.getId(), voter.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .voterId(voter.getId())
                .fullName(voter.getFullName())
                .role(voter.getRole().name())
                .build();
    }
}