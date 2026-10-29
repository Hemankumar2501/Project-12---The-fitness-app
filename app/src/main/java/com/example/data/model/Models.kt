package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    @DocumentId
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 28,
    val heightCm: Double = 175.0,
    val currentWeightKg: Double = 78.0,
    val targetWeightKg: Double = 70.0,
    val sex: String = "male",
    val primaryGoal: String = "fat_loss", // fat_loss, muscle_gain, body_recomposition
    val fitnessExperience: String = "intermediate", // beginner, intermediate, advanced
    val availableEquipment: String = "Dumbbells, Resistance Bands, Bodyweight",
    val workoutTimeAvailability: String = "45 minutes",
    val dietaryPreference: String = "all", // all, vegetarian, eggetarian, vegan
    val allergies: String = "None",
    val activityLevel: String = "moderately_active",
    val timezone: String = "Asia/Kolkata",
    val startDate: String = "2026-10-27",
    val dailyCaloriesTarget: Int = 2100,
    val dailyProteinTarget: Int = 150,
    val dailyWaterTarget: Int = 3000, // ml
    val completedOnboarding: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "workout_templates")
data class WorkoutTemplate(
    @PrimaryKey
    val templateId: String = "",
    val weekPhase: String = "Weeks 1-4 Foundation",
    val dayOfWeek: String = "Monday", // Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
    val title: String = "",
    val focus: String = "",
    val targetMuscles: String = "",
    val estimatedMinutes: Int = 45,
    val orderIndex: Int = 1
)

@Entity(tableName = "exercise_templates")
data class ExerciseTemplate(
    @PrimaryKey
    val exerciseId: String = "",
    val templateId: String = "",
    val name: String = "",
    val targetSets: Int = 3,
    val targetReps: String = "8-12 reps",
    val restSeconds: Int = 60,
    val equipmentNeeded: String = "Dumbbells",
    val instructions: String = "",
    val substituteAlternative: String = "",
    val orderIndex: Int = 1
)

@Entity(tableName = "exercises", indices = [Index("muscleGroup"), Index("category")])
data class Exercise(
    @PrimaryKey
    val exerciseId: String = "",
    val name: String = "",
    val muscleGroup: String = "", // Chest, Shoulders, Triceps, Back, Biceps, Legs, Core, Cardio
    val category: String = "Strength", // Strength, Hypertrophy, Mobility, Cardio
    val equipmentNeeded: String = "Dumbbells",
    val defaultSets: Int = 3,
    val defaultReps: String = "8-12 reps",
    val restSeconds: Int = 60,
    val instructions: String = "",
    val formCues: String = "",
    val substituteAlternative: String = "",
    val orderIndex: Int = 1
)

@IgnoreExtraProperties
@Entity(tableName = "workout_sessions", indices = [Index("date"), Index("weekNumber"), Index("userId")])
data class WorkoutSession(
    @PrimaryKey
    @DocumentId
    val sessionId: String = "",
    val userId: String = "",
    val date: String = "", // YYYY-MM-DD
    val weekNumber: Int = 1,
    val dayOfWeek: String = "Monday",
    val workoutTitle: String = "",
    val targetMuscleGroups: String = "",
    val completedExercisesCount: Int = 0,
    val totalExercisesCount: Int = 0,
    val durationMinutes: Int = 0,
    val isCompleted: Boolean = false,
    val perceivedEffort: Int = 7, // 1-10
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
@Entity(tableName = "exercise_logs")
data class ExerciseLog(
    @PrimaryKey
    @DocumentId
    val logId: String = "",
    val sessionId: String = "",
    val userId: String = "",
    val date: String = "",
    val exerciseId: String = "",
    val exerciseName: String = "",
    val set1Reps: Int = 0,
    val set1Weight: Double = 0.0,
    val set2Reps: Int = 0,
    val set2Weight: Double = 0.0,
    val set3Reps: Int = 0,
    val set3Weight: Double = 0.0,
    val set4Reps: Int = 0,
    val set4Weight: Double = 0.0,
    val isCompleted: Boolean = false,
    val notes: String = ""
)

@IgnoreExtraProperties
@Entity(
    tableName = "workout_logs",
    indices = [Index("sessionId"), Index("exerciseId"), Index("date"), Index("weekNumber")]
)
data class WorkoutLog(
    @PrimaryKey
    @DocumentId
    val logId: String = "",
    val sessionId: String = "",
    val userId: String = "",
    val date: String = "", // YYYY-MM-DD
    val weekNumber: Int = 1,
    val exerciseId: String = "",
    val exerciseName: String = "",
    val set1Reps: Int = 0,
    val set1Weight: Double = 0.0,
    val set1Completed: Boolean = false,
    val set2Reps: Int = 0,
    val set2Weight: Double = 0.0,
    val set2Completed: Boolean = false,
    val set3Reps: Int = 0,
    val set3Weight: Double = 0.0,
    val set3Completed: Boolean = false,
    val set4Reps: Int = 0,
    val set4Weight: Double = 0.0,
    val set4Completed: Boolean = false,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val maxWeight: Double = 0.0,
    val totalVolume: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class WorkoutSessionWithLogs(
    @Embedded val session: WorkoutSession,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val logs: List<WorkoutLog>
)

data class ExerciseWithHistory(
    @Embedded val exercise: Exercise,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "exerciseId"
    )
    val history: List<WorkoutLog>
)

data class ExerciseProgressionPoint(
    val date: String,
    val weekNumber: Int,
    val maxWeight: Double,
    val totalVolume: Double
)

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey
    val foodId: String = "",
    val name: String = "",
    val category: String = "General", // South Indian, North Indian, Protein, Fruits, Snacks, Dairy
    val servingSize: Double = 1.0,
    val servingUnit: String = "piece", // piece, cup, 100g, bowl, scoop
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fibre: Double = 0.0,
    val isVegetarian: Boolean = true,
    val isCustom: Boolean = false
)

@IgnoreExtraProperties
@Entity(tableName = "meal_entries")
data class MealEntry(
    @PrimaryKey
    @DocumentId
    val entryId: String = "",
    val userId: String = "",
    val date: String = "", // YYYY-MM-DD
    val mealType: String = "Breakfast", // Breakfast, Mid-Morning Snack, Lunch, Evening Snack, Dinner, Other
    val foodName: String = "",
    val portion: Double = 1.0,
    val unit: String = "piece",
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fibre: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
@Entity(tableName = "cardio_entries")
data class CardioEntry(
    @PrimaryKey
    @DocumentId
    val cardioId: String = "",
    val userId: String = "",
    val date: String = "",
    val activityType: String = "Brisk Walking", // Walking, Cycling, Jogging, Dancing, Indoor Marching, Other
    val durationMinutes: Int = 30,
    val distanceKm: Double = 0.0,
    val steps: Int = 0,
    val intensity: String = "Moderate", // Light, Moderate, High
    val caloriesBurned: Double = 150.0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
@Entity(tableName = "body_measurements")
data class BodyMeasurementEntry(
    @PrimaryKey
    @DocumentId
    val measurementId: String = "",
    val userId: String = "",
    val date: String = "", // YYYY-MM-DD
    val weightKg: Double = 0.0,
    val waistCm: Double = 0.0,
    val chestCm: Double = 0.0,
    val hipsCm: Double = 0.0,
    val armCm: Double = 0.0,
    val thighCm: Double = 0.0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
@Entity(tableName = "photo_entries")
data class PhotoEntry(
    @PrimaryKey
    @DocumentId
    val photoId: String = "",
    val userId: String = "",
    val date: String = "", // YYYY-MM-DD
    val filePath: String = "",
    val poseType: String = "front", // front, side, back
    val weekNumber: Int = 1,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_habits")
data class DailyHabit(
    @PrimaryKey
    val date: String = "", // YYYY-MM-DD
    val waterMl: Int = 0,
    val sleepHours: Double = 7.0,
    val stretchingDone: Boolean = false,
    val movementBreaksDone: Boolean = false,
    val dailyNotes: String = ""
)

@IgnoreExtraProperties
@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey
    @DocumentId
    val messageId: String = "",
    val userId: String = "",
    val role: String = "user", // user, model, system
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
