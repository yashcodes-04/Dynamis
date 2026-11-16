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

public class TrainerDashboard extends BaseDashboard {

    public TrainerDashboard(MainFrame mainFrame) {
        super(mainFrame, "Trainer Workspace", Role.TRAINER);
        buildDashboard();
    }

    private void buildDashboard() {
        contentPanel.setLayout(new BorderLayout(0, 20));

        // Top Metrics
        JPanel metricsContainer = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsContainer.setOpaque(false);
        metricsContainer.add(createMetricCard("ACTIVE CLIENTS", "24 Members", "Assigned personal roster", Theme.ROLE_TRAINER_COLOR));
        metricsContainer.add(createMetricCard("TODAY'S SESSIONS", "5 Scheduled", "Next: 06:00 PM with Jack", Theme.ROLE_MEMBER_COLOR));
        metricsContainer.add(createMetricCard("PLANS TO REVIEW", "3 Pending", "Diet & hypertrophy updates", Theme.ROLE_OWNER_COLOR));
        metricsContainer.add(createMetricCard("CLIENT GOAL RATE", "91%", "Quarterly target met", Theme.SUCCESS_COLOR));

        contentPanel.add(metricsContainer, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false);

        // Assigned Clients Table
        JPanel clientCard = new JPanel(new BorderLayout());
        clientCard.setBackground(Theme.CARD_BG);
        clientCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel clientTitle = new JLabel("My Trainees / Assigned Members");
        clientTitle.setFont(Theme.FONT_HEADER);
        clientTitle.setForeground(Theme.TEXT_LIGHT);
        clientCard.add(clientTitle, BorderLayout.NORTH);

        String[] cols = {"Client Name", "Fitness Goal", "Plan Status", "Next Session"};
        Object[][] data = {
                {"Jack Bauer", "Hypertrophy / Bulk", "Week 4 / 12", "Today 06:00 PM"},
                {"Maria Vance", "Fat Loss & HIIT", "Week 2 / 8", "Tomorrow 08:00 AM"},
                {"Ryan Howard", "Strength Powerlifting", "Week 8 / 16", "Tomorrow 10:30 AM"},
                {"Jessica Alba", "Core & Mobility", "Week 1 / 4", "Wed 05:00 PM"},
                {"Ken Adams", "Cardiovascular Health", "Week 6 / 12", "Thu 07:00 AM"}
        };
        JTable table = new JTable(new DefaultTableModel(data, cols));
        styleTable(table);
        JScrollPane scrollPane = Theme.createModernScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        clientCard.add(scrollPane, BorderLayout.CENTER);

        // Trainer Operations Card
        JPanel trainerCard = new JPanel();
        trainerCard.setLayout(new BoxLayout(trainerCard, BoxLayout.Y_AXIS));
        trainerCard.setBackground(Theme.CARD_BG);
        trainerCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(22, 24, 22, 24)
        ));

        JLabel trainerTitle = new JLabel("Training Management");
        trainerTitle.setFont(Theme.FONT_HEADER);
        trainerTitle.setForeground(Theme.TEXT_LIGHT);

        JButton assignPlanBtn = Theme.createPrimaryButton("Create / Assign Workout Routine");
        assignPlanBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        assignPlanBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Workout Plan Builder launched."));

        JButton logProgressBtn = Theme.createSecondaryButton("Log Client Measurements & Weights");
        logProgressBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        logProgressBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Client metric tracker active."));

        JButton scheduleSlotBtn = Theme.createSecondaryButton("Manage Available Booking Slots");
        scheduleSlotBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        scheduleSlotBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Calendar slots updated."));

        trainerCard.add(trainerTitle);
        trainerCard.add(Box.createVerticalStrut(20));
        trainerCard.add(assignPlanBtn);
        trainerCard.add(Box.createVerticalStrut(12));
        trainerCard.add(logProgressBtn);
        trainerCard.add(Box.createVerticalStrut(12));
        trainerCard.add(scheduleSlotBtn);

        centerPanel.add(clientCard);
        centerPanel.add(trainerCard);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
    }
}
