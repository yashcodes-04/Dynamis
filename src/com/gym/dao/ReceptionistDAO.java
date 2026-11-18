/**
 * Date: 2026-11-18 - Yash
 */
package com.gym.dao;

import com.gym.config.DBConnection;
import com.gym.model.AttendanceRecord;
import com.gym.model.FeePayment;
import com.gym.model.Role;
import com.gym.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReceptionistDAO {

    /**
     * Looks up a registered member by Gym ID / Roll No (e.g. 20261001) or Email Address
     */
    public User findMemberByGymIdOrEmail(String identifier) throws SQLException {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        String idStr = identifier.trim();
        String query = "SELECT id, gym_id, full_name, email, phone, role FROM users "
                + "WHERE LOWER(gym_id) = LOWER(?) OR LOWER(email) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idStr);
            ps.setString(2, idStr);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    String fallbackId = LocalDate.now().getYear() + "" + (1000 + rs.getInt("id"));
                    user.setGymId(rs.getString("gym_id") != null ? rs.getString("gym_id") : fallbackId);
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                    user.setRole(Role.fromString(rs.getString("role")));
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Records a fee payment tagged with the member's unique Gym ID
     */
    public boolean recordFeePayment(String gymId, String email, String memberName, String planType,
                                    double amount, String paymentMode, int durationMonths) throws SQLException {
        String insertSQL = "INSERT INTO fee_payments "
                + "(gym_id, member_email, member_name, plan_type, amount, payment_mode, renewal_due_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        LocalDate renewalDate = LocalDate.now().plusMonths(durationMonths);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSQL)) {
            ps.setString(1, gymId != null ? gymId.trim().toUpperCase() : "");
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, memberName.trim());
            ps.setString(4, planType);
            ps.setDouble(5, amount);
            ps.setString(6, paymentMode);
            ps.setDate(7, Date.valueOf(renewalDate));

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves renewal and payment history with Gym ID filter support
     */
    public List<FeePayment> getRenewalHistory(String filter) throws SQLException {
        List<FeePayment> list = new ArrayList<>();
        boolean hasFilter = (filter != null && !filter.trim().isEmpty());

        String sql = "SELECT id, gym_id, member_email, member_name, plan_type, amount, payment_mode, payment_date, renewal_due_date "
                + "FROM fee_payments ";

        if (hasFilter) {
            sql += "WHERE LOWER(gym_id) LIKE ? OR LOWER(member_email) LIKE ? OR LOWER(member_name) LIKE ? ";
        }
        sql += "ORDER BY payment_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasFilter) {
                String keyword = "%" + filter.trim().toLowerCase() + "%";
                ps.setString(1, keyword);
                ps.setString(2, keyword);
                ps.setString(3, keyword);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FeePayment fp = new FeePayment(
                            rs.getInt("id"),
                            rs.getString("gym_id") != null ? rs.getString("gym_id") : "N/A",
                            rs.getString("member_email"),
                            rs.getString("member_name"),
                            rs.getString("plan_type"),
                            rs.getDouble("amount"),
                            rs.getString("payment_mode"),
                            rs.getTimestamp("payment_date"),
                            rs.getDate("renewal_due_date")
                    );
                    list.add(fp);
                }
            }
        }
        return list;
    }

    /**
     * Marks member present in the attendance table using their unique Gym ID
     */
    public AttendanceRecord recordAttendanceByGymId(String gymIdOrEmail) throws SQLException {
        User member = findMemberByGymIdOrEmail(gymIdOrEmail);
        if (member == null) {
            throw new SQLException("Invalid Gym ID or Email: No member found matching '" + gymIdOrEmail + "'.");
        }

        String insertSQL = "INSERT INTO attendance (gym_id, member_name, member_email) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, member.getGymId());
            ps.setString(2, member.getFullName());
            ps.setString(3, member.getEmail());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    int newId = keys.next() ? keys.getInt(1) : 0;
                    return new AttendanceRecord(newId, member.getGymId(), member.getFullName(), member.getEmail(), new Timestamp(System.currentTimeMillis()));
                }
            }
        }
        return null;
    }

    /**
     * Retrieves today's attendance history
     */
    public List<AttendanceRecord> getAttendanceHistory(String filter) throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        boolean hasFilter = (filter != null && !filter.trim().isEmpty());

        String sql = "SELECT id, gym_id, member_name, member_email, check_in_time FROM attendance ";
        if (hasFilter) {
            sql += "WHERE LOWER(gym_id) LIKE ? OR LOWER(member_name) LIKE ? OR LOWER(member_email) LIKE ? ";
        }
        sql += "ORDER BY check_in_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasFilter) {
                String keyword = "%" + filter.trim().toLowerCase() + "%";
                ps.setString(1, keyword);
                ps.setString(2, keyword);
                ps.setString(3, keyword);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceRecord ar = new AttendanceRecord(
                            rs.getInt("id"),
                            rs.getString("gym_id"),
                            rs.getString("member_name"),
                            rs.getString("member_email"),
                            rs.getTimestamp("check_in_time")
                    );
                    list.add(ar);
                }
            }
        }
        return list;
    }

    public int getTodayAttendanceCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE DATE(check_in_time) = CURRENT_DATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM fee_payments";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public int getTotalRenewalsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM fee_payments";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
