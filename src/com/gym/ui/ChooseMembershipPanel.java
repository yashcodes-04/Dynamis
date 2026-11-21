/**
 * Date: 2026-11-21
 */
package com.gym.ui;

import com.gym.dao.ReceptionistDAO;
import com.gym.dao.UserDAO;
import com.gym.model.User;
import com.gym.session.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChooseMembershipPanel extends JPanel {

    public static class PlanOption {
        private final String id;
        private final String title;
        private final String durationLabel;
        private final int months;
        private final int durationDays;
        private final double price;
        private final String badge;
        private final Color badgeColor;
        private final String[] perks;

        public PlanOption(String id, String title, String durationLabel, int months, int durationDays, double price,
                          String badge, Color badgeColor, String[] perks) {
            this.id = id;
            this.title = title;
            this.durationLabel = durationLabel;
            this.months = months;
            this.durationDays = durationDays;
            this.price = price;
            this.badge = badge;
            this.badgeColor = badgeColor;
            this.perks = perks;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDurationLabel() { return durationLabel; }
        public int getMonths() { return months; }
        public int getDurationDays() { return durationDays; }
        public double getPrice() { return price; }
        public String getBadge() { return badge; }
        public Color getBadgeColor() { return badgeColor; }
        public String[] getPerks() { return perks; }
    }

    private final MainFrame mainFrame;
    private final List<PlanOption> plans = new ArrayList<>();
    private final List<PlanCardComponent> cardComponents = new ArrayList<>();
    private PlanOption selectedPlan = null;

    private JLabel messageLabel;
    private JButton confirmButton;
    private JButton signOutButton;

    public ChooseMembershipPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARK);

        initPlans();
        initUI();
    }

    private void initPlans() {
        plans.add(new PlanOption(
                "MONTHLY",
                "Monthly Pass",
                "1 Month (30 Days)",
                1,
                30,
                1500.0,
                "STARTER",
                Theme.ROLE_MEMBER_COLOR,
                new String[]{
                        "Full Gym Floor & Equipment",
                        "Locker & Shower Access",
                        "Cardio & Free Weights",
                        "Valid for 30 Days"
                }
        ));

        plans.add(new PlanOption(
                "QUARTERLY",
                "Quarterly Pass",
                "3 Months (90 Days)",
                3,
                90,
                4000.0,
                "POPULAR",
                Theme.SUCCESS_COLOR,
                new String[]{
                        "All Monthly Features",
                        "1 Fitness Assessment",
                        "Locker Room & Steam Bath",
                        "Valid for 90 Days (Save 11%)"
                }
        ));

        plans.add(new PlanOption(
                "HALF_YEARLY",
                "Half-Yearly Pass",
                "6 Months (180 Days)",
                6,
                180,
                7500.0,
                "RECOMMENDED",
                Theme.ROLE_TRAINER_COLOR,
                new String[]{
                        "All Quarterly Features",
                        "Custom Diet & Nutrition Plan",
                        "2 Complimentary Guest Passes",
                        "Valid for 180 Days (Save 17%)"
                }
        ));

        plans.add(new PlanOption(
                "ANNUAL",
                "Annual Pass",
                "12 Months (365 Days)",
                12,
                365,
                13500.0,
                "VIP BEST VALUE",
                Theme.ROLE_OWNER_COLOR,
                new String[]{
                        "VIP All-Access Pass (365 Days)",
                        "Personal Trainer Orientation",
                        "Free Dynamis Gym Merch Kit",
                        "Highest Savings (25% Off)"
                }
        ));
    }

    private void initUI() {
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(Theme.BG_DARK);
        centerContainer.setBorder(new EmptyBorder(25, 20, 25, 20));

        // Outer Membership Box
        JPanel box = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(Theme.CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(28, 32, 28, 32));
        box.setMaximumSize(new Dimension(1060, 680));
        box.setPreferredSize(new Dimension(1040, 640));

        // Header Section
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel topTag = new JLabel("⚡ DYNAMIS GYM MEMBERSHIP PORTAL");
        topTag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        topTag.setForeground(Theme.ACCENT_PRIMARY);
        topTag.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Choose Your Membership");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Theme.TEXT_LIGHT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Select one of the 4 membership plans below to activate your gym access.");
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(topTag);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitleLabel);

        // Plans Grid Container (1 Row, 4 Columns)
        JPanel plansRow = new JPanel(new GridLayout(1, 4, 16, 0));
        plansRow.setOpaque(false);
        plansRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        plansRow.setMaximumSize(new Dimension(1020, 360));
        plansRow.setPreferredSize(new Dimension(980, 350));

        for (PlanOption plan : plans) {
            PlanCardComponent card = new PlanCardComponent(plan);
            cardComponents.add(card);
            plansRow.add(card);
        }

        // Status / Validation Message Label
        messageLabel = new JLabel("Please select a membership plan above before proceeding.");
        messageLabel.setFont(Theme.FONT_BODY);
        messageLabel.setForeground(Theme.TEXT_MUTED);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Buttons Row
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        confirmButton = Theme.createPrimaryButton("Confirm Membership");
        confirmButton.setPreferredSize(new Dimension(280, 44));
        confirmButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        confirmButton.addActionListener(e -> handleConfirm());

        signOutButton = Theme.createSecondaryButton("Sign Out");
        signOutButton.setPreferredSize(new Dimension(130, 44));
        signOutButton.addActionListener(e -> {
            UserSession.logout();
            resetSelection();
            mainFrame.showCard(MainFrame.CARD_LOGIN);
        });

        buttonRow.add(confirmButton);
        buttonRow.add(signOutButton);

        // Assemble Box
        box.add(headerPanel);
        box.add(Box.createVerticalStrut(20));
        box.add(plansRow);
        box.add(Box.createVerticalStrut(18));
        box.add(messageLabel);
        box.add(Box.createVerticalStrut(14));
        box.add(buttonRow);

        centerContainer.add(box);

        JScrollPane scrollPane = Theme.createModernScrollPane(centerContainer);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void selectPlan(PlanOption plan) {
        this.selectedPlan = plan;
        messageLabel.setText("✓ Selected: " + plan.getTitle() + " (Rs. " + String.format("%,.0f", plan.getPrice())
                + " for " + plan.getDurationLabel() + ") — Click 'Confirm Membership' to open dashboard.");
        messageLabel.setForeground(Theme.SUCCESS_COLOR);
        confirmButton.setText("Confirm " + plan.getTitle());

        for (PlanCardComponent card : cardComponents) {
            card.repaint();
        }
    }

    public void resetSelection() {
        this.selectedPlan = null;
        messageLabel.setText("Please select a membership plan above before proceeding.");
        messageLabel.setForeground(Theme.TEXT_MUTED);
        confirmButton.setText("Confirm Membership");

        for (PlanCardComponent card : cardComponents) {
            card.repaint();
        }
    }

    public void setExpiredNotice(String oldPlan, LocalDate expiredDate) {
        resetSelection();
        String dateStr = expiredDate != null ? expiredDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "recently";
        messageLabel.setText("⚠️ Your " + (oldPlan != null ? oldPlan : "") + " membership expired on " + dateStr + " (0 Days Left). Please choose a plan below to renew access.");
        messageLabel.setForeground(Theme.ERROR_COLOR);
    }

    private void handleConfirm() {
        // Validation: without selecting the membership the user cannot open the member's dashboard
        if (selectedPlan == null) {
            messageLabel.setText("⚠️ You cannot open Member Dashboard without choosing a membership plan!");
            messageLabel.setForeground(Theme.ERROR_COLOR);
            ThemedDialog.showWarning(
                    this,
                    "Membership Selection Required",
                    "Please select one of the 4 membership plans to activate your membership.\nWithout selecting a membership, you cannot open the member's dashboard."
            );
            return;
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(selectedPlan.getDurationDays());

        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            currentUser.setMembershipPlan(selectedPlan.getTitle());
            currentUser.setMembershipPrice(selectedPlan.getPrice());
            currentUser.setMembershipDurationDays(selectedPlan.getDurationDays());
            currentUser.setMembershipStartDate(startDate);
            currentUser.setMembershipEndDate(endDate);
        }

        // Store active membership in session
        UserSession.setMembership(selectedPlan.getTitle(), selectedPlan.getPrice(), selectedPlan.getDurationDays(), startDate, endDate);

        // Asynchronously record membership and fee payment in database
        if (currentUser != null) {
            final PlanOption plan = selectedPlan;
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    try {
                        // 1. Update users table with membership plan, start date, end date, duration days (updates existing row in place)
                        UserDAO userDAO = new UserDAO();
                        userDAO.updateMemberMembership(
                                currentUser.getGymId(),
                                currentUser.getEmail(),
                                plan.getTitle(),
                                plan.getPrice(),
                                plan.getDurationDays(),
                                startDate,
                                endDate
                        );

                        // 2. Update fee payment in fee_payments table (updates existing record in place, no duplicate row)
                        ReceptionistDAO dao = new ReceptionistDAO();
                        dao.recordFeePayment(
                                currentUser.getGymId(),
                                currentUser.getEmail(),
                                currentUser.getFullName(),
                                plan.getTitle(),
                                plan.getPrice(),
                                "Self-Checkout",
                                endDate,
                                plan.getDurationDays()
                        );
                    } catch (Exception ex) {
                        System.err.println("Note on recording self membership fee in DB: " + ex.getMessage());
                    }
                    return null;
                }
            };
            worker.execute();
        }

        // Transition to Member Workspace
        mainFrame.openMemberDashboard(selectedPlan.getTitle(), selectedPlan.getPrice(), selectedPlan.getDurationDays());
    }

    /**
     * Card component representing one of the 4 membership plans
     */
    private class PlanCardComponent extends JPanel {
        private final PlanOption plan;
        private boolean isHovered = false;

        public PlanCardComponent(PlanOption plan) {
            this.plan = plan;
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(14, 14, 14, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            initCardContent();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    selectPlan(plan);
                }
            });
        }

        private void initCardContent() {
            // Badge Label
            JLabel badgeLbl = new JLabel("  " + plan.getBadge() + "  ") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(plan.getBadgeColor());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badgeLbl.setForeground(Color.WHITE);
            badgeLbl.setOpaque(false);
            badgeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Plan Title
            JLabel titleLbl = new JLabel(plan.getTitle());
            titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
            titleLbl.setForeground(Theme.TEXT_LIGHT);
            titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Price Label
            JLabel priceLbl = new JLabel("Rs. " + String.format("%,.0f", plan.getPrice()));
            priceLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
            priceLbl.setForeground(Theme.ACCENT_PRIMARY);
            priceLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Duration Label
            JLabel durationLbl = new JLabel("/ " + plan.getDurationLabel());
            durationLbl.setFont(Theme.FONT_SMALL);
            durationLbl.setForeground(Theme.TEXT_MUTED);
            durationLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Perks List Panel
            JPanel perksPanel = new JPanel();
            perksPanel.setOpaque(false);
            perksPanel.setLayout(new BoxLayout(perksPanel, BoxLayout.Y_AXIS));
            perksPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            perksPanel.setBorder(new EmptyBorder(10, 4, 10, 4));

            for (String perk : plan.getPerks()) {
                JPanel perkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
                perkRow.setOpaque(false);
                JLabel check = new JLabel("✓");
                check.setFont(new Font("Segoe UI", Font.BOLD, 12));
                check.setForeground(Theme.SUCCESS_COLOR);

                JLabel perkText = new JLabel(perk);
                perkText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                perkText.setForeground(Theme.TEXT_LIGHT);

                perkRow.add(check);
                perkRow.add(perkText);
                perksPanel.add(perkRow);
            }

            // Selection Indicator Pill at Bottom
            JLabel selectPill = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    boolean isSelected = (selectedPlan == plan);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isSelected) {
                        g2.setColor(Theme.ACCENT_PRIMARY);
                    } else {
                        g2.setColor(new Color(42, 48, 65));
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    super.paintComponent(g);
                    g2.dispose();
                }

                @Override
                public String getText() {
                    return (selectedPlan == plan) ? "  ✓ SELECTED  " : "  Select Plan  ";
                }
            };
            selectPill.setFont(new Font("Segoe UI", Font.BOLD, 12));
            selectPill.setForeground(Color.WHITE);
            selectPill.setOpaque(false);
            selectPill.setAlignmentX(Component.CENTER_ALIGNMENT);
            selectPill.setBorder(new EmptyBorder(6, 14, 6, 14));

            add(badgeLbl);
            add(Box.createVerticalStrut(8));
            add(titleLbl);
            add(Box.createVerticalStrut(4));
            add(priceLbl);
            add(durationLbl);
            add(Box.createVerticalStrut(8));
            add(new JSeparator(SwingConstants.HORIZONTAL) {
                {
                    setForeground(Theme.INPUT_BORDER);
                    setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                }
            });
            add(perksPanel);
            add(Box.createVerticalGlue());
            add(selectPill);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean isSelected = (selectedPlan == plan);

            // Background
            if (isSelected) {
                g2.setColor(new Color(36, 42, 58));
            } else if (isHovered) {
                g2.setColor(new Color(28, 33, 46));
            } else {
                g2.setColor(Theme.INPUT_BG);
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

            // Border
            if (isSelected) {
                g2.setColor(Theme.ACCENT_PRIMARY);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
            } else if (isHovered) {
                g2.setColor(new Color(90, 102, 130));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            } else {
                g2.setColor(Theme.INPUT_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
