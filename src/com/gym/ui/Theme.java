/**
 * Date: 2026-11-16 - Yash
 */
package com.gym.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class Theme {
    // Color Palette
    public static final Color BG_DARK = new Color(15, 17, 23);
    public static final Color CARD_BG = new Color(24, 27, 36);
    public static final Color CARD_BORDER = new Color(38, 43, 58);
    public static final Color INPUT_BG = new Color(32, 36, 48);
    public static final Color INPUT_BORDER = new Color(50, 56, 75);
    public static final Color INPUT_FOCUS_BORDER = new Color(240, 84, 84); // Glows crimson on focus
    public static final Color ACCENT_PRIMARY = new Color(240, 84, 84);    // Gym Crimson
    public static final Color ACCENT_HOVER = new Color(220, 65, 65);
    public static final Color ACCENT_SECONDARY = new Color(38, 43, 58);
    public static final Color ACCENT_SECONDARY_HOVER = new Color(50, 56, 75);
    public static final Color TEXT_LIGHT = new Color(248, 249, 250);
    public static final Color TEXT_MUTED = new Color(156, 163, 175);
    public static final Color ERROR_COLOR = new Color(248, 113, 113);
    public static final Color SUCCESS_COLOR = new Color(52, 211, 153);

    // Role Accent Colors
    public static final Color ROLE_MEMBER_COLOR = new Color(59, 130, 246);      // Vibrant Blue
    public static final Color ROLE_RECEPTIONIST_COLOR = new Color(168, 85, 247);// Purple
    public static final Color ROLE_TRAINER_COLOR = new Color(16, 185, 129);     // Emerald
    public static final Color ROLE_OWNER_COLOR = new Color(245, 158, 11);       // Amber

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    public static JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_LIGHT);
        field.setCaretColor(TEXT_LIGHT);
        field.setFont(FONT_BODY);
        field.setBorder(createFieldBorder(INPUT_BORDER));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 42));

        // Focus glow effect
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(createFieldBorder(INPUT_FOCUS_BORDER));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(createFieldBorder(INPUT_BORDER));
            }
        });

        return field;
    }

    public static JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_LIGHT);
        field.setCaretColor(TEXT_LIGHT);
        field.setFont(FONT_BODY);
        field.setBorder(createFieldBorder(INPUT_BORDER));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 42));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(createFieldBorder(INPUT_FOCUS_BORDER));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(createFieldBorder(INPUT_BORDER));
            }
        });

        return field;
    }

    public static Border createFieldBorder(Color borderColor) {
        return new CompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        );
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
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
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 42));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(ACCENT_HOVER);
                btn.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(ACCENT_PRIMARY);
                btn.repaint();
            }
        });
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BODY);
        btn.setForeground(TEXT_LIGHT);
        btn.setBackground(ACCENT_SECONDARY);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 38));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(ACCENT_SECONDARY_HOVER);
                btn.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(ACCENT_SECONDARY);
                btn.repaint();
            }
        });
        return btn;
    }

    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(220, 224, 233));
        return label;
    }

    public static JScrollPane createModernScrollPane(Component view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        return scrollPane;
    }

    public static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(55, 61, 80);
            this.trackColor = BG_DARK;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 6, 6);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }
}
