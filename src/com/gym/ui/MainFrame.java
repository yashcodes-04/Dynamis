/**
 * Date: 2026-11-16 - Yash
 */
package com.gym.ui;

import com.gym.model.Role;
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
        setSize(1150, 780);
        setMinimumSize(new Dimension(900, 650));
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
                // Do not open members page instantly; show the choose membership box first
                chooseMembershipPanel.resetSelection();
                showCard(CARD_CHOOSE_MEMBERSHIP);
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

    public void showChooseMembership() {
        chooseMembershipPanel.resetSelection();
        showCard(CARD_CHOOSE_MEMBERSHIP);
    }

    public void openMemberDashboard(String planName, double price, int months) {
        memberDashboard.refreshUserInfo();
        memberDashboard.updateMembership(planName, price, months);
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
