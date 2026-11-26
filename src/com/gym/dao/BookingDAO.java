/**
 * Date: 2026-11-21-Khushi
 */
package com.gym.dao;

import com.gym.config.DBConnection;
import com.gym.model.BookedSession;
import com.gym.model.FitnessClass;
import com.gym.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BookingDAO implements GenericDAO<BookedSession, Integer> {

    public synchronized boolean bookSessions(User user, List<FitnessClass> classes) {
        if (user == null || classes == null || classes.isEmpty()) {
            return false;
        }

        String gymId = user.getGymId() != null ? user.getGymId() : "";
        String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
        String name = user.getFullName() != null ? user.getFullName() : "Member";

        // Validate that none of the classes currently have an active 1-month booking
        for (FitnessClass fc : classes) {
            BookedSession active = getActiveBookingForClass(gymId, email, fc.getClassName());
            if (active != null) {
                System.err.println("Active booking already exists for class: " + fc.getClassName());
                return false;
            }
        }

        String sql = "INSERT INTO booked_sessions "
                + "(gym_id, member_email, member_name, class_name, description, level, duration, price, schedule_slot, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (FitnessClass fc : classes) {
                ps.setString(1, gymId);
                ps.setString(2, email);
                ps.setString(3, name);
                ps.setString(4, fc.getClassName());
                ps.setString(5, fc.getDescription());
                ps.setString(6, fc.getLevel());
                ps.setString(7, fc.getDuration());
                ps.setDouble(8, fc.getPrice());
                ps.setString(9, fc.getScheduleSlot());
                ps.setString(10, "CONFIRMED");
                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        } catch (Exception ex) {
            System.err.println("Error saving booked sessions to DB: " + ex.getMessage());
            return false;
        }
    }

    public List<BookedSession> getBookingsForMember(String gymId, String email) {
        List<BookedSession> list = new ArrayList<>();
        String normEmail = email != null ? email.trim().toLowerCase() : "";
        String normGymId = gymId != null ? gymId.trim().toLowerCase() : "";

        String sql = "SELECT * FROM booked_sessions WHERE (LOWER(gym_id) = ? AND gym_id != '') OR LOWER(member_email) = ? ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normGymId);
            ps.setString(2, normEmail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBookedSession(rs));
                }
            }
        } catch (Exception ex) {
            System.err.println("Error fetching member bookings from DB: " + ex.getMessage());
        }

        return list;
    }

    public boolean cancelBooking(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM booked_sessions WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            System.err.println("Error cancelling booking in DB: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Checks if the user already has an active (non-expired, CONFIRMED) booking for a specific class within 30 days (1 month).
     */
    public BookedSession getActiveBookingForClass(String gymId, String email, String className) {
        if (className == null || className.trim().isEmpty()) return null;
        List<BookedSession> bookings = getBookingsForMember(gymId, email);
        for (BookedSession b : bookings) {
            if (b.getClassName() != null && b.getClassName().equalsIgnoreCase(className.trim()) && b.isActive()) {
                return b;
            }
        }
        return null;
    }

    @Override
    public List<BookedSession> getAll() throws SQLException {
        List<BookedSession> list = new ArrayList<>();
        String sql = "SELECT * FROM booked_sessions ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToBookedSession(rs));
            }
        }
        return list;
    }

    @Override
    public BookedSession findById(Integer id) throws SQLException {
        if (id == null) return null;
        String sql = "SELECT * FROM booked_sessions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBookedSession(rs);
                }
            }
        }
        return null;
    }

    private BookedSession mapResultSetToBookedSession(ResultSet rs) throws SQLException {
        BookedSession b = new BookedSession();
        b.setId(rs.getInt("id"));
        b.setGymId(rs.getString("gym_id"));
        b.setMemberEmail(rs.getString("member_email"));
        b.setMemberName(rs.getString("member_name"));
        b.setClassName(rs.getString("class_name"));
        b.setDescription(rs.getString("description"));
        b.setLevel(rs.getString("level"));
        b.setDuration(rs.getString("duration"));
        b.setPrice(rs.getDouble("price"));
        b.setScheduleSlot(rs.getString("schedule_slot"));
        b.setStatus(rs.getString("status"));
        b.setBookingDate(rs.getTimestamp("booking_date"));
        return b;
    }
}
