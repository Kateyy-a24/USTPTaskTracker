package com.ustp.tracker.mail;

/** Thrown when an email could not be sent. Hides the details of the mail library from the rest of the app. */
public class MailException extends Exception {
    public MailException(String message, Throwable cause) {
        super(message, cause);
    }
}
