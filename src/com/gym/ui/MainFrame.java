/**
 * Date: 2026-11-16 - Yash
 */
package com.gym.ui;

import com.gym.dao.UserDAO;
import com.gym.model.Role;
import com.gym.model.User;
import com.gym.session.UserSession;
import com.gym.ui.dashboard.MemberDashboard;
import com.gym.ui.dashboard.OwnerDashboard;
import com.gym.ui.dashboard.ReceptionistDashboard;
import com.gym.ui.dashboard.TrainerDashboard;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_REGISTER = "REGISTER";
    public static final String CARD_CHOOSE_MEMBERSHIP = "CHOOSE_MEMBERSHIP";
    public static final String CARD_MEMBER = "MEMBER";
    public static final String CARD_RECEPTIONIST = "RECEPTIONIST";
    public static final String CARD_TRAINER = "TRAINER";
    public static final String CARD_OWNER = "OWNER";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel(cardLayout);

    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private ChooseMembershipPanel chooseMembershipPanel;
    private MemberDashboard memberDashboard;
    private ReceptionistDashboard receptionistDashboard;
    private TrainerDashboard trainerDashboard;
    private OwnerDashboard ownerDashboard;

    public MainFrame() {
        setTitle("Dynamis Gym - Multi-Role Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);

        initViews();
    }

    private void initViews() {
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        chooseMembershipPanel = new ChooseMembershipPanel(this);
        memberDashboard = new MemberDashboard(this);
        receptionistDashboard = new ReceptionistDashboard(this);
        trainerDashboard = new TrainerDashboard(this);
        ownerDashboard = new OwnerDashboard(this);

        cardsPanel.add(loginPanel, CARD_LOGIN);
        cardsPanel.add(registerPanel, CARD_REGISTER);
        cardsPanel.add(chooseMembershipPanel, CARD_CHOOSE_MEMBERSHIP);
        cardsPanel.add(memberDashboard, CARD_MEMBER);
        cardsPanel.add(receptionistDashboard, CARD_RECEPTIONIST);
        cardsPanel.add(trainerDashboard, CARD_TRAINER);
        cardsPanel.add(ownerDashboard, CARD_OWNER);

        add(cardsPanel);

        // Start at Login
        showCard(CARD_LOGIN);
    }

    public void showCard(String cardName) {
        cardLayout.show(cardsPanel, cardName);
    }

    public void showDashboardForRole(Role role) {
        if (role == null) return;
        switch (role) {
            case MEMBER:
                handleMemberDashboardAccess();
                break;
            case RECEPTIONIST:
                receptionistDashboard.refreshUserInfo();
                showCard(CARD_RECEPTIONIST);
                break;
            case TRAINER:
                trainerDashboard.refreshUserInfo();
                showCard(CARD_TRAINER);
                break;
            case OWNER:
                ownerDashboard.refreshUserInfo();
                showCard(CARD_OWNER);
                break;
        }
    }

    /**
     * Checks the user's membership conditions before opening Member Dashboard:
     * 1. If membership plan is already in DB and days are left (> 0): directly open dashboard.
     * 2. If membership days are over and have become zero (daysLeft <= 0): block entry until renewed.
     * 3. If membership plan is null: block entry until user chooses a plan.
     */
    public void handleMemberDashboardAccess() {
        User user = UserSession.getCurrentUser();
        if (user == null) {
            showCard(CARD_LOGIN);
            return;
        }

        // Synchronize latest membership plan, start date, and end date from the database
        UserDAO userDAO = new UserDAO();
        userDAO.syncUserMembership(user);

        String plan = UserSession.getMembershipPlan();
        long daysLeft = UserSession.getDaysLeft();

        // Condition 1: Plan exists in database AND days left > 0 -> directly open dashboard
        if (plan != null && !plan.trim().isEmpty() && daysLeft > 0) {
            memberDashboard.refreshUserInfo();
            showCard(CARD_MEMBER);
        }
        // Condition 2: Days are over (0 days left) -> block entry and show renewal prompt
        else if (plan != null && !plan.trim().isEmpty() && daysLeft <= 0) {
            chooseMembershipPanel.setCanReturnToDashboard(false);
            chooseMembershipPanel.setExpiredNotice(plan, UserSession.getRenewalDueDate());
            showCard(CARD_CHOOSE_MEMBERSHIP);
        }
        // Condition 3: Membership plan is null -> block entry and show choose membership
        else {
            chooseMembershipPanel.setCanReturnToDashboard(false);
            chooseMembershipPanel.resetSelection();
            showCard(CARD_CHOOSE_MEMBERSHIP);
        }
    }

    public void showChooseMembership() {
        showChooseMembership(false);
    }

    public void showChooseMembership(boolean canReturnToDashboard) {
        boolean allowReturn = canReturnToDashboard && UserSession.isLoggedIn() && UserSession.hasActiveValidMembership();
        chooseMembershipPanel.resetSelection();
        chooseMembershipPanel.setCanReturnToDashboard(allowReturn);
        showCard(CARD_CHOOSE_MEMBERSHIP);
    }

    public void openMemberDashboard(String planName, double price, int durationDays) {
        memberDashboard.updateMembership(planName, price, durationDays);
        memberDashboard.refreshUserInfo();
        showCard(CARD_MEMBER);
    }

    public LoginPanel getLoginPanel() {
        return loginPanel;
    }

    public RegisterPanel getRegisterPanel() {
        return registerPanel;
    }

    public ChooseMembershipPanel getChooseMembershipPanel() {
        return chooseMembershipPanel;
    }
}
