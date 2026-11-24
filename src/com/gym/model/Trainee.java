/**
 * Date: 2026-11-23
 * Syllabus: Unit 1 (Lectures 1-3) - Encapsulation, POJO, Data Validation
 *           Unit 2 (Lectures 23-25) - Comparable & Comparator implementations for record management
 *           Unit 5 (Lectures 43-46) - Stream API and lambda compatibility
 */
package com.gym.model;

import java.sql.Timestamp;
import java.util.Comparator;

public class Trainee implements Comparable<Trainee> {

    private int id;
    private String gymId;
    private String fullName;
    private String email;
    private String phone;
    private String bodyType;      // "Ectomorph", "Mesomorph", "Endomorph"
    private String fitnessGoal;   // "Hypertrophy / Bulk", "Fat Loss & Shred", "Strength Powerlifting", etc.
    private double currentWeight; // in kg
    private double targetWeight;  // in kg
    private double heightCm;      // in cm
    private String assignedDietPlan;
    private String trainerNotes;
    private String assignedTrainer;
    private int attendanceDays;
    private String status;        // "Active", "Pending Assessment", "Completed"
    private Timestamp lastAssessedDate;

    // Syllabus Unit 2 (Lectures 23-25): Comparators for multiple sorting criteria
    public static final Comparator<Trainee> BY_NAME_ASC = (t1, t2) -> t1.getFullName().compareToIgnoreCase(t2.getFullName());
    public static final Comparator<Trainee> BY_CURRENT_WEIGHT_DESC = (t1, t2) -> Double.compare(t2.getCurrentWeight(), t1.getCurrentWeight());
    public static final Comparator<Trainee> BY_BODY_TYPE = (t1, t2) -> t1.getBodyType().compareToIgnoreCase(t2.getBodyType());
    public static final Comparator<Trainee> BY_ATTENDANCE_DESC = (t1, t2) -> Integer.compare(t2.getAttendanceDays(), t1.getAttendanceDays());

    public Trainee() {
        this.status = "Active";
        this.lastAssessedDate = new Timestamp(System.currentTimeMillis());
    }

    public Trainee(int id, String gymId, String fullName, String email, String phone,
                   String bodyType, String fitnessGoal, double currentWeight, double targetWeight,
                   double heightCm, String assignedDietPlan, String trainerNotes, String assignedTrainer,
                   int attendanceDays, String status) {
        this.id = id;
        this.gymId = gymId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.bodyType = bodyType != null ? bodyType : "Mesomorph";
        this.fitnessGoal = fitnessGoal != null ? fitnessGoal : "General Fitness";
        this.currentWeight = currentWeight;
        this.targetWeight = targetWeight;
        this.heightCm = heightCm > 0 ? heightCm : 170.0;
        this.assignedDietPlan = assignedDietPlan != null ? assignedDietPlan : "Standard Maintenance";
        this.trainerNotes = trainerNotes;
        this.assignedTrainer = assignedTrainer != null ? assignedTrainer : "Lead Trainer";
        this.attendanceDays = attendanceDays;
        this.status = status != null ? status : "Active";
        this.lastAssessedDate = new Timestamp(System.currentTimeMillis());
    }

    /**
     * Syllabus Unit 2 (Lecture 23): Natural ordering by Full Name
     */
    @Override
    public int compareTo(Trainee other) {
        if (other == null) return 1;
        return this.fullName.compareToIgnoreCase(other.fullName);
    }

    public double getBmi() {
        if (heightCm <= 0) return 0.0;
        double heightM = heightCm / 100.0;
        return currentWeight / (heightM * heightM);
    }

    public String getBmiCategory() {
        double bmi = getBmi();
        if (bmi <= 0) return "N/A";
        if (bmi < 18.5) return "Underweight (Lean)";
        if (bmi < 24.9) return "Normal / Athletic";
        if (bmi < 29.9) return "Overweight (Bulking/Cut)";
        return "Obese (Fat Loss Focus)";
    }

    public double getWeightGap() {
        return Math.abs(currentWeight - targetWeight);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getGymId() { return gymId; }
    public void setGymId(String gymId) { this.gymId = gymId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }

    public String getFitnessGoal() { return fitnessGoal; }
    public void setFitnessGoal(String fitnessGoal) { this.fitnessGoal = fitnessGoal; }

    public double getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(double currentWeight) { this.currentWeight = currentWeight; }

    public double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(double targetWeight) { this.targetWeight = targetWeight; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    public String getAssignedDietPlan() { return assignedDietPlan; }
    public void setAssignedDietPlan(String assignedDietPlan) { this.assignedDietPlan = assignedDietPlan; }

    public String getTrainerNotes() { return trainerNotes; }
    public void setTrainerNotes(String trainerNotes) { this.trainerNotes = trainerNotes; }

    public String getAssignedTrainer() { return assignedTrainer; }
    public void setAssignedTrainer(String assignedTrainer) { this.assignedTrainer = assignedTrainer; }

    public int getAttendanceDays() { return attendanceDays; }
    public void setAttendanceDays(int attendanceDays) { this.attendanceDays = attendanceDays; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getLastAssessedDate() { return lastAssessedDate; }
    public void setLastAssessedDate(Timestamp lastAssessedDate) { this.lastAssessedDate = lastAssessedDate; }
}
