/**
 * Date: 2026-11-16 - Yash
 */
package com.gym.model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String gymId;
    private String fullName;
    private String email;
    private String phone;
    private String passwordHash;
    private Role role;
    private Timestamp createdAt;
    private String membershipPlan;
    private Double membershipPrice;
    private java.time.LocalDate membershipStartDate;
    private java.time.LocalDate membershipEndDate;
    private Integer membershipDurationDays;
    private String assignedTrainer;

    public User() {}

    public User(int id, String gymId, String fullName, String email, String phone, String passwordHash, Role role, Timestamp createdAt) {
        this.id = id;
        this.gymId = gymId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    public User(String fullName, String email, String phone, String passwordHash, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getMembershipPlan() {
        return membershipPlan;
    }

    public void setMembershipPlan(String membershipPlan) {
        this.membershipPlan = membershipPlan;
    }

    public Double getMembershipPrice() {
        return membershipPrice;
    }

    public void setMembershipPrice(Double membershipPrice) {
        this.membershipPrice = membershipPrice;
    }

    public java.time.LocalDate getMembershipStartDate() {
        return membershipStartDate;
    }

    public void setMembershipStartDate(java.time.LocalDate membershipStartDate) {
        this.membershipStartDate = membershipStartDate;
    }

    public java.time.LocalDate getMembershipEndDate() {
        return membershipEndDate;
    }

    public void setMembershipEndDate(java.time.LocalDate membershipEndDate) {
        this.membershipEndDate = membershipEndDate;
    }

    public Integer getMembershipDurationDays() {
        return membershipDurationDays;
    }

    public void setMembershipDurationDays(Integer membershipDurationDays) {
        this.membershipDurationDays = membershipDurationDays;
    }

    public String getAssignedTrainer() {
        return assignedTrainer;
    }

    public void setAssignedTrainer(String assignedTrainer) {
        this.assignedTrainer = assignedTrainer;
    }
}
