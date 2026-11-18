/**
 * Date: 2026-11-18 - Yash
 */
package com.gym.ui.dashboard;

import com.gym.dao.ReceptionistDAO;
import com.gym.model.AttendanceRecord;
import com.gym.model.FeePayment;
import com.gym.model.Role;
import com.gym.model.User;
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

    // Screen identifiers for CardLayout
    private static final String SCREEN_FEE_ENTRY = "SCREEN_FEE_ENTRY";
    private static final String SCREEN_RENEWAL_HISTORY = "SCREEN_RENEWAL_HISTORY";
    private static final String SCREEN_ATTENDANCE = "SCREEN_ATTENDANCE";

    private final CardLayout screenCardLayout = new CardLayout();
    private final JPanel screensContainer = new JPanel(screenCardLayout);

    // Nav Buttons
    private JButton tabFeeEntryBtn;
    private JButton tabHistoryBtn;
    private JButton tabAttendanceBtn;

    // Top Metric Labels
    private JLabel todayCheckinsVal;
    private JLabel totalRevenueVal;
    private JLabel totalRenewalsVal;

    // ==========================================
    // SCREEN 1: VERIFY MEMBER & FEE ENTRY FIELDS
    // ==========================================
    private JTextField verifySearchField;
    private JLabel verifiedGymIdLbl;
    private JLabel verifiedNameLbl;
    private JLabel verifiedEmailLbl;
    private JLabel verifiedPhoneLbl;
    private JLabel verifiedRoleLbl;
    private JPanel memberProfileCard;
    private User currentVerifiedMember;

    private JComboBox<String> planComboBox;
    private JTextField amountField;
    private JComboBox<String> paymentModeComboBox;
    private JLabel renewalPreviewLabel;
    private JLabel feeStatusLabel;
    private JButton submitFeeBtn;

    private final String[] plans = {
            "Monthly Pass (1 Month) - Rs. 1,500",
            "Quarterly Pass (3 Months) - Rs. 4,000",
            "Half-Yearly Pass (6 Months) - Rs. 7,500",
            "Annual Pass (12 Months) - Rs. 13,500"
    };
    private final double[] planPrices = {1500.0, 4000.0, 7500.0, 13500.0};
    private final int[] planMonths = {1, 3, 6, 12};

    // ==========================================
    // SCREEN 2: RENEWAL HISTORY FIELDS
    // ==========================================
    private JTable renewalTable;
    private DefaultTableModel renewalTableModel;
    private JTextField renewalSearchField;
    private JLabel renewalCountLabel;

    // ==========================================
    // SCREEN 3: ATTENDANCE FIELDS
    // ==========================================
    private JTextField attendanceGymIdField;
    private JLabel attendanceStatusLabel;
    private JButton markPresentBtn;
    private JTable attendanceTable;
    private DefaultTableModel attendanceTableModel;
    private JLabel attendanceCountLabel;

    public ReceptionistDashboard(MainFrame mainFrame) {
        super(mainFrame, "Receptionist Workspace", Role.RECEPTIONIST);
        buildDashboard();
        loadAllData();
    }

    private void buildDashboard() {
        contentPanel.setLayout(new BorderLayout(0, 18));

        // 1. Top Metrics Bar
        JPanel metricsContainer = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsContainer.setOpaque(false);

        metricsContainer.add(createMetricCard("TODAY'S CHECK-INS", "0 Members", "Active today", Theme.ROLE_RECEPTIONIST_COLOR, todayCheckinsVal = new JLabel("0")));
        metricsContainer.add(createMetricCard("TOTAL REVENUE COLLECTED", "Rs. 0.00", "All time renewals", Theme.SUCCESS_COLOR, totalRevenueVal = new JLabel("Rs. 0.00")));
        metricsContainer.add(createMetricCard("RENEWALS RECORDED", "0", "Completed transactions", Theme.ROLE_OWNER_COLOR, totalRenewalsVal = new JLabel("0")));
        metricsContainer.add(createMetricCard("DESK OPERATION", "ONLINE", "Ready for check-ins", Theme.ROLE_TRAINER_COLOR, new JLabel("ONLINE")));

        // 2. Navigation Tab Bar (3 Distinct Screens)
        JPanel navTabBar = buildNavigationTabBar();

        // 3. Header Container (Metrics + Tab Bar)
        JPanel topSection = new JPanel(new BorderLayout(0, 15));
        topSection.setOpaque(false);
        topSection.add(metricsContainer, BorderLayout.NORTH);
        topSection.add(navTabBar, BorderLayout.SOUTH);
        contentPanel.add(topSection, BorderLayout.NORTH);

        // 4. Screens Container with CardLayout
        screensContainer.setOpaque(false);
        screensContainer.add(buildScreen1_FeeEntry(), SCREEN_FEE_ENTRY);
        screensContainer.add(buildScreen2_RenewalHistory(), SCREEN_RENEWAL_HISTORY);
        screensContainer.add(buildScreen3_Attendance(), SCREEN_ATTENDANCE);

        contentPanel.add(screensContainer, BorderLayout.CENTER);

        // Default screen
        switchScreen(SCREEN_FEE_ENTRY);
    }

    private JPanel buildNavigationTabBar() {
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        navBar.setOpaque(false);

        tabFeeEntryBtn = createTabButton("1. Member Verification & Fee Payment");
        tabHistoryBtn = createTabButton("2. Renewal & Payment History");
        tabAttendanceBtn = createTabButton("3. Mark Attendance (By Gym ID)");

        tabFeeEntryBtn.addActionListener(e -> switchScreen(SCREEN_FEE_ENTRY));
        tabHistoryBtn.addActionListener(e -> switchScreen(SCREEN_RENEWAL_HISTORY));
        tabAttendanceBtn.addActionListener(e -> switchScreen(SCREEN_ATTENDANCE));

        navBar.add(tabFeeEntryBtn);
        navBar.add(tabHistoryBtn);
        navBar.add(tabAttendanceBtn);

        return navBar;
    }

    private JButton createTabButton(String title) {
        JButton btn = new JButton(title) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(Theme.FONT_BOLD);
        btn.setForeground(Theme.TEXT_LIGHT);
        btn.setBackground(Theme.ACCENT_SECONDARY);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void switchScreen(String screenName) {
        screenCardLayout.show(screensContainer, screenName);

        // Update tab buttons appearance
        tabFeeEntryBtn.setBackground(SCREEN_FEE_ENTRY.equals(screenName) ? Theme.ACCENT_PRIMARY : Theme.ACCENT_SECONDARY);
        tabHistoryBtn.setBackground(SCREEN_RENEWAL_HISTORY.equals(screenName) ? Theme.ACCENT_PRIMARY : Theme.ACCENT_SECONDARY);
        tabAttendanceBtn.setBackground(SCREEN_ATTENDANCE.equals(screenName) ? Theme.ACCENT_PRIMARY : Theme.ACCENT_SECONDARY);

        tabFeeEntryBtn.repaint();
        tabHistoryBtn.repaint();
        tabAttendanceBtn.repaint();

        // Refresh data when switching
        if (SCREEN_RENEWAL_HISTORY.equals(screenName)) {
            loadRenewalData("");
        } else if (SCREEN_ATTENDANCE.equals(screenName)) {
            loadAttendanceData("");
        }
    }

    // =========================================================================
    // SCREEN 1: VERIFY MEMBER & FEE ENTRY
    // =========================================================================
    private JPanel buildScreen1_FeeEntry() {
        JPanel screen = new JPanel(new GridLayout(1, 2, 20, 0));
        screen.setOpaque(false);

        // Left Card: Member Verification & Profile
        JPanel verifyCard = new JPanel();
        verifyCard.setLayout(new BoxLayout(verifyCard, BoxLayout.Y_AXIS));
        verifyCard.setBackground(Theme.CARD_BG);
        verifyCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(22, 24, 22, 24)
        ));

        JLabel title1 = new JLabel("Step 1: Verify Member by Gym ID or Email");
        title1.setFont(Theme.FONT_HEADER);
        title1.setForeground(Theme.TEXT_LIGHT);
        title1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle1 = new JLabel("Lookup member to view details before collecting fee");
        subtitle1.setFont(Theme.FONT_SUBTITLE);
        subtitle1.setForeground(Theme.TEXT_MUTED);
        subtitle1.setAlignmentX(Component.LEFT_ALIGNMENT);

        verifySearchField = Theme.createStyledTextField();
        verifySearchField.setToolTipText("Enter Gym ID / Roll No. (e.g. 20261001) or Email Address");
        verifySearchField.addActionListener(e -> handleMemberVerification());

        JButton verifyBtn = Theme.createPrimaryButton("Verify Member");
        verifyBtn.setPreferredSize(new Dimension(130, 40));
        verifyBtn.addActionListener(e -> handleMemberVerification());

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        searchRow.add(verifySearchField, BorderLayout.CENTER);
        searchRow.add(verifyBtn, BorderLayout.EAST);

        // Profile Display Box
        memberProfileCard = new JPanel();
        memberProfileCard.setLayout(new BoxLayout(memberProfileCard, BoxLayout.Y_AXIS));
        memberProfileCard.setBackground(Theme.INPUT_BG);
        memberProfileCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.INPUT_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        memberProfileCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        memberProfileCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        verifiedGymIdLbl = new JLabel("Gym ID: Not Verified");
        verifiedGymIdLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        verifiedGymIdLbl.setForeground(Theme.ACCENT_PRIMARY);

        verifiedNameLbl = new JLabel("Full Name: -");
        verifiedNameLbl.setFont(Theme.FONT_BODY);
        verifiedNameLbl.setForeground(Theme.TEXT_LIGHT);

        verifiedEmailLbl = new JLabel("Email: -");
        verifiedEmailLbl.setFont(Theme.FONT_BODY);
        verifiedEmailLbl.setForeground(Theme.TEXT_LIGHT);

        verifiedPhoneLbl = new JLabel("Phone: -");
        verifiedPhoneLbl.setFont(Theme.FONT_BODY);
        verifiedPhoneLbl.setForeground(Theme.TEXT_LIGHT);

        verifiedRoleLbl = new JLabel("Account Role: -");
        verifiedRoleLbl.setFont(Theme.FONT_BOLD);
        verifiedRoleLbl.setForeground(Theme.ROLE_MEMBER_COLOR);

        memberProfileCard.add(verifiedGymIdLbl);
        memberProfileCard.add(Box.createVerticalStrut(10));
        memberProfileCard.add(verifiedNameLbl);
        memberProfileCard.add(Box.createVerticalStrut(6));
        memberProfileCard.add(verifiedEmailLbl);
        memberProfileCard.add(Box.createVerticalStrut(6));
        memberProfileCard.add(verifiedPhoneLbl);
        memberProfileCard.add(Box.createVerticalStrut(6));
        memberProfileCard.add(verifiedRoleLbl);

        verifyCard.add(title1);
        verifyCard.add(Box.createVerticalStrut(4));
        verifyCard.add(subtitle1);
        verifyCard.add(Box.createVerticalStrut(18));
        addFormField(verifyCard, "Enter Gym ID or Email:", searchRow);
        verifyCard.add(Box.createVerticalStrut(15));
        verifyCard.add(Theme.createFieldLabel("Verified Member Information:"));
        verifyCard.add(Box.createVerticalStrut(8));
        verifyCard.add(memberProfileCard);

        // Right Card: Fee Payment & Renewal Submission
        JPanel paymentCard = new JPanel();
        paymentCard.setLayout(new BoxLayout(paymentCard, BoxLayout.Y_AXIS));
        paymentCard.setBackground(Theme.CARD_BG);
        paymentCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(22, 24, 22, 24)
        ));

        JLabel title2 = new JLabel("Step 2: Collect Fee & Process Renewal");
        title2.setFont(Theme.FONT_HEADER);
        title2.setForeground(Theme.TEXT_LIGHT);
        title2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle2 = new JLabel("Select plan, mode of payment, and confirm renewal");
        subtitle2.setFont(Theme.FONT_SUBTITLE);
        subtitle2.setForeground(Theme.TEXT_MUTED);
        subtitle2.setAlignmentX(Component.LEFT_ALIGNMENT);

        planComboBox = new JComboBox<>(plans);
        styleComboBox(planComboBox);
        planComboBox.addActionListener(e -> updatePlanCalculations());

        amountField = Theme.createStyledTextField();
        amountField.setText(String.format("%.2f", planPrices[0]));
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        String[] modes = {"Cash", "UPI / QR Code", "Credit/Debit Card", "Bank Transfer"};
        paymentModeComboBox = new JComboBox<>(modes);
        styleComboBox(paymentModeComboBox);

        renewalPreviewLabel = new JLabel("Next Renewal Due: " + LocalDate.now().plusMonths(1));
        renewalPreviewLabel.setFont(Theme.FONT_BOLD);
        renewalPreviewLabel.setForeground(Theme.SUCCESS_COLOR);
        renewalPreviewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        feeStatusLabel = new JLabel(" ");
        feeStatusLabel.setFont(Theme.FONT_SMALL);
        feeStatusLabel.setForeground(Theme.ERROR_COLOR);
        feeStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        submitFeeBtn = Theme.createPrimaryButton("Submit Payment & Extend Membership");
        submitFeeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitFeeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        submitFeeBtn.addActionListener(e -> handleFeeSubmission());

        paymentCard.add(title2);
        paymentCard.add(Box.createVerticalStrut(4));
        paymentCard.add(subtitle2);
        paymentCard.add(Box.createVerticalStrut(18));

        addFormField(paymentCard, "Select Membership Plan", planComboBox);
        addFormField(paymentCard, "Fee Amount (Rs.)", amountField);
        addFormField(paymentCard, "Payment Mode", paymentModeComboBox);

        paymentCard.add(renewalPreviewLabel);
        paymentCard.add(Box.createVerticalStrut(8));
        paymentCard.add(feeStatusLabel);
        paymentCard.add(Box.createVerticalStrut(12));
        paymentCard.add(submitFeeBtn);

        screen.add(verifyCard);
        screen.add(paymentCard);

        return screen;
    }

    private void handleMemberVerification() {
        String query = verifySearchField.getText().trim();
        if (query.isEmpty()) {
            setFeeStatus("Please enter a Gym ID or Email to verify.", true);
            return;
        }

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return receptionistDAO.findMemberByGymIdOrEmail(query);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        currentVerifiedMember = user;
                        verifiedGymIdLbl.setText("Gym ID: " + user.getGymId());
                        verifiedNameLbl.setText("Full Name: " + user.getFullName());
                        verifiedEmailLbl.setText("Email: " + user.getEmail());
                        verifiedPhoneLbl.setText("Phone: " + (user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "Not provided"));
                        verifiedRoleLbl.setText("Account Role: " + user.getRole().getDisplayName());
                        setFeeStatus("Member verified! You can now proceed to payment.", false);
                    } else {
                        currentVerifiedMember = null;
                        verifiedGymIdLbl.setText("Gym ID: Not Found");
                        verifiedNameLbl.setText("Full Name: -");
                        verifiedEmailLbl.setText("Email: -");
                        verifiedPhoneLbl.setText("Phone: -");
                        verifiedRoleLbl.setText("Account Role: -");
                        setFeeStatus("No member found matching: " + query, true);
                    }
                } catch (Exception ex) {
                    setFeeStatus("Error looking up member: " + ex.getMessage(), true);
                }
            }
        };
        worker.execute();
    }

    private void handleFeeSubmission() {
        if (currentVerifiedMember == null) {
            setFeeStatus("Please verify a member first (Step 1) before processing fee.", true);
            return;
        }

        int planIdx = planComboBox.getSelectedIndex();
        String mode = (String) paymentModeComboBox.getSelectedItem();
        String amountText = amountField.getText().trim();

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                setFeeStatus("Amount must be greater than zero.", true);
                return;
            }
        } catch (NumberFormatException ex) {
            setFeeStatus("Please enter a valid numeric amount.", true);
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
                    return receptionistDAO.recordFeePayment(
                            currentVerifiedMember.getGymId(),
                            currentVerifiedMember.getEmail(),
                            currentVerifiedMember.getFullName(),
                            planName,
                            amount,
                            mode,
                            durationMonths
                    );
                } catch (Exception ex) {
                    err = ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                submitFeeBtn.setEnabled(true);
                submitFeeBtn.setText("Submit Payment & Extend Membership");

                try {
                    boolean success = get();
                    if (success) {
                        setFeeStatus("Success! Fee recorded for " + currentVerifiedMember.getFullName() + " (" + currentVerifiedMember.getGymId() + ")", false);
                        loadMetrics();
                    } else {
                        setFeeStatus("Failed: " + (err != null ? err : "Could not record payment."), true);
                    }
                } catch (Exception ex) {
                    setFeeStatus("Error: " + ex.getMessage(), true);
                }
            }
        };
        worker.execute();
    }

    private void setFeeStatus(String msg, boolean isError) {
        feeStatusLabel.setText(msg);
        feeStatusLabel.setForeground(isError ? Theme.ERROR_COLOR : Theme.SUCCESS_COLOR);
    }

    // =========================================================================
    // SCREEN 2: RENEWAL & PAYMENT HISTORY
    // =========================================================================
    private JPanel buildScreen2_RenewalHistory() {
        JPanel screen = new JPanel(new BorderLayout(0, 14));
        screen.setBackground(Theme.CARD_BG);
        screen.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(22, 24, 22, 24)
        ));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Payment & Membership Renewal History");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_LIGHT);

        renewalCountLabel = new JLabel("Loading records...");
        renewalCountLabel.setFont(Theme.FONT_SMALL);
        renewalCountLabel.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(renewalCountLabel);

        // Search Bar
        JPanel searchBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBlock.setOpaque(false);

        renewalSearchField = Theme.createStyledTextField();
        renewalSearchField.setPreferredSize(new Dimension(240, 36));
        renewalSearchField.setToolTipText("Filter by Gym ID, Name, or Email");
        renewalSearchField.addActionListener(e -> loadRenewalData(renewalSearchField.getText().trim()));

        JButton searchBtn = Theme.createSecondaryButton("Search");
        searchBtn.addActionListener(e -> loadRenewalData(renewalSearchField.getText().trim()));

        JButton refreshBtn = Theme.createSecondaryButton("Refresh All");
        refreshBtn.addActionListener(e -> {
            renewalSearchField.setText("");
            loadRenewalData("");
        });

        searchBlock.add(renewalSearchField);
        searchBlock.add(searchBtn);
        searchBlock.add(refreshBtn);

        topBar.add(titleBlock, BorderLayout.WEST);
        topBar.add(searchBlock, BorderLayout.EAST);
        screen.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Payment Date", "Gym ID", "Member Name", "Email", "Plan", "Amount (Rs.)", "Mode", "Renewal Due"};
        renewalTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        renewalTable = new JTable(renewalTableModel);
        styleTable(renewalTable);

        renewalTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        renewalTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        renewalTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        renewalTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        renewalTable.getColumnModel().getColumn(4).setPreferredWidth(160);
        renewalTable.getColumnModel().getColumn(5).setPreferredWidth(160);
        renewalTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        renewalTable.getColumnModel().getColumn(7).setPreferredWidth(90);
        renewalTable.getColumnModel().getColumn(8).setPreferredWidth(100);

        JScrollPane scrollPane = Theme.createModernScrollPane(renewalTable);
        screen.add(scrollPane, BorderLayout.CENTER);

        return screen;
    }

    private void loadRenewalData(String filter) {
        SwingWorker<List<FeePayment>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<FeePayment> doInBackground() throws Exception {
                return receptionistDAO.getRenewalHistory(filter);
            }

            @Override
            protected void done() {
                try {
                    List<FeePayment> list = get();
                    renewalTableModel.setRowCount(0);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                    for (FeePayment fp : list) {
                        String dateStr = fp.getPaymentDate() != null ? sdf.format(fp.getPaymentDate()) : "N/A";
                        renewalTableModel.addRow(new Object[]{
                                fp.getId(),
                                dateStr,
                                fp.getGymId() != null ? fp.getGymId() : "N/A",
                                fp.getMemberName(),
                                fp.getMemberEmail(),
                                fp.getPlanType(),
                                String.format("Rs. %.2f", fp.getAmount()),
                                fp.getPaymentMode(),
                                fp.getRenewalDueDate() != null ? fp.getRenewalDueDate().toString() : "N/A"
                        });
                    }
                    renewalCountLabel.setText("Showing " + list.size() + " recorded transactions");
                } catch (Exception ex) {
                    renewalCountLabel.setText("Error loading renewals: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    // =========================================================================
    // SCREEN 3: ATTENDANCE (MARK PRESENT BY GYM ID)
    // =========================================================================
    private JPanel buildScreen3_Attendance() {
        JPanel screen = new JPanel(new BorderLayout(0, 16));
        screen.setOpaque(false);

        // Top Action Card: Mark Attendance
        JPanel checkInCard = new JPanel();
        checkInCard.setLayout(new BoxLayout(checkInCard, BoxLayout.Y_AXIS));
        checkInCard.setBackground(Theme.CARD_BG);
        checkInCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        JLabel title = new JLabel("Instant Member Attendance Desk");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_LIGHT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Enter Member Gym ID to mark present for today's workout");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        attendanceGymIdField = Theme.createStyledTextField();
        attendanceGymIdField.setToolTipText("Enter Gym ID / Roll No. (e.g. 20261001) and press Enter");
        attendanceGymIdField.addActionListener(e -> handleMarkAttendance());

        markPresentBtn = Theme.createPrimaryButton("Mark Present");
        markPresentBtn.setPreferredSize(new Dimension(140, 42));
        markPresentBtn.addActionListener(e -> handleMarkAttendance());

        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.setOpaque(false);
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        inputRow.add(attendanceGymIdField, BorderLayout.CENTER);
        inputRow.add(markPresentBtn, BorderLayout.EAST);

        attendanceStatusLabel = new JLabel(" ");
        attendanceStatusLabel.setFont(Theme.FONT_BOLD);
        attendanceStatusLabel.setForeground(Theme.SUCCESS_COLOR);
        attendanceStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        checkInCard.add(title);
        checkInCard.add(Box.createVerticalStrut(4));
        checkInCard.add(subtitle);
        checkInCard.add(Box.createVerticalStrut(14));
        addFormField(checkInCard, "Enter Member Gym ID (or Registered Email):", inputRow);
        checkInCard.add(Box.createVerticalStrut(6));
        checkInCard.add(attendanceStatusLabel);

        // Bottom Table Card: Attendance History
        JPanel logCard = new JPanel(new BorderLayout(0, 10));
        logCard.setBackground(Theme.CARD_BG);
        logCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel logTopBar = new JPanel(new BorderLayout());
        logTopBar.setOpaque(false);

        JLabel logTitle = new JLabel("Today's Live Check-in Log");
        logTitle.setFont(Theme.FONT_HEADER);
        logTitle.setForeground(Theme.TEXT_LIGHT);

        attendanceCountLabel = new JLabel("Loading today's check-ins...");
        attendanceCountLabel.setFont(Theme.FONT_SMALL);
        attendanceCountLabel.setForeground(Theme.TEXT_MUTED);

        logTopBar.add(logTitle, BorderLayout.WEST);
        logTopBar.add(attendanceCountLabel, BorderLayout.EAST);
        logCard.add(logTopBar, BorderLayout.NORTH);

        String[] cols = {"Entry ID", "Check-in Timestamp", "Gym ID", "Member Name", "Email Address", "Status"};
        attendanceTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        attendanceTable = new JTable(attendanceTableModel);
        styleTable(attendanceTable);

        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        attendanceTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        attendanceTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        attendanceTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        attendanceTable.getColumnModel().getColumn(5).setPreferredWidth(90);

        JScrollPane scrollPane = Theme.createModernScrollPane(attendanceTable);
        logCard.add(scrollPane, BorderLayout.CENTER);

        screen.add(checkInCard, BorderLayout.NORTH);
        screen.add(logCard, BorderLayout.CENTER);

        return screen;
    }

    private void handleMarkAttendance() {
        String gymId = attendanceGymIdField.getText().trim();
        if (gymId.isEmpty()) {
            attendanceStatusLabel.setText("Please enter a Gym ID.");
            attendanceStatusLabel.setForeground(Theme.ERROR_COLOR);
            return;
        }

        markPresentBtn.setEnabled(false);
        markPresentBtn.setText("Checking in...");

        SwingWorker<AttendanceRecord, Void> worker = new SwingWorker<>() {
            private String error = null;

            @Override
            protected AttendanceRecord doInBackground() {
                try {
                    return receptionistDAO.recordAttendanceByGymId(gymId);
                } catch (Exception ex) {
                    error = ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                markPresentBtn.setEnabled(true);
                markPresentBtn.setText("Mark Present");

                try {
                    AttendanceRecord rec = get();
                    if (rec != null) {
                        attendanceStatusLabel.setText("SUCCESS: " + rec.getMemberName() + " (" + rec.getGymId() + ") marked Present!");
                        attendanceStatusLabel.setForeground(Theme.SUCCESS_COLOR);
                        attendanceGymIdField.setText("");
                        loadAttendanceData("");
                        loadMetrics();
                    } else {
                        attendanceStatusLabel.setText(error != null ? error : "Could not record attendance.");
                        attendanceStatusLabel.setForeground(Theme.ERROR_COLOR);
                    }
                } catch (Exception ex) {
                    attendanceStatusLabel.setText("Error: " + ex.getMessage());
                    attendanceStatusLabel.setForeground(Theme.ERROR_COLOR);
                }
            }
        };
        worker.execute();
    }

    private void loadAttendanceData(String filter) {
        SwingWorker<List<AttendanceRecord>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AttendanceRecord> doInBackground() throws Exception {
                return receptionistDAO.getAttendanceHistory(filter);
            }

            @Override
            protected void done() {
                try {
                    List<AttendanceRecord> list = get();
                    attendanceTableModel.setRowCount(0);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    for (AttendanceRecord ar : list) {
                        String timeStr = ar.getCheckInTime() != null ? sdf.format(ar.getCheckInTime()) : "N/A";
                        attendanceTableModel.addRow(new Object[]{
                                ar.getId(),
                                timeStr,
                                ar.getGymId(),
                                ar.getMemberName(),
                                ar.getMemberEmail(),
                                "PRESENT"
                        });
                    }
                    attendanceCountLabel.setText("Total Check-ins: " + list.size());
                } catch (Exception ex) {
                    attendanceCountLabel.setText("Error loading attendance: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    // =========================================================================
    // SHARED UTILITIES & METRICS
    // =========================================================================
    private void loadAllData() {
        loadMetrics();
        loadRenewalData("");
        loadAttendanceData("");
    }

    private void loadMetrics() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private int checkins = 0;
            private double revenue = 0;
            private int renewals = 0;

            @Override
            protected Void doInBackground() throws Exception {
                checkins = receptionistDAO.getTodayAttendanceCount();
                revenue = receptionistDAO.getTotalRevenue();
                renewals = receptionistDAO.getTotalRenewalsCount();
                return null;
            }

            @Override
            protected void done() {
                todayCheckinsVal.setText(checkins + " Members");
                totalRevenueVal.setText(String.format("Rs. %.2f", revenue));
                totalRenewalsVal.setText(String.valueOf(renewals));
            }
        };
        worker.execute();
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

    @Override
    public void refreshUserInfo() {
        super.refreshUserInfo();
        loadAllData();
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
