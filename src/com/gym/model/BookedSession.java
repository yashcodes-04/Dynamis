/**
 * Date: 2026-11-21
 */
package com.gym.model;

import java.sql.Timestamp;

public class BookedSession {
    private int id;
    private String gymId;
    private String memberEmail;
    private String memberName;
    private String className;
    private String description;
    private String level;
    private String duration;
    private double price;
    private String scheduleSlot;
    private String status;
    private Timestamp bookingDate;

    public BookedSession() {}

    public BookedSession(int id, String gymId, String memberEmail, String memberName,
                         String className, String description, String level, String duration,
                         double price, String scheduleSlot, String status, Timestamp bookingDate) {
        this.id = id;
        this.gymId = gymId;
        this.memberEmail = memberEmail;
        this.memberName = memberName;
        this.className = className;
        this.description = description;
        this.level = level;
        this.duration = duration;
        this.price = price;
        this.scheduleSlot = scheduleSlot;
        this.status = status;
        this.bookingDate = bookingDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGymId() {
        return gymId;
    }

    public void setGymId(String gymId) {
        this.gymId = gymId;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getScheduleSlot() {
        return scheduleSlot;
    }

    public void setScheduleSlot(String scheduleSlot) {
        this.scheduleSlot = scheduleSlot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Timestamp bookingDate) {
        this.bookingDate = bookingDate;
    }
}
