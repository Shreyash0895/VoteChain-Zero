package com.votechainzero.entity;

import com.votechainzero.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "voters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt-hashed login password. Never store plaintext. */
    @Column(nullable = false)
    private String password;

    /** SHA-256 hash of the voter's government ID. Unique, never reversible. */
    @Column(nullable = false, unique = true)
    private String voterIdHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.VOTER;

    /** Set true only after OTP/identity verification succeeds. */
    @Builder.Default
    private boolean verified = false;

    @Builder.Default
    private boolean hasVoted = false;

    /**
     * Base64 SPKI ECDSA P-256 public key, registered once from the voter's browser.
     * The matching private key never leaves the voter's device.
     */
    @Column(length = 1000)
    private String publicKey;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}