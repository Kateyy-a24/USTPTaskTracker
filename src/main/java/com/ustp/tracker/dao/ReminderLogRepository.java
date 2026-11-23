package com.ustp.tracker.dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Remembers which reminder emails were already sent, so nobody gets the same email twice. */
public interface ReminderLogRepository {
    /** One sent reminder: which task and which stage (D0, D1, OD ...). */
    record Entry(int taskId, String stage) {}

    /** Keys "taskId:stage" of the reminders already sent on this day. */
    Set<String> sentOn(LocalDate day) throws SQLException;

    void markSent(List<Entry> entries, LocalDate day) throws SQLException;
}
