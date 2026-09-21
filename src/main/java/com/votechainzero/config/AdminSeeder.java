package com.votechainzero.config;

import com.votechainzero.blockchain.HashUtil;
import com.votechainzero.entity.Voter;
import com.votechainzero.entity.enums.Role;
import com.votechainzero.repository.VoterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs once every time the app starts. If no account exists yet with the
 * configured admin email, it creates one with the ADMIN role — this is
 * the ONLY way an admin account gets created; there's no public "become
 * an admin" endpoint, on purpose.
 *
 * Credentials come from environment variables (see .env / docker-compose.yml),
 * never hardcoded here, for the same reason the mail credentials aren't
 * hardcoded — this file is safe to commit to a public repo.
 *
 * Idempotent: if the admin account already exists (e.g. on every restart),
 * this does nothing and logs a one-line confirmation instead of erroring
 * or creating duplicates.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final VoterRepository voterRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${votechain.admin.email}")
    private String adminEmail;

    @Value("${votechain.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (voterRepository.existsByEmail(adminEmail)) {
            log.info("Admin account already exists ({}) — skipping seed", adminEmail);
            return;
        }

        // Admins don't need a real government ID — we still need a unique,
        // non-null voterIdHash to satisfy the same DB constraint every
        // voter has, so we hash a fixed, admin-specific string instead.
        String voterIdHash = HashUtil.sha256("ADMIN-SEED-" + adminEmail);

        Voter admin = Voter.builder()
                .fullName("Election Administrator")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .voterIdHash(voterIdHash)
                .role(Role.ADMIN)
                .verified(true)
                .hasVoted(false)
                .build();

        voterRepository.save(admin);
        log.info("Seeded admin account: {} — log in with the password from your .env file", adminEmail);
    }
}