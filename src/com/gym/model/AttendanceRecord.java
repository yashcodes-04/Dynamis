/**
 * Date: 2026-11-18 - Yash
 */
package com.gym.model;

import java.sql.Timestamp;

public class AttendanceRecord {
    private int id;
    private String gymId;
    private String memberName;
    private String memberEmail;
    private Timestamp checkInTime;

    public AttendanceRecord() {}

    public AttendanceRecord(int id, String gymId, String memberName, String memberEmail, Timestamp checkInTime) {
        this.id = id;
        this.gymId = gymId;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.checkInTime = checkInTime;
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

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public Timestamp getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Timestamp checkInTime) {
        this.checkInTime = checkInTime;
    }
}
