/**
 * Date: 2026-11-18 - Yash
 */
package com.gym.dao;

import com.gym.config.DBConnection;
import com.gym.model.FeePayment;
import com.gym.model.Role;
import com.gym.model.User;
import com.gym.session.UserSession;
import com.gym.util.PasswordUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
        String query = "SELECT id, gym_id, full_name, email, phone, password_hash, role, created_at, "
                + "membership_plan, membership_price, membership_start_date, membership_end_date, membership_duration_days "
                + "FROM users WHERE LOWER(email) = LOWER(?)";

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

                        User user = new User(
                                rs.getInt("id"),
                                rs.getString("gym_id") != null ? rs.getString("gym_id") : fallbackId,
                                rs.getString("full_name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                storedHash,
                                Role.fromString(rs.getString("role")),
                                rs.getTimestamp("created_at")
                        );

                        // Load membership details from database if already present
                        try {
                            String plan = rs.getString("membership_plan");
                            double price = rs.getDouble("membership_price");
                            Date startD = rs.getDate("membership_start_date");
                            Date endD = rs.getDate("membership_end_date");
                            int days = rs.getInt("membership_duration_days");

                            if (plan != null && !plan.trim().isEmpty()) {
                                user.setMembershipPlan(plan);
                                user.setMembershipPrice(price);
                                user.setMembershipDurationDays(days);
                                LocalDate start = startD != null ? startD.toLocalDate() : LocalDate.now();
                                LocalDate end = endD != null ? endD.toLocalDate() : start.plusDays(days > 0 ? days : 30);
                                user.setMembershipStartDate(start);
                                user.setMembershipEndDate(end);

                                UserSession.setMembership(plan, price, days, start, end);
                            }
                        } catch (Exception ex) {
                            System.err.println("Note reading membership columns: " + ex.getMessage());
                        }

                        return user;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Synchronizes a user's membership plan, start date, and end date from the database into UserSession.
     */
    public void syncUserMembership(User user) {
        if (user == null) return;
        String sql = "SELECT membership_plan, membership_price, membership_start_date, membership_end_date, membership_duration_days "
                   + "FROM users WHERE LOWER(email) = ? OR LOWER(gym_id) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getEmail().trim().toLowerCase());
            ps.setString(2, user.getGymId() != null ? user.getGymId().trim().toLowerCase() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String plan = rs.getString("membership_plan");
                    double price = rs.getDouble("membership_price");
                    Date startD = rs.getDate("membership_start_date");
                    Date endD = rs.getDate("membership_end_date");
                    int days = rs.getInt("membership_duration_days");

                    if (plan != null && !plan.trim().isEmpty()) {
                        user.setMembershipPlan(plan);
                        user.setMembershipPrice(price);
                        user.setMembershipDurationDays(days);
                        LocalDate start = startD != null ? startD.toLocalDate() : LocalDate.now();
                        LocalDate end = endD != null ? endD.toLocalDate() : start.plusDays(days > 0 ? days : 30);
                        user.setMembershipStartDate(start);
                        user.setMembershipEndDate(end);
                        UserSession.setMembership(plan, price, days, start, end);
                        return;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Note syncing user membership from users: " + ex.getMessage());
        }

        // Fallback: Check fee_payments table
        try {
            ReceptionistDAO dao = new ReceptionistDAO();
            FeePayment fp = dao.getLatestFeePayment(user.getGymId(), user.getEmail());
            if (fp != null && fp.getPlanType() != null && fp.getRenewalDueDate() != null) {
                String plan = fp.getPlanType();
                double price = fp.getAmount();
                LocalDate end = fp.getRenewalDueDate().toLocalDate();
                LocalDate start = fp.getPaymentDate() != null ? fp.getPaymentDate().toLocalDateTime().toLocalDate() : LocalDate.now();
                int days = (int) Math.max(0, ChronoUnit.DAYS.between(start, end));
                user.setMembershipPlan(plan);
                user.setMembershipPrice(price);
                user.setMembershipStartDate(start);
                user.setMembershipEndDate(end);
                user.setMembershipDurationDays(days);
                UserSession.setMembership(plan, price, days, start, end);
            }
        } catch (Exception ex) {
            System.err.println("Note syncing user membership from fee_payments: " + ex.getMessage());
        }
    }

    /**
     * Updates the membership plan, start date, and end date in the MySQL database.
     * Updates the existing user credentials/record in place without creating a new row.
     */
    public boolean updateMemberMembership(String gymId, String email, String plan, double price, int durationDays, LocalDate startDate, LocalDate endDate) {
        String cleanGymId = gymId != null ? gymId.trim().toLowerCase() : "";
        String cleanEmail = email != null ? email.trim().toLowerCase() : "";
        String sql = "UPDATE users SET membership_plan = ?, membership_price = ?, membership_duration_days = ?, membership_start_date = ?, membership_end_date = ? "
                   + "WHERE (LOWER(gym_id) = ? AND gym_id != '') OR LOWER(email) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plan);
            ps.setDouble(2, price);
            ps.setInt(3, durationDays);
            ps.setDate(4, Date.valueOf(startDate));
            ps.setDate(5, Date.valueOf(endDate));
            ps.setString(6, cleanGymId);
            ps.setString(7, cleanEmail);

            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            System.err.println("Error updating user membership in DB: " + ex.getMessage());
            return false;
        }
    }

    public boolean updateMemberMembership(String emailOrGymId, String plan, double price, int durationDays, LocalDate startDate, LocalDate endDate) {
        return updateMemberMembership(emailOrGymId, emailOrGymId, plan, price, durationDays, startDate, endDate);
    }
}
