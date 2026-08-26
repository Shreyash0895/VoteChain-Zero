package com.votechainzero.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * Issues and validates JWTs for logged-in voters/admins.
 *
 * IMPORTANT: this is a completely separate concept from the blockchain's
 * SHA-256 hashing (HashUtil). JWTs authenticate *who is calling the API
 * right now* (a login session). The blockchain hashes authenticate *the
 * integrity of a vote/block once it's recorded*. Don't confuse the two —
 * a JWT proves identity for the duration of a session; a vote's signature
 * proves that specific vote wasn't altered, forever.
 */
@Service
public class JwtService {

    @Value("${votechain.jwt.secret}")
    private String secret;

    @Value("${votechain.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey signingKey() {
        // jjwt requires the key to be a minimum length for HS256 — the secret
        // in application.yml must be a long, random string in production.
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UUID voterId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(voterId.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID extractVoterId(String token) {
        return UUID.fromString(extractClaim(token, Claims::getSubject));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public boolean isTokenValid(String token) {
        try {
            return !extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (Exception e) {
            // expired, malformed, or tampered token
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}