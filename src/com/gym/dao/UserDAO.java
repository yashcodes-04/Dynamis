/**
 * Date: 2026-11-18 - Yash
 */
package com.gym.dao;

import com.gym.config.DBConnection;
import com.gym.model.Role;
import com.gym.model.User;
import com.gym.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class UserDAO {

    public boolean isEmailTaken(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Registers a new user. The unique Gym ID is formatted like a roll number:
     * Year + (1000 + autoIncrementId) -> e.g. 20261001, 20261002
     */
    public boolean register(String fullName, String email, String phone, String plainPassword, Role role) throws SQLException {
        if (isEmailTaken(email)) {
            return false;
        }

        String passwordHash = PasswordUtil.hashPassword(plainPassword);
        String insertSQL = "INSERT INTO users (full_name, email, phone, password_hash, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, phone != null ? phone.trim() : "");
            ps.setString(4, passwordHash);
            ps.setString(5, role.name());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int autoId = keys.getInt(1);
                        int year = LocalDate.now().getYear();
                        String rollGymId = String.valueOf(year) + (1000 + autoId); // e.g. 20261001

                        try (PreparedStatement updatePs = conn.prepareStatement("UPDATE users SET gym_id = ? WHERE id = ?")) {
                            updatePs.setString(1, rollGymId);
                            updatePs.setInt(2, autoId);
                            updatePs.executeUpdate();
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    public User authenticate(String email, String plainPassword) throws SQLException {
        String query = "SELECT id, gym_id, full_name, email, phone, password_hash, role, created_at FROM users WHERE LOWER(email) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verifyPassword(plainPassword, storedHash)) {
                        int year = rs.getTimestamp("created_at") != null
                                ? rs.getTimestamp("created_at").toLocalDateTime().getYear()
                                : LocalDate.now().getYear();
                        String fallbackId = String.valueOf(year) + (1000 + rs.getInt("id"));

                        return new User(
                                rs.getInt("id"),
                                rs.getString("gym_id") != null ? rs.getString("gym_id") : fallbackId,
                                rs.getString("full_name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                storedHash,
                                Role.fromString(rs.getString("role")),
                                rs.getTimestamp("created_at")
                        );
                    }
                }
            }
        }
        return null;
    }
}
