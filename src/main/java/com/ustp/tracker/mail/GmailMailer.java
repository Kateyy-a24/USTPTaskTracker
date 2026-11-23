package com.ustp.tracker.mail;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Sends email through ONE system Gmail account (the sender). Students never enter any Gmail/App Password.
 * The sender is set once in mail.properties (project folder), or in ~/USTPTaskTracker/mail.properties,
 * or with the env vars USTP_GMAIL_USER / USTP_GMAIL_APP_PASSWORD.
 */
public class GmailMailer implements Mailer {
    private static final String PLACEHOLDER_USER = "yourgmail@gmail.com";

    private Properties config() {
        Properties p = new Properties();
        Path[] files = {
            Path.of("mail.properties"),
            Path.of(System.getProperty("user.home"), "USTPTaskTracker", "mail.properties")
        };
        for (Path f : files) {
            if (Files.isRegularFile(f)) {
                try (InputStream in = Files.newInputStream(f)) {
                    p.load(in);
                    return p;
                } catch (IOException ignored) { }
            }
        }
        return p;
    }

    private String user() {
        String v = System.getenv("USTP_GMAIL_USER");
        if (v != null && !v.isBlank()) return v.trim();
        return config().getProperty("gmail.user", PLACEHOLDER_USER).trim();
    }

    private String appPassword() {
        String v = System.getenv("USTP_GMAIL_APP_PASSWORD");
        if (v == null || v.isBlank()) v = config().getProperty("gmail.app.password", "");
        return v.replace(" ", "").trim();
    }

    @Override
    public boolean isConfigured() {
        String u = user(), p = appPassword();
        return !u.isEmpty() && !u.equals(PLACEHOLDER_USER) && !p.isEmpty() && !p.equals("xxxxxxxxxxxxxxxx");
    }

    @Override
    public void send(String toEmail, String subject, String html) throws MailException {
        final String from = user();
        final String pass = appPassword();

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pass);
            }
        });

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from, "USTP-CDO Task Tracker"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            msg.setSubject(subject.replaceAll("[\\r\\n]+", " "), "UTF-8");
            msg.setContent(html, "text/html; charset=UTF-8");
            Transport.send(msg);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new MailException(e.getMessage(), e);
        }
    }
}
