package com.gym.ui.dashboard;

import com.gym.model.Role;
import com.gym.ui.MainFrame;
import com.gym.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ReceptionistDashboard extends BaseDashboard {

    public ReceptionistDashboard(MainFrame mainFrame) {
        super(mainFrame, "Receptionist Workspace", Role.RECEPTIONIST);
        buildDashboard();
    }

    private void buildDashboard() {
        contentPanel.setLayout(new BorderLayout(0, 20));

        // Top Metrics
        JPanel metricsContainer = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsContainer.setOpaque(false);
        metricsContainer.add(createMetricCard("TODAY'S CHECK-INS", "142 Members", "+12% vs yesterday", Theme.ROLE_RECEPTIONIST_COLOR));
        metricsContainer.add(createMetricCard("CURRENTLY IN GYM", "38 Active", "Peak hours: 5 PM - 8 PM", Theme.SUCCESS_COLOR));
        metricsContainer.add(createMetricCard("EXPIRING THIS WEEK", "9 Members", "Pending renewal outreach", Theme.ROLE_OWNER_COLOR));
        metricsContainer.add(createMetricCard("NEW INQUIRIES", "6 Walk-ins", "Recorded today", Theme.ROLE_MEMBER_COLOR));

        contentPanel.add(metricsContainer, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false);

        // Recent Check-in Logs Table
        JPanel logCard = new JPanel(new BorderLayout());
        logCard.setBackground(Theme.CARD_BG);
        logCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel logTitle = new JLabel("Live Member Check-In Desk");
        logTitle.setFont(Theme.FONT_HEADER);
        logTitle.setForeground(Theme.TEXT_LIGHT);
        logCard.add(logTitle, BorderLayout.NORTH);

        String[] cols = {"Time", "Member Name", "Card/ID", "Status"};
        Object[][] data = {
                {"05:42 PM", "Johnathan Doe", "MEM-1049", "Authorized"},
                {"05:38 PM", "Sarah Connor", "MEM-0892", "Authorized"},
                {"05:30 PM", "Marcus Wright", "MEM-1102", "Authorized"},
                {"05:22 PM", "Emily Blunt", "MEM-0741", "Expiring Soon"},
                {"05:15 PM", "David Miller", "MEM-0994", "Authorized"}
        };
        JTable table = new JTable(new DefaultTableModel(data, cols));
        styleTable(table);
        JScrollPane scrollPane = Theme.createModernScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        logCard.add(scrollPane, BorderLayout.CENTER);

        // Desk Operations Card
        JPanel deskCard = new JPanel();
        deskCard.setLayout(new BoxLayout(deskCard, BoxLayout.Y_AXIS));
        deskCard.setBackground(Theme.CARD_BG);
        deskCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(22, 24, 22, 24)
        ));

        JLabel deskTitle = new JLabel("Reception Quick Actions");
        deskTitle.setFont(Theme.FONT_HEADER);
        deskTitle.setForeground(Theme.TEXT_LIGHT);

        JTextField scanField = Theme.createStyledTextField();
        scanField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton quickCheckInBtn = Theme.createPrimaryButton("Quick Check-in by Member ID / Email");
        quickCheckInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        quickCheckInBtn.addActionListener(e -> {
            String id = scanField.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a Member ID or Email to check-in.");
            } else {
                JOptionPane.showMessageDialog(this, "Check-in successful for: " + id);
                scanField.setText("");
            }
        });

        JButton walkInBtn = Theme.createSecondaryButton("Register Walk-in Guest / Trial Pass");
        walkInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        walkInBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Walk-in registration modal opened."));

        JButton renewMemberBtn = Theme.createSecondaryButton("Process In-person Payment / Renewal");
        renewMemberBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        renewMemberBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Payment terminal ready."));

        deskCard.add(deskTitle);
        deskCard.add(Box.createVerticalStrut(15));
        deskCard.add(Theme.createFieldLabel("Scan Member ID / Barcode:"));
        deskCard.add(Box.createVerticalStrut(6));
        deskCard.add(scanField);
        deskCard.add(Box.createVerticalStrut(12));
        deskCard.add(quickCheckInBtn);
        deskCard.add(Box.createVerticalStrut(12));
        deskCard.add(walkInBtn);
        deskCard.add(Box.createVerticalStrut(12));
        deskCard.add(renewMemberBtn);

        centerPanel.add(logCard);
        centerPanel.add(deskCard);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
    }
}
