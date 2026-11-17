/**
 * Date: 2026-11-17 - Yash
 */
package com.gym.ui.dashboard;

import com.gym.dao.ReceptionistDAO;
import com.gym.model.FeePayment;
import com.gym.model.Role;
import com.gym.ui.MainFrame;
import com.gym.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

public class ReceptionistDashboard extends BaseDashboard {

    private final ReceptionistDAO receptionistDAO = new ReceptionistDAO();

    // Metric Labels
    private JLabel totalRevenueVal;
    private JLabel totalRenewalsVal;
    private JLabel todayDateVal;

    // Fee Entry Form Components
    private JTextField memberEmailField;
    private JTextField memberNameField;
    private JComboBox<String> planComboBox;
    private JTextField amountField;
    private JComboBox<String> paymentModeComboBox;
    private JLabel renewalPreviewLabel;
    private JLabel formStatusLabel;
    private JButton submitFeeBtn;
    private JButton verifyMemberBtn;

    // History Table Components
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel recordCountLabel;

    // Plan data maps
    private final String[] plans = {
            "Monthly Pass (1 Month)",
            "Quarterly Pass (3 Months)",
            "Half-Yearly Pass (6 Months)",
            "Annual Pass (12 Months)"
    };
    private final double[] planPrices = {50.0, 135.0, 250.0, 450.0};
    private final int[] planMonths = {1, 3, 6, 12};

    public ReceptionistDashboard(MainFrame mainFrame) {
        super(mainFrame, "Receptionist Workspace", Role.RECEPTIONIST);
        buildDashboard();
        loadData("");
    }

    private void buildDashboard() {
        contentPanel.setLayout(new BorderLayout(0, 20));

        // 1. Top Metrics Bar
        JPanel metricsContainer = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsContainer.setOpaque(false);

        metricsContainer.add(createMetricCard("TOTAL REVENUE COLLECTED", "$0.00", "All time renewals", Theme.SUCCESS_COLOR, totalRevenueVal = new JLabel("$0.00")));
        metricsContainer.add(createMetricCard("RENEWALS RECORDED", "0", "Completed transactions", Theme.ROLE_RECEPTIONIST_COLOR, totalRenewalsVal = new JLabel("0")));
        metricsContainer.add(createMetricCard("TODAY'S DATE", LocalDate.now().toString(), "Desk active", Theme.ROLE_MEMBER_COLOR, todayDateVal = new JLabel(LocalDate.now().toString())));
        metricsContainer.add(createMetricCard("DESK OPERATION", "ONLINE", "Ready for check-ins", Theme.ROLE_TRAINER_COLOR, new JLabel("ONLINE")));

        contentPanel.add(metricsContainer, BorderLayout.NORTH);

        // 2. Main Workspace Split: Left Form (Fee Entry) | Right History Table
        JPanel workspacePanel = new JPanel(new GridBagLayout());
        workspacePanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Panel: Fee Entry Form
        JPanel feeEntryCard = buildFeeEntryCard();
        gbc.gridx = 0;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 15);
        workspacePanel.add(feeEntryCard, gbc);

        // Right Panel: Renewal & Payment History Table
        JPanel historyCard = buildHistoryCard();
        gbc.gridx = 1;
        gbc.weightx = 0.62;
        gbc.insets = new Insets(0, 0, 0, 0);
        workspacePanel.add(historyCard, gbc);

        contentPanel.add(workspacePanel, BorderLayout.CENTER);
    }

    private JPanel buildFeeEntryCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(22, 24, 22, 24)
        ));

        // Header
        JLabel titleLabel = new JLabel("Fee Entry & Membership Renewal");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_LIGHT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Collect fee, renew membership, and issue receipt");
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Inputs
        memberEmailField = Theme.createStyledTextField();
        memberEmailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        verifyMemberBtn = Theme.createSecondaryButton("Verify");
        verifyMemberBtn.setPreferredSize(new Dimension(80, 38));
        verifyMemberBtn.addActionListener(e -> verifyMember());

        JPanel emailRow = new JPanel(new BorderLayout(8, 0));
        emailRow.setOpaque(false);
        emailRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        emailRow.add(memberEmailField, BorderLayout.CENTER);
        emailRow.add(verifyMemberBtn, BorderLayout.EAST);

        // Auto lookup on Enter
        memberEmailField.addActionListener(e -> verifyMember());

        memberNameField = Theme.createStyledTextField();
        memberNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        planComboBox = new JComboBox<>(plans);
        styleComboBox(planComboBox);
        planComboBox.addActionListener(e -> updatePlanCalculations());

        amountField = Theme.createStyledTextField();
        amountField.setText(String.valueOf(planPrices[0]));
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        String[] modes = {"Cash", "UPI / QR Code", "Credit/Debit Card", "Bank Transfer"};
        paymentModeComboBox = new JComboBox<>(modes);
        styleComboBox(paymentModeComboBox);

        renewalPreviewLabel = new JLabel("Next Renewal Due: " + LocalDate.now().plusMonths(1));
        renewalPreviewLabel.setFont(Theme.FONT_BOLD);
        renewalPreviewLabel.setForeground(Theme.SUCCESS_COLOR);
        renewalPreviewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        formStatusLabel = new JLabel(" ");
        formStatusLabel.setFont(Theme.FONT_SMALL);
        formStatusLabel.setForeground(Theme.ERROR_COLOR);
        formStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        submitFeeBtn = Theme.createPrimaryButton("Process Fee & Record Renewal");
        submitFeeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitFeeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        submitFeeBtn.addActionListener(e -> handleFeeSubmission());

        // Assembly
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(18));

        addFormField(card, "Member Email Address", emailRow);
        addFormField(card, "Member Full Name", memberNameField);
        addFormField(card, "Membership Plan", planComboBox);
        addFormField(card, "Fee Amount ($)", amountField);
        addFormField(card, "Payment Mode", paymentModeComboBox);

        card.add(renewalPreviewLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(formStatusLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(submitFeeBtn);

        return card;
    }

    private JPanel buildHistoryCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(22, 22, 22, 22)
        ));

        // Header Row + Search
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Renewal & Payment History");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_LIGHT);

        recordCountLabel = new JLabel("Loading records...");
        recordCountLabel.setFont(Theme.FONT_SMALL);
        recordCountLabel.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(recordCountLabel);

        // Search Block
        JPanel searchBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBlock.setOpaque(false);

        searchField = Theme.createStyledTextField();
        searchField.setPreferredSize(new Dimension(200, 36));
        searchField.setToolTipText("Filter by Member Email or Name");
        searchField.addActionListener(e -> loadData(searchField.getText().trim()));

        JButton searchBtn = Theme.createSecondaryButton("Search");
        searchBtn.addActionListener(e -> loadData(searchField.getText().trim()));

        JButton resetBtn = Theme.createSecondaryButton("Refresh");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadData("");
        });

        searchBlock.add(searchField);
        searchBlock.add(searchBtn);
        searchBlock.add(resetBtn);

        topBar.add(titleBlock, BorderLayout.WEST);
        topBar.add(searchBlock, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Payment Date", "Member Name", "Email", "Plan", "Amount", "Mode", "Renewal Due"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        styleTable(historyTable);

        // Column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(75);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(95);
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(100);

        JScrollPane scrollPane = Theme.createModernScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.CARD_BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private void addFormField(JPanel parent, String labelText, JComponent component) {
        JLabel label = Theme.createFieldLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setAlignmentX(Component.LEFT_ALIGNMENT);

        parent.add(label);
        parent.add(Box.createVerticalStrut(4));
        parent.add(component);
        parent.add(Box.createVerticalStrut(10));
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setBackground(Theme.INPUT_BG);
        box.setForeground(Theme.TEXT_LIGHT);
        box.setFont(Theme.FONT_BODY);
        box.setPreferredSize(new Dimension(box.getPreferredSize().width, 38));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        box.setBorder(Theme.createFieldBorder(Theme.INPUT_BORDER));
    }

    private void updatePlanCalculations() {
        int idx = planComboBox.getSelectedIndex();
        if (idx >= 0 && idx < planPrices.length) {
            amountField.setText(String.format("%.2f", planPrices[idx]));
            LocalDate nextDue = LocalDate.now().plusMonths(planMonths[idx]);
            renewalPreviewLabel.setText("Next Renewal Due: " + nextDue);
        }
    }

    private void verifyMember() {
        String email = memberEmailField.getText().trim();
        if (email.isEmpty()) {
            setStatus("Please enter a member email to verify.", true);
            return;
        }

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return receptionistDAO.findMemberNameByEmail(email);
            }

            @Override
            protected void done() {
                try {
                    String name = get();
                    if (name != null) {
                        memberNameField.setText(name);
                        setStatus("Member verified: " + name, false);
                    } else {
                        setStatus("Notice: Member not found in registered users.", true);
                    }
                } catch (Exception ex) {
                    setStatus("Lookup error: " + ex.getMessage(), true);
                }
            }
        };
        worker.execute();
    }

    private void handleFeeSubmission() {
        String email = memberEmailField.getText().trim();
        String name = memberNameField.getText().trim();
        int planIdx = planComboBox.getSelectedIndex();
        String mode = (String) paymentModeComboBox.getSelectedItem();
        String amountText = amountField.getText().trim();

        if (email.isEmpty() || name.isEmpty() || amountText.isEmpty()) {
            setStatus("Please fill in Member Email, Name, and Amount.", true);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                setStatus("Amount must be greater than zero.", true);
                return;
            }
        } catch (NumberFormatException ex) {
            setStatus("Please enter a valid numeric amount.", true);
            return;
        }

        submitFeeBtn.setEnabled(false);
        submitFeeBtn.setText("Saving Renewal...");

        int durationMonths = planMonths[planIdx];
        String planName = plans[planIdx];

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private String err = null;

            @Override
            protected Boolean doInBackground() {
                try {
                    return receptionistDAO.recordFeePayment(email, name, planName, amount, mode, durationMonths);
                } catch (Exception ex) {
                    err = ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                submitFeeBtn.setEnabled(true);
                submitFeeBtn.setText("Process Fee & Record Renewal");

                try {
                    boolean success = get();
                    if (success) {
                        setStatus("Success! Renewal recorded for " + name, false);
                        memberEmailField.setText("");
                        memberNameField.setText("");
                        updatePlanCalculations();
                        loadData(""); // Reload table & metrics instantly!
                    } else {
                        setStatus("Failed: " + (err != null ? err : "Could not record payment."), true);
                    }
                } catch (Exception ex) {
                    setStatus("Error: " + ex.getMessage(), true);
                }
            }
        };
        worker.execute();
    }

    public void loadData(String filter) {
        SwingWorker<List<FeePayment>, Void> worker = new SwingWorker<>() {
            private double totalRevenue = 0;
            private int totalCount = 0;

            @Override
            protected List<FeePayment> doInBackground() throws Exception {
                totalRevenue = receptionistDAO.getTotalRevenue();
                totalCount = receptionistDAO.getTotalRenewalsCount();
                return receptionistDAO.getRenewalHistory(filter);
            }

            @Override
            protected void done() {
                try {
                    List<FeePayment> list = get();
                    tableModel.setRowCount(0);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    for (FeePayment fp : list) {
                        String dateStr = fp.getPaymentDate() != null ? sdf.format(fp.getPaymentDate()) : "N/A";
                        tableModel.addRow(new Object[]{
                                fp.getId(),
                                dateStr,
                                fp.getMemberName(),
                                fp.getMemberEmail(),
                                fp.getPlanType(),
                                String.format("$%.2f", fp.getAmount()),
                                fp.getPaymentMode(),
                                fp.getRenewalDueDate() != null ? fp.getRenewalDueDate().toString() : "N/A"
                        });
                    }

                    // Update Top Metric Counters
                    totalRevenueVal.setText(String.format("$%.2f", totalRevenue));
                    totalRenewalsVal.setText(String.valueOf(totalCount));
                    recordCountLabel.setText("Showing " + list.size() + " of " + totalCount + " records");
                } catch (Exception ex) {
                    recordCountLabel.setText("Error loading records: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void setStatus(String message, boolean isError) {
        formStatusLabel.setText(message);
        formStatusLabel.setForeground(isError ? Theme.ERROR_COLOR : Theme.SUCCESS_COLOR);
    }

    @Override
    public void refreshUserInfo() {
        super.refreshUserInfo();
        loadData("");
    }

    private JPanel createMetricCard(String title, String defaultValue, String subtitle, Color accentColor, JLabel valueLabel) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 5, getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 22, 16, 20));
        card.setPreferredSize(new Dimension(220, 110));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_SMALL);
        titleLbl.setForeground(Theme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(Theme.TEXT_LIGHT);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(accentColor);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subLbl);

        return card;
    }
}
