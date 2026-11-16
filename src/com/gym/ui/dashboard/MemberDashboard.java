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

public class MemberDashboard extends BaseDashboard {

    public MemberDashboard(MainFrame mainFrame) {
        super(mainFrame, "Member Workspace", Role.MEMBER);
        buildDashboard();
    }

    private void buildDashboard() {
        contentPanel.setLayout(new BorderLayout(0, 20));

        // Top Metrics
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsPanel.setOpaque(false);
        metricsPanel.add(createMetricCard("MEMBERSHIP STATUS", "ACTIVE", "Renews in 42 days", Theme.ROLE_MEMBER_COLOR));
        metricsPanel.add(createMetricCard("WORKOUT ATTENDANCE", "18 Days", "This month (Top 10%)", Theme.SUCCESS_COLOR));
        metricsPanel.add(createMetricCard("ASSIGNED TRAINER", "Alex Rivera", "Personal Training - Tier 1", Theme.ROLE_TRAINER_COLOR));
        metricsPanel.add(createMetricCard("NEXT CLASS", "HIIT Cardio", "Today at 06:30 PM", Theme.ACCENT_PRIMARY));

        contentPanel.add(metricsPanel, BorderLayout.NORTH);

        // Center: Workout Schedule & Bookings
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false);

        // Schedule Table
        JPanel scheduleCard = new JPanel(new BorderLayout());
        scheduleCard.setBackground(Theme.CARD_BG);
        scheduleCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel schedTitle = new JLabel("My Weekly Workout Schedule");
        schedTitle.setFont(Theme.FONT_HEADER);
        schedTitle.setForeground(Theme.TEXT_LIGHT);
        scheduleCard.add(schedTitle, BorderLayout.NORTH);

        String[] cols = {"Day", "Workout Target", "Trainer", "Time Slot"};
        Object[][] data = {
                {"Monday", "Chest & Triceps", "Alex Rivera", "07:00 AM"},
                {"Tuesday", "Back & Biceps", "Alex Rivera", "07:00 AM"},
                {"Wednesday", "Active Recovery / Yoga", "Elena Gomez", "08:00 AM"},
                {"Thursday", "Legs & Core Strength", "Alex Rivera", "07:00 AM"},
                {"Friday", "High Intensity Circuit", "Self Directed", "06:30 PM"},
                {"Saturday", "Cardio & Stretching", "Elena Gomez", "09:00 AM"}
        };
        JTable table = new JTable(new DefaultTableModel(data, cols));
        styleTable(table);
        JScrollPane scrollPane = Theme.createModernScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        scheduleCard.add(scrollPane, BorderLayout.CENTER);

        // Quick Actions Card
        JPanel actionCard = new JPanel();
        actionCard.setLayout(new BoxLayout(actionCard, BoxLayout.Y_AXIS));
        actionCard.setBackground(Theme.CARD_BG);
        actionCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(22, 24, 22, 24)
        ));

        JLabel actionTitle = new JLabel("Member Quick Actions");
        actionTitle.setFont(Theme.FONT_HEADER);
        actionTitle.setForeground(Theme.TEXT_LIGHT);

        JButton bookClassBtn = Theme.createPrimaryButton("Book a Fitness Class");
        bookClassBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        bookClassBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Class booking slot opened! Feature active."));

        JButton renewBtn = Theme.createSecondaryButton("Renew / Upgrade Membership");
        renewBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        renewBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Redirecting to membership plans..."));

        JButton contactTrainerBtn = Theme.createSecondaryButton("Message Assigned Trainer");
        contactTrainerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        contactTrainerBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Trainer chat window opened."));

        actionCard.add(actionTitle);
        actionCard.add(Box.createVerticalStrut(20));
        actionCard.add(bookClassBtn);
        actionCard.add(Box.createVerticalStrut(12));
        actionCard.add(renewBtn);
        actionCard.add(Box.createVerticalStrut(12));
        actionCard.add(contactTrainerBtn);

        centerPanel.add(scheduleCard);
        centerPanel.add(actionCard);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
    }
}
