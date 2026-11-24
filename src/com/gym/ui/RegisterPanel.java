/**
 * Date: 2026-11-16 - Yash
 */
package com.gym.ui;

import com.gym.dao.UserDAO;
import com.gym.model.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

public class RegisterPanel extends JPanel {

    private final MainFrame mainFrame;
    private final UserDAO userDAO = new UserDAO();

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<Role> roleComboBox;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JCheckBox showPasswordCheck;
    private JLabel messageLabel;
    private JButton registerButton;

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARK);

        initUI();
    }

    private void initUI() {
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
        card.setBorder(new EmptyBorder(32, 42, 32, 42));
        card.setPreferredSize(new Dimension(500, 700));
        card.setMaximumSize(new Dimension(500, 700));

        // Header - Full "Create Your Account" without truncation
        JLabel brandLabel = new JLabel("Create Your Account");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        brandLabel.setForeground(Theme.TEXT_LIGHT);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Join Dynamis Gym and access your workspace");
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        nameField = Theme.createStyledTextField();
        emailField = Theme.createStyledTextField();
        phoneField = Theme.createStyledTextField();

        // Role Selector with clean full text that does not truncate
        roleComboBox = new JComboBox<>(Role.values());
        Theme.styleComboBox(roleComboBox);
        roleComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Role) {
                    Role r = (Role) value;
                    setText(r.getDisplayName());
                }
                list.setBackground(Theme.INPUT_BG);
                list.setSelectionBackground(Theme.ACCENT_PRIMARY);
                list.setSelectionForeground(Color.WHITE);
                setBackground(isSelected ? Theme.ACCENT_PRIMARY : Theme.INPUT_BG);
                setForeground(isSelected ? Color.WHITE : Theme.TEXT_LIGHT);
                setBorder(new EmptyBorder(8, 12, 8, 12));
                return this;
            }
        });

        passwordField = Theme.createStyledPasswordField();
        confirmPasswordField = Theme.createStyledPasswordField();

        // Centered Field Rows
        JPanel nameRow = createFieldRow("Full Name", nameField);
        JPanel emailRow = createFieldRow("Email Address", emailField);
        JPanel phoneRow = createFieldRow("Phone Number", phoneField);
        JPanel roleRow = createFieldRow("Account Role / Workspace", roleComboBox);
        JPanel passRow = createFieldRow("Password (minimum 6 characters)", passwordField);
        JPanel confirmPassRow = createFieldRow("Confirm Password", confirmPasswordField);

        // Show/Hide Password Checkbox
        showPasswordCheck = new JCheckBox("Show passwords");
        showPasswordCheck.setFont(Theme.FONT_SMALL);
        showPasswordCheck.setForeground(Theme.TEXT_MUTED);
        showPasswordCheck.setOpaque(false);
        showPasswordCheck.setFocusPainted(false);
        showPasswordCheck.setAlignmentX(Component.CENTER_ALIGNMENT);
        showPasswordCheck.addActionListener(e -> {
            char echo = showPasswordCheck.isSelected() ? (char) 0 : '\u2022';
            passwordField.setEchoChar(echo);
            confirmPasswordField.setEchoChar(echo);
        });

        messageLabel = new JLabel(" ");
        messageLabel.setFont(Theme.FONT_SMALL);
        messageLabel.setForeground(Theme.ERROR_COLOR);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Full "Create Your Account" button text
        registerButton = Theme.createPrimaryButton("Create Your Account");
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        registerButton.addActionListener(e -> handleRegister());

        // Back to Login link
        JPanel backFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        backFooter.setOpaque(false);
        backFooter.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel haveAccountLabel = new JLabel("Already have an account?");
        haveAccountLabel.setFont(Theme.FONT_BODY);
        haveAccountLabel.setForeground(Theme.TEXT_MUTED);

        JButton backLink = new JButton("Sign In Here");
        backLink.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backLink.setForeground(Theme.ACCENT_PRIMARY);
        backLink.setBorderPainted(false);
        backLink.setContentAreaFilled(false);
        backLink.setFocusPainted(false);
        backLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLink.addActionListener(e -> {
            clearFields();
            mainFrame.showCard(MainFrame.CARD_LOGIN);
        });

        backFooter.add(haveAccountLabel);
        backFooter.add(backLink);

        // Assemble Card with all components centered
        card.add(brandLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(18));

        card.add(nameRow);
        card.add(Box.createVerticalStrut(10));
        card.add(emailRow);
        card.add(Box.createVerticalStrut(10));
        card.add(phoneRow);
        card.add(Box.createVerticalStrut(10));
        card.add(roleRow);
        card.add(Box.createVerticalStrut(10));
        card.add(passRow);
        card.add(Box.createVerticalStrut(10));
        card.add(confirmPassRow);
        card.add(Box.createVerticalStrut(6));

        JPanel checkRow = new JPanel(new BorderLayout());
        checkRow.setOpaque(false);
        checkRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        checkRow.add(showPasswordCheck, BorderLayout.WEST);
        card.add(checkRow);

        card.add(Box.createVerticalStrut(6));
        card.add(messageLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(registerButton);
        card.add(Box.createVerticalStrut(14));
        card.add(backFooter);

        centerContainer.add(card);

        JScrollPane scrollPane = Theme.createModernScrollPane(centerContainer);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createFieldRow(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = Theme.createFieldLabel(labelText);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void handleRegister() {
        String fullName = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        Role selectedRole = (Role) roleComboBox.getSelectedItem();
        String password = new String(passwordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            setMessage("Please fill in all required fields.", true);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            setMessage("Please enter a valid email address.", true);
            return;
        }

        if (password.length() < 6) {
            setMessage("Password must be at least 6 characters.", true);
            return;
        }

        if (!password.equals(confirmPass)) {
            setMessage("Passwords do not match.", true);
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Registering...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected Boolean doInBackground() {
                try {
                    if (userDAO.isEmailTaken(email)) {
                        errorMessage = "This email is already registered.";
                        return false;
                    }
                    return userDAO.register(fullName, email, phone, password, selectedRole);
                } catch (Exception ex) {
                    errorMessage = "Database Error: " + ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                registerButton.setEnabled(true);
                registerButton.setText("Create Your Account");

                try {
                    boolean success = get();
                    if (success) {
                        clearFields();
                        mainFrame.getLoginPanel().setMessage("Registration successful! Please sign in.", false);
                        mainFrame.showCard(MainFrame.CARD_LOGIN);
                    } else {
                        setMessage(errorMessage != null ? errorMessage : "Registration failed.", true);
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
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        showPasswordCheck.setSelected(false);
        passwordField.setEchoChar('\u2022');
        confirmPasswordField.setEchoChar('\u2022');
        messageLabel.setText(" ");
        if (roleComboBox != null && roleComboBox.getItemCount() > 0) {
            roleComboBox.setSelectedIndex(0);
        }
    }
}
