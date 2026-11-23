package com.ustp.tracker.dao;

import com.ustp.tracker.model.User;

import java.sql.SQLException;
import java.util.List;

/** What the application needs from the place where student accounts are stored. */
public interface UserRepository {
    /** Login with username OR email. Returns null when wrong. */
    User login(String usernameOrEmail, String password) throws SQLException;

    void register(User user, String password) throws SQLException;

    void updateProfile(User user) throws SQLException;

    /** True if the username or email is already used. */
    boolean exists(String username, String email) throws SQLException;

    /** True if another account (not userId) already uses this email. */
    boolean emailTakenByOther(String email, int userId) throws SQLException;

    /** Finds an account by username OR email. Null when none. */
    User findByUsernameOrEmail(String usernameOrEmail) throws SQLException;

    /** All accounts registered with this email. */
    List<User> findByEmail(String email) throws SQLException;

    /** Every account that has an email. */
    List<User> allWithEmail() throws SQLException;

    /** Sets a new password. Returns true when the account was updated. */
    boolean resetPassword(int userId, String newPassword) throws SQLException;
}
