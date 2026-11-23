package com.ustp.tracker.reminder;

import com.ustp.tracker.dao.ReminderLogRepository;
import com.ustp.tracker.mail.Mailer;
import com.ustp.tracker.model.Task;
import com.ustp.tracker.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deadline reminder EMAILS, sent from the system Gmail to the email registered in the account.
 *  - Pending tasks due in 3 days, 2 days, tomorrow, today: one email per task per day.
 *  - Overdue (up to 3 days late): one email per day.
 * All tasks that need attention are combined in ONE email. Sent emails are remembered in the log,
 * so closing and reopening the app never sends the same email twice.
 */
public class EmailReminderChannel implements ReminderChannel {
    private static final int MAX_OVERDUE_DAYS = 3;
    private static final long RETRY_AFTER_MS = 15L * 60 * 1000;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

    private record Row(Task task, long days, DueStage stage, String code) {}

    private final Mailer mailer;
    private final ReminderLogRepository log;
    private final ReminderSettings settings;
    private final Map<Integer, Long> failedAt = new ConcurrentHashMap<>();

    public EmailReminderChannel(Mailer mailer, ReminderLogRepository log, ReminderSettings settings) {
        this.mailer = mailer;
        this.log = log;
        this.settings = settings;
    }

    /** Emails the registered address. If sending fails, waits 15 minutes before trying again. */
    @Override
    public synchronized void remind(User user, List<Task> tasks) {
        if (!settings.isEmailEnabled(user.getId()) || !mailer.isConfigured()) return;
        Long failed = failedAt.get(user.getId());
        if (failed != null && System.currentTimeMillis() - failed < RETRY_AFTER_MS) return;
        try {
            sendDue(user, tasks);
            failedAt.remove(user.getId());
        } catch (Exception e) {
            failedAt.put(user.getId(), System.currentTimeMillis());
            System.err.println("Reminder email failed: " + e.getMessage());
        }
    }

    /**
     * Emails the user about tasks that need attention and were not emailed yet today.
     * Blocking. Returns how many tasks were included (0 = nothing sent).
     */
    public int sendDue(User user, List<Task> tasks) throws Exception {
        if (!user.hasEmail()) return 0;
        LocalDate today = LocalDate.now();
        Set<String> done = log.sentOn(today);

        List<Row> rows = new ArrayList<>();
        for (Task t : tasks) {
            if (!t.isPending()) continue;
            long days = ChronoUnit.DAYS.between(today, t.deadline());
            DueStage stage = DueStage.of(days);
            if (stage == null || days < -MAX_OVERDUE_DAYS) continue;
            String code = stage.emailCode(days);
            if (done.contains(t.id() + ":" + code)) continue;
            rows.add(new Row(t, days, stage, code));
        }
        if (rows.isEmpty()) return 0;
        rows.sort(Comparator.comparing(r -> r.task().deadline()));

        String subject = rows.size() == 1
                ? "Deadline Reminder: " + rows.get(0).task().name()
                : "Deadline Reminder: " + rows.size() + " tasks need your attention";
        mailer.send(user, subject, buildHtml(user, rows));

        List<ReminderLogRepository.Entry> entries = new ArrayList<>();
        for (Row r : rows) entries.add(new ReminderLogRepository.Entry(r.task().id(), r.code()));
        log.markSent(entries, today);   // only after the email was really sent
        return rows.size();
    }

    /** Confirmation email sent when the student turns email reminders ON. */
    public void sendConfirmation(User user) throws Exception {
        String html = "<div style=\"font-family:Segoe UI,Arial,sans-serif;max-width:520px;margin:auto;padding:24px;"
                + "border:1px solid #e5e7eb;border-radius:12px\">"
                + "<h2 style=\"color:#0C1432;margin-top:0\">USTP-CDO Task Tracker</h2>"
                + "<p>Hi " + Mailer.escape(user.getDisplayName()) + ",</p>"
                + "<p>Email reminders are now <b>ON</b>. We will email you here when your pending tasks are "
                + "due within 3 days, due today, or overdue.</p>"
                + "<p style=\"color:#6B7280;font-size:12px\">You can turn this off anytime with the Email button in the app.</p></div>";
        mailer.send(user, "Email reminders are ON", html);
    }

    private static String buildHtml(User user, List<Row> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:Segoe UI,Arial,sans-serif;max-width:600px;margin:auto;padding:24px;")
          .append("border:1px solid #e5e7eb;border-radius:12px\">")
          .append("<h2 style=\"color:#0C1432;margin-top:0\">USTP-CDO Task Tracker</h2>")
          .append("<p>Hi ").append(Mailer.escape(user.getDisplayName())).append(",</p>")
          .append("<p>You have <b>").append(rows.size()).append("</b> task")
          .append(rows.size() == 1 ? "" : "s").append(" that need").append(rows.size() == 1 ? "s" : "")
          .append(" your attention:</p>")
          .append("<table style=\"width:100%;border-collapse:collapse;font-size:14px\">")
          .append("<tr style=\"background:#F3F4F6;text-align:left\"><th style=\"padding:8px\">Task</th>")
          .append("<th style=\"padding:8px\">Subject</th><th style=\"padding:8px\">Deadline</th>")
          .append("<th style=\"padding:8px\">Status</th></tr>");
        for (Row r : rows) {
            sb.append("<tr style=\"border-top:1px solid #E5E7EB\">")
              .append("<td style=\"padding:8px\"><b>").append(Mailer.escape(r.task().name())).append("</b></td>")
              .append("<td style=\"padding:8px\">").append(Mailer.escape(r.task().subject())).append("</td>")
              .append("<td style=\"padding:8px\">").append(DATE.format(r.task().deadline())).append("</td>")
              .append("<td style=\"padding:8px;font-weight:bold;color:").append(r.stage().color()).append("\">")
              .append(r.stage().label(r.days())).append("</td></tr>");
        }
        sb.append("</table>")
          .append("<p style=\"margin-top:18px\">Open the app and mark a task as <b>Completed</b> to stop its reminders.</p>")
          .append("<p style=\"color:#6B7280;font-size:12px\">You receive this because email reminders are ON for your account. ")
          .append("You can turn them off with the Email button in the app.</p></div>");
        return sb.toString();
    }
}
