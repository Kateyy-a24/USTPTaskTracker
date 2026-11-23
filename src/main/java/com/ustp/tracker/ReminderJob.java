package com.ustp.tracker;

import com.ustp.tracker.dao.JdbcReminderLogRepository;
import com.ustp.tracker.dao.JdbcTaskRepository;
import com.ustp.tracker.dao.JdbcUserRepository;
import com.ustp.tracker.dao.TaskRepository;
import com.ustp.tracker.dao.UserRepository;
import com.ustp.tracker.mail.GmailMailer;
import com.ustp.tracker.mail.Mailer;
import com.ustp.tracker.model.User;
import com.ustp.tracker.reminder.EmailReminderChannel;
import com.ustp.tracker.reminder.ReminderSettings;
import com.ustp.tracker.security.Sha256PasswordHasher;

/**
 * Optional stand-alone job: emails EVERY student about their deadlines even when the app is closed.
 * Run this class once a day (for example with Windows Task Scheduler). See README section 2d.
 * It never sends the same reminder twice in a day.
 */
public class ReminderJob {
    public static void main(String[] args) throws Exception {
        Mailer mailer = new GmailMailer();
        if (!mailer.isConfigured()) {
            System.err.println("Fill in gmail.user and gmail.app.password in mail.properties first.");
            return;
        }
        UserRepository users = new JdbcUserRepository(new Sha256PasswordHasher());
        TaskRepository tasks = new JdbcTaskRepository();
        ReminderSettings settings = new ReminderSettings();
        EmailReminderChannel email = new EmailReminderChannel(mailer, new JdbcReminderLogRepository(), settings);

        int people = 0;
        for (User u : users.allWithEmail()) {
            if (!settings.isEmailEnabled(u.getId())) continue;
            try {
                int n = email.sendDue(u, tasks.findAll(u.getId()));
                if (n > 0) {
                    people++;
                    System.out.println("Emailed " + u.getEmail() + " about " + n + " task(s).");
                }
            } catch (Exception e) {
                System.err.println("Could not email " + u.getEmail() + ": " + e.getMessage());
            }
        }
        System.out.println("Done. Reminder emails sent to " + people + " student(s).");
    }
}
