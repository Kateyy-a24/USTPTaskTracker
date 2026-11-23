package com.ustp.tracker.controller;

import com.ustp.tracker.AppContext;
import com.ustp.tracker.Session;
import com.ustp.tracker.dao.TaskRepository;
import com.ustp.tracker.dao.UserRepository;
import com.ustp.tracker.model.User;

/**
 * Parent class of every screen controller. It gives each screen the same tools
 * (the repositories and the logged-in student) so the screens do not repeat this code.
 * It talks to the data only through interfaces, never to MySQL directly.
 */
public abstract class BaseController {
    protected final AppContext context = AppContext.get();
    protected final UserRepository userRepo = context.users();
    protected final TaskRepository taskRepo = context.tasks();

    /** The student who is logged in (null on the login screens). */
    protected User currentUser() {
        return Session.getUser();
    }
}
