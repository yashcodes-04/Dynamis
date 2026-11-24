/**
 * Date: 2026-11-16 - Yash
 */
package com.gym.ui.dashboard;

import com.gym.model.Role;
import com.gym.model.User;
import com.gym.session.UserSession;
import com.gym.ui.MainFrame;
import com.gym.ui.Theme;
import com.gym.ui.ThemedDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public abstract class BaseDashboard extends JPanel {

    protected final MainFrame mainFrame;
    protected final JPanel contentPanel;
    private JLabel welcomeLabel;
    private JLabel roleBadge;

    public BaseDashboard(MainFrame mainFrame, String dashboardTitle, Role role) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARK);

        // Top Navigation Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.CARD_BG);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.CARD_BORDER),
                new EmptyBorder(16, 30, 16, 30)
        ));

        // Brand & Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);

        JLabel brandIcon = new JLabel("⚡");
        brandIcon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandIcon.setForeground(Theme.ACCENT_PRIMARY);

        JLabel titleLabel = new JLabel("DYNAMIS  |  " + dashboardTitle.toUpperCase());
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_LIGHT);

        leftPanel.add(brandIcon);
        leftPanel.add(titleLabel);

        // User info + Role Badge + Logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setOpaque(false);

        welcomeLabel = new JLabel("Welcome, User");
        welcomeLabel.setFont(Theme.FONT_BODY);
        welcomeLabel.setForeground(Theme.TEXT_LIGHT);

        roleBadge = new JLabel("  " + role.getDisplayName() + "  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        roleBadge.setFont(Theme.FONT_SMALL);
        roleBadge.setOpaque(false);
        roleBadge.setForeground(Color.WHITE);
        roleBadge.setBackground(getRoleColor(role));
        roleBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

        JButton logoutButton = Theme.createSecondaryButton("Sign Out");
        logoutButton.addActionListener(e -> {
            boolean confirm = ThemedDialog.showConfirm(
                    this,
                    "Sign Out Confirmation",
                    "Sign Out?",
                    "Are you sure you want to sign out of your workspace session?",
                    "Sign Out",
                    "Stay"
            );
            if (confirm) {
                UserSession.logout();
                mainFrame.showCard(MainFrame.CARD_LOGIN);
            }
        });

        rightPanel.add(welcomeLabel);
        rightPanel.add(roleBadge);
        rightPanel.add(logoutButton);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Content Area inside smooth scroll pane
        contentPanel = new JPanel();
        contentPanel.setBackground(Theme.BG_DARK);
        contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JScrollPane scrollPane = Theme.createModernScrollPane(contentPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    protected Color getRoleColor(Role role) {
        if (role == null) return Theme.ACCENT_PRIMARY;
        switch (role) {
            case MEMBER: return Theme.ROLE_MEMBER_COLOR;
            case RECEPTIONIST: return Theme.ROLE_RECEPTIONIST_COLOR;
            case TRAINER: return Theme.ROLE_TRAINER_COLOR;
            case OWNER: return Theme.ROLE_OWNER_COLOR;
            default: return Theme.ACCENT_PRIMARY;
        }
    }

    public void refreshUserInfo() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            String idDisplay = user.getGymId() != null ? " [" + user.getGymId() + "]" : "";
            welcomeLabel.setText("Welcome, " + user.getFullName() + idDisplay);
            roleBadge.setText("  " + user.getRole().getDisplayName() + "  ");
            roleBadge.setBackground(getRoleColor(user.getRole()));
            roleBadge.repaint();
        }
    }

    protected JPanel createMetricCard(String title, String value, String subtitle, Color accentColor) {
        JLabel valLbl = new JLabel(value);
        JLabel subLbl = new JLabel(subtitle);
        return createMetricCard(title, value, subtitle, accentColor, valLbl, subLbl);
    }

    protected JPanel createMetricCard(String title, String value, String subtitle, Color accentColor, JLabel valueLabel, JLabel subtitleLabel) {
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
        card.setPreferredSize(new Dimension(240, 115));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_SMALL);
        titleLbl.setForeground(Theme.TEXT_MUTED);

        valueLabel.setText(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(Theme.TEXT_LIGHT);

        subtitleLabel.setText(subtitle);
        subtitleLabel.setFont(Theme.FONT_SMALL);
        subtitleLabel.setForeground(accentColor);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitleLabel);

        return card;
    }

    protected void styleTable(JTable table) {
        table.setBackground(Theme.CARD_BG);
        table.setForeground(Theme.TEXT_LIGHT);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(36);
        table.setGridColor(Theme.CARD_BORDER);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(Theme.ACCENT_PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        // Header Styling - Crisp black text for maximum visibility
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, false, row, column);
                c.setFont(new Font("Segoe UI", Font.BOLD, 13));
                c.setForeground(new Color(15, 15, 18)); // Solid black text
                c.setBackground(new Color(228, 231, 238)); // Light clean header background
                ((JLabel) c).setHorizontalAlignment(JLabel.LEFT);
                ((JLabel) c).setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        };
        table.getTableHeader().setDefaultRenderer(headerRenderer);
        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getWidth(), 38));
        table.getTableHeader().setReorderingAllowed(false);

        // Universal Row renderer with padding, alternating rows, and smooth selection (no white focused cell)
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, false, row, column);
                if (isSelected) {
                    c.setBackground(Theme.ACCENT_PRIMARY);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? Theme.CARD_BG : new Color(30, 34, 46));
                    c.setForeground(Theme.TEXT_LIGHT);
                }
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        };

        // Register for all standard data types so no column falls back to Windows light defaults
        table.setDefaultRenderer(Object.class, cellRenderer);
        table.setDefaultRenderer(String.class, cellRenderer);
        table.setDefaultRenderer(Number.class, cellRenderer);
        table.setDefaultRenderer(Integer.class, cellRenderer);
        table.setDefaultRenderer(Double.class, cellRenderer);
        table.setDefaultRenderer(java.util.Date.class, cellRenderer);
        table.setDefaultRenderer(java.sql.Timestamp.class, cellRenderer);
    }
}
