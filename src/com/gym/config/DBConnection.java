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

        String createBookedSessionsSQL = "CREATE TABLE IF NOT EXISTS booked_sessions ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "gym_id VARCHAR(20),"
                + "member_email VARCHAR(100) NOT NULL,"
                + "member_name VARCHAR(100) NOT NULL,"
                + "class_name VARCHAR(100) NOT NULL,"
                + "description VARCHAR(255),"
                + "level VARCHAR(50),"
                + "duration VARCHAR(50) NOT NULL,"
                + "price DECIMAL(10, 2) NOT NULL,"
                + "schedule_slot VARCHAR(100),"
                + "status VARCHAR(50) DEFAULT 'CONFIRMED',"
                + "booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
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
                stmt.execute("UPDATE users SET gym_id = CONCAT(YEAR(COALESCE(created_at, NOW())), 1000 + id) WHERE gym_id IS NULL OR gym_id = '' OR gym_id LIKE 'GYM-%'");
            } catch (Exception ex) {
                System.err.println("Note on users gym_id migration: " + ex.getMessage());
            }

            // Add membership columns to users if not present
            try {
                java.sql.DatabaseMetaData meta = conn.getMetaData();
                try (ResultSet rs = meta.getColumns(null, null, "users", "membership_plan")) {
                    if (!rs.next()) {
                        stmt.execute("ALTER TABLE users ADD COLUMN membership_plan VARCHAR(50) DEFAULT NULL");
                        stmt.execute("ALTER TABLE users ADD COLUMN membership_price DECIMAL(10, 2) DEFAULT 0.00");
                        stmt.execute("ALTER TABLE users ADD COLUMN membership_start_date DATE DEFAULT NULL");
                        stmt.execute("ALTER TABLE users ADD COLUMN membership_end_date DATE DEFAULT NULL");
                        stmt.execute("ALTER TABLE users ADD COLUMN membership_duration_days INT DEFAULT 0");
                    }
                }
            } catch (Exception ex) {
                System.err.println("Note on users membership columns migration: " + ex.getMessage());
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
            stmt.execute(createBookedSessionsSQL);

            String createTraineesSQL = "CREATE TABLE IF NOT EXISTS trainees ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "gym_id VARCHAR(20) UNIQUE,"
                    + "full_name VARCHAR(100) NOT NULL,"
                    + "email VARCHAR(100) NOT NULL UNIQUE,"
                    + "phone VARCHAR(20),"
                    + "body_type VARCHAR(50) DEFAULT 'Mesomorph',"
                    + "fitness_goal VARCHAR(100) DEFAULT 'General Fitness',"
                    + "current_weight DECIMAL(5, 2) DEFAULT 70.00,"
                    + "target_weight DECIMAL(5, 2) DEFAULT 75.00,"
                    + "height_cm DECIMAL(5, 2) DEFAULT 175.00,"
                    + "assigned_diet_plan VARCHAR(100) DEFAULT 'Standard Maintenance',"
                    + "trainer_notes TEXT,"
                    + "assigned_trainer VARCHAR(100) DEFAULT 'Lead Trainer',"
                    + "status VARCHAR(50) DEFAULT 'Active',"
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                    + ");";

            String createDietPlansSQL = "CREATE TABLE IF NOT EXISTS diet_plans ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "plan_name VARCHAR(100) NOT NULL UNIQUE,"
                    + "body_type VARCHAR(50) NOT NULL,"
                    + "goal VARCHAR(100) NOT NULL,"
                    + "daily_calories INT NOT NULL,"
                    + "protein_grams INT NOT NULL,"
                    + "carbs_grams INT NOT NULL,"
                    + "fats_grams INT NOT NULL,"
                    + "water_liters DECIMAL(3, 1) NOT NULL,"
                    + "breakfast TEXT,"
                    + "mid_morning_snack TEXT,"
                    + "lunch TEXT,"
                    + "pre_workout TEXT,"
                    + "post_workout TEXT,"
                    + "dinner TEXT,"
                    + "guidelines TEXT"
                    + ");";

            stmt.execute(createTraineesSQL);
            stmt.execute(createDietPlansSQL);
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
