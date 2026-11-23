package com.ustp.tracker.reminder;

import com.ustp.tracker.App;
import com.ustp.tracker.Dialogs;
import com.ustp.tracker.Session;
import com.ustp.tracker.Ui;
import com.ustp.tracker.model.Task;
import com.ustp.tracker.model.User;
import javafx.application.Platform;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * On-screen reminder: pop-up + alarm ringtone + Windows tray notification.
 *  - Overdue / due today: alerts at login, then again every 60 minutes until the task is Completed.
 *  - Due tomorrow, or in 2-3 days: alerts once per login.
 */
public class ScreenReminderChannel implements ReminderChannel {
    private static final long REPEAT_MS = 60L * 60 * 1000;

    private final AlarmSound alarm;
    private final ReminderSettings settings;
    private final Map<String, Long> sent = new HashMap<>();
    private boolean showing;
    private TrayIcon tray;

    public ScreenReminderChannel(AlarmSound alarm, ReminderSettings settings) {
        this.alarm = alarm;
        this.settings = settings;
    }

    @Override
    public void reset() {
        sent.clear();
    }

    @Override
    public void close() {
        alarm.stop();
        removeTray();
    }

    /** May be called from a background thread: the work is moved to the JavaFX thread. */
    @Override
    public void remind(User user, List<Task> tasks) {
        final int uid = user.getId();
        Platform.runLater(() -> process(uid, tasks));
    }

    private void process(int uid, List<Task> tasks) {
        User current = Session.getUser();
        if (current == null || current.getId() != uid || showing) return;

        LocalDate today = LocalDate.now();
        long now = System.currentTimeMillis();
        List<Task> toAlert = new ArrayList<>();
        boolean urgent = false;

        for (Task t : tasks) {
            if (!t.isPending()) continue;
            long days = ChronoUnit.DAYS.between(today, t.deadline());
            DueStage stage = DueStage.of(days);
            if (stage == null) continue;                  // more than 3 days away
            boolean repeating = stage.repeats();
            String key = t.id() + ":" + stage.screenCode() + ":" + t.deadline();
            Long last = sent.get(key);
            if (last == null || (repeating && now - last >= REPEAT_MS)) {
                toAlert.add(t);
                sent.put(key, now);
                if (repeating) urgent = true;
            }
        }
        if (toAlert.isEmpty()) return;

        toAlert.sort(Comparator.comparing(Task::deadline));
        StringBuilder sb = new StringBuilder();
        int shown = 0;
        for (Task t : toAlert) {
            if (shown == 6) break;
            if (sb.length() > 0) sb.append('\n');
            sb.append("• ").append(t.name()).append(" (").append(t.subject()).append(") - ")
              .append(Ui.dueText(t.deadline()));
            shown++;
        }
        if (toAlert.size() > shown) sb.append("\n+ ").append(toAlert.size() - shown).append(" more");

        show(uid, urgent ? "Deadline Alert!" : "Upcoming Deadline", sb.toString(), urgent);
    }

    private void show(int uid, String title, String msg, boolean urgent) {
        showing = true;
        javafx.stage.Stage st = App.stage();
        if (st != null) {
            if (st.isIconified()) st.setIconified(false);
            st.toFront();
        }
        trayNotify(title, msg);
        if (settings.isSoundEnabled(uid)) alarm.play(urgent ? 4 : 2);

        // runLater so the dialog is not opened in the middle of an animation pulse
        Platform.runLater(() -> {
            boolean open = false;
            try {
                open = Dialogs.show(urgent ? Dialogs.Kind.DANGER : Dialogs.Kind.INFO,
                        urgent ? Dialogs.ALERT : Dialogs.INFO,
                        title, msg, "View Deadlines", "Dismiss");
            } finally {
                alarm.stop();
                showing = false;
            }
            if (open && Session.getMain() != null && Session.getUser() != null) Session.getMain().showUpcoming();
        });
    }

    private void trayNotify(String title, String msg) {
        try {
            if (!SystemTray.isSupported()) return;
            if (tray == null) {
                BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(0x0F1B3D));
                g.fillOval(1, 1, 30, 30);
                g.setColor(new Color(0xF5B301));
                g.setStroke(new java.awt.BasicStroke(3f));
                g.drawOval(4, 4, 24, 24);
                g.fillOval(13, 9, 6, 11);
                g.fillOval(13, 22, 6, 4);
                g.dispose();
                tray = new TrayIcon(img, "USTP-CDO Task Tracker");
                tray.setImageAutoSize(true);
                SystemTray.getSystemTray().add(tray);
            }
            String shortMsg = msg.length() > 200 ? msg.substring(0, 197) + "..." : msg;
            tray.displayMessage(title, shortMsg, TrayIcon.MessageType.WARNING);
        } catch (Throwable ignored) {
            // tray not available - the pop-up and sound still work
        }
    }

    private void removeTray() {
        try {
            if (tray != null) SystemTray.getSystemTray().remove(tray);
        } catch (Throwable ignored) { }
        tray = null;
    }
}
