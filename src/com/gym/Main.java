/**
 * Date: 2026-11-16 - Yash
 */
package com.gym;

import com.gym.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Enable high-DPI font anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame app = new MainFrame();
            app.setVisible(true);
        });
    }
}
