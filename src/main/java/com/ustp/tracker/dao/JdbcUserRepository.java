package com.ustp.tracker.dao;

import com.ustp.tracker.model.User;
import com.ustp.tracker.security.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcUserRepository extends JdbcRepository implements UserRepository {
    private final PasswordHasher hasher;

    public JdbcUserRepository(PasswordHasher hasher) {
        this.hasher = hasher;
    }

    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setStudentId(rs.getString("student_id"));
        u.setCollege(rs.getString("college"));
        u.setProgram(rs.getString("program"));
        u.setSemester(rs.getString("semester"));
        u.setAvatarPath(rs.getString("avatar_path"));
        return u;
    }

    private List<User> query(String sql, Object... params) throws SQLException {
        List<User> list = new ArrayList<>();
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public User login(String id, String pw) throws SQLException {
        List<User> found = query("SELECT * FROM users WHERE (username=? OR email=?) AND password_hash=?",
                id, id, hasher.hash(pw));
        return found.isEmpty() ? null : found.get(0);
    }

    @Override
    public void register(User u, String password) throws SQLException {
        execute("INSERT INTO users (username,password_hash,full_name,email,student_id,college,program,semester) VALUES (?,?,?,?,?,?,?,?)",
                u.getUsername(), hasher.hash(password), u.getFullName(), u.getEmail(), u.getStudentId(),
                u.getCollege() == null ? "" : u.getCollege(),
                u.getProgram() == null ? "" : u.getProgram(),
                u.getSemester() == null ? "" : u.getSemester());
    }

    @Override
    public void updateProfile(User u) throws SQLException {
        execute("UPDATE users SET full_name=?, email=?, student_id=?, college=?, program=?, semester=?, avatar_path=? WHERE id=?",
                u.getFullName(), u.getEmail(), u.getStudentId(), u.getCollege(), u.getProgram(),
                u.getSemester(), u.getAvatarPath(), u.getId());
    }

    @Override
    public boolean exists(String username, String email) throws SQLException {
        return queryExists("SELECT 1 FROM users WHERE username=? OR email=?", username, email);
    }

    @Override
    public boolean emailTakenByOther(String email, int userId) throws SQLException {
        return queryExists("SELECT 1 FROM users WHERE email=? AND id<>?", email, userId);
    }

    @Override
    public User findByUsernameOrEmail(String id) throws SQLException {
        List<User> found = query("SELECT * FROM users WHERE username=? OR email=? LIMIT 1", id, id);
        return found.isEmpty() ? null : found.get(0);
    }

    @Override
    public List<User> findByEmail(String email) throws SQLException {
        return query("SELECT * FROM users WHERE email=?", email);
    }

    @Override
    public List<User> allWithEmail() throws SQLException {
        return query("SELECT * FROM users WHERE email IS NOT NULL AND email<>''");
    }

    @Override
    public boolean resetPassword(int userId, String newPassword) throws SQLException {
        return execute("UPDATE users SET password_hash=? WHERE id=?", hasher.hash(newPassword), userId) > 0;
    }
}
