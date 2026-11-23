package com.ustp.tracker.dao;

import com.ustp.tracker.model.Task;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTaskRepository extends JdbcRepository implements TaskRepository {

    @Override
    public List<Task> findAll(int userId) throws SQLException {
        List<Task> list = new ArrayList<>();
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM tasks WHERE user_id=? ORDER BY deadline")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Task(rs.getInt("id"), rs.getString("name"), rs.getString("subject"),
                            rs.getString("description"), rs.getDate("deadline").toLocalDate(), rs.getString("status")));
                }
            }
        }
        return list;
    }

    @Override
    public void add(int userId, Task t) throws SQLException {
        execute("INSERT INTO tasks (user_id,name,subject,description,deadline,status) VALUES (?,?,?,?,?,?)",
                userId, t.name(), t.subject(), t.description(), Date.valueOf(t.deadline()), t.status());
    }

    @Override
    public void update(Task t) throws SQLException {
        execute("UPDATE tasks SET name=?, subject=?, description=?, deadline=?, status=? WHERE id=?",
                t.name(), t.subject(), t.description(), Date.valueOf(t.deadline()), t.status(), t.id());
    }

    @Override
    public void setStatus(int taskId, String status) throws SQLException {
        execute("UPDATE tasks SET status=? WHERE id=?", status, taskId);
    }

    @Override
    public void delete(int taskId) throws SQLException {
        execute("DELETE FROM tasks WHERE id=?", taskId);
    }
}
