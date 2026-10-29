package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfile::class,
        WorkoutTemplate::class,
        ExerciseTemplate::class,
        Exercise::class,
        WorkoutSession::class,
        ExerciseLog::class,
        WorkoutLog::class,
        FoodItem::class,
        MealEntry::class,
        CardioEntry::class,
        BodyMeasurementEntry::class,
        PhotoEntry::class,
        DailyHabit::class,
        ChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class Project12Database : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun foodDao(): FoodDao
    abstract fun mealDao(): MealDao
    abstract fun cardioDao(): CardioDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun photoDao(): PhotoDao
    abstract fun habitDao(): HabitDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: Project12Database? = null

        fun getInstance(context: Context): Project12Database {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Project12Database::class.java,
                    "project12_transformation.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).seedInitialData()
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun seedInitialData() {
        val templates = listOf(
            WorkoutTemplate(
                templateId = "mon_push",
                weekPhase = "Weeks 1-12",
                dayOfWeek = "Monday",
                title = "Push Day",
                focus = "Chest, Shoulders & Triceps",
                targetMuscles = "Pectorals, Anterior Deltoids, Triceps Brachii",
                estimatedMinutes = 45,
                orderIndex = 1
            ),
            WorkoutTemplate(
                templateId = "tue_legs",
                weekPhase = "Weeks 1-12",
                dayOfWeek = "Tuesday",
                title = "Legs & Core",
                focus = "Quads, Hamstrings, Glutes & Abs",
                targetMuscles = "Quadriceps, Gluteus, Hamstrings, Calves, Core",
                estimatedMinutes = 45,
                orderIndex = 2
            ),
            WorkoutTemplate(
                templateId = "wed_cardio_mobility",
                weekPhase = "Weeks 1-12",
                dayOfWeek = "Wednesday",
                title = "Brisk Walking & Mobility",
                focus = "Aerobic Base & Joint Recovery",
                targetMuscles = "Cardiovascular system, Hip Flexors, Thoracic spine",
                estimatedMinutes = 35,
                orderIndex = 3
            ),
            WorkoutTemplate(
                templateId = "thu_pull",
                weekPhase = "Weeks 1-12",
                dayOfWeek = "Thursday",
                title = "Pull Day",
                focus = "Back, Biceps & Rear Shoulders",
                targetMuscles = "Latissimus Dorsi, Rhomboids, Biceps, Posterior Deltoid",
                estimatedMinutes = 45,
                orderIndex = 4
            ),
            WorkoutTemplate(
                templateId = "fri_full_body",
                weekPhase = "Weeks 1-12",
                dayOfWeek = "Friday",
                title = "Full Body & Core",
                focus = "Compound Strength & Core Endurance",
                targetMuscles = "Full Body compound movements, Obliques, Rectus Abdominis",
                estimatedMinutes = 45,
                orderIndex = 5
            ),
            WorkoutTemplate(
                templateId = "sat_cardio",
                weekPhase = "Weeks 1-12",
                dayOfWeek = "Saturday",
                title = "Cardio & Active Recovery",
                focus = "Endurance, Walking, Cycling or Jogging",
                targetMuscles = "Heart, Lungs, Lower Body endurance",
                estimatedMinutes = 40,
                orderIndex = 6
            ),
            WorkoutTemplate(
                templateId = "sun_rest",
                weekPhase = "Weeks 1-12",
                dayOfWeek = "Sunday",
                title = "Rest & Weekly Review",
                focus = "Systemic Recovery & Meal Prep",
                targetMuscles = "Nervous system recovery, Muscle protein synthesis",
                estimatedMinutes = 15,
                orderIndex = 7
            )
        )
        workoutDao().insertTemplates(templates)

        val exercises = listOf(
            // Monday Push
            ExerciseTemplate("ex_mon_1", "mon_push", "Dumbbell Floor Press", 3, "8-12 reps", 60, "Dumbbells", "Lie flat on floor with dumbbells at chest level. Press up smoothly, lower under control with elbows at 45 degrees.", "Push-ups or incline push-ups", 1),
            ExerciseTemplate("ex_mon_2", "mon_push", "Push-ups", 2, "6-12 reps", 45, "Bodyweight", "Maintain rigid plank line, tuck elbows slightly, lower chest until 2 inches above ground.", "Knee push-ups or hands elevated push-ups", 2),
            ExerciseTemplate("ex_mon_3", "mon_push", "Dumbbell Shoulder Press", 2, "8-12 reps", 60, "Dumbbells", "Seated or standing with core braced. Press overhead in a natural arc without arching lower back.", "Band overhead press", 3),
            ExerciseTemplate("ex_mon_4", "mon_push", "Dumbbell Lateral Raises", 2, "12-15 reps", 45, "Dumbbells / Bands", "Slight forward torso lean. Raise dumbbells to shoulder height leading with elbows.", "Resistance band lateral raises", 4),
            ExerciseTemplate("ex_mon_5", "mon_push", "Overhead DB Triceps Extensions", 2, "10-15 reps", 45, "Dumbbell", "Hold single dumbbell vertically overhead with both hands. Lower behind head flexing elbows, press up.", "Diamond push-ups or bench dips", 5),

            // Tuesday Legs & Core
            ExerciseTemplate("ex_tue_1", "tue_legs", "Goblet Squats", 3, "10-12 reps", 60, "Dumbbell", "Hold dumbbell vertically against chest. Squat down with knees tracking toes until thighs parallel.", "Bodyweight air squats", 1),
            ExerciseTemplate("ex_tue_2", "tue_legs", "Dumbbell Romanian Deadlifts", 3, "10-12 reps", 60, "Dumbbells", "Soft bend in knees. Hinge hips backwards keeping back flat and dumbbells skimming shins.", "Good mornings with band", 2),
            ExerciseTemplate("ex_tue_3", "tue_legs", "Reverse Lunges", 2, "8-10 reps/leg", 60, "Dumbbells / Bodyweight", "Step backward smoothly, lower rear knee until nearly touching floor, drive through front heel.", "Split squats", 3),
            ExerciseTemplate("ex_tue_4", "tue_legs", "Calf Raises", 3, "15-20 reps", 45, "Dumbbell / Bodyweight", "Stand on edge of step or floor. Rise up on toes for 2-second squeeze, lower slowly.", "Single leg calf raises", 4),
            ExerciseTemplate("ex_tue_5", "tue_legs", "Dead Bugs", 3, "8-10 reps/side", 45, "Bodyweight", "Lie on back with lower back pressed into floor. Extend opposite arm and leg synchronously.", "Bird dogs on floor", 5),
            ExerciseTemplate("ex_tue_6", "tue_legs", "Plank", 3, "20-40 seconds", 45, "Bodyweight", "Forearms on ground, glutes clenched, core bracing like anticipating a punch.", "Knee plank", 6),

            // Wednesday Cardio & Mobility
            ExerciseTemplate("ex_wed_1", "wed_cardio_mobility", "Brisk Walking", 1, "25-35 minutes", 0, "Walking Shoes", "Maintain brisk pace where conversation is slightly elevated. Zone 2 aerobic base.", "Indoor marching / treadmill", 1),
            ExerciseTemplate("ex_wed_2", "wed_cardio_mobility", "Mobility Exercises", 1, "5-10 minutes", 30, "Bodyweight", "Hip 90/90s, cat-cow, thoracic twists, and ankle dorsiflexion rocks.", "Gentle yoga flow", 2),

            // Thursday Pull
            ExerciseTemplate("ex_thu_1", "thu_pull", "One-Arm Dumbbell Rows", 3, "8-12 reps/arm", 60, "Dumbbell", "Hand on bench or chair. Pull elbow toward hip pocket, squeezing lats at peak contraction.", "Inverted bodyweight rows", 1),
            ExerciseTemplate("ex_thu_2", "thu_pull", "Resistance-Band Rows", 2, "10-15 reps", 45, "Resistance Band", "Anchor band around feet or door. Pull elbows back squeezing shoulder blades together.", "Towel isometric rows", 2),
            ExerciseTemplate("ex_thu_3", "thu_pull", "Dumbbell Biceps Curls", 3, "10-15 reps", 45, "Dumbbells", "Keep upper arms pinned to sides. Curl with control, supinate wrists at top, slow negative.", "Band hammer curls", 3),
            ExerciseTemplate("ex_thu_4", "thu_pull", "Rear-Delt Fly", 2, "12-15 reps", 45, "Light Dumbbells", "Hinged forward at hips. Raise arms outwards targeting rear deltoids and rhomboids.", "Face pulls with band", 4),
            ExerciseTemplate("ex_thu_5", "thu_pull", "Bird Dogs", 2, "10 reps/side", 30, "Bodyweight", "On all fours. Reach opposite arm and leg out horizontally without twisting hips.", "Dead bugs", 5),

            // Friday Full Body & Core
            ExerciseTemplate("ex_fri_1", "fri_full_body", "Goblet Squats", 2, "10-12 reps", 60, "Dumbbell", "Deep squat depth, upright chest, powerful hip extension to lockout.", "Squat jumps", 1),
            ExerciseTemplate("ex_fri_2", "fri_full_body", "Push-ups", 2, "8-12 reps", 45, "Bodyweight", "Chest to deck, crisp lockout, firm midline posture throughout.", "Decline push-ups", 2),
            ExerciseTemplate("ex_fri_3", "fri_full_body", "Dumbbell Rows", 2, "10-12 reps", 60, "Dumbbells", "Chest-supported or bent-over dual dumbbell rows.", "Band pull-aparts", 3),
            ExerciseTemplate("ex_fri_4", "fri_full_body", "Dumbbell Curls", 2, "10-15 reps", 45, "Dumbbells", "Strict form bicep curls with controlled eccentric cadence.", "Band curls", 4),
            ExerciseTemplate("ex_fri_5", "fri_full_body", "Reverse Crunches", 3, "10-15 reps", 45, "Bodyweight", "Lie on back, curl pelvis off floor curling knees toward chest, lower gently.", "Hanging knee raises", 5),
            ExerciseTemplate("ex_fri_6", "fri_full_body", "Side Plank", 2, "20-30 sec/side", 30, "Bodyweight", "Elbow directly below shoulder. Lift hips to create straight line head to feet.", "Knee side plank", 6),

            // Saturday Cardio
            ExerciseTemplate("ex_sat_1", "sat_cardio", "Cardio of Choice (Walk/Cycle/Jog)", 1, "30-45 minutes", 0, "Outdoor / Equipment", "Select your preferred low-impact steady state cardio. Record duration and perceived effort.", "Indoor dancing / aerobics", 1),

            // Sunday Rest
            ExerciseTemplate("ex_sun_1", "sun_rest", "Weekly Transformation Review", 1, "1 session", 0, "Self", "Record waist circumference, upload Sunday progress photo, review upcoming week.", "Active walk", 1)
        )
        workoutDao().insertExerciseTemplates(exercises)

        val standardExercises = exercises.map { ex ->
            val muscle = when {
                ex.name.contains("Press", ignoreCase = true) || ex.name.contains("Push-up", ignoreCase = true) -> "Chest"
                ex.name.contains("Lateral", ignoreCase = true) || ex.name.contains("Shoulder", ignoreCase = true) || ex.name.contains("Rear-Delt", ignoreCase = true) -> "Shoulders"
                ex.name.contains("Triceps", ignoreCase = true) -> "Triceps"
                ex.name.contains("Squat", ignoreCase = true) || ex.name.contains("Deadlift", ignoreCase = true) || ex.name.contains("Lunge", ignoreCase = true) || ex.name.contains("Calf", ignoreCase = true) -> "Legs"
                ex.name.contains("Row", ignoreCase = true) -> "Back"
                ex.name.contains("Curl", ignoreCase = true) -> "Biceps"
                ex.name.contains("Plank", ignoreCase = true) || ex.name.contains("Crunch", ignoreCase = true) || ex.name.contains("Bug", ignoreCase = true) || ex.name.contains("Bird", ignoreCase = true) -> "Core"
                else -> "Cardio & Mobility"
            }
            Exercise(
                exerciseId = ex.exerciseId,
                name = ex.name,
                muscleGroup = muscle,
                category = if (muscle == "Cardio & Mobility") "Cardio" else "Strength",
                equipmentNeeded = ex.equipmentNeeded,
                defaultSets = ex.targetSets,
                defaultReps = ex.targetReps,
                restSeconds = ex.restSeconds,
                instructions = ex.instructions,
                formCues = "Maintain controlled cadence and neutral spine posture.",
                substituteAlternative = ex.substituteAlternative,
                orderIndex = ex.orderIndex
            )
        }
        workoutDao().insertExercises(standardExercises)

        val foods = listOf(
            FoodItem("food_idli", "Idli", "South Indian", 1.0, "piece (35g)", 58.0, 2.0, 12.0, 0.2, 0.5, true),
            FoodItem("food_dosa", "Plain Dosa", "South Indian", 1.0, "dosa (80g)", 133.0, 3.2, 24.0, 3.7, 1.2, true),
            FoodItem("food_sambar", "Sambar", "South Indian", 1.0, "bowl (150g)", 115.0, 5.5, 16.0, 3.2, 3.8, true),
            FoodItem("food_white_rice", "Steamed White Rice", "Grains", 1.0, "cup cooked (150g)", 195.0, 4.1, 43.0, 0.5, 0.8, true),
            FoodItem("food_brown_rice", "Brown Rice", "Grains", 1.0, "cup cooked (150g)", 168.0, 3.8, 35.0, 1.2, 2.5, true),
            FoodItem("food_chapati", "Chapati / Whole Wheat Roti", "North Indian", 1.0, "roti (40g)", 104.0, 3.1, 20.0, 1.5, 2.8, true),
            FoodItem("food_moong_dal", "Moong Dal Tadka", "North Indian", 1.0, "cup (180g)", 140.0, 9.0, 20.0, 3.2, 4.0, true),
            FoodItem("food_dal_makhani", "Dal Makhani", "North Indian", 1.0, "cup (180g)", 180.0, 8.5, 22.0, 6.5, 4.5, true),
            FoodItem("food_boiled_egg", "Whole Boiled Egg", "Protein", 1.0, "large egg", 74.0, 6.3, 0.4, 5.0, 0.0, false),
            FoodItem("food_egg_white", "Egg White", "Protein", 1.0, "large white", 17.0, 3.6, 0.2, 0.1, 0.0, false),
            FoodItem("food_egg_bhurji", "Egg Bhurji (2 Eggs)", "Protein", 1.0, "serving", 185.0, 13.5, 4.0, 12.0, 0.8, false),
            FoodItem("food_chicken_breast", "Chicken Breast (Curry/Grilled)", "Protein", 100.0, "100 grams", 165.0, 31.0, 0.0, 3.6, 0.0, false),
            FoodItem("food_fish_curry", "Fish Curry (Rohu/Salmon)", "Protein", 1.0, "serving (150g)", 180.0, 22.0, 3.0, 9.0, 0.5, false),
            FoodItem("food_curd", "Curd / Plain Dahi", "Dairy", 1.0, "cup (150g)", 98.0, 5.2, 7.0, 4.8, 0.0, true),
            FoodItem("food_greek_yogurt", "Greek Yogurt / Hung Curd", "Dairy", 100.0, "100 grams", 97.0, 10.0, 3.6, 5.0, 0.0, true),
            FoodItem("food_cow_milk", "Cow Milk", "Dairy", 1.0, "glass (200ml)", 122.0, 6.4, 9.6, 6.2, 0.0, true),
            FoodItem("food_roasted_chana", "Roasted Chana", "Snacks", 1.0, "small bowl (50g)", 185.0, 9.5, 29.0, 3.2, 8.5, true),
            FoodItem("food_soya_chunks", "Soya Chunks (Cooked)", "Protein", 1.0, "cup (100g)", 172.0, 26.0, 16.5, 0.5, 6.5, true),
            FoodItem("food_paneer", "Raw Paneer Cubes", "Dairy", 100.0, "100 grams", 265.0, 18.3, 3.5, 20.8, 0.0, true),
            FoodItem("food_paneer_bhurji", "Paneer Bhurji", "Dairy", 1.0, "cup (150g)", 240.0, 14.0, 6.0, 18.0, 1.2, true),
            FoodItem("food_banana", "Banana", "Fruits", 1.0, "medium", 105.0, 1.3, 27.0, 0.3, 3.1, true),
            FoodItem("food_guava", "Guava", "Fruits", 1.0, "medium (100g)", 68.0, 2.6, 14.0, 1.0, 5.4, true),
            FoodItem("food_oats", "Oats Porridge", "Breakfast", 1.0, "bowl (40g dry)", 155.0, 5.5, 27.0, 2.8, 4.0, true),
            FoodItem("food_upma", "Rava Upma", "Breakfast", 1.0, "cup (150g)", 190.0, 4.5, 32.0, 5.0, 2.0, true),
            FoodItem("food_poha", "Vegetable Poha", "Breakfast", 1.0, "plate (150g)", 180.0, 3.5, 34.0, 4.0, 2.5, true),
            FoodItem("food_besan_chilla", "Besan Chilla", "Breakfast", 1.0, "piece (60g)", 125.0, 6.2, 15.0, 4.5, 3.2, true),
            FoodItem("food_sprouts_salad", "Moong Sprouts Salad", "Snacks", 1.0, "cup (120g)", 110.0, 7.8, 18.0, 0.8, 4.5, true),
            FoodItem("food_peanut_butter", "Peanut Butter", "Snacks", 1.0, "tbsp (16g)", 95.0, 4.0, 3.0, 8.0, 1.0, true),
            FoodItem("food_almonds", "Almonds & Walnuts", "Snacks", 1.0, "handful (30g)", 180.0, 5.5, 6.0, 16.0, 3.0, true),
            FoodItem("food_whey", "Whey Protein Isolate", "Protein", 1.0, "scoop (30g)", 120.0, 24.0, 2.0, 1.5, 0.0, true)
        )
        foodDao().insertFoods(foods)
    }
}
