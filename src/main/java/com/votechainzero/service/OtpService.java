package com.votechainzero.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final JavaMailSender mailSender;

    @Value("${votechain.otp.expiry-minutes}")
    private int expiryMinutes;

    private record OtpEntry(String code, LocalDateTime expiresAt) {}

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static final SecureRandom RANDOM = new SecureRandom();

    /** Generates a fresh 6-digit OTP, stores it, and emails it to the voter. */
    public void generateAndSend(String email) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        otpStore.put(email, new OtpEntry(code, LocalDateTime.now().plusMinutes(expiryMinutes)));

        sendEmail(email, code);
        log.info("OTP generated for {} (expires in {} min)", email, expiryMinutes);
    }

    /** Verifies the submitted code, and consumes it on success (one-time use only). */
    public boolean verify(String email, String submittedCode) {
        OtpEntry entry = otpStore.get(email);

        if (entry == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            otpStore.remove(email);
            return false;
        }
        if (!entry.code().equals(submittedCode)) {
            return false;
        }

        otpStore.remove(email); // one-time use
        return true;
    }

    private void sendEmail(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("VoteChain Zero — Your verification code");
            message.setText("Your one-time verification code is: " + code
                    + "\n\nThis code expires in " + expiryMinutes + " minutes."
                    + "\nIf you didn't request this, you can safely ignore this email.");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
        }
    }
}