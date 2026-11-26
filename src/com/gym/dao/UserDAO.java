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
import java.util.ArrayList;
import java.util.List;

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

    public boolean isPhoneTaken(String phone) throws SQLException {
        if (phone == null || phone.trim().isEmpty()) return false;
        String query = "SELECT COUNT(*) FROM users WHERE phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Option 2: Least-Loaded Balancing (Smart Auto-Assignment)
     * Queries all registered trainers and calculates how many active members each trainer currently has.
     * Selects the trainer with the lowest client workload to prevent overloading any one trainer.
     * If counts are tied, breaks tie deterministically by registration order (t.id ASC).
     * Returns null if no trainer is registered in the system yet.
     */
    public String getLeastLoadedTrainerName() {
        String query = "SELECT t.full_name, COUNT(m.id) AS member_count "
                     + "FROM users t "
                     + "LEFT JOIN users m ON m.role = 'MEMBER' AND LOWER(TRIM(m.assigned_trainer)) = LOWER(TRIM(t.full_name)) "
                     + "WHERE t.role = 'TRAINER' "
                     + "GROUP BY t.id, t.full_name "
                     + "ORDER BY member_count ASC, t.id ASC "
                     + "LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String name = rs.getString("full_name");
                if (name != null && !name.trim().isEmpty()) {
                    return name.trim();
                }
            }
        } catch (Exception ex) {
            System.err.println("Note querying least-loaded trainer: " + ex.getMessage());
        }

        return getFirstAvailableTrainerFallback();
    }

    private String getFirstAvailableTrainerFallback() {
        String query = "SELECT full_name FROM users WHERE role = 'TRAINER' ORDER BY id ASC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String name = rs.getString("full_name");
                if (name != null && !name.trim().isEmpty()) {
                    return name.trim();
                }
            }
        } catch (Exception ex) {
            System.err.println("Note reading fallback trainer: " + ex.getMessage());
        }
        return null;
    }

    public String getRandomTrainerName() {
        return getLeastLoadedTrainerName();
    }

    /**
     * When a new trainer registers, distributes any existing members who do NOT have an assigned trainer yet
     * across trainers using least-loaded balancing.
     * Members who already have a real trainer assigned are left unchanged.
     */
    public void assignTrainerToUnassignedMembers(String trainerName) {
        try (Connection conn = DBConnection.getConnection()) {
            String findUnassigned = "SELECT id, email, gym_id FROM users WHERE role = 'MEMBER' AND (assigned_trainer IS NULL OR assigned_trainer = '') ORDER BY id ASC";
            List<Integer> unassignedIds = new java.util.ArrayList<>();
            List<String> unassignedEmails = new java.util.ArrayList<>();
            List<String> unassignedGymIds = new java.util.ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(findUnassigned);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    unassignedIds.add(rs.getInt("id"));
                    unassignedEmails.add(rs.getString("email"));
                    unassignedGymIds.add(rs.getString("gym_id"));
                }
            }

            for (int i = 0; i < unassignedIds.size(); i++) {
                int memberId = unassignedIds.get(i);
                String email = unassignedEmails.get(i);
                String gymId = unassignedGymIds.get(i);

                String assigned = getLeastLoadedTrainerName();
                if (assigned == null || assigned.trim().isEmpty()) {
                    assigned = trainerName;
                }
                if (assigned != null && !assigned.trim().isEmpty()) {
                    try (PreparedStatement updatePs = conn.prepareStatement("UPDATE users SET assigned_trainer = ? WHERE id = ?")) {
                        updatePs.setString(1, assigned);
                        updatePs.setInt(2, memberId);
                        updatePs.executeUpdate();
                    }
                    try (PreparedStatement updateTr = conn.prepareStatement("UPDATE trainees SET assigned_trainer = ? WHERE LOWER(email) = ? OR (LOWER(gym_id) = ? AND gym_id != '')")) {
                        updateTr.setString(1, assigned);
                        updateTr.setString(2, email != null ? email.toLowerCase() : "");
                        updateTr.setString(3, gymId != null ? gymId.toLowerCase() : "");
                        updateTr.executeUpdate();
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Note assigning trainer to unassigned members: " + ex.getMessage());
        }
    }

    /**
     * Registers a new user. The unique Gym ID is formatted like a roll number:
     * Year + (1000 + autoIncrementId) -> e.g. 20261001, 20261002
     */
    public boolean register(String fullName, String email, String phone, String plainPassword, Role role) throws SQLException {
        if (isEmailTaken(email)) {
            return false;
        }
        if (isPhoneTaken(phone)) {
            return false;
        }

        String passwordHash = PasswordUtil.hashPassword(plainPassword);
        String assignedTrainer = null;
        if (role == Role.MEMBER) {
            assignedTrainer = getRandomTrainerName(); // null if no trainer registered yet
        }

        String insertSQL = "INSERT INTO users (full_name, email, phone, password_hash, role, assigned_trainer) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, phone != null ? phone.trim() : "");
            ps.setString(4, passwordHash);
            ps.setString(5, role.name());
            ps.setString(6, assignedTrainer);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                int autoId = 0;
                String rollGymId = "";
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        autoId = keys.getInt(1);
                        int year = LocalDate.now().getYear();
                        rollGymId = String.valueOf(year) + (1000 + autoId); // e.g. 20261001

                        try (PreparedStatement updatePs = conn.prepareStatement("UPDATE users SET gym_id = ? WHERE id = ?")) {
                            updatePs.setString(1, rollGymId);
                            updatePs.setInt(2, autoId);
                            updatePs.executeUpdate();
                        }
                    }
                }

                // If Member registered, create/sync initial entry in trainees table
                if (role == Role.MEMBER) {
                    try {
                        String traineeSql = "INSERT INTO trainees (gym_id, full_name, email, phone, assigned_trainer, status) "
                                          + "VALUES (?, ?, ?, ?, ?, 'Active') "
                                          + "ON DUPLICATE KEY UPDATE assigned_trainer = COALESCE(assigned_trainer, VALUES(assigned_trainer))";
                        try (PreparedStatement trPs = conn.prepareStatement(traineeSql)) {
                            trPs.setString(1, rollGymId);
                            trPs.setString(2, fullName.trim());
                            trPs.setString(3, email.trim().toLowerCase());
                            trPs.setString(4, phone != null ? phone.trim() : "");
                            trPs.setString(5, assignedTrainer);
                            trPs.executeUpdate();
                        }
                    } catch (Exception ex) {
                        System.err.println("Note initializing trainee row: " + ex.getMessage());
                    }
                }

                // If Trainer registered, assign this new trainer to existing members who don't have one
                if (role == Role.TRAINER) {
                    assignTrainerToUnassignedMembers(fullName.trim());
                }

                return true;
            }
        }
        return false;
    }

    public User authenticate(String email, String plainPassword) throws SQLException {
        String query = "SELECT id, gym_id, full_name, email, phone, password_hash, role, created_at, "
                + "membership_plan, membership_price, membership_start_date, membership_end_date, membership_duration_days, assigned_trainer "
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

                        String trainer = rs.getString("assigned_trainer");
                        user.setAssignedTrainer(trainer);
                        UserSession.setAssignedTrainer(trainer);

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
        syncUserTrainer(user);
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
     * Synchronizes the assigned trainer for a member.
     * If no trainer is currently assigned (e.g. member registered when 0 trainers existed),
     * checks if any trainer is registered now in the database, assigns them, and saves to DB.
     * If a real trainer is already assigned, leaves it untouched.
     */
    public String syncUserTrainer(User user) {
        if (user == null || user.getRole() != Role.MEMBER) return null;
        String existingTrainer = user.getAssignedTrainer();

        // 1. Read latest assigned_trainer from users table
        String sql = "SELECT assigned_trainer FROM users WHERE LOWER(email) = ? OR LOWER(gym_id) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getEmail().trim().toLowerCase());
            ps.setString(2, user.getGymId() != null ? user.getGymId().trim().toLowerCase() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dbTrainer = rs.getString("assigned_trainer");
                    if (dbTrainer != null && !dbTrainer.trim().isEmpty()) {
                        existingTrainer = dbTrainer.trim();
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Note reading user assigned_trainer: " + ex.getMessage());
        }

        // 2. If no trainer is assigned yet, check if any trainer is registered now in the database
        if (existingTrainer == null || existingTrainer.trim().isEmpty()) {
            String newTrainer = getRandomTrainerName();
            if (newTrainer != null && !newTrainer.trim().isEmpty()) {
                existingTrainer = newTrainer;
                // Persist assignment for this member in users and trainees tables
                try (Connection conn = DBConnection.getConnection()) {
                    try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET assigned_trainer = ? WHERE LOWER(email) = ? OR LOWER(gym_id) = ?")) {
                        ps.setString(1, existingTrainer);
                        ps.setString(2, user.getEmail().trim().toLowerCase());
                        ps.setString(3, user.getGymId() != null ? user.getGymId().trim().toLowerCase() : "");
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement("UPDATE trainees SET assigned_trainer = ? WHERE LOWER(email) = ? OR (LOWER(gym_id) = ? AND gym_id != '')")) {
                        ps.setString(1, existingTrainer);
                        ps.setString(2, user.getEmail().trim().toLowerCase());
                        ps.setString(3, user.getGymId() != null ? user.getGymId().trim().toLowerCase() : "");
                        ps.executeUpdate();
                    }
                } catch (Exception ex) {
                    System.err.println("Note persisting assigned trainer: " + ex.getMessage());
                }
            }
        }

        user.setAssignedTrainer(existingTrainer);
        UserSession.setAssignedTrainer(existingTrainer);
        return existingTrainer;
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
