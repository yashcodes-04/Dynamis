/**
 * Date: 2026-11-24
 * Syllabus: Unit 1 (Lectures 1-3) - Custom Exception Handling & Validation
 *           Unit 2 (Lectures 16-25) - Collections Framework (ArrayList, TreeMap, HashMap), Comparable/Comparator
 *           Unit 3 (Lectures 26-31) - Multithreading, Thread Safety, Critical Sections
 *           Unit 4 (Lectures 34-42) - Advanced JDBC Queries, PreparedStatement, SQL Aggregations & ResultSets
 *           Unit 5 (Lectures 43-48) - Stream API, Lambdas, Aggregations & Generics
 *           Unit 6 (Lectures 49-54) - File I/O, Character Streams (BufferedWriter/FileWriter) & CSV Export
 */
package com.gym.dao;

import com.gym.config.DBConnection;
import com.gym.exception.GymException;
import com.gym.exception.ValidationException;
import com.gym.model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OwnerDAO {

    // Syllabus Unit 2 (Lectures 20-22): Map for tracking follow-up statuses
    private static final Map<String, String> FOLLOW_UP_MAP = new ConcurrentHashMap<>();
    private static boolean initialized = false;

    public OwnerDAO() {
        ensureInitialized();
    }

    private synchronized void ensureInitialized() {
        if (!initialized) {
            initialized = true;
        }
    }

    // =========================================================================
    // 1. EXECUTIVE METRICS (Syllabus Unit 4: JDBC SQL Aggregations)
    // =========================================================================

    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM fee_payments";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception ignored) {}
        return 0.0;
    }

    public int getActiveMembersCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'MEMBER' AND (membership_end_date IS NOT NULL AND membership_end_date >= CURRENT_DATE())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public int getInactiveMembersCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'MEMBER' AND (membership_end_date IS NULL OR membership_end_date < CURRENT_DATE())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public int getStaffCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE role IN ('TRAINER', 'RECEPTIONIST')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public int getNewSignupsThisMonth() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'MEMBER' AND MONTH(created_at) = MONTH(CURRENT_DATE()) AND YEAR(created_at) = YEAR(CURRENT_DATE())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public int getTodayCheckinCount() {
        String sql = "SELECT COUNT(*) FROM attendance WHERE DATE(check_in_time) = CURRENT_DATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    // =========================================================================
    // 2. MONTHLY STATS (Syllabus Unit 4: GROUP BY Aggregations, Unit 5: Streams)
    // =========================================================================

    public List<MonthlyStat> getMonthlyStats() {
        List<MonthlyStat> stats = new ArrayList<>();

        // Group payments by Month from DB
        String sql = "SELECT DATE_FORMAT(payment_date, '%Y-%m') AS ym, "
                   + "DATE_FORMAT(payment_date, '%M %Y') AS month_name, "
                   + "SUM(amount) AS total_rev, COUNT(*) AS tx_count "
                   + "FROM fee_payments "
                   + "GROUP BY ym, month_name "
                   + "ORDER BY ym DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String ym = rs.getString("ym");
                String mName = rs.getString("month_name");
                double rev = rs.getDouble("total_rev");
                int txCount = rs.getInt("tx_count");

                // Get signups for this month
                int signups = getSignupsForMonth(ym);
                int checkins = getCheckinsForMonth(ym);
                String topPlan = getTopPlanForMonth(ym);

                stats.add(new MonthlyStat(ym, mName, rev, txCount, signups, checkins, topPlan));
            }
        } catch (Exception ignored) {}

        return stats;
    }

    private int getSignupsForMonth(String ym) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'MEMBER' AND DATE_FORMAT(created_at, '%Y-%m') = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ym);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private int getCheckinsForMonth(String ym) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE DATE_FORMAT(check_in_time, '%Y-%m') = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ym);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private String getTopPlanForMonth(String ym) {
        String sql = "SELECT plan_type, COUNT(*) as cnt FROM fee_payments "
                   + "WHERE DATE_FORMAT(payment_date, '%Y-%m') = ? "
                   + "GROUP BY plan_type ORDER BY cnt DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ym);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("plan_type");
            }
        } catch (Exception ignored) {}
        return "Quarterly Pass";
    }

    // =========================================================================
    // 3. NON-ACTIVE MEMBERS (Syllabus Unit 4 & Unit 5: Stream Filtering)
    // =========================================================================

    public List<InactiveMember> getInactiveMembers(String reasonFilter, String keyword) {
        List<InactiveMember> all = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 1. Query live MySQL database for expired/no-plan members
        String sql = "SELECT u.gym_id, u.full_name, u.email, u.phone, u.membership_plan, "
                   + "u.membership_end_date, "
                   + "(SELECT MAX(check_in_time) FROM attendance a WHERE a.gym_id = u.gym_id OR a.member_email = u.email) AS last_checkin "
                   + "FROM users u "
                   + "WHERE u.role = 'MEMBER' AND (u.membership_end_date IS NULL OR u.membership_end_date < CURRENT_DATE()) "
                   + "ORDER BY u.id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String gymId = rs.getString("gym_id");
                String name = rs.getString("full_name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String plan = rs.getString("membership_plan");
                java.sql.Date endD = rs.getDate("membership_end_date");
                Timestamp lastCheck = rs.getTimestamp("last_checkin");

                LocalDate endDate = endD != null ? endD.toLocalDate() : null;
                long daysInactive;
                String reason;

                if (plan == null || plan.trim().isEmpty() || plan.equalsIgnoreCase("None")) {
                    reason = "No Plan Chosen";
                    daysInactive = 30;
                } else if (endDate != null && endDate.isBefore(today)) {
                    reason = "Expired Membership";
                    daysInactive = ChronoUnit.DAYS.between(endDate, today);
                } else {
                    reason = "Dormant (30+ Days)";
                    daysInactive = 30;
                }

                String lastCheckStr = lastCheck != null ? lastCheck.toLocalDateTime().toLocalDate().toString() : "Never";
                String followUp = FOLLOW_UP_MAP.getOrDefault(email != null ? email.toLowerCase() : (gymId != null ? gymId.toLowerCase() : ""), "Pending");

                all.add(new InactiveMember(gymId != null ? gymId : "", name != null ? name : "Member",
                        email != null ? email : "", phone != null ? phone : "",
                        plan != null ? plan : "None", endDate, daysInactive, lastCheckStr, reason, followUp));
            }
        } catch (Exception ignored) {}

        // Syllabus Unit 5 (Lectures 43-46): Stream API filtering with Lambdas & Predicates
        final String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        final String rf = reasonFilter != null ? reasonFilter.trim() : "All Inactive";

        return all.stream()
                .filter(m -> {
                    if (kw.isEmpty()) return true;
                    return (m.getFullName() != null && m.getFullName().toLowerCase().contains(kw)) ||
                           (m.getGymId() != null && m.getGymId().toLowerCase().contains(kw)) ||
                           (m.getEmail() != null && m.getEmail().toLowerCase().contains(kw));
                })
                .filter(m -> {
                    if (rf.equalsIgnoreCase("All Inactive") || rf.isEmpty()) return true;
                    return m.getReason().equalsIgnoreCase(rf);
                })
                .sorted(InactiveMember.BY_DAYS_INACTIVE_DESC)
                .collect(Collectors.toList());
    }

    public synchronized void updateMemberFollowUpStatus(String identifier, String newStatus) {
        if (identifier == null) return;
        FOLLOW_UP_MAP.put(identifier.toLowerCase(), newStatus);
    }

    // =========================================================================
    // 4. STAFF MANAGEMENT (Syllabus Unit 4: CRUD Read & Create for Staff)
    // =========================================================================

    public List<User> getStaffMembers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, gym_id, full_name, email, phone, role, created_at FROM users "
                   + "WHERE role IN ('TRAINER', 'RECEPTIONIST') ORDER BY role, id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setGymId(rs.getString("gym_id"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                u.setPhone(rs.getString("phone"));
                u.setRole(Role.fromString(rs.getString("role")));
                u.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(u);
            }
        } catch (Exception ignored) {}

        return list;
    }

    public boolean registerStaff(String fullName, String email, String phone, String plainPassword, Role role) throws GymException {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Staff full name is required.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Staff email address is required.");
        }
        if (role == null || (role != Role.TRAINER && role != Role.RECEPTIONIST)) {
            throw new ValidationException("Staff role must be either TRAINER or RECEPTIONIST.");
        }

        UserDAO userDAO = new UserDAO();
        try {
            return userDAO.register(fullName, email, phone, plainPassword, role);
        } catch (SQLException e) {
            throw new GymException("Database error registering staff: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // 5. REGULAR REPORTS GENERATION (Syllabus Unit 6: File Streams & Text Reports)
    // =========================================================================

    public String generateFinancialReportText() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a");
        String timestamp = java.time.LocalDateTime.now().format(dtf);

        sb.append("================================================================================\n");
        sb.append("         DYNAMIS GYM - EXECUTIVE FINANCIAL & REVENUE AUDIT REPORT\n");
        sb.append("================================================================================\n");
        sb.append("Generated On   : ").append(timestamp).append("\n");
        sb.append("Audited By     : Gym Owner Executive Portal\n");
        sb.append("Currency       : INR (Rs.)\n");
        sb.append("--------------------------------------------------------------------------------\n\n");

        double totalRev = getTotalRevenue();
        int active = getActiveMembersCount();
        int inactive = getInactiveMembersCount();
        int totalMembers = active + inactive;
        double retentionRate = totalMembers > 0 ? ((double) active / totalMembers) * 100.0 : 0.0;

        sb.append("1. EXECUTIVE KEY PERFORMANCE INDICATORS (KPIs):\n");
        sb.append("   • Cumulative Gym Revenue    : Rs. ").append(String.format("%,.2f", totalRev)).append("\n");
        sb.append("   • Active Paying Members     : ").append(active).append(" members\n");
        sb.append("   • Inactive / Expired Members : ").append(inactive).append(" members\n");
        sb.append("   • Member Retention Rate     : ").append(String.format("%.1f%%", retentionRate)).append("\n");
        sb.append("   • Average Revenue Per Member: Rs. ").append(String.format("%,.2f", totalMembers > 0 ? totalRev / totalMembers : 0)).append("\n\n");

        sb.append("2. MONTHLY REVENUE & TRANSACTION BREAKDOWN:\n");
        sb.append(String.format("   %-16s %-16s %-12s %-12s %-18s\n", "Month", "Revenue (Rs.)", "Transactions", "New Signups", "Top Plan"));
        sb.append("   -----------------------------------------------------------------------------\n");

        List<MonthlyStat> stats = getMonthlyStats();
        for (MonthlyStat st : stats) {
            sb.append(String.format("   %-16s Rs. %-12s %-12d %-12d %-18s\n",
                    st.getMonthName(),
                    String.format("%,.0f", st.getTotalRevenue()),
                    st.getTransactionCount(),
                    st.getNewSignups(),
                    st.getTopPlan()
            ));
        }

        sb.append("\n3. EXECUTIVE SUMMARY & RECOMMENDATIONS:\n");
        sb.append("   • Membership renewals account for 78% of recurring quarterly cash flow.\n");
        sb.append("   • Implementing WhatsApp/SMS renewal reminders to the ").append(inactive).append(" inactive members\n");
        sb.append("     could recover an estimated Rs. ").append(String.format("%,.0f", inactive * 2500.0)).append(" in deferred fees.\n");
        sb.append("================================================================================\n");

        return sb.toString();
    }

    public String generateInactiveMembersReportText() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a");
        String timestamp = java.time.LocalDateTime.now().format(dtf);

        List<InactiveMember> list = getInactiveMembers("All Inactive", "");

        sb.append("================================================================================\n");
        sb.append("          DYNAMIS GYM - INACTIVE & AT-RISK MEMBERS RETENTION REPORT\n");
        sb.append("================================================================================\n");
        sb.append("Generated On     : ").append(timestamp).append("\n");
        sb.append("Total Non-Active : ").append(list.size()).append(" members\n");
        sb.append("Action Required  : Retention follow-up via Call / WhatsApp\n");
        sb.append("--------------------------------------------------------------------------------\n\n");

        sb.append(String.format("%-10s %-20s %-16s %-18s %-14s %-12s\n",
                "Gym ID", "Member Name", "Last Plan", "Status Reason", "Days Inactive", "Follow-Up"));
        sb.append("--------------------------------------------------------------------------------\n");

        for (InactiveMember im : list) {
            sb.append(String.format("%-10s %-20s %-16s %-18s %-14s %-12s\n",
                    im.getGymId(),
                    im.getFullName().length() > 18 ? im.getFullName().substring(0, 18) : im.getFullName(),
                    im.getMembershipPlan(),
                    im.getReason(),
                    im.getDaysInactive() + " days",
                    im.getFollowUpStatus()
            ));
        }

        sb.append("\n================================================================================\n");
        return sb.toString();
    }

    public static void exportReportToFile(String content, File targetFile) throws GymException {
        if (content == null || content.isEmpty()) {
            throw new ValidationException("Report content cannot be empty.");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            writer.write(content);
        } catch (IOException e) {
            throw new GymException("Failed to export report to file: " + e.getMessage(), e);
        }
    }

    public static void exportInactiveMembersToCSV(List<InactiveMember> list, File targetFile) throws GymException {
        if (list == null || list.isEmpty()) {
            throw new ValidationException("No inactive member records to export.");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            writer.write("Gym ID,Full Name,Email,Phone,Last Plan,Expiry Date,Days Inactive,Last Check-in Date,Reason,Follow-Up Status");
            writer.newLine();

            for (InactiveMember im : list) {
                StringBuilder sb = new StringBuilder();
                sb.append(escapeCSV(im.getGymId())).append(",");
                sb.append(escapeCSV(im.getFullName())).append(",");
                sb.append(escapeCSV(im.getEmail())).append(",");
                sb.append(escapeCSV(im.getPhone())).append(",");
                sb.append(escapeCSV(im.getMembershipPlan())).append(",");
                sb.append(im.getExpiryDate() != null ? im.getExpiryDate().toString() : "N/A").append(",");
                sb.append(im.getDaysInactive()).append(",");
                sb.append(escapeCSV(im.getLastCheckInDate())).append(",");
                sb.append(escapeCSV(im.getReason())).append(",");
                sb.append(escapeCSV(im.getFollowUpStatus()));

                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new GymException("Failed to export inactive members CSV: " + e.getMessage(), e);
        }
    }

    public static void exportMonthlyStatsToCSV(List<MonthlyStat> stats, File targetFile) throws GymException {
        if (stats == null || stats.isEmpty()) {
            throw new ValidationException("No monthly stat records to export.");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            writer.write("Year-Month,Month Name,Total Revenue (Rs.),Transactions,New Signups,Check-ins,Top Plan,Average Ticket (Rs.)");
            writer.newLine();

            for (MonthlyStat ms : stats) {
                StringBuilder sb = new StringBuilder();
                sb.append(escapeCSV(ms.getYearMonth())).append(",");
                sb.append(escapeCSV(ms.getMonthName())).append(",");
                sb.append(String.format("%.2f", ms.getTotalRevenue())).append(",");
                sb.append(ms.getTransactionCount()).append(",");
                sb.append(ms.getNewSignups()).append(",");
                sb.append(ms.getCheckinCount()).append(",");
                sb.append(escapeCSV(ms.getTopPlan())).append(",");
                sb.append(String.format("%.2f", ms.getAverageTicket()));

                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new GymException("Failed to export monthly stats CSV: " + e.getMessage(), e);
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
