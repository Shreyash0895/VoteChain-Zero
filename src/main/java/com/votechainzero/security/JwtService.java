package com.votechainzero.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;


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

        // jjwt 0.12.x: signWith(Key) infers the algorithm from the key type
        // automatically — no need to pass SignatureAlgorithm explicitly
        // (that overload is deprecated as of 0.12).
        return Jwts.builder()
                .subject(voterId.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
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