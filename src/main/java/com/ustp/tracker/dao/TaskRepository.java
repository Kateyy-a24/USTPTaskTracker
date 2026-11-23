package com.ustp.tracker.dao;

import com.ustp.tracker.model.Task;

import java.sql.SQLException;
import java.util.List;

/** What the application needs from the place where tasks are stored. */
public interface TaskRepository {
    /** All tasks of one student, ordered by deadline. */
    List<Task> findAll(int userId) throws SQLException;

    void add(int userId, Task task) throws SQLException;

    void update(Task task) throws SQLException;

    void setStatus(int taskId, String status) throws SQLException;

    void delete(int taskId) throws SQLException;
}
