/**
 * Date: 2026-11-16 - Yash
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

    public boolean register(String fullName, String email, String phone, String plainPassword, Role role) throws SQLException {
        if (isEmailTaken(email)) {
            return false;
        }

        String passwordHash = PasswordUtil.hashPassword(plainPassword);
        String insertSQL = "INSERT INTO users (full_name, email, phone, password_hash, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSQL)) {
            ps.setString(1, fullName.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, phone != null ? phone.trim() : "");
            ps.setString(4, passwordHash);
            ps.setString(5, role.name());

            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    public User authenticate(String email, String plainPassword) throws SQLException {
        String query = "SELECT id, full_name, email, phone, password_hash, role, created_at FROM users WHERE LOWER(email) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verifyPassword(plainPassword, storedHash)) {
                        return new User(
                                rs.getInt("id"),
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
