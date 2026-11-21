/**
 * Date: 2026-11-21
 */
package com.gym.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FitnessClass {
    private final String className;
    private final String description; // "Kya hota hai"
    private final String level;
    private final String duration;
    private final double price;
    private final String scheduleSlot;

    public FitnessClass(String className, String description, String level, String duration, double price, String scheduleSlot) {
        this.className = className;
        this.description = description;
        this.level = level;
        this.duration = duration;
        this.price = price;
        this.scheduleSlot = scheduleSlot;
    }

    public String getClassName() {
        return className;
    }

    public String getDescription() {
        return description;
    }

    public String getLevel() {
        return level;
    }

    public String getDuration() {
        return duration;
    }

    public double getPrice() {
        return price;
    }

    public String getScheduleSlot() {
        return scheduleSlot;
    }

    public static List<FitnessClass> getDefaultClasses() {
        List<FitnessClass> list = new ArrayList<>();
        list.add(new FitnessClass(
                "Yoga Flow",
                "Yoga poses, breathing aur balance practice",
                "All levels",
                "45 min",
                350.0,
                "Tue, Thu • 07:00 AM"
        ));
        list.add(new FitnessClass(
                "Dance Fitness",
                "Music ke saath choreographed cardio",
                "All levels",
                "45 min",
                400.0,
                "Mon, Wed • 06:00 PM"
        ));
        list.add(new FitnessClass(
                "HIIT Circuit",
                "Short, intense exercise rounds with recovery breaks",
                "Intermediate",
                "30 min",
                450.0,
                "Mon, Wed, Fri • 06:30 PM"
        ));
        list.add(new FitnessClass(
                "Strength Fundamentals",
                "Coach ke saath squat, hinge, push aur pull ki technique",
                "Beginner",
                "45 min",
                500.0,
                "Tue, Sat • 08:00 AM"
        ));
        list.add(new FitnessClass(
                "Spin Class",
                "Stationary bike par speed aur resistance intervals",
                "All levels",
                "45 min",
                400.0,
                "Wed, Fri • 07:30 AM"
        ));
        list.add(new FitnessClass(
                "Mat Pilates",
                "Controlled movements for core strength and stability",
                "Beginner",
                "45 min",
                450.0,
                "Mon, Thu • 09:00 AM"
        ));
        list.add(new FitnessClass(
                "Boxing Fitness",
                "Footwork, shadowboxing aur bag drills",
                "All levels",
                "45 min",
                500.0,
                "Tue, Fri • 05:30 PM"
        ));
        list.add(new FitnessClass(
                "Mobility & Stretch",
                "Hips, shoulders aur back ke liye mobility drills aur stretches",
                "All levels",
                "30 min",
                300.0,
                "Sat, Sun • 10:00 AM"
        ));
        return Collections.unmodifiableList(list);
    }
}
