package com.ustp.tracker.security;

/** Abstraction for turning a password into the text that is stored in the database. */
public interface PasswordHasher {
    String hash(String rawPassword);
}
