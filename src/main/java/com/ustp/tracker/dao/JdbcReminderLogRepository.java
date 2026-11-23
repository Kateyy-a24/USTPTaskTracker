package com.ustp.tracker.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JdbcReminderLogRepository extends JdbcRepository implements ReminderLogRepository {
    private boolean tableReady;

    /** The table is created automatically, so older databases do not need to be re-imported. */
    private synchronized void ensureTable() throws SQLException {
        if (tableReady) return;
        try (Connection c = connection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS reminder_log ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, task_id INT NOT NULL, stage VARCHAR(8) NOT NULL, "
                    + "sent_on DATE NOT NULL, UNIQUE KEY uq_reminder (task_id, stage, sent_on), "
                    + "FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE)");
        }
        tableReady = true;
    }

    @Override
    public Set<String> sentOn(LocalDate day) throws SQLException {
        ensureTable();
        Set<String> keys = new HashSet<>();
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement("SELECT task_id, stage FROM reminder_log WHERE sent_on=?")) {
            ps.setDate(1, Date.valueOf(day));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) keys.add(rs.getInt(1) + ":" + rs.getString(2));
            }
        }
        return keys;
    }

    @Override
    public void markSent(List<Entry> entries, LocalDate day) throws SQLException {
        ensureTable();
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT IGNORE INTO reminder_log (task_id, stage, sent_on) VALUES (?,?,?)")) {
            for (Entry e : entries) {
                ps.setInt(1, e.taskId());
                ps.setString(2, e.stage());
                ps.setDate(3, Date.valueOf(day));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
