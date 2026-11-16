/**
 * Date: 2026-11-16 - Yash
 */
package com.gym.ui;

import com.gym.dao.UserDAO;
import com.gym.model.User;
import com.gym.session.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPanel extends JPanel {

    private final MainFrame mainFrame;
    private final UserDAO userDAO = new UserDAO();

    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheck;
    private JLabel messageLabel;
    private JButton loginButton;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARK);

        initUI();
    }

    private void initUI() {
        // Centering container
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(Theme.BG_DARK);
        centerContainer.setBorder(new EmptyBorder(30, 20, 30, 20));

        // Modern Card Panel
        JPanel card = new JPanel() {
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
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(36, 42, 36, 42));
        card.setPreferredSize(new Dimension(460, 560));
        card.setMaximumSize(new Dimension(460, 560));

        // Modern Branded Logo Badge "D" (Replaces unicode glyph that showed as broken box)
        JPanel logoBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT_PRIMARY);
                g2.fillOval(2, 2, 48, 48);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                FontMetrics fm = g2.getFontMetrics();
                int x = (52 - fm.stringWidth("D")) / 2;
                int y = ((52 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString("D", x, y);
                g2.dispose();
            }
        };
        logoBadge.setPreferredSize(new Dimension(52, 52));
        logoBadge.setMaximumSize(new Dimension(52, 52));
        logoBadge.setOpaque(false);
        logoBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Brand Name & Subtitle
        JLabel brandLabel = new JLabel("DYNAMIS");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        brandLabel.setForeground(Theme.TEXT_LIGHT);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Member & Staff Authentication Portal");
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Inputs
        emailField = Theme.createStyledTextField();
        passwordField = Theme.createStyledPasswordField();

        // Enter key submits form
        passwordField.addActionListener(e -> handleLogin());
        emailField.addActionListener(e -> passwordField.requestFocusInWindow());

        // Field Rows (Using consistent centering in card)
        JPanel emailRow = createFieldRow("Email Address", emailField);
        JPanel passRow = createFieldRow("Password", passwordField);

        // Show/Hide Password Checkbox
        showPasswordCheck = new JCheckBox("Show password");
        showPasswordCheck.setFont(Theme.FONT_SMALL);
        showPasswordCheck.setForeground(Theme.TEXT_MUTED);
        showPasswordCheck.setOpaque(false);
        showPasswordCheck.setFocusPainted(false);
        showPasswordCheck.setAlignmentX(Component.CENTER_ALIGNMENT);
        showPasswordCheck.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        showPasswordCheck.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : '\u2022');
        });

        // Error / Status Message
        messageLabel = new JLabel(" ");
        messageLabel.setFont(Theme.FONT_SMALL);
        messageLabel.setForeground(Theme.ERROR_COLOR);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login Button
        loginButton = Theme.createPrimaryButton("Sign In to Workspace");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.addActionListener(e -> handleLogin());

        // Register Footer Link
        JPanel registerFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        registerFooter.setOpaque(false);
        registerFooter.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noAccountLabel = new JLabel("Don't have an account yet?");
        noAccountLabel.setFont(Theme.FONT_BODY);
        noAccountLabel.setForeground(Theme.TEXT_MUTED);

        JButton registerLink = new JButton("Create an Account");
        registerLink.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerLink.setForeground(Theme.ACCENT_PRIMARY);
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setFocusPainted(false);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> {
            clearFields();
            mainFrame.showCard(MainFrame.CARD_REGISTER);
        });

        registerFooter.add(noAccountLabel);
        registerFooter.add(registerLink);

        // Assemble Card with all elements sharing Component.CENTER_ALIGNMENT
        card.add(logoBadge);
        card.add(Box.createVerticalStrut(8));
        card.add(brandLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(24));

        card.add(emailRow);
        card.add(Box.createVerticalStrut(14));

        card.add(passRow);
        card.add(Box.createVerticalStrut(6));

        // Checkbox row aligned with inputs
        JPanel checkRow = new JPanel(new BorderLayout());
        checkRow.setOpaque(false);
        checkRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        checkRow.add(showPasswordCheck, BorderLayout.WEST);
        card.add(checkRow);

        card.add(Box.createVerticalStrut(10));
        card.add(messageLabel);
        card.add(Box.createVerticalStrut(12));

        card.add(loginButton);
        card.add(Box.createVerticalStrut(18));
        card.add(registerFooter);

        centerContainer.add(card);

        JScrollPane scrollPane = Theme.createModernScrollPane(centerContainer);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createFieldRow(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel label = Theme.createFieldLabel(labelText);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            setMessage("Please enter both email and password.", true);
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Verifying...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            private String error = null;

            @Override
            protected User doInBackground() {
                try {
                    return userDAO.authenticate(email, password);
                } catch (Exception ex) {
                    error = "Database Error: " + ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Sign In to Workspace");

                try {
                    User user = get();
                    if (user != null) {
                        UserSession.setCurrentUser(user);
                        clearFields();
                        mainFrame.showDashboardForRole(user.getRole());
                    } else if (error != null) {
                        setMessage("Connection error. Check db.properties / MySQL.", true);
                    } else {
                        setMessage("Invalid email or password.", true);
                    }
                } catch (Exception ex) {
                    setMessage("Unexpected error: " + ex.getMessage(), true);
                }
            }
        };

        worker.execute();
    }

    public void setMessage(String msg, boolean isError) {
        messageLabel.setText(msg);
        messageLabel.setForeground(isError ? Theme.ERROR_COLOR : Theme.SUCCESS_COLOR);
    }

    public void clearFields() {
        emailField.setText("");
        passwordField.setText("");
        showPasswordCheck.setSelected(false);
        passwordField.setEchoChar('\u2022');
        messageLabel.setText(" ");
    }
}
