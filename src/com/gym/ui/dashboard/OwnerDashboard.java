/**
 * Date: 2026-11-24
 * Syllabus: Unit 1 (Lectures 1-3) - Custom Exceptions & Input Validation
 *           Unit 2 (Lectures 16-25) - Collections Framework, Comparator & Record Sorting
 *           Unit 3 (Lectures 26-31) - Multithreading, SwingWorker & Daemon Thread
 *           Unit 4 (Lectures 34-42) - Advanced JDBC, PreparedStatement & Database Aggregations
 *           Unit 5 (Lectures 43-48) - Stream API, Lambdas & Aggregations
 *           Unit 6 (Lectures 49-54) - Character Streams, File Handling & CSV/Text Report Export
 */
package com.gym.ui.dashboard;

import com.gym.dao.OwnerDAO;
import com.gym.exception.GymException;
import com.gym.model.InactiveMember;
import com.gym.model.MonthlyStat;
import com.gym.model.Role;
import com.gym.model.User;
import com.gym.ui.MainFrame;
import com.gym.ui.Theme;
import com.gym.ui.ThemedDialog;
import com.gym.util.LiveDeskTracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.Comparator;
import java.util.List;

public class OwnerDashboard extends BaseDashboard {

    private final OwnerDAO ownerDAO = new OwnerDAO();

    // Screen identifiers for CardLayout
    private static final String SCREEN_MONTHLY_STATS = "SCREEN_MONTHLY_STATS";
    private static final String SCREEN_NON_ACTIVE_MEMBERS = "SCREEN_NON_ACTIVE_MEMBERS";
    private static final String SCREEN_REPORTS = "SCREEN_REPORTS";
    private static final String SCREEN_STAFF = "SCREEN_STAFF";

    private final CardLayout screenCardLayout = new CardLayout();
    private final JPanel screensContainer = new JPanel(screenCardLayout);

    // Nav Buttons
    private JButton tabMonthlyStatsBtn;
    private JButton tabNonActiveBtn;
    private JButton tabReportsBtn;
    private JButton tabStaffBtn;

    // Top Metric Labels
    private JLabel totalRevenueVal;
    private JLabel activeMembersVal;
    private JLabel nonActiveMembersVal;
    private JLabel staffCountVal;
    private JLabel newSignupsVal;
    private LiveDeskTracker liveDeskTracker;

    // ==========================================
    // SCREEN 1: MONTHLY STATS FIELDS
    // ==========================================
    private JTable monthlyStatsTable;
    private DefaultTableModel monthlyStatsTableModel;
    private JLabel monthlySummaryLabel;
    private List<MonthlyStat> currentLoadedStats;

    // ==========================================
    // SCREEN 2: NON-ACTIVE MEMBERS FIELDS
    // ==========================================
    private JTable inactiveTable;
    private DefaultTableModel inactiveTableModel;
    private JComboBox<String> inactiveReasonFilterCombo;
    private JTextField inactiveSearchField;
    private JLabel inactiveCountLabel;
    private List<InactiveMember> currentLoadedInactive;

    // ==========================================
    // SCREEN 3: REGULAR REPORTS FIELDS
    // ==========================================
    private JComboBox<String> reportTypeCombo;
    private JTextArea reportPreviewArea;
    private JButton generateReportBtn;
    private JButton exportReportBtn;
    private String currentGeneratedReportText = "";

    // ==========================================
    // SCREEN 4: STAFF MANAGEMENT FIELDS
    // ==========================================
    private JTable staffTable;
    private DefaultTableModel staffTableModel;
    private JLabel staffCountLabel;
    private List<User> currentLoadedStaff;

    public OwnerDashboard(MainFrame mainFrame) {
        super(mainFrame, "Gym Owner Executive Workspace", Role.OWNER);
        buildDashboard();
        startLiveTracker();
        loadAllData();
    }

    private void buildDashboard() {
        contentPanel.setLayout(new BorderLayout(0, 18));

        // 1. Top Live Dynamic Metrics Bar (5 Metrics)
        JPanel metricsContainer = new JPanel(new GridLayout(1, 5, 12, 0));
        metricsContainer.setOpaque(false);

        metricsContainer.add(createMetricCard("TOTAL REVENUE", "Rs. 0.00", "All time fee collections",
                Theme.ROLE_OWNER_COLOR, totalRevenueVal = new JLabel("Rs. 0.00")));
        metricsContainer.add(createMetricCard("ACTIVE MEMBERS", "0 Active", "Valid membership plans",
                Theme.ROLE_MEMBER_COLOR, activeMembersVal = new JLabel("0 Active")));
        metricsContainer.add(createMetricCard("NON-ACTIVE MEMBERS", "0 Inactive", "Expired / Need follow-up",
                Theme.ERROR_COLOR, nonActiveMembersVal = new JLabel("0 Inactive")));
        metricsContainer.add(createMetricCard("STAFF ON ROSTER", "0 Personnel", "Trainers & Receptionists",
                Theme.ROLE_TRAINER_COLOR, staffCountVal = new JLabel("0 Staff")));
        metricsContainer.add(createMetricCard("NEW SIGNUPS", "0 Members", "Joined this month", Theme.SUCCESS_COLOR,
                newSignupsVal = new JLabel("0 Members")));

        // 2. Navigation Tab Bar (4 Dedicated Tabs)
        JPanel navTabBar = buildNavigationTabBar();

        // 3. Header Container
        JPanel topSection = new JPanel(new BorderLayout(0, 14));
        topSection.setOpaque(false);
        topSection.add(metricsContainer, BorderLayout.NORTH);
        topSection.add(navTabBar, BorderLayout.SOUTH);
        contentPanel.add(topSection, BorderLayout.NORTH);

        // 4. Screens Container with CardLayout
        screensContainer.setOpaque(false);
        screensContainer.add(buildScreen1_MonthlyStats(), SCREEN_MONTHLY_STATS);
        screensContainer.add(buildScreen2_NonActiveMembers(), SCREEN_NON_ACTIVE_MEMBERS);
        screensContainer.add(buildScreen3_RegularReports(), SCREEN_REPORTS);
        screensContainer.add(buildScreen4_StaffManagement(), SCREEN_STAFF);

        contentPanel.add(screensContainer, BorderLayout.CENTER);

        // Default screen: Monthly Stats
        switchScreen(SCREEN_MONTHLY_STATS);
    }

    private void startLiveTracker() {
        liveDeskTracker = new LiveDeskTracker(null, null);
        liveDeskTracker.start();
    }

    private JPanel buildNavigationTabBar() {
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        navBar.setOpaque(false);

        tabMonthlyStatsBtn = createTabButton("1. Monthly Statistics & Revenue");
        tabNonActiveBtn = createTabButton("2. Non-Active & Expired Members");
        tabReportsBtn = createTabButton("3. Regular Executive Reports");
        tabStaffBtn = createTabButton("4. Staff Personnel & Hiring");

        tabMonthlyStatsBtn.addActionListener(e -> switchScreen(SCREEN_MONTHLY_STATS));
        tabNonActiveBtn.addActionListener(e -> switchScreen(SCREEN_NON_ACTIVE_MEMBERS));
        tabReportsBtn.addActionListener(e -> switchScreen(SCREEN_REPORTS));
        tabStaffBtn.addActionListener(e -> switchScreen(SCREEN_STAFF));

        navBar.add(tabMonthlyStatsBtn);
        navBar.add(tabNonActiveBtn);
        navBar.add(tabReportsBtn);
        navBar.add(tabStaffBtn);

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

        tabMonthlyStatsBtn
                .setBackground(SCREEN_MONTHLY_STATS.equals(screenName) ? Theme.ACCENT_PRIMARY : Theme.ACCENT_SECONDARY);
        tabNonActiveBtn.setBackground(
                SCREEN_NON_ACTIVE_MEMBERS.equals(screenName) ? Theme.ACCENT_PRIMARY : Theme.ACCENT_SECONDARY);
        tabReportsBtn.setBackground(SCREEN_REPORTS.equals(screenName) ? Theme.ACCENT_PRIMARY : Theme.ACCENT_SECONDARY);
        tabStaffBtn.setBackground(SCREEN_STAFF.equals(screenName) ? Theme.ACCENT_PRIMARY : Theme.ACCENT_SECONDARY);

        tabMonthlyStatsBtn.repaint();
        tabNonActiveBtn.repaint();
        tabReportsBtn.repaint();
        tabStaffBtn.repaint();

        if (SCREEN_MONTHLY_STATS.equals(screenName)) {
            loadMonthlyStatsData();
        } else if (SCREEN_NON_ACTIVE_MEMBERS.equals(screenName)) {
            loadInactiveMembersData();
        } else if (SCREEN_REPORTS.equals(screenName)) {
            loadReportPreview();
        } else if (SCREEN_STAFF.equals(screenName)) {
            loadStaffData();
        }
    }

    // =========================================================================
    // SCREEN 1: MONTHLY STATISTICS (month stads)
    // =========================================================================
    private JPanel buildScreen1_MonthlyStats() {
        JPanel screen = new JPanel(new BorderLayout(0, 14));
        screen.setBackground(Theme.CARD_BG);
        screen.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(18, 22, 18, 22)));

        // Top Controls
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Monthly Performance & Revenue Breakdown");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_LIGHT);

        monthlySummaryLabel = new JLabel("Loading monthly performance statistics...");
        monthlySummaryLabel.setFont(Theme.FONT_SMALL);
        monthlySummaryLabel.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(monthlySummaryLabel);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        JButton refreshBtn = Theme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadMonthlyStatsData());

        JButton exportCsvBtn = Theme.createSecondaryButton("Export Monthly CSV");
        exportCsvBtn.setBackground(new Color(22, 101, 52));
        exportCsvBtn.addActionListener(e -> handleExportMonthlyCSV());

        controls.add(refreshBtn);
        controls.add(exportCsvBtn);

        topBar.add(titleBlock, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);
        screen.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = { "Month & Year", "Total Revenue (Rs.)", "Transactions", "New Signups", "Check-ins Logged",
                "Average Ticket (Rs.)", "Top Selling Plan" };
        monthlyStatsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        monthlyStatsTable = new JTable(monthlyStatsTableModel);
        styleTable(monthlyStatsTable);

        monthlyStatsTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        monthlyStatsTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        monthlyStatsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        monthlyStatsTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        monthlyStatsTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        monthlyStatsTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        monthlyStatsTable.getColumnModel().getColumn(6).setPreferredWidth(160);

        JScrollPane scrollPane = Theme.createModernScrollPane(monthlyStatsTable);
        screen.add(scrollPane, BorderLayout.CENTER);

        return screen;
    }

    private void loadMonthlyStatsData() {
        SwingWorker<List<MonthlyStat>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MonthlyStat> doInBackground() {
                return ownerDAO.getMonthlyStats();
            }

            @Override
            protected void done() {
                try {
                    currentLoadedStats = get();
                    monthlyStatsTableModel.setRowCount(0);

                    double grandTotal = 0.0;
                    int totalTx = 0;

                    for (MonthlyStat st : currentLoadedStats) {
                        grandTotal += st.getTotalRevenue();
                        totalTx += st.getTransactionCount();
                        monthlyStatsTableModel.addRow(new Object[] {
                                st.getMonthName(),
                                String.format("Rs. %,.2f", st.getTotalRevenue()),
                                st.getTransactionCount(),
                                st.getNewSignups() + " members",
                                st.getCheckinCount() + " visits",
                                String.format("Rs. %,.2f", st.getAverageTicket()),
                                st.getTopPlan()
                        });
                    }

                    monthlySummaryLabel.setText(String.format(
                            "Recorded %d months | Total Cumulative Revenue: Rs. %,.2f across %d transactions",
                            currentLoadedStats.size(), grandTotal, totalTx));
                } catch (Exception ex) {
                    monthlySummaryLabel.setText("Error loading monthly statistics: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleExportMonthlyCSV() {
        if (currentLoadedStats == null || currentLoadedStats.isEmpty()) {
            ThemedDialog.showInfo(this, "Export Notice", "No monthly statistics loaded to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("gym_monthly_revenue_stats.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try {
                OwnerDAO.exportMonthlyStatsToCSV(currentLoadedStats, target);
                ThemedDialog.showSuccess(this, "Export Succeeded",
                        "Monthly stats CSV saved to:\n" + target.getAbsolutePath());
            } catch (GymException ex) {
                ThemedDialog.showError(this, "Export Error", ex.getMessage());
            }
        }
    }

    // =========================================================================
    // SCREEN 2: NON-ACTIVE MEMBERS (non active members)
    // =========================================================================
    private JPanel buildScreen2_NonActiveMembers() {
        JPanel screen = new JPanel(new BorderLayout(0, 14));
        screen.setBackground(Theme.CARD_BG);
        screen.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(18, 22, 18, 22)));

        // Top Controls
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Non-Active, Expired & At-Risk Members");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_LIGHT);

        inactiveCountLabel = new JLabel("Loading inactive members roster...");
        inactiveCountLabel.setFont(Theme.FONT_SMALL);
        inactiveCountLabel.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(inactiveCountLabel);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        String[] reasons = { "All Inactive", "Expired Membership", "No Plan Chosen", "Dormant (30+ Days)" };
        inactiveReasonFilterCombo = new JComboBox<>(reasons);
        styleComboBox(inactiveReasonFilterCombo);
        inactiveReasonFilterCombo.setPreferredSize(new Dimension(170, 36));
        inactiveReasonFilterCombo.addActionListener(e -> loadInactiveMembersData());

        inactiveSearchField = Theme.createStyledTextField();
        inactiveSearchField.setPreferredSize(new Dimension(170, 36));
        inactiveSearchField.setToolTipText("Filter by Gym ID, Name, or Email");
        inactiveSearchField.addActionListener(e -> loadInactiveMembersData());

        JButton searchBtn = Theme.createSecondaryButton("Search");
        searchBtn.addActionListener(e -> loadInactiveMembersData());

        JButton refreshBtn = Theme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> {
            inactiveSearchField.setText("");
            inactiveReasonFilterCombo.setSelectedIndex(0);
            loadInactiveMembersData();
        });

        JButton exportCsvBtn = Theme.createSecondaryButton("Export CSV");
        exportCsvBtn.setBackground(new Color(22, 101, 52));
        exportCsvBtn.addActionListener(e -> handleExportInactiveCSV());

        controls.add(inactiveReasonFilterCombo);
        controls.add(inactiveSearchField);
        controls.add(searchBtn);
        controls.add(refreshBtn);
        controls.add(exportCsvBtn);

        topBar.add(titleBlock, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);
        screen.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = { "Gym ID", "Member Name", "Email", "Phone", "Last Plan", "Expiry Date", "Days Inactive",
                "Last Check-in", "Reason", "Follow-up Status" };
        inactiveTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inactiveTable = new JTable(inactiveTableModel);
        styleTable(inactiveTable);

        inactiveTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        inactiveTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        inactiveTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        inactiveTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        inactiveTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        inactiveTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        inactiveTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        inactiveTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        inactiveTable.getColumnModel().getColumn(8).setPreferredWidth(140);
        inactiveTable.getColumnModel().getColumn(9).setPreferredWidth(110);

        JScrollPane scrollPane = Theme.createModernScrollPane(inactiveTable);
        screen.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar: Send Reminder / Follow-up
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        JButton reminderBtn = Theme.createPrimaryButton("Send Renewal Reminder / Mark Followed-Up");
        reminderBtn.addActionListener(e -> handleSendRenewalReminder());

        bottomBar.add(reminderBtn);
        screen.add(bottomBar, BorderLayout.SOUTH);

        return screen;
    }

    private void loadInactiveMembersData() {
        String filter = (String) inactiveReasonFilterCombo.getSelectedItem();
        String keyword = inactiveSearchField.getText().trim();

        SwingWorker<List<InactiveMember>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<InactiveMember> doInBackground() {
                return ownerDAO.getInactiveMembers(filter, keyword);
            }

            @Override
            protected void done() {
                try {
                    currentLoadedInactive = get();
                    inactiveTableModel.setRowCount(0);

                    for (InactiveMember im : currentLoadedInactive) {
                        inactiveTableModel.addRow(new Object[] {
                                im.getGymId(),
                                im.getFullName(),
                                im.getEmail(),
                                im.getPhone(),
                                im.getMembershipPlan(),
                                im.getExpiryDate() != null ? im.getExpiryDate().toString() : "None",
                                im.getDaysInactive() + " days",
                                im.getLastCheckInDate(),
                                im.getReason(),
                                im.getFollowUpStatus()
                        });
                    }

                    inactiveCountLabel.setText(
                            "Showing " + currentLoadedInactive.size() + " non-active members requiring attention");
                } catch (Exception ex) {
                    inactiveCountLabel.setText("Error loading inactive members: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleSendRenewalReminder() {
        int selected = inactiveTable.getSelectedRow();
        if (selected < 0 || currentLoadedInactive == null || selected >= currentLoadedInactive.size()) {
            ThemedDialog.showInfo(this, "Select Member",
                    "Please select an inactive member row from the table to send a renewal reminder.");
            return;
        }

        InactiveMember im = currentLoadedInactive.get(selected);
        boolean confirm = ThemedDialog.showConfirm(
                this,
                "Send Renewal Reminder",
                "Contact Member for Renewal?",
                "Member: " + im.getFullName() + " (" + im.getGymId() + ")\n"
                        + "Email: " + im.getEmail() + "\n"
                        + "Last Plan: " + im.getMembershipPlan() + " (" + im.getDaysInactive() + " days inactive)\n\n"
                        + "Dispatch personalized renewal incentive (10% discount reminder)?",
                "Send Reminder",
                "Cancel");

        if (confirm) {
            ownerDAO.updateMemberFollowUpStatus(im.getEmail(), "Reminder Sent");
            ThemedDialog.showSuccess(this, "Reminder Dispatched",
                    "Renewal reminder and discount offer dispatched to " + im.getFullName() + " (" + im.getEmail()
                            + ").\nStatus updated to 'Reminder Sent'.");
            loadInactiveMembersData();
        }
    }

    private void handleExportInactiveCSV() {
        if (currentLoadedInactive == null || currentLoadedInactive.isEmpty()) {
            ThemedDialog.showInfo(this, "Export Notice", "No inactive member records to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("gym_inactive_members_retention.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try {
                OwnerDAO.exportInactiveMembersToCSV(currentLoadedInactive, target);
                ThemedDialog.showSuccess(this, "Export Succeeded",
                        "Inactive members report saved to:\n" + target.getAbsolutePath());
            } catch (GymException ex) {
                ThemedDialog.showError(this, "Export Error", ex.getMessage());
            }
        }
    }

    // =========================================================================
    // SCREEN 3: REGULAR EXECUTIVE REPORTS (regular reports)
    // =========================================================================
    private JPanel buildScreen3_RegularReports() {
        JPanel screen = new JPanel(new BorderLayout(0, 14));
        screen.setBackground(Theme.CARD_BG);
        screen.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(18, 22, 18, 22)));

        // Top Controls
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Executive Auditing & Regular Reports Generator");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_LIGHT);

        JLabel subLabel = new JLabel(
                "Select an audit report to generate live metrics, preview data, and export to text or CSV file.");
        subLabel.setFont(Theme.FONT_SUBTITLE);
        subLabel.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subLabel);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        String[] reportTypes = {
                "Executive Financial & Revenue Audit",
                "Non-Active & At-Risk Members Retention Report"
        };
        reportTypeCombo = new JComboBox<>(reportTypes);
        styleComboBox(reportTypeCombo);
        reportTypeCombo.setPreferredSize(new Dimension(320, 36));
        reportTypeCombo.addActionListener(e -> loadReportPreview());

        generateReportBtn = Theme.createPrimaryButton("Generate Live Report");
        generateReportBtn.addActionListener(e -> loadReportPreview());

        exportReportBtn = Theme.createSecondaryButton("Export Report (.txt)");
        exportReportBtn.addActionListener(e -> handleExportReportText());

        controls.add(reportTypeCombo);
        controls.add(generateReportBtn);
        controls.add(exportReportBtn);

        topBar.add(titleBlock, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);
        screen.add(topBar, BorderLayout.NORTH);

        // Center Preview Area
        reportPreviewArea = new JTextArea();
        reportPreviewArea.setBackground(Theme.INPUT_BG);
        reportPreviewArea.setForeground(new Color(230, 235, 245));
        reportPreviewArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        reportPreviewArea.setEditable(false);
        reportPreviewArea.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JScrollPane scrollPane = Theme.createModernScrollPane(reportPreviewArea);
        screen.add(scrollPane, BorderLayout.CENTER);

        return screen;
    }

    private void loadReportPreview() {
        int idx = reportTypeCombo != null ? reportTypeCombo.getSelectedIndex() : 0;
        if (idx == 0) {
            currentGeneratedReportText = ownerDAO.generateFinancialReportText();
        } else {
            currentGeneratedReportText = ownerDAO.generateInactiveMembersReportText();
        }
        if (reportPreviewArea != null) {
            reportPreviewArea.setText(currentGeneratedReportText);
            reportPreviewArea.setCaretPosition(0);
        }
    }

    private void handleExportReportText() {
        if (currentGeneratedReportText == null || currentGeneratedReportText.trim().isEmpty()) {
            loadReportPreview();
        }

        JFileChooser chooser = new JFileChooser();
        int idx = reportTypeCombo.getSelectedIndex();
        String filename = idx == 0 ? "executive_financial_report.txt" : "inactive_members_retention_report.txt";
        chooser.setSelectedFile(new File(filename));

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try {
                OwnerDAO.exportReportToFile(currentGeneratedReportText, target);
                ThemedDialog.showSuccess(this, "Report Exported",
                        "Report file saved successfully to:\n" + target.getAbsolutePath());
            } catch (GymException ex) {
                ThemedDialog.showError(this, "Export Error", ex.getMessage());
            }
        }
    }

    // =========================================================================
    // SCREEN 4: STAFF MANAGEMENT (Real staff roster + Hiring dialog)
    // =========================================================================
    private JPanel buildScreen4_StaffManagement() {
        JPanel screen = new JPanel(new BorderLayout(0, 14));
        screen.setBackground(Theme.CARD_BG);
        screen.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(18, 22, 18, 22)));

        // Top Controls
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Gym Personnel Roster & Staff Management");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_LIGHT);

        staffCountLabel = new JLabel("Loading staff personnel roster...");
        staffCountLabel.setFont(Theme.FONT_SMALL);
        staffCountLabel.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(staffCountLabel);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        JButton addStaffBtn = Theme.createPrimaryButton("Hire / Register New Staff Member");
        addStaffBtn.addActionListener(e -> handleRegisterNewStaffDialog());

        JButton refreshBtn = Theme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadStaffData());

        controls.add(refreshBtn);
        controls.add(addStaffBtn);

        topBar.add(titleBlock, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);
        screen.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = { "Staff ID / Gym ID", "Employee Name", "Role / Position", "Email Address", "Phone Number",
                "Employment Status" };
        staffTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        staffTable = new JTable(staffTableModel);
        styleTable(staffTable);

        staffTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        staffTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        staffTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        staffTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        staffTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        staffTable.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane scrollPane = Theme.createModernScrollPane(staffTable);
        screen.add(scrollPane, BorderLayout.CENTER);

        return screen;
    }

    private void loadStaffData() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() {
                return ownerDAO.getStaffMembers();
            }

            @Override
            protected void done() {
                try {
                    currentLoadedStaff = get();
                    staffTableModel.setRowCount(0);

                    for (User u : currentLoadedStaff) {
                        staffTableModel.addRow(new Object[] {
                                u.getGymId() != null ? u.getGymId() : "N/A",
                                u.getFullName(),
                                u.getRole() != null ? u.getRole().getDisplayName() : "Staff",
                                u.getEmail(),
                                u.getPhone() != null && !u.getPhone().isEmpty() ? u.getPhone() : "--",
                                "Active (On Roster)"
                        });
                    }

                    staffCountLabel.setText(
                            "Active Personnel: " + currentLoadedStaff.size() + " employees on active gym duty");
                } catch (Exception ex) {
                    staffCountLabel.setText("Error loading staff: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleRegisterNewStaffDialog() {
        JTextField nameField = Theme.createStyledTextField();
        JTextField emailField = Theme.createStyledTextField();
        JTextField phoneField = Theme.createStyledTextField();
        JPasswordField passField = Theme.createStyledPasswordField();

        Role[] staffRoles = { Role.TRAINER, Role.RECEPTIONIST };
        JComboBox<Role> roleCombo = new JComboBox<>(staffRoles);
        styleComboBox(roleCombo);

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 10));
        panel.setBackground(Theme.CARD_BG);
        panel.add(Theme.createFieldLabel("Full Name:"));
        panel.add(nameField);
        panel.add(Theme.createFieldLabel("Staff Email:"));
        panel.add(emailField);
        panel.add(Theme.createFieldLabel("Phone Number:"));
        panel.add(phoneField);
        panel.add(Theme.createFieldLabel("Password:"));
        panel.add(passField);
        panel.add(Theme.createFieldLabel("Assigned Role:"));
        panel.add(roleCombo);

        int res = JOptionPane.showConfirmDialog(this, panel, "Hire / Register New Staff Member",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            Role role = (Role) roleCombo.getSelectedItem();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                ThemedDialog.showError(this, "Validation Error", "Name, Email, and Temporary Password are required.");
                return;
            }

            try {
                boolean success = ownerDAO.registerStaff(name, email, phone, password, role);
                if (success) {
                    ThemedDialog.showSuccess(this, "Staff Registered",
                            "New staff member " + name + " (" + role.getDisplayName()
                                    + ") has been successfully hired and added to the roster.");
                    loadAllData();
                } else {
                    ThemedDialog.showError(this, "Registration Failed", "Email may already be registered.");
                }
            } catch (GymException ex) {
                ThemedDialog.showError(this, "Registration Error", ex.getMessage());
            }
        }
    }

    // =========================================================================
    // SHARED UTILITIES & METRICS (Live DB query + Dynamic Refresh)
    // =========================================================================
    private void loadAllData() {
        loadMetrics();
        loadMonthlyStatsData();
        loadInactiveMembersData();
        loadReportPreview();
        loadStaffData();
    }

    private void loadMetrics() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private double totalRevenue = 0.0;
            private int activeMembers = 0;
            private int inactiveMembers = 0;
            private int staffCount = 0;
            private int newSignups = 0;

            @Override
            protected Void doInBackground() {
                totalRevenue = ownerDAO.getTotalRevenue();
                activeMembers = ownerDAO.getActiveMembersCount();
                inactiveMembers = ownerDAO.getInactiveMembersCount();
                staffCount = ownerDAO.getStaffCount();
                newSignups = ownerDAO.getNewSignupsThisMonth();
                return null;
            }

            @Override
            protected void done() {
                totalRevenueVal.setText(String.format("Rs. %,.0f", totalRevenue));
                activeMembersVal.setText(activeMembers + " Active");
                nonActiveMembersVal.setText(inactiveMembers + " Inactive");
                staffCountVal.setText(staffCount + " Staff");
                newSignupsVal.setText(newSignups + " Members");
            }
        };
        worker.execute();
    }

    private JPanel createMetricCard(String title, String defaultValue, String subtitle, Color accentColor,
            JLabel valueLabel) {
        JLabel subLbl = new JLabel(subtitle);
        return createMetricCard(title, defaultValue, subtitle, accentColor, valueLabel, subLbl);
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setBackground(Theme.INPUT_BG);
        box.setForeground(Theme.TEXT_LIGHT);
        box.setFont(Theme.FONT_BODY);
        box.setPreferredSize(new Dimension(box.getPreferredSize().width, 38));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        box.setBorder(Theme.createFieldBorder(Theme.INPUT_BORDER));
    }

    @Override
    public void refreshUserInfo() {
        super.refreshUserInfo();
        loadAllData();
    }
}
