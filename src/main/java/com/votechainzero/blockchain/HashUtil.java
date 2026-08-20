package com.votechainzero.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * SHA-256 hashing helper — the cryptographic backbone of the whole chain.
 *
 * Every hash in this system (block hashes, merkle roots, voter ID hashes,
 * transaction hashes) goes through this one method, so there's exactly one
 * place that defines "how we hash" for the entire project.
 */
public final class HashUtil {

    private HashUtil() {
        // utility class — no instances
    }

    /**
     * Returns the SHA-256 hash of the input as a lowercase hex string.
     * e.g. sha256("hello") -> "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to exist on every standard JVM — this should never happen
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Checks whether a hash meets the Proof-of-Work difficulty target,
     * i.e. starts with `difficulty` leading zero characters.
     * e.g. meetsDifficulty("0000a1b2...", 4) -> true
     */
    public static boolean meetsDifficulty(String hash, int difficulty) {
        if (difficulty <= 0) return true;
        String target = "0".repeat(difficulty);
        return hash.startsWith(target);
    }
}