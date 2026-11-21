/**
 * Date: 2026-11-21
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

    // In-memory fallback and cache for instant access and offline tolerance
    private static final List<BookedSession> IN_MEMORY_BOOKINGS = new CopyOnWriteArrayList<>();
    private static int idSequence = 1000;

    public synchronized boolean bookSessions(User user, List<FitnessClass> classes) {
        if (user == null || classes == null || classes.isEmpty()) {
            return false;
        }

        String gymId = user.getGymId() != null ? user.getGymId() : "";
        String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
        String name = user.getFullName() != null ? user.getFullName() : "Member";

        // 1. Attempt Database insert
        String sql = "INSERT INTO booked_sessions "
                + "(gym_id, member_email, member_name, class_name, description, level, duration, price, schedule_slot, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        } catch (Exception ex) {
            System.err.println("Note on DB booking persistence: " + ex.getMessage() + " (using memory store)");
        }

        // 2. Add to in-memory store
        for (FitnessClass fc : classes) {
            BookedSession session = new BookedSession();
            session.setId(++idSequence);
            session.setGymId(gymId);
            session.setMemberEmail(email);
            session.setMemberName(name);
            session.setClassName(fc.getClassName());
            session.setDescription(fc.getDescription());
            session.setLevel(fc.getLevel());
            session.setDuration(fc.getDuration());
            session.setPrice(fc.getPrice());
            session.setScheduleSlot(fc.getScheduleSlot());
            session.setStatus("CONFIRMED");
            session.setBookingDate(new Timestamp(System.currentTimeMillis()));

            IN_MEMORY_BOOKINGS.add(session);
        }

        return true;
    }

    public List<BookedSession> getBookingsForMember(String gymId, String email) {
        List<BookedSession> list = new ArrayList<>();
        String normEmail = email != null ? email.trim().toLowerCase() : "";
        String normGymId = gymId != null ? gymId.trim().toLowerCase() : "";

        // Attempt DB fetch first
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM booked_sessions WHERE (LOWER(gym_id) = ? AND gym_id != '') OR LOWER(member_email) = ? ORDER BY id DESC")) {
            ps.setString(1, normGymId);
            ps.setString(2, normEmail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    list.add(b);
                }
            }
        } catch (Exception ex) {
            System.err.println("Note fetching DB bookings: " + ex.getMessage());
        }

        // Fallback / merge with in-memory if DB list is empty or offline
        if (list.isEmpty()) {
            for (BookedSession b : IN_MEMORY_BOOKINGS) {
                boolean matchEmail = !normEmail.isEmpty() && normEmail.equalsIgnoreCase(b.getMemberEmail());
                boolean matchGymId = !normGymId.isEmpty() && normGymId.equalsIgnoreCase(b.getGymId());
                if (matchEmail || matchGymId) {
                    list.add(b);
                }
            }
        }

        return list;
    }

    public boolean cancelBooking(int id) {
        IN_MEMORY_BOOKINGS.removeIf(b -> b.getId() == id);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM booked_sessions WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            System.err.println("Note cancelling booking in DB: " + ex.getMessage());
            return true;
        }
    }

    @Override
    public List<BookedSession> getAll() throws SQLException {
        return new ArrayList<>(IN_MEMORY_BOOKINGS);
    }

    @Override
    public BookedSession findById(Integer id) throws SQLException {
        for (BookedSession b : IN_MEMORY_BOOKINGS) {
            if (b.getId() == id) {
                return b;
            }
        }
        return null;
    }
}
