/**
 * Date: 2026-11-23
 * Syllabus: Unit 1 (Lectures 1-3) - Custom Exception Handling & Validation
 *           Unit 2 (Lectures 16-25) - Collections Framework (ArrayList, HashSet, HashMap), Comparator & Sorting
 *           Unit 3 (Lectures 26-31) - Multithreading, Runnable, Daemon Thread, Synchronization
 *           Unit 4 (Lectures 34-42) - JDBC, PreparedStatement, CRUD & ResultSet Mapping
 *           Unit 5 (Lectures 43-48) - Lambda Expressions, Stream API Pipelines & Generics
 *           Unit 6 (Lectures 49-54) - File Streams, Character Streams (BufferedWriter/FileWriter) & CSV Export
 */
package com.gym.ui.dashboard;

import com.gym.dao.TrainerDAO;
import com.gym.exception.GymException;
import com.gym.model.DietPlan;
import com.gym.model.Role;
import com.gym.model.Trainee;
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

public class TrainerDashboard extends BaseDashboard {

    private final TrainerDAO trainerDAO = new TrainerDAO();

    // Screen identifiers for CardLayout
    private static final String SCREEN_MY_TRAINEES = "SCREEN_MY_TRAINEES";
    private static final String SCREEN_DIET_PLANS = "SCREEN_DIET_PLANS";

    private final CardLayout screenCardLayout = new CardLayout();
    private final JPanel screensContainer = new JPanel(screenCardLayout);

    // Nav Buttons
    private JButton tabTraineesBtn;
    private JButton tabDietPlansBtn;

    // Top Metric Labels
    private JLabel totalTraineesVal;
    private JLabel ectomorphCountVal;
    private JLabel mesomorphCountVal;
    private JLabel endomorphCountVal;
    private JLabel totalDietPlansVal;
    private JLabel deskClockVal;
    private LiveDeskTracker liveDeskTracker;

    // ==========================================
    // SCREEN 1: MY TRAINEES FIELDS
    // ==========================================
    private JTable traineeTable;
    private DefaultTableModel traineeTableModel;
    private JTextField traineeSearchField;
    private JComboBox<String> traineeBodyTypeCombo;
    private JComboBox<String> traineeSortCombo;
    private JLabel traineeCountLabel;
    private List<Trainee> currentLoadedTrainees;

    // Trainee Assessment & Assignment Form Fields
    private JTextField formGymIdField;
    private JTextField formNameField;
    private JTextField formEmailField;
    private JTextField formPhoneField;
    private JComboBox<String> formBodyTypeCombo;
    private JTextField formGoalField;
    private JTextField formCurrentWeightField;
    private JTextField formTargetWeightField;
    private JTextField formHeightField;
    private JComboBox<String> formDietPlanCombo;
    private JTextArea formNotesArea;
    private JLabel formBmiPreviewLabel;
    private JLabel formStatusLabel;
    private JButton saveTraineeBtn;

    // ==========================================
    // SCREEN 2: DIET PLANS FIELDS
    // ==========================================
    private JTable dietPlanTable;
    private DefaultTableModel dietPlanTableModel;
    private JComboBox<String> dietBodyTypeFilterCombo;
    private JTextField dietSearchField;
    private JLabel dietPlanCountLabel;
    private List<DietPlan> currentLoadedDietPlans;
    private DietPlan currentSelectedPlan;

    // Detailed Plan Card Labels
    private JLabel detailPlanTitle;
    private JLabel detailBodyTypeBadge;
    private JLabel detailGoalLabel;
    private JLabel detailCaloriesVal;
    private JLabel detailProteinVal;
    private JLabel detailCarbsVal;
    private JLabel detailFatsVal;
    private JLabel detailWaterVal;
    private JTextArea detailBreakfastArea;
    private JTextArea detailSnackArea;
    private JTextArea detailLunchArea;
    private JTextArea detailPreWorkoutArea;
    private JTextArea detailPostWorkoutArea;
    private JTextArea detailDinnerArea;
    private JTextArea detailGuidelinesArea;
    private JButton assignPlanToTraineeBtn;
    private JButton exportPlanBtn;

    public TrainerDashboard(MainFrame mainFrame) {
        super(mainFrame, "Trainer Workspace & Trainee Portal", Role.TRAINER);
        buildDashboard();
        startLiveTracker();
        loadAllData();
    }

    private void buildDashboard() {
        contentPanel.setLayout(new BorderLayout(0, 18));

        // 1. Top Metrics Bar (5 Metrics)
        JPanel metricsContainer = new JPanel(new GridLayout(1, 5, 12, 0));
        metricsContainer.setOpaque(false);

        metricsContainer.add(createMetricCard("TOTAL TRAINEES", "0 Members", "Active client roster", Theme.ROLE_TRAINER_COLOR, totalTraineesVal = new JLabel("0")));
        metricsContainer.add(createMetricCard("ECTOMORPHS", "0 Clients", "High calorie / Hypertrophy", new Color(56, 189, 248), ectomorphCountVal = new JLabel("0")));
        metricsContainer.add(createMetricCard("MESOMORPHS", "0 Clients", "Athletic / Recomp", Theme.SUCCESS_COLOR, mesomorphCountVal = new JLabel("0")));
        metricsContainer.add(createMetricCard("ENDOMORPHS", "0 Clients", "Low-carb / Fat loss", Theme.ROLE_OWNER_COLOR, endomorphCountVal = new JLabel("0")));
        metricsContainer.add(createMetricCard("DIET PLANS", "0 Plans", "Curated for all body types", Theme.ROLE_RECEPTIONIST_COLOR, totalDietPlansVal = new JLabel("0")));

        // 2. Navigation Tab Bar (2 Main Pages as requested)
        JPanel navTabBar = buildNavigationTabBar();

        // 3. Header Container
        JPanel topSection = new JPanel(new BorderLayout(0, 14));
        topSection.setOpaque(false);
        topSection.add(metricsContainer, BorderLayout.NORTH);
        topSection.add(navTabBar, BorderLayout.SOUTH);
        contentPanel.add(topSection, BorderLayout.NORTH);

        // 4. Screens Container with CardLayout
        screensContainer.setOpaque(false);
        screensContainer.add(buildScreen1_MyTrainees(), SCREEN_MY_TRAINEES);
        screensContainer.add(buildScreen2_DietPlans(), SCREEN_DIET_PLANS);

        contentPanel.add(screensContainer, BorderLayout.CENTER);

        // Default screen: My Trainees
        switchScreen(SCREEN_MY_TRAINEES);
    }

    /**
     * Syllabus Unit 3 (Lectures 26-29): Background Multithreading with Daemon Thread
     */
    private void startLiveTracker() {
        liveDeskTracker = new LiveDeskTracker(null, null);
        liveDeskTracker.start();
    }

    private JPanel buildNavigationTabBar() {
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        navBar.setOpaque(false);

        tabTraineesBtn = createTabButton("1. My Trainees (Roster & Assessment)");
        tabDietPlansBtn = createTabButton("2. Diet Plans for Every Body Type (Ectomorph / Mesomorph / Endomorph)");

        tabTraineesBtn.addActionListener(e -> switchScreen(SCREEN_MY_TRAINEES));
        tabDietPlansBtn.addActionListener(e -> switchScreen(SCREEN_DIET_PLANS));

        navBar.add(tabTraineesBtn);
        navBar.add(tabDietPlansBtn);

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

        tabTraineesBtn.setBackground(SCREEN_MY_TRAINEES.equals(screenName) ? Theme.ACCENT_PRIMARY : Theme.ACCENT_SECONDARY);
        tabDietPlansBtn.setBackground(SCREEN_DIET_PLANS.equals(screenName) ? Theme.ACCENT_PRIMARY : Theme.ACCENT_SECONDARY);

        tabTraineesBtn.repaint();
        tabDietPlansBtn.repaint();

        if (SCREEN_MY_TRAINEES.equals(screenName)) {
            loadTraineeData();
        } else if (SCREEN_DIET_PLANS.equals(screenName)) {
            loadDietPlanData();
        }
    }

    // =========================================================================
    // PAGE 1: MY TRAINEES (Client Roster, Filters, Assessment & Diet Assignment)
    // =========================================================================
    private JPanel buildScreen1_MyTrainees() {
        JPanel screen = new JPanel(new BorderLayout(0, 16));
        screen.setOpaque(false);

        // Top Filter Bar & Action Card
        JPanel tableCard = new JPanel(new BorderLayout(0, 12));
        tableCard.setBackground(Theme.CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel topControls = new JPanel(new BorderLayout(10, 0));
        topControls.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Assigned Trainee Roster & Fitness Progress");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_LIGHT);

        traineeCountLabel = new JLabel("Loading trainees...");
        traineeCountLabel.setFont(Theme.FONT_SMALL);
        traineeCountLabel.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(traineeCountLabel);

        // Search & Filter Controls (Syllabus Unit 2 & Unit 5: Collections, Comparators & Streams)
        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterControls.setOpaque(false);

        String[] bodyTypes = {"All Body Types", "Ectomorph", "Mesomorph", "Endomorph", "Universal"};
        traineeBodyTypeCombo = new JComboBox<>(bodyTypes);
        styleComboBox(traineeBodyTypeCombo);
        traineeBodyTypeCombo.setPreferredSize(new Dimension(150, 36));
        traineeBodyTypeCombo.addActionListener(e -> loadTraineeData());

        String[] sortOptions = {"Sort: Name (A-Z)", "Sort: Weight (High-Low)", "Sort: Body Type", "Sort: Attendance"};
        traineeSortCombo = new JComboBox<>(sortOptions);
        styleComboBox(traineeSortCombo);
        traineeSortCombo.setPreferredSize(new Dimension(170, 36));
        traineeSortCombo.addActionListener(e -> applyTraineeSorting());

        traineeSearchField = Theme.createStyledTextField();
        traineeSearchField.setPreferredSize(new Dimension(180, 36));
        traineeSearchField.setToolTipText("Filter by Gym ID, Name, or Goal");
        traineeSearchField.addActionListener(e -> loadTraineeData());

        JButton searchBtn = Theme.createSecondaryButton("Search");
        searchBtn.addActionListener(e -> loadTraineeData());

        JButton refreshBtn = Theme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> {
            traineeSearchField.setText("");
            traineeBodyTypeCombo.setSelectedIndex(0);
            loadTraineeData();
        });

        // Syllabus Unit 6 (Lectures 49-54): CSV Export using Character Streams
        JButton exportCsvBtn = Theme.createSecondaryButton("Export CSV");
        exportCsvBtn.setBackground(new Color(22, 101, 52)); // Subtle forest green
        exportCsvBtn.addActionListener(e -> handleExportTraineesCSV());

        filterControls.add(traineeBodyTypeCombo);
        filterControls.add(traineeSortCombo);
        filterControls.add(traineeSearchField);
        filterControls.add(searchBtn);
        filterControls.add(refreshBtn);
        filterControls.add(exportCsvBtn);

        topControls.add(titleBlock, BorderLayout.WEST);
        topControls.add(filterControls, BorderLayout.EAST);
        tableCard.add(topControls, BorderLayout.NORTH);

        // Table
        String[] cols = {"Gym ID", "Member Name", "Email", "Phone", "Body Type", "Fitness Goal", "Current Wt", "Target Wt", "Height", "BMI", "Assigned Diet Plan", "Status"};
        traineeTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        traineeTable = new JTable(traineeTableModel);
        styleTable(traineeTable);

        traineeTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        traineeTable.getColumnModel().getColumn(1).setPreferredWidth(125);
        traineeTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        traineeTable.getColumnModel().getColumn(3).setPreferredWidth(95);
        traineeTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        traineeTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        traineeTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        traineeTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        traineeTable.getColumnModel().getColumn(8).setPreferredWidth(70);
        traineeTable.getColumnModel().getColumn(9).setPreferredWidth(65);
        traineeTable.getColumnModel().getColumn(10).setPreferredWidth(170);
        traineeTable.getColumnModel().getColumn(11).setPreferredWidth(75);

        // Selection listener to populate assessment form
        traineeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedTrainee();
            }
        });

        JScrollPane tableScroll = Theme.createModernScrollPane(traineeTable);
        tableScroll.setPreferredSize(new Dimension(tableScroll.getPreferredSize().width, 240));
        tableCard.add(tableScroll, BorderLayout.CENTER);

        // Bottom Assessment & Diet Assignment Form
        JPanel formCard = buildTraineeAssessmentFormCard();

        screen.add(tableCard, BorderLayout.CENTER);
        screen.add(formCard, BorderLayout.SOUTH);

        return screen;
    }

    private JPanel buildTraineeAssessmentFormCard() {
        JPanel formCard = new JPanel(new BorderLayout(0, 12));
        formCard.setBackground(Theme.CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel formTitle = new JLabel("Trainee Assessment, Target Setting & Diet Plan Assignment");
        formTitle.setFont(Theme.FONT_HEADER);
        formTitle.setForeground(Theme.TEXT_LIGHT);

        JLabel formSub = new JLabel("Select a trainee from the table above or enter credentials to update profile & prescribe a body-type diet plan.");
        formSub.setFont(Theme.FONT_SUBTITLE);
        formSub.setForeground(Theme.TEXT_MUTED);

        JPanel headerBox = new JPanel();
        headerBox.setLayout(new BoxLayout(headerBox, BoxLayout.Y_AXIS));
        headerBox.setOpaque(false);
        headerBox.add(formTitle);
        headerBox.add(Box.createVerticalStrut(3));
        headerBox.add(formSub);
        formCard.add(headerBox, BorderLayout.NORTH);

        // Grid of inputs (3 columns)
        JPanel grid = new JPanel(new GridLayout(3, 4, 14, 10));
        grid.setOpaque(false);

        formGymIdField = Theme.createStyledTextField();
        formGymIdField.setToolTipText("e.g. 20261001");
        formNameField = Theme.createStyledTextField();
        formEmailField = Theme.createStyledTextField();
        formPhoneField = Theme.createStyledTextField();

        String[] types = {"Ectomorph", "Mesomorph", "Endomorph", "Universal"};
        formBodyTypeCombo = new JComboBox<>(types);
        styleComboBox(formBodyTypeCombo);

        formGoalField = Theme.createStyledTextField();
        formGoalField.setText("Hypertrophy / Bulk");

        formCurrentWeightField = Theme.createStyledTextField();
        formCurrentWeightField.setText("70.0");
        formTargetWeightField = Theme.createStyledTextField();
        formTargetWeightField.setText("75.0");

        formHeightField = Theme.createStyledTextField();
        formHeightField.setText("175.0");

        formDietPlanCombo = new JComboBox<>();
        styleComboBox(formDietPlanCombo);
        refreshDietPlanComboOptions();

        formNotesArea = new JTextArea(2, 20);
        formNotesArea.setBackground(Theme.INPUT_BG);
        formNotesArea.setForeground(Theme.TEXT_LIGHT);
        formNotesArea.setFont(Theme.FONT_BODY);
        formNotesArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        formNotesArea.setLineWrap(true);
        formNotesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = Theme.createModernScrollPane(formNotesArea);
        notesScroll.setPreferredSize(new Dimension(notesScroll.getPreferredSize().width, 42));

        formBmiPreviewLabel = new JLabel("BMI: 22.9 (Normal / Athletic)");
        formBmiPreviewLabel.setFont(Theme.FONT_BOLD);
        formBmiPreviewLabel.setForeground(Theme.SUCCESS_COLOR);

        // Add to grid
        addFormGridField(grid, "Gym ID / Roll No:", formGymIdField);
        addFormGridField(grid, "Full Name:", formNameField);
        addFormGridField(grid, "Email Address:", formEmailField);
        addFormGridField(grid, "Phone Number:", formPhoneField);

        addFormGridField(grid, "Body Type (Metabolism):", formBodyTypeCombo);
        addFormGridField(grid, "Fitness Goal:", formGoalField);
        addFormGridField(grid, "Current Weight (kg):", formCurrentWeightField);
        addFormGridField(grid, "Target Goal Weight (kg):", formTargetWeightField);

        addFormGridField(grid, "Height (cm):", formHeightField);
        addFormGridField(grid, "Assign Diet Plan:", formDietPlanCombo);
        addFormGridField(grid, "Trainer Coaching Notes:", notesScroll);
        addFormGridField(grid, "Calculated BMI & Status:", formBmiPreviewLabel);

        formCard.add(grid, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottomActions.setOpaque(false);

        formStatusLabel = new JLabel(" ");
        formStatusLabel.setFont(Theme.FONT_SMALL);

        saveTraineeBtn = Theme.createPrimaryButton("Save Assessment & Prescribe Diet Plan");
        saveTraineeBtn.setPreferredSize(new Dimension(280, 42));
        saveTraineeBtn.addActionListener(e -> handleSaveTrainee());

        JButton clearFormBtn = Theme.createSecondaryButton("Clear Form");
        clearFormBtn.addActionListener(e -> clearTraineeForm());

        bottomActions.add(formStatusLabel);
        bottomActions.add(clearFormBtn);
        bottomActions.add(saveTraineeBtn);
        formCard.add(bottomActions, BorderLayout.SOUTH);

        return formCard;
    }

    private void addFormGridField(JPanel parent, String labelText, JComponent component) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = Theme.createFieldLabel(labelText);
        p.add(lbl, BorderLayout.NORTH);
        p.add(component, BorderLayout.CENTER);
        parent.add(p);
    }

    private void refreshDietPlanComboOptions() {
        if (formDietPlanCombo == null) return;
        formDietPlanCombo.removeAllItems();
        List<DietPlan> plans = trainerDAO.getAllDietPlans();
        for (DietPlan dp : plans) {
            formDietPlanCombo.addItem(dp.getPlanName() + " (" + dp.getBodyType() + ")");
        }
    }

    private void populateFormFromSelectedTrainee() {
        int selected = traineeTable.getSelectedRow();
        if (selected < 0 || currentLoadedTrainees == null || selected >= currentLoadedTrainees.size()) return;

        Trainee t = currentLoadedTrainees.get(selected);
        formGymIdField.setText(t.getGymId());
        formNameField.setText(t.getFullName());
        formEmailField.setText(t.getEmail());
        formPhoneField.setText(t.getPhone());
        formBodyTypeCombo.setSelectedItem(t.getBodyType());
        formGoalField.setText(t.getFitnessGoal());
        formCurrentWeightField.setText(String.valueOf(t.getCurrentWeight()));
        formTargetWeightField.setText(String.valueOf(t.getTargetWeight()));
        formHeightField.setText(String.valueOf(t.getHeightCm()));
        formNotesArea.setText(t.getTrainerNotes() != null ? t.getTrainerNotes() : "");

        double bmi = t.getBmi();
        formBmiPreviewLabel.setText(String.format("BMI: %.1f (%s)", bmi, t.getBmiCategory()));
        formBmiPreviewLabel.setForeground(bmi >= 18.5 && bmi <= 25.0 ? Theme.SUCCESS_COLOR : Theme.ROLE_OWNER_COLOR);

        // Match diet plan combo
        for (int i = 0; i < formDietPlanCombo.getItemCount(); i++) {
            String item = formDietPlanCombo.getItemAt(i);
            if (t.getAssignedDietPlan() != null && item.toLowerCase().contains(t.getAssignedDietPlan().toLowerCase())) {
                formDietPlanCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void handleSaveTrainee() {
        String name = formNameField.getText().trim();
        String gymId = formGymIdField.getText().trim();
        String email = formEmailField.getText().trim();
        String phone = formPhoneField.getText().trim();
        String bodyType = (String) formBodyTypeCombo.getSelectedItem();
        String goal = formGoalField.getText().trim();
        String notes = formNotesArea.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            ThemedDialog.showError(this, "Validation Error", "Full Name and Registered Email are required fields.");
            return;
        }

        double currWeight, targetWeight, height;
        try {
            currWeight = Double.parseDouble(formCurrentWeightField.getText().trim());
            targetWeight = Double.parseDouble(formTargetWeightField.getText().trim());
            height = Double.parseDouble(formHeightField.getText().trim());
        } catch (NumberFormatException ex) {
            ThemedDialog.showError(this, "Validation Error", "Weight and Height must be valid numeric values (e.g. 72.5, 175.0).");
            return;
        }

        String rawPlan = (String) formDietPlanCombo.getSelectedItem();
        String assignedPlan = rawPlan != null ? rawPlan.split(" \\(")[0] : "Standard Maintenance";

        Trainee trainee = new Trainee(
                0, gymId, name, email, phone, bodyType, goal, currWeight, targetWeight, height,
                assignedPlan, notes, "Lead Trainer", 10, "Active"
        );

        saveTraineeBtn.setEnabled(false);
        saveTraineeBtn.setText("Saving Assessment...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return trainerDAO.upsertTrainee(trainee);
            }

            @Override
            protected void done() {
                saveTraineeBtn.setEnabled(true);
                saveTraineeBtn.setText("Save Assessment & Prescribe Diet Plan");
                try {
                    boolean ok = get();
                    if (ok) {
                        ThemedDialog.showSuccess(
                                TrainerDashboard.this,
                                "Assessment Saved",
                                "Trainee profile for " + name + " (" + bodyType + ") has been updated.\n"
                                        + "Prescribed Diet: " + assignedPlan + "\n"
                                        + "Target Weight: " + targetWeight + " kg"
                        );
                        loadAllData();
                    }
                } catch (Exception ex) {
                    ThemedDialog.showError(TrainerDashboard.this, "Save Error", ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void clearTraineeForm() {
        formGymIdField.setText("");
        formNameField.setText("");
        formEmailField.setText("");
        formPhoneField.setText("");
        formGoalField.setText("Hypertrophy / Bulk");
        formCurrentWeightField.setText("70.0");
        formTargetWeightField.setText("75.0");
        formHeightField.setText("175.0");
        formNotesArea.setText("");
        formBmiPreviewLabel.setText("BMI: --");
        traineeTable.clearSelection();
    }

    private void loadTraineeData() {
        String keyword = traineeSearchField != null ? traineeSearchField.getText().trim() : "";
        String bodyType = traineeBodyTypeCombo != null ? (String) traineeBodyTypeCombo.getSelectedItem() : "All Body Types";

        SwingWorker<List<Trainee>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Trainee> doInBackground() {
                return trainerDAO.filterTrainees(keyword, bodyType);
            }

            @Override
            protected void done() {
                try {
                    currentLoadedTrainees = get();
                    applyTraineeSorting();
                } catch (Exception ex) {
                    traineeCountLabel.setText("Error loading trainees: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Syllabus Unit 2 (Lectures 23-25): Collections Sorting using Comparator
     */
    private void applyTraineeSorting() {
        if (currentLoadedTrainees == null || currentLoadedTrainees.isEmpty()) {
            traineeTableModel.setRowCount(0);
            traineeCountLabel.setText("Showing 0 trainees");
            return;
        }

        int sortIdx = traineeSortCombo != null ? traineeSortCombo.getSelectedIndex() : 0;
        Comparator<Trainee> comp;
        switch (sortIdx) {
            case 1: comp = Trainee.BY_CURRENT_WEIGHT_DESC; break;
            case 2: comp = Trainee.BY_BODY_TYPE; break;
            case 3: comp = Trainee.BY_ATTENDANCE_DESC; break;
            case 0:
            default: comp = Trainee.BY_NAME_ASC; break;
        }

        currentLoadedTrainees.sort(comp);
        traineeTableModel.setRowCount(0);

        for (Trainee t : currentLoadedTrainees) {
            traineeTableModel.addRow(new Object[]{
                    t.getGymId() != null ? t.getGymId() : "N/A",
                    t.getFullName(),
                    t.getEmail(),
                    t.getPhone() != null ? t.getPhone() : "--",
                    t.getBodyType(),
                    t.getFitnessGoal(),
                    String.format("%.1f kg", t.getCurrentWeight()),
                    String.format("%.1f kg", t.getTargetWeight()),
                    String.format("%.0f cm", t.getHeightCm()),
                    String.format("%.1f", t.getBmi()),
                    t.getAssignedDietPlan(),
                    t.getStatus()
            });
        }

        traineeCountLabel.setText("Showing " + currentLoadedTrainees.size() + " active trainees");
    }

    private void handleExportTraineesCSV() {
        if (currentLoadedTrainees == null || currentLoadedTrainees.isEmpty()) {
            ThemedDialog.showInfo(this, "Export Notice", "No trainee records currently loaded to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("gym_trainees_roster.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try {
                TrainerDAO.exportTraineesToCSV(currentLoadedTrainees, target);
                ThemedDialog.showSuccess(this, "Export Succeeded", "Trainee roster exported successfully to:\n" + target.getAbsolutePath());
            } catch (GymException ex) {
                ThemedDialog.showError(this, "Export Error", ex.getMessage());
            }
        }
    }

    // =========================================================================
    // PAGE 2: BODY TYPE DIET PLANS (Ectomorph / Mesomorph / Endomorph)
    // =========================================================================
    private JPanel buildScreen2_DietPlans() {
        JPanel screen = new JPanel(new GridLayout(1, 2, 20, 0));
        screen.setOpaque(false);

        // LEFT COLUMN: Diet Plans Directory & Body-type filters
        JPanel catalogCard = new JPanel(new BorderLayout(0, 14));
        catalogCard.setBackground(Theme.CARD_BG);
        catalogCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel catTop = new JPanel(new BorderLayout(10, 0));
        catTop.setOpaque(false);

        JPanel catTitleBlock = new JPanel();
        catTitleBlock.setLayout(new BoxLayout(catTitleBlock, BoxLayout.Y_AXIS));
        catTitleBlock.setOpaque(false);

        JLabel catTitle = new JLabel("Curated Diet Plans Directory");
        catTitle.setFont(Theme.FONT_HEADER);
        catTitle.setForeground(Theme.TEXT_LIGHT);

        dietPlanCountLabel = new JLabel("Loading nutrition plans...");
        dietPlanCountLabel.setFont(Theme.FONT_SMALL);
        dietPlanCountLabel.setForeground(Theme.TEXT_MUTED);

        catTitleBlock.add(catTitle);
        catTitleBlock.add(Box.createVerticalStrut(2));
        catTitleBlock.add(dietPlanCountLabel);

        // Filters
        JPanel catFilters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        catFilters.setOpaque(false);

        String[] types = {"All Body Types", "Ectomorph", "Mesomorph", "Endomorph", "Universal"};
        dietBodyTypeFilterCombo = new JComboBox<>(types);
        styleComboBox(dietBodyTypeFilterCombo);
        dietBodyTypeFilterCombo.setPreferredSize(new Dimension(140, 36));
        dietBodyTypeFilterCombo.addActionListener(e -> loadDietPlanData());

        dietSearchField = Theme.createStyledTextField();
        dietSearchField.setPreferredSize(new Dimension(140, 36));
        dietSearchField.setToolTipText("Search by plan name or goal");
        dietSearchField.addActionListener(e -> loadDietPlanData());

        JButton catSearchBtn = Theme.createSecondaryButton("Search");
        catSearchBtn.addActionListener(e -> loadDietPlanData());

        catFilters.add(dietBodyTypeFilterCombo);
        catFilters.add(dietSearchField);
        catFilters.add(catSearchBtn);

        catTop.add(catTitleBlock, BorderLayout.WEST);
        catTop.add(catFilters, BorderLayout.EAST);
        catalogCard.add(catTop, BorderLayout.NORTH);

        // Diet Plans Table
        String[] cols = {"Plan Name", "Body Type", "Fitness Goal", "Calories", "Macros (P/C/F)"};
        dietPlanTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        dietPlanTable = new JTable(dietPlanTableModel);
        styleTable(dietPlanTable);

        dietPlanTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        dietPlanTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        dietPlanTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        dietPlanTable.getColumnModel().getColumn(3).setPreferredWidth(75);
        dietPlanTable.getColumnModel().getColumn(4).setPreferredWidth(120);

        dietPlanTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedDietPlanDetails();
            }
        });

        JScrollPane tableScroll = Theme.createModernScrollPane(dietPlanTable);
        catalogCard.add(tableScroll, BorderLayout.CENTER);

        // RIGHT COLUMN: Selected Plan Detailed Meal Schedule Card
        JPanel detailCard = buildDietPlanDetailCard();

        screen.add(catalogCard);
        screen.add(detailCard);

        return screen;
    }

    private JPanel buildDietPlanDetailCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        detailPlanTitle = new JLabel("Select a Diet Plan");
        detailPlanTitle.setFont(Theme.FONT_TITLE);
        detailPlanTitle.setForeground(Theme.TEXT_LIGHT);

        detailGoalLabel = new JLabel("Click any plan in the table to view the full meal plan and macros.");
        detailGoalLabel.setFont(Theme.FONT_SUBTITLE);
        detailGoalLabel.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(detailPlanTitle);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(detailGoalLabel);

        detailBodyTypeBadge = new JLabel("BODY TYPE: --");
        detailBodyTypeBadge.setFont(Theme.FONT_BOLD);
        detailBodyTypeBadge.setForeground(Theme.ROLE_MEMBER_COLOR);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(detailBodyTypeBadge, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Center: Macros grid + Meals breakdown
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Macro Cards Grid
        JPanel macrosGrid = new JPanel(new GridLayout(1, 5, 8, 0));
        macrosGrid.setOpaque(false);
        macrosGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        detailCaloriesVal = new JLabel("0 kcal");
        detailProteinVal = new JLabel("0 g");
        detailCarbsVal = new JLabel("0 g");
        detailFatsVal = new JLabel("0 g");
        detailWaterVal = new JLabel("0 L");

        macrosGrid.add(createMiniMacroCard("ENERGY", detailCaloriesVal, Theme.ACCENT_PRIMARY));
        macrosGrid.add(createMiniMacroCard("PROTEIN", detailProteinVal, Theme.SUCCESS_COLOR));
        macrosGrid.add(createMiniMacroCard("CARBS", detailCarbsVal, new Color(56, 189, 248)));
        macrosGrid.add(createMiniMacroCard("FATS", detailFatsVal, Theme.ROLE_OWNER_COLOR));
        macrosGrid.add(createMiniMacroCard("WATER", detailWaterVal, Theme.ROLE_RECEPTIONIST_COLOR));

        center.add(macrosGrid);
        center.add(Box.createVerticalStrut(14));

        // Meal Schedule Items in scrollable area
        JPanel mealsPanel = new JPanel();
        mealsPanel.setLayout(new BoxLayout(mealsPanel, BoxLayout.Y_AXIS));
        mealsPanel.setOpaque(false);

        detailBreakfastArea = createMealTextArea();
        detailSnackArea = createMealTextArea();
        detailLunchArea = createMealTextArea();
        detailPreWorkoutArea = createMealTextArea();
        detailPostWorkoutArea = createMealTextArea();
        detailDinnerArea = createMealTextArea();
        detailGuidelinesArea = createMealTextArea();

        addMealRow(mealsPanel, "Breakfast (07:30 AM)", detailBreakfastArea);
        addMealRow(mealsPanel, "Mid-Morning Fuel (10:30 AM)", detailSnackArea);
        addMealRow(mealsPanel, "Lunch - Anabolic Meal (01:30 PM)", detailLunchArea);
        addMealRow(mealsPanel, "Pre-Workout Energy (05:00 PM)", detailPreWorkoutArea);
        addMealRow(mealsPanel, "Post-Workout Recovery (07:30 PM)", detailPostWorkoutArea);
        addMealRow(mealsPanel, "Dinner - Night Protein (09:00 PM)", detailDinnerArea);
        addMealRow(mealsPanel, "Trainer Tips & Body-Type Rules", detailGuidelinesArea);

        JScrollPane mealsScroll = Theme.createModernScrollPane(mealsPanel);
        center.add(mealsScroll);
        card.add(center, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        assignPlanToTraineeBtn = Theme.createPrimaryButton("Assign This Plan to Trainee");
        assignPlanToTraineeBtn.addActionListener(e -> handleAssignDietPlanModal());

        exportPlanBtn = Theme.createSecondaryButton("Export Plan to Text");
        exportPlanBtn.addActionListener(e -> handleExportDietPlanText());

        actions.add(exportPlanBtn);
        actions.add(assignPlanToTraineeBtn);
        card.add(actions, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createMiniMacroCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.INPUT_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.INPUT_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(Theme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        valueLabel.setForeground(accentColor);

        p.add(titleLbl);
        p.add(Box.createVerticalStrut(2));
        p.add(valueLabel);
        return p;
    }

    private JTextArea createMealTextArea() {
        JTextArea area = new JTextArea(2, 30);
        area.setBackground(Theme.INPUT_BG);
        area.setForeground(Theme.TEXT_LIGHT);
        area.setFont(Theme.FONT_BODY);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return area;
    }

    private void addMealRow(JPanel parent, String title, JTextArea textArea) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JLabel lbl = new JLabel("• " + title);
        lbl.setFont(Theme.FONT_BOLD);
        lbl.setForeground(Theme.ROLE_MEMBER_COLOR);

        row.add(lbl, BorderLayout.NORTH);
        row.add(textArea, BorderLayout.CENTER);

        parent.add(row);
        parent.add(Box.createVerticalStrut(8));
    }

    private void loadDietPlanData() {
        String bodyType = (String) dietBodyTypeFilterCombo.getSelectedItem();
        String keyword = dietSearchField.getText().trim();

        SwingWorker<List<DietPlan>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DietPlan> doInBackground() {
                return trainerDAO.filterDietPlans(bodyType, keyword);
            }

            @Override
            protected void done() {
                try {
                    currentLoadedDietPlans = get();
                    dietPlanTableModel.setRowCount(0);

                    for (DietPlan p : currentLoadedDietPlans) {
                        dietPlanTableModel.addRow(new Object[]{
                                p.getPlanName(),
                                p.getBodyType(),
                                p.getGoal(),
                                p.getDailyCalories() + " kcal",
                                "P:" + p.getProteinGrams() + "g C:" + p.getCarbsGrams() + "g F:" + p.getFatsGrams() + "g"
                        });
                    }

                    dietPlanCountLabel.setText("Showing " + currentLoadedDietPlans.size() + " tailored diet plans");

                    if (!currentLoadedDietPlans.isEmpty()) {
                        dietPlanTable.setRowSelectionInterval(0, 0);
                        displaySelectedDietPlanDetails();
                    }
                } catch (Exception ex) {
                    dietPlanCountLabel.setText("Error loading diet plans: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void displaySelectedDietPlanDetails() {
        int selected = dietPlanTable.getSelectedRow();
        if (selected < 0 || currentLoadedDietPlans == null || selected >= currentLoadedDietPlans.size()) return;

        DietPlan plan = currentLoadedDietPlans.get(selected);
        currentSelectedPlan = plan;

        detailPlanTitle.setText(plan.getPlanName());
        detailGoalLabel.setText("Goal: " + plan.getGoal());
        detailBodyTypeBadge.setText("BODY TYPE: " + plan.getBodyType().toUpperCase());

        Color badgeColor;
        switch (plan.getBodyType().toLowerCase()) {
            case "ectomorph": badgeColor = new Color(56, 189, 248); break;
            case "endomorph": badgeColor = Theme.ROLE_OWNER_COLOR; break;
            case "universal": badgeColor = Theme.ROLE_RECEPTIONIST_COLOR; break;
            case "mesomorph":
            default: badgeColor = Theme.SUCCESS_COLOR; break;
        }
        detailBodyTypeBadge.setForeground(badgeColor);

        detailCaloriesVal.setText(plan.getDailyCalories() + " kcal");
        detailProteinVal.setText(plan.getProteinGrams() + " g");
        detailCarbsVal.setText(plan.getCarbsGrams() + " g");
        detailFatsVal.setText(plan.getFatsGrams() + " g");
        detailWaterVal.setText(plan.getWaterLiters() + " L");

        detailBreakfastArea.setText(plan.getBreakfast());
        detailSnackArea.setText(plan.getMidMorningSnack());
        detailLunchArea.setText(plan.getLunch());
        detailPreWorkoutArea.setText(plan.getPreWorkout());
        detailPostWorkoutArea.setText(plan.getPostWorkout());
        detailDinnerArea.setText(plan.getDinner());
        detailGuidelinesArea.setText(plan.getGuidelines());
    }

    private void handleAssignDietPlanModal() {
        if (currentSelectedPlan == null) {
            ThemedDialog.showInfo(this, "Select Plan", "Please select a diet plan from the catalog first.");
            return;
        }

        List<Trainee> trainees = trainerDAO.getAll();
        if (trainees.isEmpty()) {
            ThemedDialog.showInfo(this, "No Trainees", "There are no registered trainees in your roster yet.");
            return;
        }

        String[] traineeNames = trainees.stream()
                .map(t -> t.getFullName() + " (" + t.getGymId() + ") - " + t.getBodyType())
                .toArray(String[]::new);

        JComboBox<String> combo = new JComboBox<>(traineeNames);
        styleComboBox(combo);

        JPanel panel = new JPanel(new GridLayout(2, 1, 6, 6));
        panel.setBackground(Theme.CARD_BG);
        panel.add(Theme.createFieldLabel("Select Trainee to assign '" + currentSelectedPlan.getPlanName() + "':"));
        panel.add(combo);

        int res = JOptionPane.showConfirmDialog(this, panel, "Assign Diet Plan to Trainee", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            int idx = combo.getSelectedIndex();
            if (idx >= 0 && idx < trainees.size()) {
                Trainee target = trainees.get(idx);
                target.setAssignedDietPlan(currentSelectedPlan.getPlanName());
                try {
                    trainerDAO.upsertTrainee(target);
                    ThemedDialog.showSuccess(this, "Diet Plan Assigned",
                            "Diet Plan '" + currentSelectedPlan.getPlanName() + "' has been assigned to:\n"
                            + target.getFullName() + " (" + target.getGymId() + ").");
                    loadAllData();
                } catch (GymException ex) {
                    ThemedDialog.showError(this, "Assignment Error", ex.getMessage());
                }
            }
        }
    }

    private void handleExportDietPlanText() {
        if (currentSelectedPlan == null) {
            ThemedDialog.showInfo(this, "Select Plan", "Please select a diet plan from the list to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        String safeName = currentSelectedPlan.getPlanName().toLowerCase().replaceAll("[^a-z0-9]", "_") + ".txt";
        chooser.setSelectedFile(new File(safeName));

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try {
                TrainerDAO.exportDietPlanToText(currentSelectedPlan, target);
                ThemedDialog.showSuccess(this, "Plan Exported",
                        "Personalized Diet Plan has been written to:\n" + target.getAbsolutePath());
            } catch (GymException ex) {
                ThemedDialog.showError(this, "Export Error", ex.getMessage());
            }
        }
    }

    // =========================================================================
    // SHARED UTILITIES & METRICS (Syllabus Lectures 43-46: Stream Aggregations)
    // =========================================================================
    private void loadAllData() {
        loadMetrics();
        loadTraineeData();
        loadDietPlanData();
        refreshDietPlanComboOptions();
    }

    /**
     * Syllabus Unit 5 (Lectures 43-46): Stream API filtering, counting, and mapping
     */
    private void loadMetrics() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private int total = 0;
            private long ecto = 0;
            private long meso = 0;
            private long endo = 0;
            private int dietCount = 0;

            @Override
            protected Void doInBackground() {
                List<Trainee> list = trainerDAO.getAll();
                total = list.size();

                // Stream API count with lambda predicate (Lectures 43-46)
                ecto = list.stream().filter(t -> "Ectomorph".equalsIgnoreCase(t.getBodyType())).count();
                meso = list.stream().filter(t -> "Mesomorph".equalsIgnoreCase(t.getBodyType())).count();
                endo = list.stream().filter(t -> "Endomorph".equalsIgnoreCase(t.getBodyType())).count();

                dietCount = trainerDAO.getAllDietPlans().size();
                return null;
            }

            @Override
            protected void done() {
                totalTraineesVal.setText(total + " Clients");
                ectomorphCountVal.setText(ecto + " Clients");
                mesomorphCountVal.setText(meso + " Clients");
                endomorphCountVal.setText(endo + " Clients");
                totalDietPlansVal.setText(dietCount + " Plans");
            }
        };
        worker.execute();
    }

    private JPanel createMetricCard(String title, String defaultValue, String subtitle, Color accentColor, JLabel valueLabel) {
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
