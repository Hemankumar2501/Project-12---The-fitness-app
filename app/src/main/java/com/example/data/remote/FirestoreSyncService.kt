package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreSyncService"

class FirestoreSyncService(context: Context) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )

    private val auth = Firebase.auth

    private fun currentUserId(): String? = auth.currentUser?.uid

    suspend fun syncProfile(profile: UserProfile) {
        val uid = currentUserId() ?: return
        val docRef = db.collection("users").document(uid)
        try {
            val payload = mapOf(
                "userId" to uid,
                "name" to profile.name,
                "email" to profile.email,
                "age" to profile.age,
                "heightCm" to profile.heightCm,
                "currentWeightKg" to profile.currentWeightKg,
                "targetWeightKg" to profile.targetWeightKg,
                "sex" to profile.sex,
                "primaryGoal" to profile.primaryGoal,
                "fitnessExperience" to profile.fitnessExperience,
                "availableEquipment" to profile.availableEquipment,
                "workoutTimeAvailability" to profile.workoutTimeAvailability,
                "dietaryPreference" to profile.dietaryPreference,
                "allergies" to profile.allergies,
                "activityLevel" to profile.activityLevel,
                "timezone" to profile.timezone,
                "startDate" to profile.startDate,
                "dailyCaloriesTarget" to profile.dailyCaloriesTarget,
                "dailyProteinTarget" to profile.dailyProteinTarget,
                "dailyWaterTarget" to profile.dailyWaterTarget,
                "completedOnboarding" to profile.completedOnboarding,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload, SetOptions.merge()).await()
            Log.d(TAG, "Profile synced successfully to Firestore for $uid")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync profile to Firestore: ${e.message}")
        }
    }

    suspend fun syncWorkoutSession(session: WorkoutSession) {
        val uid = currentUserId() ?: return
        val docId = session.sessionId.ifBlank { "session_${session.date}" }
        val docRef = db.collection("users").document(uid).collection("workoutSessions").document(docId)
        try {
            val payload = mapOf(
                "userId" to uid,
                "date" to session.date,
                "weekNumber" to session.weekNumber,
                "dayOfWeek" to session.dayOfWeek,
                "workoutTitle" to session.workoutTitle,
                "targetMuscleGroups" to session.targetMuscleGroups,
                "completedExercisesCount" to session.completedExercisesCount,
                "totalExercisesCount" to session.totalExercisesCount,
                "durationMinutes" to session.durationMinutes,
                "isCompleted" to session.isCompleted,
                "perceivedEffort" to session.perceivedEffort,
                "notes" to session.notes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync workout session: ${e.message}")
        }
    }

    suspend fun syncMealEntry(meal: MealEntry) {
        val uid = currentUserId() ?: return
        val docId = meal.entryId.ifBlank { "meal_${System.currentTimeMillis()}" }
        val docRef = db.collection("users").document(uid).collection("mealLogs").document(docId)
        try {
            val payload = mapOf(
                "userId" to uid,
                "date" to meal.date,
                "mealType" to meal.mealType,
                "foodName" to meal.foodName,
                "portion" to meal.portion,
                "unit" to meal.unit,
                "calories" to meal.calories,
                "protein" to meal.protein,
                "carbs" to meal.carbs,
                "fat" to meal.fat,
                "fibre" to meal.fibre,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync meal entry: ${e.message}")
        }
    }

    suspend fun deleteMealEntry(mealId: String) {
        val uid = currentUserId() ?: return
        try {
            db.collection("users").document(uid).collection("mealLogs").document(mealId).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete meal from Firestore: ${e.message}")
        }
    }

    suspend fun syncCardio(cardio: CardioEntry) {
        val uid = currentUserId() ?: return
        val docId = cardio.cardioId.ifBlank { "cardio_${System.currentTimeMillis()}" }
        val docRef = db.collection("users").document(uid).collection("cardioLogs").document(docId)
        try {
            val payload = mapOf(
                "userId" to uid,
                "date" to cardio.date,
                "activityType" to cardio.activityType,
                "durationMinutes" to cardio.durationMinutes,
                "distanceKm" to cardio.distanceKm,
                "steps" to cardio.steps,
                "intensity" to cardio.intensity,
                "caloriesBurned" to cardio.caloriesBurned,
                "notes" to cardio.notes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync cardio entry: ${e.message}")
        }
    }

    suspend fun syncMeasurement(measurement: BodyMeasurementEntry) {
        val uid = currentUserId() ?: return
        val docId = measurement.measurementId.ifBlank { "measurement_${measurement.date}" }
        val docRef = db.collection("users").document(uid).collection("bodyMeasurements").document(docId)
        try {
            val payload = mapOf(
                "userId" to uid,
                "date" to measurement.date,
                "weightKg" to measurement.weightKg,
                "waistCm" to measurement.waistCm,
                "chestCm" to measurement.chestCm,
                "hipsCm" to measurement.hipsCm,
                "armCm" to measurement.armCm,
                "thighCm" to measurement.thighCm,
                "notes" to measurement.notes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync body measurement: ${e.message}")
        }
    }

    suspend fun syncPhotoEntry(photo: PhotoEntry) {
        val uid = currentUserId() ?: return
        val docId = photo.photoId.ifBlank { "photo_${photo.date}" }
        val docRef = db.collection("users").document(uid).collection("photoEntries").document(docId)
        try {
            val payload = mapOf(
                "userId" to uid,
                "date" to photo.date,
                "filePath" to photo.filePath,
                "poseType" to photo.poseType,
                "weekNumber" to photo.weekNumber,
                "notes" to photo.notes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync photo entry: ${e.message}")
        }
    }

    suspend fun syncChatMessage(msg: ChatMessage) {
        val uid = currentUserId() ?: return
        val docId = msg.messageId.ifBlank { "msg_${System.currentTimeMillis()}" }
        val docRef = db.collection("users").document(uid).collection("chatMessages").document(docId)
        try {
            val payload = mapOf(
                "userId" to uid,
                "role" to msg.role,
                "content" to msg.content,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync chat message: ${e.message}")
        }
    }
}
