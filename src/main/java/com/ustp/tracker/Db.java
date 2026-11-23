package com.ustp.tracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** XAMPP defaults: user root, empty password. Change here if yours is different. */
public final class Db {
    private static final String URL =
        "jdbc:mysql://localhost:3306/ustp_task_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila";
    private static final String USER = "root";
    private static final String PASS = "";

    private Db() {}

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
