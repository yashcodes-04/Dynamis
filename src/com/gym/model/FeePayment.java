/**
 * Date: 2026-11-18 - Yash
 * Syllabus: Unit 2 (Lecture 8) - Comparable & Comparator implementations
 */
package com.gym.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Comparator;

public class FeePayment implements Comparable<FeePayment> {
    private int id;
    private String gymId;
    private String memberEmail;
    private String memberName;
    private String planType;
    private double amount;
    private String paymentMode;
    private Timestamp paymentDate;
    private Date renewalDueDate;

    // Comparators for custom sorting (Lecture 8)
    public static final Comparator<FeePayment> BY_AMOUNT_DESC = (p1, p2) -> Double.compare(p2.getAmount(), p1.getAmount());
    public static final Comparator<FeePayment> BY_MEMBER_NAME = (p1, p2) -> p1.getMemberName().compareToIgnoreCase(p2.getMemberName());
    public static final Comparator<FeePayment> BY_DATE_DESC = (p1, p2) -> {
        if (p1.getPaymentDate() == null || p2.getPaymentDate() == null) return 0;
        return p2.getPaymentDate().compareTo(p1.getPaymentDate());
    };

    public FeePayment() {}

    public FeePayment(int id, String gymId, String memberEmail, String memberName, String planType,
                      double amount, String paymentMode, Timestamp paymentDate, Date renewalDueDate) {
        this.id = id;
        this.gymId = gymId;
        this.memberEmail = memberEmail;
        this.memberName = memberName;
        this.planType = planType;
        this.amount = amount;
        this.paymentMode = paymentMode;
        this.paymentDate = paymentDate;
        this.renewalDueDate = renewalDueDate;
    }

    /**
     * Natural sorting order: Latest payment date first (Comparable interface)
     */
    @Override
    public int compareTo(FeePayment other) {
        if (this.paymentDate == null || other.paymentDate == null) {
            return Integer.compare(other.id, this.id);
        }
        return other.paymentDate.compareTo(this.paymentDate);
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

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public Timestamp getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Timestamp paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Date getRenewalDueDate() {
        return renewalDueDate;
    }

    public void setRenewalDueDate(Date renewalDueDate) {
        this.renewalDueDate = renewalDueDate;
    }
}
