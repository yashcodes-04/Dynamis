/**
 * Date: 2026-11-17 - Yash
 */
package com.gym.dao;

import com.gym.config.DBConnection;
import com.gym.model.FeePayment;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReceptionistDAO {

    public String findMemberNameByEmail(String email) throws SQLException {
        String query = "SELECT full_name FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("full_name");
                }
            }
        }
        return null;
    }

    public boolean recordFeePayment(String email, String memberName, String planType,
                                   double amount, String paymentMode, int durationMonths) throws SQLException {
        String insertSQL = "INSERT INTO fee_payments "
                + "(member_email, member_name, plan_type, amount, payment_mode, renewal_due_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        LocalDate renewalDate = LocalDate.now().plusMonths(durationMonths);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSQL)) {
            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, memberName.trim());
            ps.setString(3, planType);
            ps.setDouble(4, amount);
            ps.setString(5, paymentMode);
            ps.setDate(6, Date.valueOf(renewalDate));

            return ps.executeUpdate() > 0;
        }
    }

    public List<FeePayment> getRenewalHistory(String filter) throws SQLException {
        List<FeePayment> list = new ArrayList<>();
        boolean hasFilter = (filter != null && !filter.trim().isEmpty());

        String sql = "SELECT id, member_email, member_name, plan_type, amount, payment_mode, payment_date, renewal_due_date "
                + "FROM fee_payments ";

        if (hasFilter) {
            sql += "WHERE LOWER(member_email) LIKE ? OR LOWER(member_name) LIKE ? ";
        }
        sql += "ORDER BY payment_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasFilter) {
                String keyword = "%" + filter.trim().toLowerCase() + "%";
                ps.setString(1, keyword);
                ps.setString(2, keyword);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FeePayment fp = new FeePayment(
                            rs.getInt("id"),
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
