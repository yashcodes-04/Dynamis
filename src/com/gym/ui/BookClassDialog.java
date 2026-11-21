/**
 * Date: 2026-11-21
 */
package com.gym.ui;

import com.gym.dao.BookingDAO;
import com.gym.model.FitnessClass;
import com.gym.model.User;
import com.gym.session.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class BookClassDialog extends JDialog {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final Runnable onSuccessCallback;
    private final List<FitnessClass> classList;

    private JTable classTable;
    private DefaultTableModel tableModel;
    private JLabel summaryLabel;
    private JButton bookButton;

    public BookClassDialog(Frame parent, Runnable onSuccessCallback) {
        super(parent, "Book a Fitness Class - Dynamis Gym", true);
        this.onSuccessCallback = onSuccessCallback;
        this.classList = FitnessClass.getDefaultClasses();

        setSize(920, 620);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.CARD_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.CARD_BORDER),
                new EmptyBorder(18, 26, 18, 26)
        ));

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel tagLabel = new JLabel("⚡ DYNAMIS GROUP TRAINING & STUDIO SESSIONS");
        tagLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tagLabel.setForeground(Theme.ACCENT_PRIMARY);

        JLabel titleLabel = new JLabel("Book a Fitness Class");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Theme.TEXT_LIGHT);

        JLabel subLabel = new JLabel("Click any session or use checkboxes to book multiple sessions. Select your slots and confirm.");
        subLabel.setFont(Theme.FONT_SUBTITLE);
        subLabel.setForeground(Theme.TEXT_MUTED);

        headerLeft.add(tagLabel);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(titleLabel);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(subLabel);

        // Header Right - Quick selection buttons
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);

        JButton selectAllBtn = Theme.createSecondaryButton("Select All");
        selectAllBtn.setFont(Theme.FONT_SMALL);
        selectAllBtn.setPreferredSize(new Dimension(100, 32));
        selectAllBtn.addActionListener(e -> setAllSelected(true));

        JButton clearAllBtn = Theme.createSecondaryButton("Clear Selection");
        clearAllBtn.setFont(Theme.FONT_SMALL);
        clearAllBtn.setPreferredSize(new Dimension(115, 32));
        clearAllBtn.addActionListener(e -> setAllSelected(false));

        headerRight.add(selectAllBtn);
        headerRight.add(clearAllBtn);

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content - Modern Table
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Theme.BG_DARK);
        tableContainer.setBorder(new EmptyBorder(16, 26, 16, 26));

        String[] columns = {"Book", "Class", "Level", "Duration", "Price / Session", "Schedule Slot"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        for (FitnessClass fc : classList) {
            tableModel.addRow(new Object[]{
                    Boolean.FALSE,
                    fc.getClassName(),
                    fc.getLevel(),
                    fc.getDuration(),
                    "Rs. " + String.format("%,.0f", fc.getPrice()),
                    fc.getScheduleSlot()
            });
        }

        classTable = new JTable(tableModel);
        styleClassTable(classTable);

        // Allow clicking anywhere on the row to toggle checkbox
        classTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = classTable.rowAtPoint(e.getPoint());
                int col = classTable.columnAtPoint(e.getPoint());
                if (row >= 0) {
                    if (col != 0) {
                        Boolean current = (Boolean) tableModel.getValueAt(row, 0);
                        tableModel.setValueAt(!Boolean.TRUE.equals(current), row, 0);
                    }
                    updateSummary();
                }
            }
        });

        tableModel.addTableModelListener(e -> updateSummary());

        JScrollPane scrollPane = Theme.createModernScrollPane(classTable);
        scrollPane.setBorder(new LineBorder(Theme.CARD_BORDER, 1, true));
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(Theme.CARD_BG);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.CARD_BORDER),
                new EmptyBorder(16, 26, 16, 26)
        ));

        summaryLabel = new JLabel("Selected: 0 sessions  |  Total: Rs. 0");
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryLabel.setForeground(Theme.TEXT_MUTED);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonGroup.setOpaque(false);

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(110, 42));
        cancelBtn.addActionListener(e -> dispose());

        bookButton = Theme.createPrimaryButton("Book My Sessions (0)");
        bookButton.setPreferredSize(new Dimension(220, 42));
        bookButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bookButton.addActionListener(e -> handleBookSessions());

        buttonGroup.add(cancelBtn);
        buttonGroup.add(bookButton);

        bottomBar.add(summaryLabel, BorderLayout.WEST);
        bottomBar.add(buttonGroup, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void styleClassTable(JTable table) {
        table.setBackground(Theme.CARD_BG);
        table.setForeground(Theme.TEXT_LIGHT);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(44);
        table.setGridColor(Theme.CARD_BORDER);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(new Color(45, 52, 70));
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        // Header Styling
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, false, row, column);
                c.setFont(new Font("Segoe UI", Font.BOLD, 13));
                c.setForeground(new Color(20, 22, 28));
                c.setBackground(new Color(230, 233, 240));
                ((JLabel) c).setHorizontalAlignment(column == 0 ? JLabel.CENTER : JLabel.LEFT);
                ((JLabel) c).setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        table.getTableHeader().setDefaultRenderer(headerRenderer);
        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getWidth(), 38));
        table.getTableHeader().setReorderingAllowed(false);

        // Checkbox Renderer
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(0).setMaxWidth(65);
        table.getColumnModel().getColumn(0).setCellRenderer(new CheckBoxCellRenderer());

        // Column widths
        table.getColumnModel().getColumn(1).setPreferredWidth(190); // Class Name
        table.getColumnModel().getColumn(2).setPreferredWidth(130); // Level
        table.getColumnModel().getColumn(3).setPreferredWidth(110); // Duration
        table.getColumnModel().getColumn(4).setPreferredWidth(140); // Price / Session
        table.getColumnModel().getColumn(5).setPreferredWidth(210); // Schedule Slot

        // Custom Renderer for regular columns
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, false, row, column);
                boolean isChecked = Boolean.TRUE.equals(tbl.getValueAt(row, 0));

                if (isChecked) {
                    c.setBackground(new Color(40, 48, 66));
                } else {
                    c.setBackground(row % 2 == 0 ? Theme.CARD_BG : new Color(28, 32, 44));
                }

                if (column == 1) { // Class Name
                    c.setFont(Theme.FONT_BOLD);
                    c.setForeground(isChecked ? Theme.ACCENT_PRIMARY : Theme.TEXT_LIGHT);
                } else if (column == 4) { // Price / Session
                    c.setFont(Theme.FONT_BOLD);
                    c.setForeground(Theme.SUCCESS_COLOR);
                } else if (column == 2) { // Level
                    c.setFont(Theme.FONT_SMALL);
                    c.setForeground(new Color(147, 197, 253));
                } else {
                    c.setFont(Theme.FONT_BODY);
                    c.setForeground(Theme.TEXT_LIGHT);
                }

                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };

        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    private static class CheckBoxCellRenderer extends JCheckBox implements TableCellRenderer {
        public CheckBoxCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setSelected(Boolean.TRUE.equals(value));
            boolean isChecked = Boolean.TRUE.equals(value);
            if (isChecked) {
                setBackground(new Color(40, 48, 66));
            } else {
                setBackground(row % 2 == 0 ? Theme.CARD_BG : new Color(28, 32, 44));
            }
            return this;
        }
    }

    private void setAllSelected(boolean selected) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(selected, i, 0);
        }
        updateSummary();
    }

    private void updateSummary() {
        int count = 0;
        double total = 0.0;
        List<String> names = new ArrayList<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, 0))) {
                count++;
                FitnessClass fc = classList.get(i);
                total += fc.getPrice();
                names.add(fc.getClassName());
            }
        }

        if (count == 0) {
            summaryLabel.setText("Selected: 0 sessions  |  Total: Rs. 0");
            summaryLabel.setForeground(Theme.TEXT_MUTED);
            bookButton.setText("Book My Sessions (0)");
        } else {
            String namesStr = String.join(", ", names);
            if (namesStr.length() > 35) {
                namesStr = namesStr.substring(0, 32) + "...";
            }
            summaryLabel.setText("✓ Selected: " + count + " session(s) [" + namesStr + "]  |  Total: Rs. " + String.format("%,.0f", total));
            summaryLabel.setForeground(Theme.SUCCESS_COLOR);
            bookButton.setText("Book My Sessions (" + count + ")");
        }
    }

    private void handleBookSessions() {
        List<FitnessClass> selected = new ArrayList<>();
        double total = 0.0;
        StringBuilder classDetails = new StringBuilder();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, 0))) {
                FitnessClass fc = classList.get(i);
                selected.add(fc);
                total += fc.getPrice();
                classDetails.append("  • ").append(fc.getClassName())
                        .append(" (").append(fc.getDuration()).append(" - Rs. ")
                        .append(String.format("%,.0f", fc.getPrice())).append(") [")
                        .append(fc.getScheduleSlot()).append("]\n");
            }
        }

        if (selected.isEmpty()) {
            ThemedDialog.showWarning(
                    this,
                    "No Session Selected",
                    "Please select at least one fitness session using the checkboxes to book."
            );
            return;
        }

        User user = UserSession.getCurrentUser();
        if (user == null) {
            user = new User();
            user.setGymId("MEMBER-DEMO");
            user.setEmail("member@dynamis.com");
            user.setFullName("Valued Member");
        }

        boolean success = bookingDAO.bookSessions(user, selected);

        if (success) {
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }

            ThemedDialog.showSuccess(
                    this,
                    "Sessions Booked Successfully",
                    "You have successfully booked " + selected.size() + " session(s):\n"
                            + classDetails.toString() + "\n"
                            + "Total Price: Rs. " + String.format("%,.0f", total) + "\n\n"
                            + "Your booked sessions are now visible under 'My Booked Sessions' on your Member Dashboard."
            );

            dispose();
        } else {
            ThemedDialog.showError(
                    this,
                    "Booking Error",
                    "Unable to save booking. Please check database connection and try again."
            );
        }
    }
}
