/**
 * Date: 2026-11-23
 * Syllabus: Unit 1 (Lectures 1-3) - Object-Oriented Modeling & Encapsulation
 *           Unit 2 (Lectures 23-25) - Comparable & Comparator for sorting
 *           Unit 5 (Lectures 43-46) - Stream API friendly immutable/getter patterns
 */
package com.gym.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DietPlan implements Comparable<DietPlan> {

    private int id;
    private String planName;
    private String bodyType; // "Ectomorph", "Mesomorph", "Endomorph", "Universal"
    private String goal;     // "Bulking / Hypertrophy", "Lean Recomp", "Fat Shred / Cut", "Maintenance"
    private int dailyCalories;
    private int proteinGrams;
    private int carbsGrams;
    private int fatsGrams;
    private double waterLiters;
    private String breakfast;
    private String midMorningSnack;
    private String lunch;
    private String preWorkout;
    private String postWorkout;
    private String dinner;
    private String guidelines;

    // Syllabus Unit 2 (Lectures 23-25): Comparators for sorting collections
    public static final Comparator<DietPlan> BY_CALORIES_DESC = (p1, p2) -> Integer.compare(p2.getDailyCalories(), p1.getDailyCalories());
    public static final Comparator<DietPlan> BY_PROTEIN_DESC = (p1, p2) -> Integer.compare(p2.getProteinGrams(), p1.getProteinGrams());
    public static final Comparator<DietPlan> BY_NAME_ASC = (p1, p2) -> p1.getPlanName().compareToIgnoreCase(p2.getPlanName());
    public static final Comparator<DietPlan> BY_BODY_TYPE = (p1, p2) -> p1.getBodyType().compareToIgnoreCase(p2.getBodyType());

    public DietPlan() {}

    public DietPlan(int id, String planName, String bodyType, String goal, int dailyCalories,
                    int proteinGrams, int carbsGrams, int fatsGrams, double waterLiters,
                    String breakfast, String midMorningSnack, String lunch,
                    String preWorkout, String postWorkout, String dinner, String guidelines) {
        this.id = id;
        this.planName = planName;
        this.bodyType = bodyType;
        this.goal = goal;
        this.dailyCalories = dailyCalories;
        this.proteinGrams = proteinGrams;
        this.carbsGrams = carbsGrams;
        this.fatsGrams = fatsGrams;
        this.waterLiters = waterLiters;
        this.breakfast = breakfast;
        this.midMorningSnack = midMorningSnack;
        this.lunch = lunch;
        this.preWorkout = preWorkout;
        this.postWorkout = postWorkout;
        this.dinner = dinner;
        this.guidelines = guidelines;
    }

    /**
     * Syllabus Unit 2 (Lecture 23): Natural ordering by plan name
     */
    @Override
    public int compareTo(DietPlan other) {
        if (other == null) return 1;
        return this.planName.compareToIgnoreCase(other.planName);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public int getDailyCalories() { return dailyCalories; }
    public void setDailyCalories(int dailyCalories) { this.dailyCalories = dailyCalories; }

    public int getProteinGrams() { return proteinGrams; }
    public void setProteinGrams(int proteinGrams) { this.proteinGrams = proteinGrams; }

    public int getCarbsGrams() { return carbsGrams; }
    public void setCarbsGrams(int carbsGrams) { this.carbsGrams = carbsGrams; }

    public int getFatsGrams() { return fatsGrams; }
    public void setFatsGrams(int fatsGrams) { this.fatsGrams = fatsGrams; }

    public double getWaterLiters() { return waterLiters; }
    public void setWaterLiters(double waterLiters) { this.waterLiters = waterLiters; }

    public String getBreakfast() { return breakfast; }
    public void setBreakfast(String breakfast) { this.breakfast = breakfast; }

    public String getMidMorningSnack() { return midMorningSnack; }
    public void setMidMorningSnack(String midMorningSnack) { this.midMorningSnack = midMorningSnack; }

    public String getLunch() { return lunch; }
    public void setLunch(String lunch) { this.lunch = lunch; }

    public String getPreWorkout() { return preWorkout; }
    public void setPreWorkout(String preWorkout) { this.preWorkout = preWorkout; }

    public String getPostWorkout() { return postWorkout; }
    public void setPostWorkout(String postWorkout) { this.postWorkout = postWorkout; }

    public String getDinner() { return dinner; }
    public void setDinner(String dinner) { this.dinner = dinner; }

    public String getGuidelines() { return guidelines; }
    public void setGuidelines(String guidelines) { this.guidelines = guidelines; }

    public String getMacroSummary() {
        return "P: " + proteinGrams + "g | C: " + carbsGrams + "g | F: " + fatsGrams + "g (" + dailyCalories + " kcal)";
    }

    /**
     * Default science-backed diet plans curated for every body type
     */
    public static List<DietPlan> getDefaultDietPlans() {
        List<DietPlan> list = new ArrayList<>();

        // 1. ECTOMORPH PLANS (Lean, fast metabolism, needs calorie surplus & high carbohydrates)
        list.add(new DietPlan(
                1,
                "Ectomorph Mass Surge Surplus",
                "Ectomorph",
                "Hypertrophy / Bulk",
                3200,
                180,
                430,
                85,
                4.0,
                "4 Whole Eggs + 2 slices Whole Wheat Toast with Peanut Butter + 1 Banana + 250ml Whole Milk",
                "Handful of Almonds & Walnuts + Fruit Smoothie with Oats and Honey",
                "200g Grilled Chicken Breast or Paneer + 1.5 cups Brown Rice + Steamed Broccoli + Olive Oil Drizzle",
                "Large Sweet Potato + 1 Scoop Whey Protein with Water",
                "1 Scoop Whey Protein + 1 Banana + 50g Dextrose/Rice Cakes for rapid glycogen replenishment",
                "180g Salmon or Tofu Curry + 2 Rotis + Mixed Green Salad with Avocado",
                "Ectomorph Tip: Never skip meals. Aim for calorie surplus (+500 kcal). Eat complex carbohydrates with every meal."
        ));

        list.add(new DietPlan(
                2,
                "Ectomorph Clean Hypertrophy Fuel",
                "Ectomorph",
                "Lean Bulk / Muscle Gain",
                2800,
                165,
                370,
                75,
                3.5,
                "100g Rolled Oats with 1 Scoop Whey + Chia Seeds + Berries + 2 Boiled Eggs",
                "Greek Yogurt with Honey and Granola + 1 Apple",
                "180g Lean Turkey/Soya Chunks + 1 cup Quinoa + Mixed Bell Peppers and Zucchini",
                "2 Multigrain Toasts with 2 tbsp Peanut Butter + Black Coffee",
                "Post-Workout Whey Shake + 30g Crushed Rolled Oats",
                "150g Grilled Fish or Cottage Cheese (Paneer) + 1 cup Jasmine Rice + Asparagus",
                "Ectomorph Tip: Eat every 2.5-3 hours to maintain an anabolic environment. High carbohydrate tolerance."
        ));

        // 2. MESOMORPH PLANS (Naturally athletic, responds quickly to training, balanced macros)
        list.add(new DietPlan(
                3,
                "Mesomorph Athletic Recomposition",
                "Mesomorph",
                "Lean Recomp / Performance",
                2400,
                180,
                240,
                70,
                3.8,
                "3 Scrambled Eggs + 2 Egg Whites + 1/2 Avocado + 1 slice Sourdough Toast + Green Tea",
                "1 Scoop Whey Isolate + 1 Apple + 15g Roasted Almonds",
                "200g Grilled Chicken or Tofu Bowl + 1 cup Brown Rice + Roasted Veggies + Hummus",
                "Rice Cakes with Natural Peanut Butter + 1 Espresso Shot",
                "1.5 Scoops Whey Isolate + 1 Banana + 5g Creatine Monohydrate",
                "180g Grilled Fish / Paneer Tikka + Mixed Steamed Greens + 1 Small Sweet Potato",
                "Mesomorph Tip: Maintain 40/30/30 macro balance. Easily burns fat and gains muscle with consistent macros."
        ));

        list.add(new DietPlan(
                4,
                "Mesomorph High-Protein Power Fuel",
                "Mesomorph",
                "Strength & Hypertrophy",
                2600,
                200,
                270,
                75,
                4.0,
                "1 Cup Oatmeal with 1 Scoop Protein + 4 Egg Whites Omelet with Spinach",
                "Low-Fat Cottage Cheese (Paneer) + Handful of Mixed Berries",
                "220g Lean Minced Beef or Soya Granules + 1 cup Basmati Rice + Cucumber Tomato Salad",
                "1 Banana + 1 tbsp Almond Butter + Pre-workout Drink",
                "Whey Protein Shake + 40g Quick Oats in Water",
                "200g Chicken Breast or Grilled Paneer + Sautéed Mushrooms and Green Beans + 1 Roti",
                "Mesomorph Tip: Optimal carb timing around workouts supports heavy compound lifts and quick recovery."
        ));

        // 3. ENDOMORPH PLANS (Slower carb tolerance, higher fat retention, needs low carb / carb cycling)
        list.add(new DietPlan(
                5,
                "Endomorph Carb-Cycling Fat Shred",
                "Endomorph",
                "Fat Loss & Conditioning",
                1900,
                175,
                120,
                75,
                4.2,
                "4 Egg Whites + 1 Whole Egg Vegetable Omelet (Spinach, Tomato, Mushroom) + Green Tea",
                "1 Scoop Whey Protein in Water + 10 Almonds",
                "180g Herb Grilled Chicken or Paneer + Large Bowl of Green Salad + Olive Oil Dressing",
                "Black Coffee + 1 Apple with a pinch of Cinnamon",
                "1 Scoop Whey Isolate in Cold Water + 5g Glutamine",
                "180g Baked Salmon or Soya Chunks + Steamed Cauliflower & Broccoli with Garlic",
                "Endomorph Tip: Limit refined carbs. Consume majority of carbs strictly post-workout. Focus on healthy fats and lean protein."
        ));

        list.add(new DietPlan(
                6,
                "Endomorph Ketogenic Metabolic Reset",
                "Endomorph",
                "Rapid Fat Shred",
                1750,
                160,
                40,
                105,
                4.5,
                "3 Whole Eggs cooked in Butter + 2 strips Turkey Bacon or Grilled Mushrooms + Avocado slices",
                "String Cheese or 30g Roasted Walnuts + Green Tea",
                "200g Chicken Thighs or Paneer Tikka + Sautéed Spinach in Olive Oil + Cucumber slices",
                "Celery sticks with 1 tbsp Almond Butter + Black Coffee",
                "Zero-Carb Whey Isolate in Water",
                "200g Grilled White Fish or Tofu + Asparagus & Zucchini in Virgin Olive Oil",
                "Endomorph Tip: Keep carbs under 50g. Body switches to ketone bodies for fuel. Drink at least 4L water with electrolytes."
        ));

        // 4. UNIVERSAL / VEGETARIAN PLAN
        list.add(new DietPlan(
                7,
                "Universal High-Protein Vegetarian Sculpt",
                "Universal",
                "Vegetarian Muscle Definition",
                2200,
                150,
                250,
                65,
                3.5,
                "Paneer Bhurji (150g) + 2 Multigrain Rotis + 1 Cup Skimmed Milk with Chia Seeds",
                "Sprouted Moong Salad with Lemon, Tomato, and Roasted Peanuts",
                "1.5 Cups Boiled Rajma / Chickpeas + 1 cup Brown Rice + Large Cucumber Salad",
                "Roasted Chana (40g) + 1 Cup Green Tea",
                "Plant Protein / Whey Shake + 1 Banana",
                "150g Tofu or Soya Bean Curry + 1 Roti + Steamed Spinach & Broccoli",
                "Universal Tip: Combine grain + legume sources (e.g. rice + dal) to ensure a complete essential amino acid profile."
        ));

        return Collections.unmodifiableList(list);
    }
}
