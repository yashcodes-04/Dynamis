/**
 * Date: 2026-11-21
 */
package com.gym.ui.dashboard;

import com.gym.dao.BookingDAO;
import com.gym.dao.ReceptionistDAO;
import com.gym.dao.UserDAO;
import com.gym.model.BookedSession;
import com.gym.model.FeePayment;
import com.gym.model.Role;
import com.gym.model.User;
import com.gym.session.UserSession;
import com.gym.ui.BookClassDialog;
import com.gym.ui.MainFrame;
import com.gym.ui.Theme;
import com.gym.ui.ThemedDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class MemberDashboard extends BaseDashboard {

    private JLabel membershipStatusVal;
    private JLabel membershipStatusSub;
    private JLabel membershipDaysVal;
    private JLabel membershipDaysSub;
    private JLabel nextClassVal;
    private JLabel nextClassSub;

    private JLabel bookedBadge;
    private DefaultTableModel bookedTableModel;
    private JTable bookedTable;
    private CardLayout bookedCardLayout;
    private JPanel bookedCardContainer;
    private JButton cancelBookingBtn;

    private JLabel summaryMembershipExpiry;
    private JLabel summaryBookedCount;
    private JLabel summaryTotalSpend;
    private JLabel summaryNextSession;

    private final BookingDAO bookingDAO = new BookingDAO();
    private List<BookedSession> currentBookings = new ArrayList<>();

    public MemberDashboard(MainFrame mainFrame) {
        super(mainFrame, "Member Workspace", Role.MEMBER);
        buildDashboard();
        loadBookedSessions();
    }

    private void buildDashboard() {
        contentPanel.setLayout(new BorderLayout(0, 20));

        // Top Metrics (1 Row, 5 Columns)
        JPanel metricsPanel = new JPanel(new GridLayout(1, 5, 12, 0));
        metricsPanel.setOpaque(false);

        membershipStatusVal = new JLabel("ACTIVE");
        membershipStatusSub = new JLabel("Monthly Pass");
        metricsPanel.add(createMetricCard("MEMBERSHIP STATUS", "ACTIVE", "Monthly Pass", Theme.ROLE_MEMBER_COLOR, membershipStatusVal, membershipStatusSub));

        membershipDaysVal = new JLabel("30 Days Left");
        membershipDaysSub = new JLabel("Valid until --");
        metricsPanel.add(createMetricCard("MEMBERSHIP DAYS LEFT", "30 Days Left", "Valid until --", Theme.ROLE_OWNER_COLOR, membershipDaysVal, membershipDaysSub));

        metricsPanel.add(createMetricCard("WORKOUT ATTENDANCE", "18 Days", "This month (Top 10%)", Theme.SUCCESS_COLOR));
        metricsPanel.add(createMetricCard("ASSIGNED TRAINER", "Alex Rivera", "Personal Training - Tier 1", Theme.ROLE_TRAINER_COLOR));

        nextClassVal = new JLabel("No Bookings");
        nextClassSub = new JLabel("Book a class to start");
        metricsPanel.add(createMetricCard("NEXT CLASS", "No Bookings", "Book a class to start", Theme.ACCENT_PRIMARY, nextClassVal, nextClassSub));

        contentPanel.add(metricsPanel, BorderLayout.NORTH);

        // Center Panel (2 Columns)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false);

        // LEFT COLUMN: Weekly Workout Schedule + My Booked Sessions
        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setOpaque(false);

        // 1. My Weekly Workout Schedule Card
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
        DefaultTableModel scheduleModel = new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(scheduleModel);
        styleTable(table);
        JScrollPane scrollPane = Theme.createModernScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, 215));
        scheduleCard.add(scrollPane, BorderLayout.CENTER);

        // 2. My Booked Sessions Card (Positioned directly under weekly schedule)
        JPanel bookedCard = new JPanel(new BorderLayout(0, 12));
        bookedCard.setBackground(Theme.CARD_BG);
        bookedCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        // Booked Header
        JPanel bookedHeader = new JPanel(new BorderLayout());
        bookedHeader.setOpaque(false);

        JPanel bookedHeaderLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bookedHeaderLeft.setOpaque(false);

        JLabel bookedTitle = new JLabel("My Booked Sessions");
        bookedTitle.setFont(Theme.FONT_HEADER);
        bookedTitle.setForeground(Theme.TEXT_LIGHT);

        bookedBadge = new JLabel("0 Sessions");
        bookedBadge.setFont(Theme.FONT_SMALL);
        bookedBadge.setForeground(Theme.ACCENT_PRIMARY);

        bookedHeaderLeft.add(bookedTitle);
        bookedHeaderLeft.add(bookedBadge);

        // Booked Header Right - Actions
        JPanel bookedHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bookedHeaderRight.setOpaque(false);

        JButton bookMoreBtn = Theme.createSecondaryButton("+ Book Class");
        bookMoreBtn.setFont(Theme.FONT_SMALL);
        bookMoreBtn.setPreferredSize(new Dimension(110, 32));
        bookMoreBtn.addActionListener(e -> openBookClassDialog());

        cancelBookingBtn = Theme.createSecondaryButton("Cancel Selected");
        cancelBookingBtn.setFont(Theme.FONT_SMALL);
        cancelBookingBtn.setPreferredSize(new Dimension(125, 32));
        cancelBookingBtn.setEnabled(false);
        cancelBookingBtn.addActionListener(e -> handleCancelBooking());

        bookedHeaderRight.add(bookMoreBtn);
        bookedHeaderRight.add(cancelBookingBtn);

        bookedHeader.add(bookedHeaderLeft, BorderLayout.WEST);
        bookedHeader.add(bookedHeaderRight, BorderLayout.EAST);
        bookedCard.add(bookedHeader, BorderLayout.NORTH);

        // Booked Content Container (CardLayout: EMPTY vs TABLE)
        bookedCardLayout = new CardLayout();
        bookedCardContainer = new JPanel(bookedCardLayout);
        bookedCardContainer.setOpaque(false);

        // Empty state view
        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setOpaque(false);
        emptyPanel.setBorder(new EmptyBorder(35, 20, 35, 20));

        JLabel emptyIcon = new JLabel("📅");
        emptyIcon.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptyMsg = new JLabel("No fitness sessions booked yet.");
        emptyMsg.setFont(Theme.FONT_BOLD);
        emptyMsg.setForeground(Theme.TEXT_LIGHT);
        emptyMsg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptySub = new JLabel("Click 'Book a Fitness Class' in Member Quick Actions to reserve your spot!");
        emptySub.setFont(Theme.FONT_SMALL);
        emptySub.setForeground(Theme.TEXT_MUTED);
        emptySub.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyPanel.add(emptyIcon);
        emptyPanel.add(Box.createVerticalStrut(8));
        emptyPanel.add(emptyMsg);
        emptyPanel.add(Box.createVerticalStrut(4));
        emptyPanel.add(emptySub);

        // Table view
        String[] bookedCols = {"Class", "Duration", "Price / Session", "Schedule Slot", "Status"};
        bookedTableModel = new DefaultTableModel(bookedCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookedTable = new JTable(bookedTableModel);
        styleTable(bookedTable);

        // Custom highlight for Status column
        bookedTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, false, row, column);
                c.setFont(Theme.FONT_BOLD);
                c.setForeground(Theme.SUCCESS_COLOR);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Theme.CARD_BG : new Color(30, 34, 46));
                }
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        JScrollPane bookedScrollPane = Theme.createModernScrollPane(bookedTable);
        bookedScrollPane.setBorder(BorderFactory.createEmptyBorder());
        bookedScrollPane.setPreferredSize(new Dimension(bookedScrollPane.getPreferredSize().width, 180));

        bookedCardContainer.add(emptyPanel, "EMPTY");
        bookedCardContainer.add(bookedScrollPane, "TABLE");

        bookedCard.add(bookedCardContainer, BorderLayout.CENTER);

        // Assemble Left Column
        leftColumn.add(scheduleCard);
        leftColumn.add(Box.createVerticalStrut(18));
        leftColumn.add(bookedCard);

        // RIGHT COLUMN: Quick Actions Card + Session Overview Card
        JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setOpaque(false);

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
        bookClassBtn.addActionListener(e -> openBookClassDialog());

        JButton renewBtn = Theme.createSecondaryButton("Renew / Upgrade Membership");
        renewBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        renewBtn.addActionListener(e -> mainFrame.showChooseMembership());

        actionCard.add(actionTitle);
        actionCard.add(Box.createVerticalStrut(18));
        actionCard.add(bookClassBtn);
        actionCard.add(Box.createVerticalStrut(12));
        actionCard.add(renewBtn);

        // Summary Card
        JPanel summaryCard = new JPanel();
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));
        summaryCard.setBackground(Theme.CARD_BG);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        JLabel summaryTitle = new JLabel("Bookings & Activity Summary");
        summaryTitle.setFont(Theme.FONT_HEADER);
        summaryTitle.setForeground(Theme.TEXT_LIGHT);

        summaryMembershipExpiry = new JLabel("• Membership Validity: 30 Days Remaining");
        summaryMembershipExpiry.setFont(Theme.FONT_BODY);
        summaryMembershipExpiry.setForeground(Theme.ROLE_OWNER_COLOR);

        summaryBookedCount = new JLabel("• Total Booked Sessions: 0");
        summaryBookedCount.setFont(Theme.FONT_BODY);
        summaryBookedCount.setForeground(Theme.TEXT_LIGHT);

        summaryTotalSpend = new JLabel("• Total Classes Investment: Rs. 0");
        summaryTotalSpend.setFont(Theme.FONT_BODY);
        summaryTotalSpend.setForeground(Theme.SUCCESS_COLOR);

        summaryNextSession = new JLabel("• Upcoming Session: None");
        summaryNextSession.setFont(Theme.FONT_BODY);
        summaryNextSession.setForeground(Theme.TEXT_MUTED);

        JLabel trainerNote = new JLabel("• Trainer Guidance: Available on gym floor");
        trainerNote.setFont(Theme.FONT_BODY);
        trainerNote.setForeground(Theme.TEXT_MUTED);

        summaryCard.add(summaryTitle);
        summaryCard.add(Box.createVerticalStrut(14));
        summaryCard.add(summaryMembershipExpiry);
        summaryCard.add(Box.createVerticalStrut(8));
        summaryCard.add(summaryBookedCount);
        summaryCard.add(Box.createVerticalStrut(8));
        summaryCard.add(summaryTotalSpend);
        summaryCard.add(Box.createVerticalStrut(8));
        summaryCard.add(summaryNextSession);
        summaryCard.add(Box.createVerticalStrut(8));
        summaryCard.add(trainerNote);

        rightColumn.add(actionCard);
        rightColumn.add(Box.createVerticalStrut(18));
        rightColumn.add(summaryCard);

        centerPanel.add(leftColumn);
        centerPanel.add(rightColumn);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
    }

    private void openBookClassDialog() {
        BookClassDialog dialog = new BookClassDialog(mainFrame, this::loadBookedSessions);
        dialog.setVisible(true);
    }

    public void loadBookedSessions() {
        User user = UserSession.getCurrentUser();
        String gymId = user != null ? user.getGymId() : "";
        String email = user != null ? user.getEmail() : "";

        currentBookings = bookingDAO.getBookingsForMember(gymId, email);

        bookedTableModel.setRowCount(0);

        if (currentBookings.isEmpty()) {
            bookedCardLayout.show(bookedCardContainer, "EMPTY");
            bookedBadge.setText("0 Sessions");
            cancelBookingBtn.setEnabled(false);

            if (nextClassVal != null) nextClassVal.setText("No Bookings");
            if (nextClassSub != null) nextClassSub.setText("Book a class to start");

            if (summaryBookedCount != null) summaryBookedCount.setText("• Total Booked Sessions: 0");
            if (summaryTotalSpend != null) summaryTotalSpend.setText("• Total Classes Investment: Rs. 0");
            if (summaryNextSession != null) summaryNextSession.setText("• Upcoming Session: None");
        } else {
            double total = 0.0;
            for (BookedSession b : currentBookings) {
                total += b.getPrice();
                bookedTableModel.addRow(new Object[]{
                        b.getClassName(),
                        b.getDuration(),
                        "Rs. " + String.format("%,.0f", b.getPrice()),
                        b.getScheduleSlot(),
                        b.getStatus()
                });
            }

            bookedCardLayout.show(bookedCardContainer, "TABLE");
            bookedBadge.setText(currentBookings.size() + (currentBookings.size() == 1 ? " Session Booked" : " Sessions Booked"));
            cancelBookingBtn.setEnabled(true);

            // Update Next Class Metric Card with the first upcoming booked class
            BookedSession next = currentBookings.get(0);
            if (nextClassVal != null) nextClassVal.setText(next.getClassName());
            if (nextClassSub != null) nextClassSub.setText(next.getScheduleSlot() != null ? next.getScheduleSlot() : "Booked");

            if (summaryBookedCount != null) {
                summaryBookedCount.setText("• Total Booked Sessions: " + currentBookings.size());
            }
            if (summaryTotalSpend != null) {
                summaryTotalSpend.setText("• Total Classes Investment: Rs. " + String.format("%,.0f", total));
            }
            if (summaryNextSession != null) {
                summaryNextSession.setText("• Upcoming Session: " + next.getClassName() + " (" + next.getScheduleSlot() + ")");
            }
        }
    }

    private void handleCancelBooking() {
        int selectedRow = bookedTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentBookings.size()) {
            ThemedDialog.showInfo(
                    this,
                    "Select Booking to Cancel",
                    "Please select a booked session row from the table to cancel."
            );
            return;
        }

        BookedSession session = currentBookings.get(selectedRow);
        boolean confirmed = ThemedDialog.showConfirm(
                this,
                "Confirm Booking Cancellation",
                "Cancel Session Booking?",
                "Are you sure you want to cancel your booking for:\n\n"
                        + "Class: " + session.getClassName() + "\n"
                        + "Time Slot: " + session.getScheduleSlot() + "\n\n"
                        + "This session will be removed from your schedule.",
                "Yes, Cancel",
                "Keep Booking"
        );

        if (confirmed) {
            bookingDAO.cancelBooking(session.getId());
            loadBookedSessions();
            ThemedDialog.showSuccess(
                    this,
                    "Booking Cancelled",
                    "Booking for " + session.getClassName() + " (" + session.getScheduleSlot() + ") has been successfully removed from your schedule."
            );
        }
    }

    public void updateMembership(String planName, double price, int durationDays) {
        LocalDate startDate = LocalDate.now();
        LocalDate dueDate = startDate.plusDays(durationDays);
        UserSession.setMembership(planName, price, durationDays, startDate, dueDate);
        updateMembershipDisplay();
    }

    private void updateMembershipDisplay() {
        String plan = UserSession.getMembershipPlan();
        LocalDate startDate = UserSession.getMembershipStartDate();
        LocalDate dueDate = UserSession.getRenewalDueDate();
        long daysLeft = UserSession.getDaysLeft();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

        // Case 1: Plan is null or days have expired (0 days left)
        if (plan == null || daysLeft <= 0) {
            if (membershipStatusVal != null) {
                membershipStatusVal.setText("EXPIRED");
            }
            if (membershipStatusSub != null) {
                membershipStatusSub.setText(plan != null ? plan + " • 0d left" : "No Plan Selected");
            }
            if (membershipDaysVal != null) {
                membershipDaysVal.setText("0 Days Left");
            }
            if (membershipDaysSub != null) {
                membershipDaysSub.setText(dueDate != null ? "Expired on " + dueDate.format(fmt) : "Plan Required");
                membershipDaysSub.setForeground(Theme.ERROR_COLOR);
            }
            if (summaryMembershipExpiry != null) {
                summaryMembershipExpiry.setText("• Membership Plan: " + (plan != null ? plan : "None") + " (EXPIRED - 0 Days Left)");
                summaryMembershipExpiry.setForeground(Theme.ERROR_COLOR);
            }
            return;
        }

        // Case 2: Active plan with days left > 0
        if (membershipStatusVal != null) {
            membershipStatusVal.setText("ACTIVE");
        }
        if (membershipStatusSub != null) {
            membershipStatusSub.setText(plan + " • " + daysLeft + "d left");
        }

        String formattedDate = dueDate.format(fmt);

        if (membershipDaysVal != null) {
            membershipDaysVal.setText(daysLeft + (daysLeft == 1 ? " Day Left" : " Days Left"));
        }
        if (membershipDaysSub != null) {
            if (daysLeft <= 5) {
                membershipDaysSub.setText("Expires soon (" + formattedDate + ")");
                membershipDaysSub.setForeground(Theme.ERROR_COLOR);
            } else {
                membershipDaysSub.setText("Valid until " + formattedDate);
                membershipDaysSub.setForeground(Theme.ROLE_OWNER_COLOR);
            }
        }

        if (summaryMembershipExpiry != null) {
            summaryMembershipExpiry.setText("• Membership Plan: " + plan + " (" + daysLeft + " Days Left till " + formattedDate + ")");
            summaryMembershipExpiry.setForeground(Theme.ROLE_OWNER_COLOR);
        }
    }

    @Override
    public void refreshUserInfo() {
        super.refreshUserInfo();
        User user = UserSession.getCurrentUser();
        if (user != null) {
            UserDAO dao = new UserDAO();
            dao.syncUserMembership(user);
        }
        updateMembershipDisplay();
        loadBookedSessions();
    }
}
