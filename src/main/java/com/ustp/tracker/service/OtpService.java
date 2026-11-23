package com.ustp.tracker.service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/** Generates and checks one-time codes. The codes are kept in memory only, one per account. */
public class OtpService {
    public static final int VALID_MINUTES = 5;
    public static final int RESEND_SECONDS = 60;
    private static final int MAX_ATTEMPTS = 5;

    public enum Result { OK, WRONG, EXPIRED, TOO_MANY, NONE }

    private static final class Entry {
        String code;
        long expiresAt;
        long sentAt;
        int attempts;
    }

    private final Map<String, Entry> codes = new HashMap<>();
    private final SecureRandom rng = new SecureRandom();

    private static String key(String id) { return id.trim().toLowerCase(); }

    /** Seconds the user must still wait before asking for another code (0 = can send now). */
    public synchronized long cooldownLeft(String id) {
        Entry e = codes.get(key(id));
        if (e == null) return 0;
        long left = RESEND_SECONDS - (System.currentTimeMillis() - e.sentAt) / 1000;
        return Math.max(0, left);
    }

    /** Creates a fresh 6-digit code (replaces any old one). */
    public synchronized String generate(String id) {
        Entry e = new Entry();
        e.code = String.format("%06d", rng.nextInt(1_000_000));
        e.sentAt = System.currentTimeMillis();
        e.expiresAt = e.sentAt + VALID_MINUTES * 60_000L;
        codes.put(key(id), e);
        return e.code;
    }

    public synchronized void discard(String id) {
        codes.remove(key(id));
    }

    public synchronized Result verify(String id, String input) {
        Entry e = codes.get(key(id));
        if (e == null) return Result.NONE;
        if (System.currentTimeMillis() > e.expiresAt) {
            codes.remove(key(id));
            return Result.EXPIRED;
        }
        if (e.attempts >= MAX_ATTEMPTS) {
            codes.remove(key(id));
            return Result.TOO_MANY;
        }
        if (e.code.equals(input.trim())) return Result.OK;
        e.attempts++;
        if (e.attempts >= MAX_ATTEMPTS) {
            codes.remove(key(id));
            return Result.TOO_MANY;
        }
        return Result.WRONG;
    }
}
