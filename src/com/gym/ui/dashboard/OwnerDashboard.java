/**
 * Date: 2026-11-16 - Yash
 */
package com.gym.ui.dashboard;

import com.gym.model.Role;
import com.gym.ui.MainFrame;
import com.gym.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OwnerDashboard extends BaseDashboard {

    public OwnerDashboard(MainFrame mainFrame) {
        super(mainFrame, "Gym Owner Executive Workspace", Role.OWNER);
        buildDashboard();
    }

    private void buildDashboard() {
        contentPanel.setLayout(new BorderLayout(0, 20));

        // Top Metrics
        JPanel metricsContainer = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsContainer.setOpaque(false);
        metricsContainer.add(createMetricCard("TOTAL REVENUE", "Rs. 3,85,000", "+18.4% this quarter", Theme.ROLE_OWNER_COLOR));
        metricsContainer.add(createMetricCard("ACTIVE MEMBERS", "520 Active", "94% retention rate", Theme.ROLE_MEMBER_COLOR));
        metricsContainer.add(createMetricCard("TRAINER STAFF", "12 Trainers", "Avg rating: 4.9 / 5.0", Theme.ROLE_TRAINER_COLOR));
        metricsContainer.add(createMetricCard("NEW SIGNUPS", "34 Members", "Joined this week", Theme.SUCCESS_COLOR));

        contentPanel.add(metricsContainer, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false);

        // Staff & Payroll Overview
        JPanel staffCard = new JPanel(new BorderLayout());
        staffCard.setBackground(Theme.CARD_BG);
        staffCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel staffTitle = new JLabel("Gym Staff & Personnel Roster");
        staffTitle.setFont(Theme.FONT_HEADER);
        staffTitle.setForeground(Theme.TEXT_LIGHT);
        staffCard.add(staffTitle, BorderLayout.NORTH);

        String[] cols = {"Employee Name", "Role", "Assigned Area", "Status"};
        Object[][] data = {
                {"Alex Rivera", "Lead Trainer", "Weightlifting Zone", "On Duty"},
                {"Elena Gomez", "Yoga/Cardio Trainer", "Aerobic Studio A", "On Duty"},
                {"Samira Patel", "Head Receptionist", "Front Desk 1", "Shift Active"},
                {"Lucas Vance", "Front Desk Associate", "Front Desk 2", "Break"},
                {"Chris Thorne", "Strength Coach", "CrossFit Area", "Scheduled 6 PM"}
        };
        JTable table = new JTable(new DefaultTableModel(data, cols));
        styleTable(table);
        JScrollPane scrollPane = Theme.createModernScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        staffCard.add(scrollPane, BorderLayout.CENTER);

        // Executive Actions Card
        JPanel ownerCard = new JPanel();
        ownerCard.setLayout(new BoxLayout(ownerCard, BoxLayout.Y_AXIS));
        ownerCard.setBackground(Theme.CARD_BG);
        ownerCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(22, 24, 22, 24)
        ));

        JLabel ownerTitle = new JLabel("Executive Controls");
        ownerTitle.setFont(Theme.FONT_HEADER);
        ownerTitle.setForeground(Theme.TEXT_LIGHT);

        JButton addStaffBtn = Theme.createPrimaryButton("Hire / Register New Staff Member");
        addStaffBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        addStaffBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Staff registration wizard opened."));

        JButton financialReportBtn = Theme.createSecondaryButton("Export Monthly Revenue & Expense Report");
        financialReportBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        financialReportBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Generating PDF Financial Report..."));

        JButton membershipPlansBtn = Theme.createSecondaryButton("Configure Pricing & Membership Packages");
        membershipPlansBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        membershipPlansBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Membership packages editor opened."));

        ownerCard.add(ownerTitle);
        ownerCard.add(Box.createVerticalStrut(20));
        ownerCard.add(addStaffBtn);
        ownerCard.add(Box.createVerticalStrut(12));
        ownerCard.add(financialReportBtn);
        ownerCard.add(Box.createVerticalStrut(12));
        ownerCard.add(membershipPlansBtn);

        centerPanel.add(staffCard);
        centerPanel.add(ownerCard);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
    }
}
