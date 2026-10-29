package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.data.local.Project12Database
import com.example.data.model.*
import com.example.data.remote.FirestoreSyncService
import com.example.data.remote.GeminiCoachService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

private const val TAG = "TransformationRepo"

class TransformationRepository(
    private val context: Context,
    private val db: Project12Database,
    private val firestoreSync: FirestoreSyncService,
    private val coachService: GeminiCoachService
) {
    private val profileDao = db.userProfileDao()
    private val workoutDao = db.workoutDao()
    private val foodDao = db.foodDao()
    private val mealDao = db.mealDao()
    private val cardioDao = db.cardioDao()
    private val measurementDao = db.measurementDao()
    private val photoDao = db.photoDao()
    private val habitDao = db.habitDao()
    private val chatDao = db.chatDao()

    // -------------------------------------------------------------
    // PROFILE & ONBOARDING
    // -------------------------------------------------------------
    fun observeProfile(): Flow<UserProfile?> = profileDao.observeProfile()

    suspend fun getProfile(): UserProfile? = profileDao.getProfile()

    suspend fun saveProfile(profile: UserProfile) {
        profileDao.insertProfile(profile)
        firestoreSync.syncProfile(profile)
    }

    fun calculateMacroTargets(
        age: Int,
        heightCm: Double,
        weightKg: Double,
        sex: String,
        goal: String,
        activityLevel: String
    ): Pair<Int, Int> {
        val bmr = if (sex.equals("female", ignoreCase = true)) {
            (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) - 161.0
        } else {
            (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) + 5.0
        }

        val activityMultiplier = when (activityLevel.lowercase()) {
            "sedentary" -> 1.2
            "lightly_active" -> 1.375
            "moderately_active" -> 1.55
            "very_active" -> 1.725
            else -> 1.45
        }

        val tdee = bmr * activityMultiplier

        val targetCalories = when (goal.lowercase()) {
            "fat_loss" -> (tdee - 450.0).toInt().coerceAtLeast(1400)
            "muscle_gain" -> (tdee + 300.0).toInt()
            "body_recomposition" -> (tdee - 150.0).toInt()
            else -> tdee.toInt()
        }

        // 1.8 - 2.2g of protein per kg of body weight
        val targetProtein = (weightKg * 2.0).toInt().coerceIn(80, 220)

        return Pair(targetCalories, targetProtein)
    }

    // -------------------------------------------------------------
    // WORKOUTS & TEMPLATES
    // -------------------------------------------------------------
    fun observeWorkoutTemplates(): Flow<List<WorkoutTemplate>> = workoutDao.observeTemplates()

    fun observeExercisesForTemplate(templateId: String): Flow<List<ExerciseTemplate>> =
        workoutDao.observeExercisesForTemplate(templateId)

    suspend fun getExercisesForTemplate(templateId: String): List<ExerciseTemplate> =
        workoutDao.getExercisesForTemplate(templateId)

    fun observeSessionForDate(date: String): Flow<WorkoutSession?> = workoutDao.observeSessionForDate(date)

    suspend fun getSessionForDate(date: String): WorkoutSession? = workoutDao.getSessionForDate(date)

    fun observeExerciseLogsForDate(date: String): Flow<List<ExerciseLog>> =
        workoutDao.observeExerciseLogsForDate(date)

    suspend fun saveExerciseLog(log: ExerciseLog) {
        workoutDao.insertExerciseLog(log)
    }

    // --- Exercises ---
    fun observeAllExercises(): Flow<List<Exercise>> = workoutDao.observeAllExercises()

    fun observeExercisesByMuscleGroup(muscleGroup: String): Flow<List<Exercise>> =
        workoutDao.observeExercisesByMuscleGroup(muscleGroup)

    suspend fun getExerciseById(id: String): Exercise? = workoutDao.getExerciseById(id)

    // --- Workout Sessions & Logs ---
    fun observeSessionsForWeek(week: Int): Flow<List<WorkoutSession>> =
        workoutDao.observeSessionsForWeek(week)

    fun observeSessionWithLogs(sessionId: String): Flow<WorkoutSessionWithLogs?> =
        workoutDao.observeSessionWithLogs(sessionId)

    fun observeAllSessionsWithLogs(): Flow<List<WorkoutSessionWithLogs>> =
        workoutDao.observeAllSessionsWithLogs()

    fun observeWorkoutLogsForExercise(exerciseId: String): Flow<List<WorkoutLog>> =
        workoutDao.observeWorkoutLogsForExercise(exerciseId)

    fun observePersonalRecord(exerciseId: String): Flow<Double?> =
        workoutDao.observePersonalRecord(exerciseId)

    fun observeWeeklyVolume(weekNumber: Int): Flow<Double?> =
        workoutDao.observeWeeklyVolume(weekNumber)

    suspend fun saveWorkoutLog(log: WorkoutLog) {
        workoutDao.insertWorkoutLog(log)
    }

    suspend fun saveWorkoutSession(session: WorkoutSession) {
        workoutDao.insertSession(session)
        firestoreSync.syncWorkoutSession(session)
    }

    fun calculateCurrentWeek(startDateStr: String): Int {
        return try {
            val start = LocalDate.parse(startDateStr)
            val now = LocalDate.now()
            val daysBetween = ChronoUnit.DAYS.between(start, now)
            val week = (daysBetween / 7).toInt() + 1
            week.coerceIn(1, 12)
        } catch (e: Exception) {
            1
        }
    }

    // -------------------------------------------------------------
    // NUTRITION & FOOD
    // -------------------------------------------------------------
    fun observeAllFoods(): Flow<List<FoodItem>> = foodDao.observeAllFoods()

    fun searchFoods(query: String): Flow<List<FoodItem>> = foodDao.searchFoods(query)

    suspend fun addCustomFood(food: FoodItem) = foodDao.insertFood(food)

    fun observeMealsForDate(date: String): Flow<List<MealEntry>> = mealDao.observeMealsForDate(date)

    suspend fun addMealEntry(meal: MealEntry) {
        mealDao.insertMeal(meal)
        firestoreSync.syncMealEntry(meal)
    }

    suspend fun deleteMealEntry(meal: MealEntry) {
        mealDao.deleteMeal(meal)
        firestoreSync.deleteMealEntry(meal.entryId)
    }

    // -------------------------------------------------------------
    // CARDIO & ACTIVITY
    // -------------------------------------------------------------
    fun observeCardioForDate(date: String): Flow<List<CardioEntry>> = cardioDao.observeCardioForDate(date)

    fun observeAllCardio(): Flow<List<CardioEntry>> = cardioDao.observeAllCardio()

    suspend fun addCardioEntry(cardio: CardioEntry) {
        cardioDao.insertCardio(cardio)
        firestoreSync.syncCardio(cardio)
    }

    // -------------------------------------------------------------
    // BODY MEASUREMENTS
    // -------------------------------------------------------------
    fun observeAllMeasurements(): Flow<List<BodyMeasurementEntry>> = measurementDao.observeAllMeasurements()

    fun observeLatestMeasurement(): Flow<BodyMeasurementEntry?> = measurementDao.observeLatestMeasurement()

    suspend fun addMeasurement(entry: BodyMeasurementEntry) {
        measurementDao.insertMeasurement(entry)
        firestoreSync.syncMeasurement(entry)
    }

    // -------------------------------------------------------------
    // PROGRESS PHOTOS (PRIVATE DEVICE SANDBOX)
    // -------------------------------------------------------------
    fun observeAllPhotos(): Flow<List<PhotoEntry>> = photoDao.observeAllPhotos()

    suspend fun getPhotoForDate(date: String): PhotoEntry? = photoDao.getPhotoForDate(date)

    suspend fun savePhotoFromUri(uri: Uri, date: String, poseType: String, weekNumber: Int, notes: String): PhotoEntry? {
        return withContext(Dispatchers.IO) {
            try {
                val photosDir = File(context.filesDir, "transformation_photos").apply {
                    if (!exists()) mkdirs()
                }
                val filename = "photo_${date}_${poseType}_${System.currentTimeMillis()}.jpg"
                val destFile = File(photosDir, filename)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    val bitmap = BitmapFactory.decodeStream(input)
                    FileOutputStream(destFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                }

                val entry = PhotoEntry(
                    photoId = UUID.randomUUID().toString(),
                    userId = profileDao.getProfile()?.userId ?: "local_user",
                    date = date,
                    filePath = destFile.absolutePath,
                    poseType = poseType,
                    weekNumber = weekNumber,
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                )
                photoDao.insertPhoto(entry)
                firestoreSync.syncPhotoEntry(entry)
                entry
            } catch (e: Exception) {
                Log.e(TAG, "Error saving transformation photo: ${e.message}", e)
                null
            }
        }
    }

    suspend fun deletePhoto(photoId: String) {
        photoDao.deletePhotoById(photoId)
    }

    // -------------------------------------------------------------
    // HABITS (WATER, SLEEP)
    // -------------------------------------------------------------
    fun observeHabitForDate(date: String): Flow<DailyHabit?> = habitDao.observeHabitForDate(date)

    suspend fun addWater(date: String, amountMl: Int) {
        val existing = habitDao.getHabitForDate(date) ?: DailyHabit(date = date)
        habitDao.insertHabit(existing.copy(waterMl = existing.waterMl + amountMl))
    }

    // -------------------------------------------------------------
    // AI FITNESS COACH (GEMINI 3.8 FLASH)
    // -------------------------------------------------------------
    fun observeChatMessages(): Flow<List<ChatMessage>> = chatDao.observeMessages()

    suspend fun sendMessageToCoach(userMessage: String, contextInfo: String): String {
        val uid = profileDao.getProfile()?.userId ?: "user"
        val userChatMsg = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            userId = uid,
            role = "user",
            content = userMessage,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(userChatMsg)
        firestoreSync.syncChatMessage(userChatMsg)

        val coachReply = coachService.getAdvice(contextInfo, userMessage)

        val modelChatMsg = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            userId = uid,
            role = "model",
            content = coachReply,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(modelChatMsg)
        firestoreSync.syncChatMessage(modelChatMsg)

        return coachReply
    }
}
