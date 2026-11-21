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
    private static LocalDate renewalDueDate;

    public static void setCurrentUser(User user) {
        currentUser = user;
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
        membershipPlan = plan;
        membershipPrice = price;
        renewalDueDate = LocalDate.now().plusMonths(months);
    }

    public static void setMembership(String plan, double price, LocalDate dueDate) {
        membershipPlan = plan;
        membershipPrice = price;
        renewalDueDate = dueDate;
    }

    public static long getDaysLeft() {
        if (renewalDueDate == null) {
            return 30;
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), renewalDueDate);
        return Math.max(0, days);
    }

    public static String getMembershipPlan() {
        return membershipPlan;
    }

    public static Double getMembershipPrice() {
        return membershipPrice;
    }

    public static LocalDate getRenewalDueDate() {
        return renewalDueDate;
    }

    public static boolean hasActiveMembership() {
        return membershipPlan != null;
    }

    public static void logout() {
        currentUser = null;
        membershipPlan = null;
        membershipPrice = null;
        renewalDueDate = null;
    }
}
