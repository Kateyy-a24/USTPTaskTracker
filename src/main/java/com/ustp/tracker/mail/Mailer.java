package com.ustp.tracker.mail;

import com.ustp.tracker.model.Person;

/**
 * Abstraction for "something that can send an email".
 * The rest of the app only knows this interface, so Gmail can be replaced by another mail service
 * without touching the screens or the reminder code.
 */
public interface Mailer {
    /** False while the sender account has not been set up yet (see mail.properties). */
    boolean isConfigured();

    /** Sends one HTML email. Blocking - call from a background thread. */
    void send(String toEmail, String subject, String html) throws MailException;

    /** Same as above, but addressed to a person: the email saved in the account is used (method overloading). */
    default void send(Person recipient, String subject, String html) throws MailException {
        send(recipient.getEmail(), subject, html);
    }

    /** Sends the one-time-password email. Every Mailer gets this for free. */
    default void sendOtp(String toEmail, String fullName, String code, int validMinutes, String purpose)
            throws MailException {
        String name = fullName == null || fullName.isBlank() ? "Student" : fullName;
        String html = "<div style=\"font-family:Segoe UI,Arial,sans-serif;max-width:480px;margin:auto;padding:24px;"
                + "border:1px solid #e5e7eb;border-radius:12px\">"
                + "<h2 style=\"color:#0C1432;margin-top:0\">USTP-CDO Task Tracker</h2>"
                + "<p>Hi " + escape(name) + ",</p>"
                + "<p>Use this code to " + escape(purpose) + ":</p>"
                + "<p style=\"font-size:34px;font-weight:bold;letter-spacing:8px;text-align:center;"
                + "background:#F3F4F6;border-radius:8px;padding:14px;margin:18px 0\">" + code + "</p>"
                + "<p>This code expires in " + validMinutes + " minutes. "
                + "If you did not request this, you can ignore this email.</p></div>";
        send(toEmail, "Your verification code: " + code, html);
    }

    /** Makes text safe to put inside HTML. */
    static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
