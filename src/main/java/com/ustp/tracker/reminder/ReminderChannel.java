package com.ustp.tracker.reminder;

import com.ustp.tracker.model.Task;
import com.ustp.tracker.model.User;

import java.util.List;

/**
 * One way of reminding a student about deadlines (on-screen alert, email ...).
 * The ReminderService only knows this interface and calls every channel the same way,
 * so a new kind of reminder can be added without changing the service (polymorphism).
 */
public interface ReminderChannel {
    /** Called on every check with all the tasks of the student. The channel decides what to send. */
    void remind(User user, List<Task> tasks);

    /** Called when a student logs in, to forget what was already shown. */
    default void reset() { }

    /** Called when the student logs out or the app closes, to release sounds, icons, and so on. */
    default void close() { }
}
