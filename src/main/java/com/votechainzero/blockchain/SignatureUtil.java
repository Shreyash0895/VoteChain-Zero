package com.votechainzero.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

/**
 * ECDSA (P-256 / SHA-256) helpers. Keys and signatures arrive from the browser's
 * WebCrypto API: public key = base64 SPKI, signature = base64 raw r||s (P1363).
 */
public final class SignatureUtil {

    private SignatureUtil() { }

    /** The exact string a voter signs. Must match the frontend. */
    public static String payload(UUID electionId, UUID candidateId) {
        return electionId + ":" + candidateId;
    }

    public static PublicKey parsePublicKey(String base64Spki) {
        try {
            byte[] der = Base64.getDecoder().decode(base64Spki.trim());
            PublicKey key = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(der));
            if (!(key instanceof ECPublicKey ec) || ec.getParams().getOrder().bitLength() != 256) {
                throw new IllegalArgumentException("Public key must be an ECDSA P-256 key");
            }
            return key;
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid public key", e);
        }
    }

    /** Returns false (never throws) for any malformed key or signature. */
    public static boolean verify(String base64PublicKey, String payload, String base64Signature) {
        try {
            PublicKey key = parsePublicKey(base64PublicKey);
            Signature sig = Signature.getInstance("SHA256withECDSAinP1363Format");
            sig.initVerify(key);
            sig.update(payload.getBytes(StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(base64Signature.trim()));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Short, human-friendly identifier for a public key. */
    public static String fingerprint(String base64PublicKey) {
        return HashUtil.sha256(base64PublicKey).substring(0, 16);
    }
}