/**
 * Date: 2026-11-23
 * Syllabus: Unit 1 (Lectures 1-3) - Custom Exception Handling & Validation
 *           Unit 2 (Lectures 16-25) - Collections (ArrayList, HashSet, HashMap, LinkedHashMap), Comparator & Sorting
 *           Unit 3 (Lectures 26-31) - Synchronization, Thread-safety & Critical Sections
 *           Unit 4 (Lectures 34-42) - JDBC, PreparedStatement, CRUD & ResultSet Mapping
 *           Unit 5 (Lectures 43-48) - Stream API, Lambdas, Method References & Generics
 *           Unit 6 (Lectures 49-54) - Character Streams & CSV File Handling with Try-with-resources
 */
package com.gym.dao;

import com.gym.config.DBConnection;
import com.gym.exception.GymException;
import com.gym.exception.ValidationException;
import com.gym.model.DietPlan;
import com.gym.model.Role;
import com.gym.model.Trainee;
import com.gym.model.User;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TrainerDAO implements GenericDAO<Trainee, Integer> {

    // Syllabus Unit 2 (Lectures 20-22): HashMap for O(1) in-memory caching
    // Syllabus Unit 3 (Lectures 30-31): Thread-safe collection for concurrent
    // SwingWorker access
    private static final Map<String, Trainee> TRAINEE_CACHE = new ConcurrentHashMap<>();
    private static final List<Trainee> IN_MEMORY_TRAINEES = new CopyOnWriteArrayList<>();
    private static final List<DietPlan> IN_MEMORY_DIET_PLANS = new CopyOnWriteArrayList<>();
    private static boolean initialized = false;

    public TrainerDAO() {
        ensureInitialized();
    }

    /**
     * Initializes default trainees & diet plans in-memory so app functions
     * immediately
     * even prior to or without active MySQL connection.
     */
    private synchronized void ensureInitialized() {
        if (!initialized) {
            // Seed default curated diet plans
            IN_MEMORY_DIET_PLANS.addAll(DietPlan.getDefaultDietPlans());

            // Seed default sample trainees with diverse body types
            IN_MEMORY_TRAINEES.add(new Trainee(
                    1, "20261001", "Jack Bauer", "jack.bauer@gym.com", "9876543210",
                    "Ectomorph", "Hypertrophy / Bulk", 64.5, 75.0, 180.0,
                    "Ectomorph Mass Surge Surplus", "Hard gainer. Focus on compound lifts: Deadlift, Squat, Bench.",
                    "Lead Trainer", 18, "Active"));

            IN_MEMORY_TRAINEES.add(new Trainee(
                    2, "20261002", "Maria Vance", "maria.vance@gym.com", "9876543211",
                    "Endomorph", "Fat Loss & Conditioning", 78.0, 62.0, 162.0,
                    "Endomorph Carb-Cycling Fat Shred", "Carb sensitivity noted. Advised 45 min daily cardio + HIIT.",
                    "Lead Trainer", 14, "Active"));

            IN_MEMORY_TRAINEES.add(new Trainee(
                    3, "20261003", "Ryan Howard", "ryan.howard@gym.com", "9876543212",
                    "Mesomorph", "Strength & Hypertrophy", 82.0, 85.0, 178.0,
                    "Mesomorph High-Protein Power Fuel", "Powerlifting focus. Excellent muscle recovery rate.",
                    "Lead Trainer", 22, "Active"));

            IN_MEMORY_TRAINEES.add(new Trainee(
                    4, "20261004", "Jessica Alba", "jessica.alba@gym.com", "9876543213",
                    "Mesomorph", "Lean Recomp / Performance", 58.0, 56.0, 168.0,
                    "Mesomorph Athletic Recomposition", "Core strength & mobility focus. Great discipline.",
                    "Lead Trainer", 9, "Active"));

            IN_MEMORY_TRAINEES.add(new Trainee(
                    5, "20261005", "Ken Adams", "ken.adams@gym.com", "9876543214",
                    "Ectomorph", "Lean Bulk / Muscle Gain", 61.0, 70.0, 175.0,
                    "Ectomorph Clean Hypertrophy Fuel", "Calorie surplus reminder given. Track macros daily.",
                    "Lead Trainer", 12, "Active"));

            IN_MEMORY_TRAINEES.add(new Trainee(
                    6, "20261006", "Priya Sharma", "priya.sharma@gym.com", "9876543215",
                    "Universal", "Vegetarian Muscle Definition", 65.0, 58.0, 165.0,
                    "Universal High-Protein Vegetarian Sculpt",
                    "Pure vegetarian. Supplemented with plant protein and sprouts.",
                    "Lead Trainer", 16, "Active"));

            for (Trainee t : IN_MEMORY_TRAINEES) {
                if (t.getGymId() != null)
                    TRAINEE_CACHE.put(t.getGymId().toLowerCase(), t);
                if (t.getEmail() != null)
                    TRAINEE_CACHE.put(t.getEmail().toLowerCase(), t);
            }

            initialized = true;
        }
    }

    // =========================================================================
    // CRUD OPERATIONS FOR TRAINEES (Lectures 37-42)
    // =========================================================================

    /**
     * Syllabus Unit 4 (Lectures 37-42): JDBC Statement & PreparedStatement, CRUD
     * Create/Update
     * Upserts a trainee profile in MySQL and updates in-memory cache.
     */
    public synchronized boolean upsertTrainee(Trainee trainee) throws GymException {
        if (trainee == null) {
            throw new ValidationException("Trainee object cannot be null.");
        }
        if (trainee.getFullName() == null || trainee.getFullName().trim().isEmpty()) {
            throw new ValidationException("Trainee Full Name is required.");
        }

        // 1. Update in-memory collections immediately (Zero offline lag)
        boolean existingFound = false;
        for (int i = 0; i < IN_MEMORY_TRAINEES.size(); i++) {
            Trainee existing = IN_MEMORY_TRAINEES.get(i);
            if ((existing.getGymId() != null && existing.getGymId().equalsIgnoreCase(trainee.getGymId())) ||
                    (existing.getEmail() != null && existing.getEmail().equalsIgnoreCase(trainee.getEmail()))) {
                trainee.setId(existing.getId());
                IN_MEMORY_TRAINEES.set(i, trainee);
                existingFound = true;
                break;
            }
        }
        if (!existingFound) {
            trainee.setId(1000 + IN_MEMORY_TRAINEES.size() + 1);
            IN_MEMORY_TRAINEES.add(trainee);
        }

        if (trainee.getGymId() != null)
            TRAINEE_CACHE.put(trainee.getGymId().toLowerCase(), trainee);
        if (trainee.getEmail() != null)
            TRAINEE_CACHE.put(trainee.getEmail().toLowerCase(), trainee);

        // 2. Persist to MySQL database (Lectures 37-42)
        String sql = "INSERT INTO trainees (gym_id, full_name, email, phone, body_type, fitness_goal, "
                + "current_weight, target_weight, height_cm, assigned_diet_plan, trainer_notes, assigned_trainer, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), phone = VALUES(phone), "
                + "body_type = VALUES(body_type), fitness_goal = VALUES(fitness_goal), "
                + "current_weight = VALUES(current_weight), target_weight = VALUES(target_weight), "
                + "height_cm = VALUES(height_cm), assigned_diet_plan = VALUES(assigned_diet_plan), "
                + "trainer_notes = VALUES(trainer_notes), assigned_trainer = VALUES(assigned_trainer), "
                + "status = VALUES(status)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, trainee.getGymId() != null ? trainee.getGymId().trim() : "");
            ps.setString(2, trainee.getFullName().trim());
            ps.setString(3, trainee.getEmail() != null ? trainee.getEmail().trim().toLowerCase() : "");
            ps.setString(4, trainee.getPhone() != null ? trainee.getPhone().trim() : "");
            ps.setString(5, trainee.getBodyType() != null ? trainee.getBodyType() : "Mesomorph");
            ps.setString(6, trainee.getFitnessGoal() != null ? trainee.getFitnessGoal() : "General Fitness");
            ps.setDouble(7, trainee.getCurrentWeight());
            ps.setDouble(8, trainee.getTargetWeight());
            ps.setDouble(9, trainee.getHeightCm());
            ps.setString(10,
                    trainee.getAssignedDietPlan() != null ? trainee.getAssignedDietPlan() : "Standard Maintenance");
            ps.setString(11, trainee.getTrainerNotes() != null ? trainee.getTrainerNotes() : "");
            ps.setString(12, trainee.getAssignedTrainer() != null ? trainee.getAssignedTrainer() : "Lead Trainer");
            ps.setString(13, trainee.getStatus() != null ? trainee.getStatus() : "Active");

            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            System.err.println("Note on DB trainees upsert: " + ex.getMessage() + " (In-memory record updated)");
            return true;
        }
    }

    /**
     * Syllabus Unit 4 (Lectures 40-42): CRUD Read & ResultSet Mapping
     * Implements GenericDAO.getAll()
     */
    @Override
    public List<Trainee> getAll() {
        List<Trainee> dbList = new ArrayList<>();
        String sql = "SELECT * FROM trainees ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Trainee t = new Trainee();
                t.setId(rs.getInt("id"));
                t.setGymId(rs.getString("gym_id"));
                t.setFullName(rs.getString("full_name"));
                t.setEmail(rs.getString("email"));
                t.setPhone(rs.getString("phone"));
                t.setBodyType(rs.getString("body_type"));
                t.setFitnessGoal(rs.getString("fitness_goal"));
                t.setCurrentWeight(rs.getDouble("current_weight"));
                t.setTargetWeight(rs.getDouble("target_weight"));
                t.setHeightCm(rs.getDouble("height_cm"));
                t.setAssignedDietPlan(rs.getString("assigned_diet_plan"));
                t.setTrainerNotes(rs.getString("trainer_notes"));
                t.setAssignedTrainer(rs.getString("assigned_trainer"));
                t.setStatus(rs.getString("status"));
                dbList.add(t);
            }
        } catch (Exception ex) {
            System.err.println("Note fetching trainees from DB: " + ex.getMessage());
        }

        // If DB has records, synchronize with cache and return
        if (!dbList.isEmpty()) {
            for (Trainee t : dbList) {
                if (t.getGymId() != null)
                    TRAINEE_CACHE.put(t.getGymId().toLowerCase(), t);
                if (t.getEmail() != null)
                    TRAINEE_CACHE.put(t.getEmail().toLowerCase(), t);
            }
            return dbList;
        }

        // Fallback: return in-memory trainees
        return new ArrayList<>(IN_MEMORY_TRAINEES);
    }

    @Override
    public Trainee findById(Integer id) {
        if (id == null)
            return null;
        for (Trainee t : getAll()) {
            if (t.getId() == id)
                return t;
        }
        return null;
    }

    public Trainee findByGymIdOrEmail(String identifier) {
        if (identifier == null || identifier.trim().isEmpty())
            return null;
        String key = identifier.trim().toLowerCase();

        // 1. Check HashMap cache first (Lectures 20-22)
        if (TRAINEE_CACHE.containsKey(key)) {
            return TRAINEE_CACHE.get(key);
        }

        // 2. Query in-memory list
        for (Trainee t : IN_MEMORY_TRAINEES) {
            if (key.equalsIgnoreCase(t.getGymId()) || key.equalsIgnoreCase(t.getEmail())) {
                TRAINEE_CACHE.put(key, t);
                return t;
            }
        }
        return null;
    }

    /**
     * Syllabus Unit 5 (Lectures 43-46): Stream API, Filter, Map, Sorted, Collect
     * Filters trainees by search keyword and body type.
     */
    public List<Trainee> filterTrainees(String keyword, String bodyTypeFilter) {
        List<Trainee> all = getAll();

        Predicate<Trainee> matchesKeyword = t -> {
            if (keyword == null || keyword.trim().isEmpty())
                return true;
            String k = keyword.trim().toLowerCase();
            return (t.getFullName() != null && t.getFullName().toLowerCase().contains(k)) ||
                    (t.getGymId() != null && t.getGymId().toLowerCase().contains(k)) ||
                    (t.getEmail() != null && t.getEmail().toLowerCase().contains(k)) ||
                    (t.getFitnessGoal() != null && t.getFitnessGoal().toLowerCase().contains(k));
        };

        Predicate<Trainee> matchesBodyType = t -> {
            if (bodyTypeFilter == null || bodyTypeFilter.equalsIgnoreCase("All Body Types")
                    || bodyTypeFilter.trim().isEmpty()) {
                return true;
            }
            return t.getBodyType() != null && t.getBodyType().equalsIgnoreCase(bodyTypeFilter.trim());
        };

        // Stream pipeline demonstrating filter and sorted (Lectures 43-46)
        return all.stream()
                .filter(matchesKeyword.and(matchesBodyType))
                .sorted(Trainee.BY_NAME_ASC)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // DIET PLANS OPERATIONS (Lectures 16-25, 43-46)
    // =========================================================================

    public List<DietPlan> getAllDietPlans() {
        List<DietPlan> dbList = new ArrayList<>();
        String sql = "SELECT * FROM diet_plans ORDER BY body_type, id ASC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DietPlan p = new DietPlan(
                        rs.getInt("id"),
                        rs.getString("plan_name"),
                        rs.getString("body_type"),
                        rs.getString("goal"),
                        rs.getInt("daily_calories"),
                        rs.getInt("protein_grams"),
                        rs.getInt("carbs_grams"),
                        rs.getInt("fats_grams"),
                        rs.getDouble("water_liters"),
                        rs.getString("breakfast"),
                        rs.getString("mid_morning_snack"),
                        rs.getString("lunch"),
                        rs.getString("pre_workout"),
                        rs.getString("post_workout"),
                        rs.getString("dinner"),
                        rs.getString("guidelines"));
                dbList.add(p);
            }
        } catch (Exception ex) {
            // Offline tolerance
        }

        if (!dbList.isEmpty()) {
            return dbList;
        }

        return new ArrayList<>(IN_MEMORY_DIET_PLANS);
    }

    /**
     * Syllabus Unit 5 (Lectures 43-46): Stream API filtering of Diet Plans by Body
     * Type & Goal
     */
    public List<DietPlan> filterDietPlans(String bodyType, String goalKeyword) {
        List<DietPlan> all = getAllDietPlans();

        return all.stream()
                .filter(p -> {
                    if (bodyType == null || bodyType.equalsIgnoreCase("All Body Types") || bodyType.trim().isEmpty()) {
                        return true;
                    }
                    return p.getBodyType().equalsIgnoreCase(bodyType.trim());
                })
                .filter(p -> {
                    if (goalKeyword == null || goalKeyword.trim().isEmpty())
                        return true;
                    String g = goalKeyword.trim().toLowerCase();
                    return p.getPlanName().toLowerCase().contains(g) ||
                            p.getGoal().toLowerCase().contains(g);
                })
                .sorted(DietPlan.BY_BODY_TYPE.thenComparing(DietPlan.BY_CALORIES_DESC))
                .collect(Collectors.toList());
    }

    public synchronized boolean addOrUpdateDietPlan(DietPlan plan) throws GymException {
        if (plan == null)
            throw new ValidationException("Diet plan cannot be null.");
        if (plan.getPlanName() == null || plan.getPlanName().trim().isEmpty()) {
            throw new ValidationException("Diet plan name cannot be blank.");
        }
        if (plan.getDailyCalories() <= 0) {
            throw new ValidationException("Daily calories must be a positive integer.");
        }

        // In-memory update
        boolean found = false;
        for (int i = 0; i < IN_MEMORY_DIET_PLANS.size(); i++) {
            if (IN_MEMORY_DIET_PLANS.get(i).getPlanName().equalsIgnoreCase(plan.getPlanName())) {
                plan.setId(IN_MEMORY_DIET_PLANS.get(i).getId());
                IN_MEMORY_DIET_PLANS.set(i, plan);
                found = true;
                break;
            }
        }
        if (!found) {
            plan.setId(IN_MEMORY_DIET_PLANS.size() + 1);
            IN_MEMORY_DIET_PLANS.add(plan);
        }

        // MySQL database persistence
        String sql = "INSERT INTO diet_plans (plan_name, body_type, goal, daily_calories, protein_grams, carbs_grams, "
                + "fats_grams, water_liters, breakfast, mid_morning_snack, lunch, pre_workout, post_workout, dinner, guidelines) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE body_type = VALUES(body_type), goal = VALUES(goal), "
                + "daily_calories = VALUES(daily_calories), protein_grams = VALUES(protein_grams), "
                + "carbs_grams = VALUES(carbs_grams), fats_grams = VALUES(fats_grams), "
                + "water_liters = VALUES(water_liters), breakfast = VALUES(breakfast), "
                + "lunch = VALUES(lunch), dinner = VALUES(dinner), guidelines = VALUES(guidelines)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, plan.getPlanName());
            ps.setString(2, plan.getBodyType());
            ps.setString(3, plan.getGoal());
            ps.setInt(4, plan.getDailyCalories());
            ps.setInt(5, plan.getProteinGrams());
            ps.setInt(6, plan.getCarbsGrams());
            ps.setInt(7, plan.getFatsGrams());
            ps.setDouble(8, plan.getWaterLiters());
            ps.setString(9, plan.getBreakfast() != null ? plan.getBreakfast() : "");
            ps.setString(10, plan.getMidMorningSnack() != null ? plan.getMidMorningSnack() : "");
            ps.setString(11, plan.getLunch() != null ? plan.getLunch() : "");
            ps.setString(12, plan.getPreWorkout() != null ? plan.getPreWorkout() : "");
            ps.setString(13, plan.getPostWorkout() != null ? plan.getPostWorkout() : "");
            ps.setString(14, plan.getDinner() != null ? plan.getDinner() : "");
            ps.setString(15, plan.getGuidelines() != null ? plan.getGuidelines() : "");

            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            System.err.println("Note on DB diet plan save: " + ex.getMessage());
            return true;
        }
    }

    // =========================================================================
    // SYLLABUS UNIT 6 (Lectures 49-54): CSV EXPORT & FILE STREAMS
    // =========================================================================

    /**
     * Exports the trainee list to CSV using Character Streams (BufferedWriter &
     * FileWriter)
     * with exception-safe resource handling (try-with-resources).
     */
    public static void exportTraineesToCSV(List<Trainee> trainees, File targetFile) throws GymException {
        if (trainees == null || trainees.isEmpty()) {
            throw new ValidationException("No trainee records available to export.");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            writer.write(
                    "Gym ID,Full Name,Email,Phone,Body Type,Fitness Goal,Current Weight (kg),Target Weight (kg),Height (cm),BMI,Assigned Diet Plan,Status");
            writer.newLine();

            for (Trainee t : trainees) {
                StringBuilder sb = new StringBuilder();
                sb.append(escapeCSV(t.getGymId())).append(",");
                sb.append(escapeCSV(t.getFullName())).append(",");
                sb.append(escapeCSV(t.getEmail())).append(",");
                sb.append(escapeCSV(t.getPhone())).append(",");
                sb.append(escapeCSV(t.getBodyType())).append(",");
                sb.append(escapeCSV(t.getFitnessGoal())).append(",");
                sb.append(String.format("%.1f", t.getCurrentWeight())).append(",");
                sb.append(String.format("%.1f", t.getTargetWeight())).append(",");
                sb.append(String.format("%.1f", t.getHeightCm())).append(",");
                sb.append(String.format("%.1f", t.getBmi())).append(",");
                sb.append(escapeCSV(t.getAssignedDietPlan())).append(",");
                sb.append(escapeCSV(t.getStatus()));

                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new GymException("Failed to export trainees CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Exports a selected Diet Plan to a formatted readable text file using
     * FileWriter.
     */
    public static void exportDietPlanToText(DietPlan plan, File targetFile) throws GymException {
        if (plan == null)
            throw new ValidationException("Diet plan is null.");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            writer.write("================================================================================");
            writer.newLine();
            writer.write("           DYNAMIS GYM - PERSONALIZED NUTRITION & DIET PLAN");
            writer.newLine();
            writer.write("================================================================================");
            writer.newLine();
            writer.write("Plan Name         : " + plan.getPlanName());
            writer.newLine();
            writer.write("Target Body Type  : " + plan.getBodyType());
            writer.newLine();
            writer.write("Fitness Goal      : " + plan.getGoal());
            writer.newLine();
            writer.write("Daily Energy      : " + plan.getDailyCalories() + " kcal");
            writer.newLine();
            writer.write("Macros Split      : Protein: " + plan.getProteinGrams() + "g | Carbs: " + plan.getCarbsGrams()
                    + "g | Fats: " + plan.getFatsGrams() + "g");
            writer.newLine();
            writer.write("Hydration Target  : " + plan.getWaterLiters() + " Liters / day");
            writer.newLine();
            writer.write("--------------------------------------------------------------------------------");
            writer.newLine();
            writer.write("DAILY MEAL SCHEDULE:");
            writer.newLine();
            writer.write("• Breakfast (07:30 AM)    : " + plan.getBreakfast());
            writer.newLine();
            writer.write("• Snack (10:30 AM)        : " + plan.getMidMorningSnack());
            writer.newLine();
            writer.write("• Lunch (01:30 PM)        : " + plan.getLunch());
            writer.newLine();
            writer.write("• Pre-Workout (05:00 PM)  : " + plan.getPreWorkout());
            writer.newLine();
            writer.write("• Post-Workout (07:30 PM) : " + plan.getPostWorkout());
            writer.newLine();
            writer.write("• Dinner (09:00 PM)       : " + plan.getDinner());
            writer.newLine();
            writer.write("--------------------------------------------------------------------------------");
            writer.newLine();
            writer.write("TRAINER GUIDELINES & TIPS:");
            writer.newLine();
            writer.write(plan.getGuidelines() != null ? plan.getGuidelines() : "Follow macros consistently.");
            writer.newLine();
            writer.write("================================================================================");
            writer.newLine();
        } catch (IOException e) {
            throw new GymException("Failed to export diet plan file: " + e.getMessage(), e);
        }
    }

    private static String escapeCSV(String value) {
        if (value == null)
            return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // =========================================================================
    // SYLLABUS UNIT 5 (Lecture 47-48): INTRODUCTORY GENERICS HELPER METHOD
    // =========================================================================
    public static <T> List<T> filterGenericList(List<T> list, Predicate<? super T> predicate) {
        if (list == null)
            return Collections.emptyList();
        return list.stream().filter(predicate).collect(Collectors.toList());
    }
}
