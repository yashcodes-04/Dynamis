/**
 * Date: 2026-11-24
 * Syllabus: Unit 1 (Lectures 1-3) - Object-Oriented Modeling & Encapsulation
 *           Unit 2 (Lectures 23-25) - Comparable & Comparator for sorting monthly performance records
 */
package com.gym.model;

import java.util.Comparator;

public class MonthlyStat implements Comparable<MonthlyStat> {

    private String yearMonth;    // "2026-11"
    private String monthName;    // "November 2026"
    private double totalRevenue;
    private int transactionCount;
    private int newSignups;
    private int checkinCount;
    private String topPlan;

    public static final Comparator<MonthlyStat> BY_REVENUE_DESC = (s1, s2) -> Double.compare(s2.getTotalRevenue(), s1.getTotalRevenue());
    public static final Comparator<MonthlyStat> BY_DATE_DESC = (s1, s2) -> s2.getYearMonth().compareTo(s1.getYearMonth());

    public MonthlyStat() {}

    public MonthlyStat(String yearMonth, String monthName, double totalRevenue, int transactionCount,
                       int newSignups, int checkinCount, String topPlan) {
        this.yearMonth = yearMonth;
        this.monthName = monthName;
        this.totalRevenue = totalRevenue;
        this.transactionCount = transactionCount;
        this.newSignups = newSignups;
        this.checkinCount = checkinCount;
        this.topPlan = topPlan != null ? topPlan : "Monthly Pass";
    }

    public double getAverageTicket() {
        if (transactionCount <= 0) return 0.0;
        return totalRevenue / transactionCount;
    }

    @Override
    public int compareTo(MonthlyStat other) {
        if (other == null) return 1;
        return other.yearMonth.compareTo(this.yearMonth); // Default latest month first
    }

    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }

    public String getMonthName() { return monthName; }
    public void setMonthName(String monthName) { this.monthName = monthName; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }

    public int getNewSignups() { return newSignups; }
    public void setNewSignups(int newSignups) { this.newSignups = newSignups; }

    public int getCheckinCount() { return checkinCount; }
    public void setCheckinCount(int checkinCount) { this.checkinCount = checkinCount; }

    public String getTopPlan() { return topPlan; }
    public void setTopPlan(String topPlan) { this.topPlan = topPlan; }
}
