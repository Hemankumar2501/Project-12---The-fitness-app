package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun observeProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Query("DELETE FROM user_profile")
    suspend fun clearProfile()
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_templates ORDER BY orderIndex ASC")
    fun observeTemplates(): Flow<List<WorkoutTemplate>>

    @Query("SELECT * FROM workout_templates WHERE dayOfWeek = :day LIMIT 1")
    suspend fun getTemplateForDay(day: String): WorkoutTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<WorkoutTemplate>)

    // --- Exercises ---
    @Query("SELECT * FROM exercises ORDER BY orderIndex ASC")
    fun observeAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup ORDER BY orderIndex ASC")
    fun observeExercisesByMuscleGroup(muscleGroup: String): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE exerciseId = :id LIMIT 1")
    suspend fun getExerciseById(id: String): Exercise?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)

    @Query("SELECT * FROM exercise_templates WHERE templateId = :templateId ORDER BY orderIndex ASC")
    fun observeExercisesForTemplate(templateId: String): Flow<List<ExerciseTemplate>>

    @Query("SELECT * FROM exercise_templates WHERE templateId = :templateId ORDER BY orderIndex ASC")
    suspend fun getExercisesForTemplate(templateId: String): List<ExerciseTemplate>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseTemplates(exercises: List<ExerciseTemplate>)

    // --- Workout Sessions ---
    @Query("SELECT * FROM workout_sessions WHERE date = :date LIMIT 1")
    fun observeSessionForDate(date: String): Flow<WorkoutSession?>

    @Query("SELECT * FROM workout_sessions WHERE date = :date LIMIT 1")
    suspend fun getSessionForDate(date: String): WorkoutSession?

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun observeAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE weekNumber = :weekNumber ORDER BY date ASC")
    fun observeSessionsForWeek(weekNumber: Int): Flow<List<WorkoutSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession)

    @Query("UPDATE workout_sessions SET isCompleted = :isCompleted, durationMinutes = :durationMinutes WHERE sessionId = :sessionId")
    suspend fun updateSessionCompletion(sessionId: String, isCompleted: Boolean, durationMinutes: Int)

    // --- Workout Logs (Per-exercise set logs for 12-week progression) ---
    @Query("SELECT * FROM workout_logs WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun observeWorkoutLogsForSession(sessionId: String): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs WHERE exerciseId = :exerciseId ORDER BY date ASC")
    fun observeWorkoutLogsForExercise(exerciseId: String): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs WHERE weekNumber = :weekNumber ORDER BY date ASC")
    fun observeWorkoutLogsForWeek(weekNumber: Int): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLogs(logs: List<WorkoutLog>)

    @Query("SELECT MAX(maxWeight) FROM workout_logs WHERE exerciseId = :exerciseId")
    fun observePersonalRecord(exerciseId: String): Flow<Double?>

    @Query("SELECT SUM(totalVolume) FROM workout_logs WHERE weekNumber = :weekNumber")
    fun observeWeeklyVolume(weekNumber: Int): Flow<Double?>

    // --- Transactions & Relations ---
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE sessionId = :sessionId LIMIT 1")
    fun observeSessionWithLogs(sessionId: String): Flow<WorkoutSessionWithLogs?>

    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun observeAllSessionsWithLogs(): Flow<List<WorkoutSessionWithLogs>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE weekNumber = :weekNumber ORDER BY date ASC")
    fun observeSessionsWithLogsForWeek(weekNumber: Int): Flow<List<WorkoutSessionWithLogs>>

    @Transaction
    @Query("SELECT * FROM exercises WHERE exerciseId = :exerciseId LIMIT 1")
    fun observeExerciseWithHistory(exerciseId: String): Flow<ExerciseWithHistory?>

    // --- Backwards compatible exercise logs ---
    @Query("SELECT * FROM exercise_logs WHERE date = :date")
    fun observeExerciseLogsForDate(date: String): Flow<List<ExerciseLog>>

    @Query("SELECT * FROM exercise_logs WHERE date = :date")
    suspend fun getExerciseLogsForDate(date: String): List<ExerciseLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLog(log: ExerciseLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLogs(logs: List<ExerciseLog>)
}

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_items ORDER BY name ASC")
    fun observeAllFoods(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFoods(query: String): Flow<List<FoodItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodItem)

    @Delete
    suspend fun deleteFood(food: FoodItem)
}

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_entries WHERE date = :date ORDER BY timestamp ASC")
    fun observeMealsForDate(date: String): Flow<List<MealEntry>>

    @Query("SELECT * FROM meal_entries ORDER BY timestamp DESC")
    fun observeAllMeals(): Flow<List<MealEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntry)

    @Delete
    suspend fun deleteMeal(meal: MealEntry)

    @Query("DELETE FROM meal_entries WHERE entryId = :id")
    suspend fun deleteMealById(id: String)
}

@Dao
interface CardioDao {
    @Query("SELECT * FROM cardio_entries WHERE date = :date ORDER BY timestamp ASC")
    fun observeCardioForDate(date: String): Flow<List<CardioEntry>>

    @Query("SELECT * FROM cardio_entries ORDER BY timestamp DESC")
    fun observeAllCardio(): Flow<List<CardioEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardio(entry: CardioEntry)

    @Delete
    suspend fun deleteCardio(entry: CardioEntry)
}

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM body_measurements ORDER BY date ASC")
    fun observeAllMeasurements(): Flow<List<BodyMeasurementEntry>>

    @Query("SELECT * FROM body_measurements ORDER BY date DESC LIMIT 1")
    fun observeLatestMeasurement(): Flow<BodyMeasurementEntry?>

    @Query("SELECT * FROM body_measurements WHERE date = :date LIMIT 1")
    suspend fun getMeasurementForDate(date: String): BodyMeasurementEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(entry: BodyMeasurementEntry)

    @Delete
    suspend fun deleteMeasurement(entry: BodyMeasurementEntry)
}

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photo_entries ORDER BY date DESC")
    fun observeAllPhotos(): Flow<List<PhotoEntry>>

    @Query("SELECT * FROM photo_entries WHERE date = :date LIMIT 1")
    suspend fun getPhotoForDate(date: String): PhotoEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntry)

    @Delete
    suspend fun deletePhoto(photo: PhotoEntry)

    @Query("DELETE FROM photo_entries WHERE photoId = :photoId")
    suspend fun deletePhotoById(photoId: String)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM daily_habits WHERE date = :date LIMIT 1")
    fun observeHabitForDate(date: String): Flow<DailyHabit?>

    @Query("SELECT * FROM daily_habits WHERE date = :date LIMIT 1")
    suspend fun getHabitForDate(date: String): DailyHabit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: DailyHabit)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun observeMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearMessages()
}
