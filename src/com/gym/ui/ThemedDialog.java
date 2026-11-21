/**
 * Date: 2026-11-21
 */
package com.gym.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class ThemedDialog extends JDialog {

    public enum Type {
        SUCCESS(Theme.SUCCESS_COLOR, "✓"),
        INFO(Theme.ROLE_MEMBER_COLOR, "ℹ"),
        WARNING(Theme.ROLE_OWNER_COLOR, "⚠"),
        CONFIRM(Theme.ACCENT_PRIMARY, "?"),
        ERROR(Theme.ERROR_COLOR, "✕");

        private final Color accentColor;
        private final String symbol;

        Type(Color accentColor, String symbol) {
            this.accentColor = accentColor;
            this.symbol = symbol;
        }

        public Color getAccentColor() { return accentColor; }
        public String getSymbol() { return symbol; }
    }

    private boolean confirmed = false;
    private Point mouseClickPoint;

    public ThemedDialog(Window owner, String title, String heading, String message,
                        Type type, boolean isConfirm, String confirmText, String cancelText) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // Transparent for rounded corners

        JPanel rootCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2.setColor(Theme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                // Accent top stripe
                g2.setColor(type.getAccentColor());
                g2.fillRoundRect(0, 0, getWidth(), 4, 16, 16);

                // Border
                g2.setColor(Theme.CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        rootCard.setOpaque(false);
        rootCard.setLayout(new BorderLayout());
        rootCard.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Header Panel (Draggable)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(18, 22, 12, 20));

        // Make dialog draggable
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseClickPoint = e.getPoint();
            }
        });
        header.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouseClickPoint != null) {
                    Point current = e.getLocationOnScreen();
                    setLocation(current.x - mouseClickPoint.x, current.y - mouseClickPoint.y);
                }
            }
        });

        // Left Header: Icon Badge + Title
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftHeader.setOpaque(false);

        // Circular Glowing Badge
        JPanel iconBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color accent = type.getAccentColor();

                // Soft glowing background circle
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35));
                g2.fillOval(0, 0, getWidth(), getHeight());

                // Subtle outline ring
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);

                // Symbol
                g2.setColor(accent);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(type.getSymbol())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(type.getSymbol(), x, y);

                g2.dispose();
            }
        };
        iconBadge.setPreferredSize(new Dimension(34, 34));
        iconBadge.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_HEADER);
        titleLbl.setForeground(Theme.TEXT_LIGHT);

        leftHeader.add(iconBadge);
        leftHeader.add(titleLbl);

        // Right Header: Close Button
        JButton closeBtn = new JButton("✕") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
                g2.dispose();
            }
        };
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setForeground(Theme.TEXT_MUTED);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(28, 28));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeBtn.setForeground(Theme.ACCENT_PRIMARY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                closeBtn.setForeground(Theme.TEXT_MUTED);
            }
        });
        closeBtn.addActionListener(e -> dispose());

        header.add(leftHeader, BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);
        rootCard.add(header, BorderLayout.NORTH);

        // Center: Content Area
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(6, 26, 16, 26));

        if (heading != null && !heading.trim().isEmpty()) {
            JLabel headLbl = new JLabel(heading);
            headLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            headLbl.setForeground(type.getAccentColor());
            headLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(headLbl);
            contentPanel.add(Box.createVerticalStrut(10));
        }

        // Parse and style the message nicely
        JPanel messagePanel = formatMessagePanel(message, type);
        messagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(messagePanel);

        rootCard.add(contentPanel, BorderLayout.CENTER);

        // Bottom Button Bar
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonBar.setOpaque(false);
        buttonBar.setBorder(new EmptyBorder(12, 24, 20, 24));

        if (isConfirm) {
            JButton cancelBtn = Theme.createSecondaryButton(cancelText != null ? cancelText : "Cancel");
            cancelBtn.setPreferredSize(new Dimension(110, 40));
            cancelBtn.addActionListener(e -> {
                confirmed = false;
                dispose();
            });

            JButton confirmBtn = Theme.createPrimaryButton(confirmText != null ? confirmText : "Confirm");
            confirmBtn.setPreferredSize(new Dimension(130, 40));
            confirmBtn.addActionListener(e -> {
                confirmed = true;
                dispose();
            });

            buttonBar.add(cancelBtn);
            buttonBar.add(confirmBtn);
        } else {
            JButton okBtn = Theme.createPrimaryButton(confirmText != null ? confirmText : "OK");
            okBtn.setPreferredSize(new Dimension(110, 40));
            okBtn.addActionListener(e -> {
                confirmed = true;
                dispose();
            });
            buttonBar.add(okBtn);
        }

        rootCard.add(buttonBar, BorderLayout.SOUTH);

        // Key Listeners
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    confirmed = false;
                    dispose();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    confirmed = true;
                    dispose();
                }
            }
        });
        setFocusable(true);

        setContentPane(rootCard);
        pack();

        // Enforce smooth min/preferred sizing
        int targetWidth = Math.max(480, Math.min(620, getWidth() + 20));
        setSize(new Dimension(targetWidth, getHeight()));

        setLocationRelativeTo(owner);
    }

    private static JPanel formatMessagePanel(String message, Type type) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        if (message == null || message.trim().isEmpty()) {
            return panel;
        }

        String[] lines = message.split("\n");
        JPanel detailCard = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                panel.add(Box.createVerticalStrut(6));
                continue;
            }

            // Check if this line looks like a detail card line (e.g. • Mat Pilates..., Total Price:..., Class:..., Time Slot:...)
            boolean isDetailLine = line.startsWith("•") || line.startsWith("-") || line.startsWith("Class:")
                    || line.startsWith("Time Slot") || line.startsWith("Total Price:") || line.startsWith("Total:");

            if (isDetailLine) {
                if (detailCard == null) {
                    detailCard = new JPanel();
                    detailCard.setLayout(new BoxLayout(detailCard, BoxLayout.Y_AXIS));
                    detailCard.setBackground(Theme.INPUT_BG);
                    detailCard.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(Theme.INPUT_BORDER, 1, true),
                            new EmptyBorder(10, 14, 10, 14)
                    ));
                    detailCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                    panel.add(detailCard);
                    panel.add(Box.createVerticalStrut(8));
                }

                JLabel itemLbl = new JLabel(line);
                itemLbl.setFont(Theme.FONT_BODY);
                if (line.contains("Total Price:") || line.contains("Total:")) {
                    itemLbl.setFont(Theme.FONT_BOLD);
                    itemLbl.setForeground(Theme.SUCCESS_COLOR);
                } else if (line.startsWith("•") || line.startsWith("-")) {
                    itemLbl.setForeground(Theme.TEXT_LIGHT);
                } else {
                    itemLbl.setForeground(new Color(220, 224, 233));
                }
                detailCard.add(itemLbl);
                detailCard.add(Box.createVerticalStrut(4));
            } else {
                detailCard = null; // Close current detail card
                JLabel lbl = new JLabel("<html><body style='width: 380px;'>" + escapeHtml(line) + "</body></html>");
                lbl.setFont(Theme.FONT_BODY);
                lbl.setForeground(Theme.TEXT_LIGHT);
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(lbl);
                panel.add(Box.createVerticalStrut(4));
            }
        }

        return panel;
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static Window getWindowForComponent(Component parent) {
        if (parent == null) return null;
        if (parent instanceof Window) return (Window) parent;
        return SwingUtilities.getWindowAncestor(parent);
    }

    // Static Helper Methods
    public static void showSuccess(Component parent, String title, String message) {
        ThemedDialog dlg = new ThemedDialog(
                getWindowForComponent(parent),
                title != null ? title : "Success",
                "Booking Confirmed!",
                message,
                Type.SUCCESS,
                false,
                "Done",
                null
        );
        dlg.setVisible(true);
    }

    public static void showInfo(Component parent, String title, String message) {
        ThemedDialog dlg = new ThemedDialog(
                getWindowForComponent(parent),
                title != null ? title : "Information",
                null,
                message,
                Type.INFO,
                false,
                "OK",
                null
        );
        dlg.setVisible(true);
    }

    public static void showWarning(Component parent, String title, String message) {
        ThemedDialog dlg = new ThemedDialog(
                getWindowForComponent(parent),
                title != null ? title : "Attention",
                "Notice",
                message,
                Type.WARNING,
                false,
                "OK",
                null
        );
        dlg.setVisible(true);
    }

    public static void showError(Component parent, String title, String message) {
        ThemedDialog dlg = new ThemedDialog(
                getWindowForComponent(parent),
                title != null ? title : "Error",
                "Action Failed",
                message,
                Type.ERROR,
                false,
                "Dismiss",
                null
        );
        dlg.setVisible(true);
    }

    public static boolean showConfirm(Component parent, String title, String heading, String message,
                                     String confirmText, String cancelText) {
        ThemedDialog dlg = new ThemedDialog(
                getWindowForComponent(parent),
                title != null ? title : "Confirm Action",
                heading,
                message,
                Type.CONFIRM,
                true,
                confirmText != null ? confirmText : "Confirm",
                cancelText != null ? cancelText : "Cancel"
        );
        dlg.setVisible(true);
        return dlg.confirmed;
    }
}
