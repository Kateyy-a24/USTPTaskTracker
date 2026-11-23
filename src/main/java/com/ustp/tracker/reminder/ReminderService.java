package com.ustp.tracker.reminder;

import com.ustp.tracker.Session;
import com.ustp.tracker.dao.TaskRepository;
import com.ustp.tracker.model.Task;
import com.ustp.tracker.model.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;

/**
 * Checks the logged-in student's tasks every minute and lets every {@link ReminderChannel} decide
 * how to remind them. The service does not know what the channels do (abstraction): it just calls them.
 */
public class ReminderService {
    private final TaskRepository tasks;
    private final List<ReminderChannel> channels;
    private Timeline first, every;

    public ReminderService(TaskRepository tasks, List<ReminderChannel> channels) {
        this.tasks = tasks;
        this.channels = List.copyOf(channels);
    }

    public void start() {
        stop();
        for (ReminderChannel c : channels) c.reset();
        first = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> check()));
        first.play();
        every = new Timeline(new KeyFrame(Duration.seconds(60), e -> check()));
        every.setCycleCount(Timeline.INDEFINITE);
        every.play();
    }

    public void stop() {
        if (first != null) first.stop();
        if (every != null) every.stop();
        first = every = null;
        for (ReminderChannel c : channels) c.close();
    }

    private void check() {
        final User user = Session.getUser();
        if (user == null) return;
        Thread t = new Thread(() -> {
            try {
                List<Task> list = tasks.findAll(user.getId());
                for (ReminderChannel c : channels) c.remind(user, list);   // same call, different behavior
            } catch (Exception ex) {
                System.err.println("Reminder check failed: " + ex.getMessage());
            }
        }, "deadline-reminder");
        t.setDaemon(true);
        t.start();
    }
}
