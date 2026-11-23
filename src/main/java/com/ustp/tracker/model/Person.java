package com.ustp.tracker.model;

/**
 * Base class for anyone who owns an account: a name and an email.
 * The fields are private (encapsulation); subclasses decide how the person is shown (abstraction).
 */
public abstract class Person {
    private String fullName;
    private String email;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /** True when an email address is saved (needed for OTP codes and reminder emails). */
    public boolean hasEmail() { return email != null && !email.isBlank(); }

    /** The name that is shown on screens and in emails. Each kind of person decides this itself. */
    public abstract String getDisplayName();
}
