/**
 * Date: 2026-11-18 - Yash
 * Syllabus: Unit 1 (Lecture 1, 3) - Abstraction, Custom Exceptions
 *           Unit 2 (Lecture 5, 6, 7, 8) - Collections, HashMap, Comparator
 *           Unit 5 (Lecture 21, 22, 23, 24) - Lambda, Streams, Generics
 */
package com.gym.dao;

import com.gym.config.DBConnection;
import com.gym.exception.MemberNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.AttendanceRecord;
import com.gym.model.FeePayment;
import com.gym.model.Role;
import com.gym.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ReceptionistDAO implements GenericDAO<FeePayment, Integer> {

    // Syllabus Unit 2 (Lecture 6-7): HashMap for caching verified members in-memory
    private final Map<String, User> verifiedMemberCache = new HashMap<>();

    /**
     * Looks up a registered member by Gym ID or Email Address.
     * Demonstrates HashMap caching (Unit 2, Lecture 6) and exception throwing (Unit 1, Lecture 3).
     */
    public synchronized User findMemberByGymIdOrEmail(String identifier) throws SQLException {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        String idStr = identifier.trim().toLowerCase();

        // 1. Check in-memory HashMap cache first (Lecture 6)
        if (verifiedMemberCache.containsKey(idStr)) {
            return verifiedMemberCache.get(idStr);
        }

        // 2. Query MySQL database using PreparedStatement (Lecture 19)
        String query = "SELECT id, gym_id, full_name, email, phone, role FROM users "
                + "WHERE LOWER(gym_id) = ? OR LOWER(email) = ?";

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

                    // Store in HashMap cache for O(1) future lookups (Lecture 6)
                    verifiedMemberCache.put(idStr, user);
                    if (user.getGymId() != null) {
                        verifiedMemberCache.put(user.getGymId().toLowerCase(), user);
                    }
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Records or updates a fee payment for a member.
     * When the user changes or renews their membership plan, this updates their existing record
     * in the database in place instead of creating duplicate rows.
     */
    public boolean recordFeePayment(String gymId, String email, String memberName, String planType,
                                    double amount, String paymentMode, int durationMonths) throws SQLException {
        int durationDays;
        switch (durationMonths) {
            case 1: durationDays = 30; break;
            case 3: durationDays = 90; break;
            case 6: durationDays = 180; break;
            case 12: durationDays = 365; break;
            default: durationDays = durationMonths * 30; break;
        }
        LocalDate renewalDate = LocalDate.now().plusDays(durationDays);
        return upsertFeePayment(gymId, email, memberName, planType, amount, paymentMode, renewalDate, durationDays);
    }

    /**
     * Overload accepting exact renewal due date and duration in days.
     * Updates existing record in-place instead of creating a duplicate row.
     */
    public boolean recordFeePayment(String gymId, String email, String memberName, String planType,
                                    double amount, String paymentMode, LocalDate renewalDate, int durationDays) throws SQLException {
        return upsertFeePayment(gymId, email, memberName, planType, amount, paymentMode, renewalDate, durationDays);
    }

    /**
     * Updates existing fee payment and membership record for the member if one already exists,
     * or inserts a new one if it is the member's very first record.
     * Ensures only one active record exists per member in the database without creating extra rows.
     */
    public boolean upsertFeePayment(String gymId, String email, String memberName, String planType,
                                    double amount, String paymentMode, LocalDate renewalDate, int durationDays) throws SQLException {
        String cleanEmail = email != null ? email.trim().toLowerCase() : "";
        String cleanGymId = gymId != null ? gymId.trim().toLowerCase() : "";

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Locate all existing fee_payments record IDs for this member
            String selectSQL = "SELECT id FROM fee_payments WHERE (LOWER(gym_id) = ? AND gym_id != '') OR LOWER(member_email) = ? ORDER BY id ASC";
            List<Integer> existingIds = new ArrayList<>();
            try (PreparedStatement psSelect = conn.prepareStatement(selectSQL)) {
                psSelect.setString(1, cleanGymId);
                psSelect.setString(2, cleanEmail);
                try (ResultSet rs = psSelect.executeQuery()) {
                    while (rs.next()) {
                        existingIds.add(rs.getInt("id"));
                    }
                }
            }

            boolean paymentSuccess;
            if (!existingIds.isEmpty()) {
                // Update existing record in place - DO NOT create a new row
                int targetId = existingIds.get(0);
                String updateSQL = "UPDATE fee_payments SET gym_id = ?, member_email = ?, member_name = ?, "
                        + "plan_type = ?, amount = ?, payment_mode = ?, payment_date = CURRENT_TIMESTAMP, "
                        + "renewal_due_date = ? WHERE id = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSQL)) {
                    psUpdate.setString(1, gymId != null ? gymId.trim() : "");
                    psUpdate.setString(2, cleanEmail);
                    psUpdate.setString(3, memberName != null ? memberName.trim() : "");
                    psUpdate.setString(4, planType);
                    psUpdate.setDouble(5, amount);
                    psUpdate.setString(6, paymentMode);
                    psUpdate.setDate(7, java.sql.Date.valueOf(renewalDate));
                    psUpdate.setInt(8, targetId);
                    paymentSuccess = psUpdate.executeUpdate() > 0;
                }

                // If duplicate rows accumulated previously, prune them so only 1 record remains
                if (existingIds.size() > 1) {
                    for (int i = 1; i < existingIds.size(); i++) {
                        try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM fee_payments WHERE id = ?")) {
                            psDel.setInt(1, existingIds.get(i));
                            psDel.executeUpdate();
                        } catch (Exception ignored) {}
                    }
                }
            } else {
                // First-time record: insert initial row
                String insertSQL = "INSERT INTO fee_payments "
                        + "(gym_id, member_email, member_name, plan_type, amount, payment_mode, renewal_due_date) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement psInsert = conn.prepareStatement(insertSQL)) {
                    psInsert.setString(1, gymId != null ? gymId.trim() : "");
                    psInsert.setString(2, cleanEmail);
                    psInsert.setString(3, memberName != null ? memberName.trim() : "");
                    psInsert.setString(4, planType);
                    psInsert.setDouble(5, amount);
                    psInsert.setString(6, paymentMode);
                    psInsert.setDate(7, java.sql.Date.valueOf(renewalDate));
                    paymentSuccess = psInsert.executeUpdate() > 0;
                }
            }

            // 2. Also keep users table updated in place for this member
            try {
                LocalDate startDate = LocalDate.now();
                String updateUserSQL = "UPDATE users SET membership_plan = ?, membership_price = ?, "
                        + "membership_duration_days = ?, membership_start_date = ?, membership_end_date = ? "
                        + "WHERE (LOWER(gym_id) = ? AND gym_id != '') OR LOWER(email) = ?";
                try (PreparedStatement psUser = conn.prepareStatement(updateUserSQL)) {
                    psUser.setString(1, planType);
                    psUser.setDouble(2, amount);
                    psUser.setInt(3, durationDays);
                    psUser.setDate(4, java.sql.Date.valueOf(startDate));
                    psUser.setDate(5, java.sql.Date.valueOf(renewalDate));
                    psUser.setString(6, cleanGymId);
                    psUser.setString(7, cleanEmail);
                    psUser.executeUpdate();
                }
            } catch (Exception ex) {
                System.err.println("Note syncing users membership from payment: " + ex.getMessage());
            }

            return paymentSuccess;
        }
    }

    /**
     * Retrieves the latest fee payment and membership renewal date for a member.
     */
    public FeePayment getLatestFeePayment(String gymId, String email) {
        String sql = "SELECT id, gym_id, member_email, member_name, plan_type, amount, payment_mode, payment_date, renewal_due_date "
                + "FROM fee_payments WHERE (LOWER(gym_id) = ? AND gym_id != '') OR LOWER(member_email) = ? "
                + "ORDER BY payment_date DESC, id DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gymId != null ? gymId.trim().toLowerCase() : "");
            ps.setString(2, email != null ? email.trim().toLowerCase() : "");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FeePayment fp = new FeePayment();
                    fp.setId(rs.getInt("id"));
                    fp.setGymId(rs.getString("gym_id"));
                    fp.setMemberEmail(rs.getString("member_email"));
                    fp.setMemberName(rs.getString("member_name"));
                    fp.setPlanType(rs.getString("plan_type"));
                    fp.setAmount(rs.getDouble("amount"));
                    fp.setPaymentMode(rs.getString("payment_mode"));
                    fp.setPaymentDate(rs.getTimestamp("payment_date"));
                    fp.setRenewalDueDate(rs.getDate("renewal_due_date"));
                    return fp;
                }
            }
        } catch (Exception ex) {
            System.err.println("Note on loading member fee payment: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Retrieves renewal and payment history from MySQL and applies Java Stream API (Lecture 21-23).
     */
    public List<FeePayment> getRenewalHistory(String filter) throws SQLException {
        List<FeePayment> list = getAll(); // Implements GenericDAO method (Lecture 24)

        if (filter == null || filter.trim().isEmpty()) {
            return list;
        }

        String keyword = filter.trim().toLowerCase();

        // Syllabus Unit 5 (Lecture 21, 22, 23): Stream API, Lambdas, filter, and collect
        return list.stream()
                .filter(fp -> (fp.getGymId() != null && fp.getGymId().toLowerCase().contains(keyword))
                           || (fp.getMemberName() != null && fp.getMemberName().toLowerCase().contains(keyword))
                           || (fp.getMemberEmail() != null && fp.getMemberEmail().toLowerCase().contains(keyword)))
                .sorted(FeePayment.BY_DATE_DESC) // Uses Comparator (Lecture 8)
                .collect(Collectors.toList());
    }

    /**
     * Implements GenericDAO<FeePayment, Integer>.getAll() (Lecture 24 - Generics)
     */
    @Override
    public List<FeePayment> getAll() throws SQLException {
        List<FeePayment> list = new ArrayList<>();
        String sql = "SELECT id, gym_id, member_email, member_name, plan_type, amount, payment_mode, payment_date, renewal_due_date "
                + "FROM fee_payments ORDER BY payment_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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
        return list;
    }

    @Override
    public FeePayment findById(Integer id) throws SQLException {
        String sql = "SELECT id, gym_id, member_email, member_name, plan_type, amount, payment_mode, payment_date, renewal_due_date "
                + "FROM fee_payments WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new FeePayment(
                            rs.getInt("id"),
                            rs.getString("gym_id"),
                            rs.getString("member_email"),
                            rs.getString("member_name"),
                            rs.getString("plan_type"),
                            rs.getDouble("amount"),
                            rs.getString("payment_mode"),
                            rs.getTimestamp("payment_date"),
                            rs.getDate("renewal_due_date")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Marks member present in the attendance table using their unique Gym ID.
     * Demonstrates Custom Exception throwing (Unit 1, Lecture 3).
     * Rejects check-in if the member does not have an active (unexpired) membership.
     */
    public AttendanceRecord recordAttendanceByGymId(String gymIdOrEmail) throws SQLException, MemberNotFoundException, ValidationException {
        User member = findMemberByGymIdOrEmail(gymIdOrEmail);
        if (member == null) {
            // Throws custom checked exception (Lecture 3)
            throw new MemberNotFoundException("You aren't a member. No member registered matching '" + gymIdOrEmail + "'.");
        }
        if (member.getRole() != Role.MEMBER) {
            throw new ValidationException("You aren't a member (" + member.getFullName() + " is registered as " + member.getRole() + ").");
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Verify live active membership status directly from DB
            LocalDate activeEndDate = getLiveActiveMembershipEndDate(conn, member);
            if (activeEndDate == null) {
                throw new ValidationException("Attendance blocked: " + member.getFullName() + " (" + member.getGymId() + ") has no active membership.");
            }
            if (activeEndDate.isBefore(LocalDate.now())) {
                throw new ValidationException("Attendance blocked: " + member.getFullName() + "'s membership expired on " + activeEndDate + ".");
            }

            String insertSQL = "INSERT INTO attendance (gym_id, member_name, member_email) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
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
        }
        return null;
    }

    /**
     * Checks live membership_end_date from users (and falls back to fee_payments.renewal_due_date).
     * Returns null if no membership plan has been purchased.
     */
    private LocalDate getLiveActiveMembershipEndDate(Connection conn, User member) throws SQLException {
        String userSql = "SELECT membership_plan, membership_end_date FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(userSql)) {
            ps.setInt(1, member.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String plan = rs.getString("membership_plan");
                    java.sql.Date endDate = rs.getDate("membership_end_date");
                    if (plan != null && !plan.trim().isEmpty() && !plan.equalsIgnoreCase("None") && endDate != null) {
                        return endDate.toLocalDate();
                    }
                }
            }
        }

        String feeSql = "SELECT renewal_due_date FROM fee_payments "
                + "WHERE (LOWER(gym_id) = ? AND gym_id != '') OR LOWER(member_email) = ? "
                + "ORDER BY payment_date DESC, id DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(feeSql)) {
            ps.setString(1, member.getGymId() != null ? member.getGymId().trim().toLowerCase() : "");
            ps.setString(2, member.getEmail() != null ? member.getEmail().trim().toLowerCase() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.sql.Date dueDate = rs.getDate("renewal_due_date");
                    if (dueDate != null) {
                        return dueDate.toLocalDate();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Syllabus Unit 4 (Lecture 19, 20): CRUD - Delete operation on attendance records.
     * Allows receptionist to remove mistaken or canceled check-in entries.
     */
    public boolean deleteAttendanceRecord(int attendanceId) throws SQLException {
        String deleteSQL = "DELETE FROM attendance WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
            ps.setInt(1, attendanceId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves attendance history and applies Stream filtering (Lecture 21-23).
     * Filters strictly to today's check-ins when todayOnly is true.
     */
    public List<AttendanceRecord> getAttendanceHistory(String filter, boolean todayOnly) throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT id, gym_id, member_name, member_email, check_in_time FROM attendance "
                + (todayOnly ? "WHERE DATE(check_in_time) = CURRENT_DATE() " : "")
                + "ORDER BY check_in_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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

        if (filter == null || filter.trim().isEmpty()) {
            return list;
        }

        String keyword = filter.trim().toLowerCase();
        // Stream API filtering (Lecture 23)
        return list.stream()
                .filter(ar -> (ar.getGymId() != null && ar.getGymId().toLowerCase().contains(keyword))
                           || (ar.getMemberName() != null && ar.getMemberName().toLowerCase().contains(keyword))
                           || (ar.getMemberEmail() != null && ar.getMemberEmail().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());
    }

    public List<AttendanceRecord> getAttendanceHistory(String filter) throws SQLException {
        return getAttendanceHistory(filter, true); // Default: strictly today's check-ins
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

    /**
     * Retrieves total check-in count for a specific member by Gym ID or email.
     */
    public int getMemberAttendanceCount(String gymId, String email) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE (LOWER(gym_id) = ? AND gym_id != '') OR LOWER(member_email) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gymId != null ? gymId.trim().toLowerCase() : "");
            ps.setString(2, email != null ? email.trim().toLowerCase() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception ex) {
            System.err.println("Note counting member attendance: " + ex.getMessage());
        }
        return 0;
    }

    /**
     * Demonstrates Stream API mapToDouble() and method reference (Lecture 21-23)
     */
    public double getTotalRevenue() throws SQLException {
        List<FeePayment> all = getAll();
        // Stream pipeline with method reference FeePayment::getAmount (Lecture 21 & 23)
        return all.stream()
                .mapToDouble(FeePayment::getAmount)
                .sum();
    }

    public int getTotalRenewalsCount() throws SQLException {
        return getAll().size();
    }

    /**
     * Syllabus Unit 5 (Lecture 23): Stream Operations: map(), distinct(), sorted(), collect()
     * Syllabus Unit 2 (Lecture 5): TreeSet for naturally ordered unique values
     */
    public Set<String> getDistinctActivePlans() throws SQLException {
        List<FeePayment> all = getAll();
        return all.stream()
                .map(FeePayment::getPlanType)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Syllabus Unit 5 (Lecture 23): Stream Operations: limit(), skip(), sorted()
     */
    public List<FeePayment> getTopRecentPayments(int limitCount, int skipCount) throws SQLException {
        List<FeePayment> all = getAll();
        return all.stream()
                .sorted(FeePayment.BY_DATE_DESC)
                .skip(skipCount)
                .limit(limitCount)
                .collect(Collectors.toList());
    }

    /**
     * Syllabus Unit 5 (Lecture 24): Introductory Generics with upper bounded wildcard <? extends FeePayment>
     */
    public double calculateTotalAmount(List<? extends FeePayment> paymentList) {
        if (paymentList == null) return 0.0;
        return paymentList.stream()
                .mapToDouble(FeePayment::getAmount)
                .sum();
    }
}
