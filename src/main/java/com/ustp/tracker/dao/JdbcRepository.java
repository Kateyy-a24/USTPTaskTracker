package com.ustp.tracker.dao;

import com.ustp.tracker.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Shared base class of all MySQL repositories: one place for the connection and common helpers. */
public abstract class JdbcRepository {

    protected Connection connection() throws SQLException {
        return Db.get();
    }

    protected static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
    }

    /** Runs a SELECT and tells whether it returned at least one row. */
    protected boolean queryExists(String sql, Object... params) throws SQLException {
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Runs an INSERT / UPDATE / DELETE and returns how many rows changed. */
    protected int execute(String sql, Object... params) throws SQLException {
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }
}
