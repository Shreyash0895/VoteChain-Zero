package com.votechainzero.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;


public final class HashUtil {

    private HashUtil() {
        // utility class — no instances
    }

   
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

    
    public static boolean meetsDifficulty(String hash, int difficulty) {
        if (difficulty <= 0) return true;
        String target = "0".repeat(difficulty);
        return hash.startsWith(target);
    }
}