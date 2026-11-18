/**
 * Date: 2026-11-16 - Yash
 */
package com.gym.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBConnection {

    private static String url;
    private static String user;
    private static String password;
    private static boolean initialized = false;

    static {
        loadConfig();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Warning: MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    private static synchronized void loadConfig() {
        Properties props = new Properties();
        boolean loaded = false;

        // 1. Try loading directly from file system (working directory)
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream input = new FileInputStream(propFile)) {
                props.load(input);
                loaded = true;
            } catch (Exception e) {
                System.err.println("Error reading db.properties: " + e.getMessage());
            }
        }

        // 2. Fallback: Try loading from classpath
        if (!loaded) {
            try (InputStream cpInput = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (cpInput != null) {
                    props.load(cpInput);
                    loaded = true;
                }
            } catch (Exception e) {
                System.err.println("Error reading db.properties from classpath: " + e.getMessage());
            }
        }

        if (loaded) {
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");
            if (url != null) url = url.trim();
            if (user != null) user = user.trim();
            if (password != null) password = password.trim();
        } else {
            System.err.println("ERROR: Could not locate db.properties in workspace or classpath.");
        }
    }

    public static Connection getConnection() throws SQLException {
        if (url == null || user == null) {
            loadConfig();
            if (url == null || user == null) {
                throw new SQLException("Unable to establish connection: db.properties could not be loaded.");
            }
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            // MySQL error code 1049 is "Unknown database"
            if (e.getErrorCode() == 1049 || (e.getMessage() != null && e.getMessage().toLowerCase().contains("unknown database"))) {
                createDatabaseIfNotExists();
                conn = DriverManager.getConnection(url, user, password);
            } else {
                throw e;
            }
        }

        if (!initialized) {
            initTable(conn);
            initialized = true;
        }
        return conn;
    }

    private static void createDatabaseIfNotExists() {
        try {
            String serverUrl = url;
            String dbName = "gymdb";

            int protoEnd = url.indexOf("://");
            int slashIndex = (protoEnd != -1) ? url.indexOf("/", protoEnd + 3) : url.indexOf("/");

            if (slashIndex != -1) {
                int questionIndex = url.indexOf("?", slashIndex);
                if (questionIndex != -1) {
                    dbName = url.substring(slashIndex + 1, questionIndex);
                    serverUrl = url.substring(0, slashIndex + 1) + url.substring(questionIndex);
                } else {
                    dbName = url.substring(slashIndex + 1);
                    serverUrl = url.substring(0, slashIndex + 1);
                }
            }

            try (Connection serverConn = DriverManager.getConnection(serverUrl, user, password);
                 Statement stmt = serverConn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "`");
                System.out.println("Auto-created missing database: " + dbName);
            }
        } catch (Exception ex) {
            System.err.println("Could not auto-create database: " + ex.getMessage());
        }
    }

    private static void initTable(Connection conn) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "gym_id VARCHAR(20) UNIQUE,"
                + "full_name VARCHAR(100) NOT NULL,"
                + "email VARCHAR(100) NOT NULL UNIQUE,"
                + "phone VARCHAR(20),"
                + "password_hash VARCHAR(255) NOT NULL,"
                + "role ENUM('MEMBER', 'RECEPTIONIST', 'TRAINER', 'OWNER') NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";

        String createFeePaymentsSQL = "CREATE TABLE IF NOT EXISTS fee_payments ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "gym_id VARCHAR(20),"
                + "member_email VARCHAR(100) NOT NULL,"
                + "member_name VARCHAR(100) NOT NULL,"
                + "plan_type VARCHAR(50) NOT NULL,"
                + "amount DECIMAL(10, 2) NOT NULL,"
                + "payment_mode VARCHAR(50) NOT NULL,"
                + "payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "renewal_due_date DATE NOT NULL"
                + ");";

        String createAttendanceSQL = "CREATE TABLE IF NOT EXISTS attendance ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "gym_id VARCHAR(20) NOT NULL,"
                + "member_name VARCHAR(100) NOT NULL,"
                + "member_email VARCHAR(100) NOT NULL,"
                + "check_in_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);

            // Add gym_id to users if not present and backfill existing users
            try {
                java.sql.DatabaseMetaData meta = conn.getMetaData();
                try (ResultSet rs = meta.getColumns(null, null, "users", "gym_id")) {
                    if (!rs.next()) {
                        stmt.execute("ALTER TABLE users ADD COLUMN gym_id VARCHAR(20) UNIQUE AFTER id");
                    }
                }
                stmt.execute("UPDATE users SET gym_id = CONCAT('GYM-', 1000 + id) WHERE gym_id IS NULL OR gym_id = ''");
            } catch (Exception ex) {
                System.err.println("Note on users gym_id migration: " + ex.getMessage());
            }

            stmt.execute(createFeePaymentsSQL);

            // Add gym_id to fee_payments if not present
            try {
                java.sql.DatabaseMetaData meta = conn.getMetaData();
                try (ResultSet rs = meta.getColumns(null, null, "fee_payments", "gym_id")) {
                    if (!rs.next()) {
                        stmt.execute("ALTER TABLE fee_payments ADD COLUMN gym_id VARCHAR(20) AFTER id");
                    }
                }
            } catch (Exception ex) {
                System.err.println("Note on fee_payments gym_id migration: " + ex.getMessage());
            }

            stmt.execute(createAttendanceSQL);
        } catch (SQLException e) {
            System.err.println("Note on table check: " + e.getMessage());
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }
}
