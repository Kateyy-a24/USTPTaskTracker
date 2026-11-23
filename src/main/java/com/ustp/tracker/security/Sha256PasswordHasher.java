package com.ustp.tracker.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** SHA-256 hashing (same result as the SHA2(...,256) used in schema.sql, so existing accounts still log in). */
public class Sha256PasswordHasher implements PasswordHasher {
    @Override
    public String hash(String rawPassword) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
