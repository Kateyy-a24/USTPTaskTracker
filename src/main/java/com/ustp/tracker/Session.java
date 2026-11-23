package com.ustp.tracker;

import com.ustp.tracker.controller.MainController;
import com.ustp.tracker.model.Task;
import com.ustp.tracker.model.User;

/** Holds the logged-in user and what the next page needs to know. The data is private; use the getters/setters. */
public final class Session {
    private static User user;
    private static MainController main;
    private static Task editTask;     // set when editing a task
    private static Task statusTask;   // set when updating a task's status

    private Session() {}

    public static User getUser() { return user; }
    public static void setUser(User u) { user = u; }

    public static MainController getMain() { return main; }
    public static void setMain(MainController m) { main = m; }

    public static Task getEditTask() { return editTask; }
    public static void setEditTask(Task t) { editTask = t; }

    public static Task getStatusTask() { return statusTask; }
    public static void setStatusTask(Task t) { statusTask = t; }
}
