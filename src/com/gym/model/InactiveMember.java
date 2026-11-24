/**
 * Date: 2026-11-24
 * Syllabus: Unit 1 (Lectures 1-3) - Encapsulation & Validation
 *           Unit 2 (Lectures 23-25) - Comparable & Comparator implementations for record management
 */
package com.gym.model;

import java.time.LocalDate;
import java.util.Comparator;

public class InactiveMember implements Comparable<InactiveMember> {

    private String gymId;
    private String fullName;
    private String email;
    private String phone;
    private String membershipPlan;
    private LocalDate expiryDate;
    private long daysInactive;
    private String lastCheckInDate;
    private String reason;          // "Expired Membership", "No Plan Chosen", "Dormant (30+ Days)"
    private String followUpStatus;  // "Pending", "Reminder Sent", "Contacted"

    public static final Comparator<InactiveMember> BY_DAYS_INACTIVE_DESC = (m1, m2) -> Long.compare(m2.getDaysInactive(), m1.getDaysInactive());
    public static final Comparator<InactiveMember> BY_NAME_ASC = (m1, m2) -> m1.getFullName().compareToIgnoreCase(m2.getFullName());
    public static final Comparator<InactiveMember> BY_REASON = (m1, m2) -> m1.getReason().compareToIgnoreCase(m2.getReason());

    public InactiveMember() {}

    public InactiveMember(String gymId, String fullName, String email, String phone,
                          String membershipPlan, LocalDate expiryDate, long daysInactive,
                          String lastCheckInDate, String reason, String followUpStatus) {
        this.gymId = gymId != null ? gymId : "N/A";
        this.fullName = fullName;
        this.email = email;
        this.phone = phone != null ? phone : "--";
        this.membershipPlan = membershipPlan != null ? membershipPlan : "None";
        this.expiryDate = expiryDate;
        this.daysInactive = Math.max(0, daysInactive);
        this.lastCheckInDate = lastCheckInDate != null ? lastCheckInDate : "Never";
        this.reason = reason != null ? reason : "Expired Membership";
        this.followUpStatus = followUpStatus != null ? followUpStatus : "Pending";
    }

    @Override
    public int compareTo(InactiveMember other) {
        if (other == null) return 1;
        return Long.compare(other.daysInactive, this.daysInactive); // Most inactive first
    }

    public String getGymId() { return gymId; }
    public void setGymId(String gymId) { this.gymId = gymId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getMembershipPlan() { return membershipPlan; }
    public void setMembershipPlan(String membershipPlan) { this.membershipPlan = membershipPlan; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public long getDaysInactive() { return daysInactive; }
    public void setDaysInactive(long daysInactive) { this.daysInactive = daysInactive; }

    public String getLastCheckInDate() { return lastCheckInDate; }
    public void setLastCheckInDate(String lastCheckInDate) { this.lastCheckInDate = lastCheckInDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getFollowUpStatus() { return followUpStatus; }
    public void setFollowUpStatus(String followUpStatus) { this.followUpStatus = followUpStatus; }
}
