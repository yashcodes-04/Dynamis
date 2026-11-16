/**
 * Date: 2026-11-16 - Yash
 */
package com.gym.session;

import com.gym.model.Role;
import com.gym.model.User;

public class UserSession {
    private static User currentUser;

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

    public static void logout() {
        currentUser = null;
    }
}
