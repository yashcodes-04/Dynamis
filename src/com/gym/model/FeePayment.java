/**
 * Date: 2026-11-17 - Yash
 */
package com.gym.model;

import java.sql.Date;
import java.sql.Timestamp;

public class FeePayment {
    private int id;
    private String memberEmail;
    private String memberName;
    private String planType;
    private double amount;
    private String paymentMode;
    private Timestamp paymentDate;
    private Date renewalDueDate;

    public FeePayment() {}

    public FeePayment(int id, String memberEmail, String memberName, String planType,
                      double amount, String paymentMode, Timestamp paymentDate, Date renewalDueDate) {
        this.id = id;
        this.memberEmail = memberEmail;
        this.memberName = memberName;
        this.planType = planType;
        this.amount = amount;
        this.paymentMode = paymentMode;
        this.paymentDate = paymentDate;
        this.renewalDueDate = renewalDueDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
