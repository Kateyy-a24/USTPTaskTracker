package com.ustp.tracker;

import com.ustp.tracker.dao.JdbcReminderLogRepository;
import com.ustp.tracker.dao.JdbcTaskRepository;
import com.ustp.tracker.dao.JdbcUserRepository;
import com.ustp.tracker.dao.ReminderLogRepository;
import com.ustp.tracker.dao.TaskRepository;
import com.ustp.tracker.dao.UserRepository;
import com.ustp.tracker.mail.GmailMailer;
import com.ustp.tracker.mail.Mailer;
import com.ustp.tracker.reminder.AlarmSound;
import com.ustp.tracker.reminder.EmailReminderChannel;
import com.ustp.tracker.reminder.ReminderChannel;
import com.ustp.tracker.reminder.ReminderService;
import com.ustp.tracker.reminder.ReminderSettings;
import com.ustp.tracker.reminder.ScreenReminderChannel;
import com.ustp.tracker.security.Sha256PasswordHasher;
import com.ustp.tracker.service.OtpService;

import java.util.List;

/**
 * Creates every object of the app ONCE and connects them together.
 * Each object only knows the interfaces of the others (UserRepository, Mailer ...), so one part can be
 * replaced (for example a different database or mail service) by changing only this class.
 */
public final class AppContext {
    private static final AppContext INSTANCE = new AppContext();

    private final UserRepository users;
    private final TaskRepository tasks;
    private final Mailer mailer;
    private final OtpService otp;
    private final ReminderSettings settings;
    private final AlarmSound alarm;
    private final EmailReminderChannel emailChannel;
    private final ReminderService reminders;

    private AppContext() {
        users = new JdbcUserRepository(new Sha256PasswordHasher());
        tasks = new JdbcTaskRepository();
        ReminderLogRepository log = new JdbcReminderLogRepository();
        mailer = new GmailMailer();
        otp = new OtpService();
        settings = new ReminderSettings();
        alarm = new AlarmSound();
        emailChannel = new EmailReminderChannel(mailer, log, settings);
        List<ReminderChannel> channels = List.of(new ScreenReminderChannel(alarm, settings), emailChannel);
        reminders = new ReminderService(tasks, channels);
    }

    public static AppContext get() { return INSTANCE; }

    public UserRepository users() { return users; }
    public TaskRepository tasks() { return tasks; }
    public Mailer mailer() { return mailer; }
    public OtpService otp() { return otp; }
    public ReminderSettings settings() { return settings; }
    public AlarmSound alarm() { return alarm; }
    public EmailReminderChannel emailChannel() { return emailChannel; }
    public ReminderService reminders() { return reminders; }
}
