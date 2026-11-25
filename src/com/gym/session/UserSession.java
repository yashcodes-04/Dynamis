/**
 * Date: 2026-11-16 - Yash
 */
package com.gym.session;

import com.gym.model.Role;
import com.gym.model.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class UserSession {
    private static User currentUser;
    private static String membershipPlan;
    private static Double membershipPrice;
    private static LocalDate membershipStartDate;
    private static LocalDate renewalDueDate;
    private static int membershipDurationDays;
    private static String assignedTrainer;

    public static void setCurrentUser(User user) {
        currentUser = user;
        if (user != null) {
            assignedTrainer = user.getAssignedTrainer();
        } else {
            assignedTrainer = null;
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static Role getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public static void setMembership(String plan, double price, int months) {
        int days = months == 1 ? 30 : (months == 3 ? 90 : (months == 6 ? 180 : (months == 12 ? 365 : months * 30)));
        LocalDate start = LocalDate.now();
        LocalDate due = start.plusDays(days);
        setMembership(plan, price, days, start, due);
    }

    public static void setMembership(String plan, double price, LocalDate dueDate) {
        LocalDate start = LocalDate.now();
        int days = (int) Math.max(0, ChronoUnit.DAYS.between(start, dueDate));
        setMembership(plan, price, days, start, dueDate);
    }

    public static void setMembership(String plan, double price, int durationDays, LocalDate startDate, LocalDate endDate) {
        membershipPlan = plan;
        membershipPrice = price;
        membershipDurationDays = durationDays;
        membershipStartDate = startDate;
        renewalDueDate = endDate;
    }

    /**
     * Calculates days of membership left based on current date.
     * Returns 0 if membership plan is null, due date is null, or due date has passed.
     */
    public static long getDaysLeft() {
        if (membershipPlan == null || renewalDueDate == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        if (today.isAfter(renewalDueDate)) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(today, renewalDueDate);
        return Math.max(0, days);
    }

    public static boolean hasActiveValidMembership() {
        return membershipPlan != null && getDaysLeft() > 0;
    }

    public static String getMembershipPlan() {
        return membershipPlan;
    }

    public static Double getMembershipPrice() {
        return membershipPrice;
    }

    public static LocalDate getMembershipStartDate() {
        return membershipStartDate;
    }

    public static LocalDate getRenewalDueDate() {
        return renewalDueDate;
    }

    public static int getMembershipDurationDays() {
        return membershipDurationDays;
    }

    public static boolean hasActiveMembership() {
        return membershipPlan != null;
    }

    public static String getAssignedTrainer() {
        return assignedTrainer;
    }

    public static void setAssignedTrainer(String trainer) {
        assignedTrainer = trainer;
    }

    public static void logout() {
        currentUser = null;
        membershipPlan = null;
        membershipPrice = null;
        membershipStartDate = null;
        renewalDueDate = null;
        membershipDurationDays = 0;
        assignedTrainer = null;
    }
}
