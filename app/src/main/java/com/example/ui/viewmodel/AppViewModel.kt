package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.Project12Database
import com.example.data.model.*
import com.example.data.remote.FirestoreSyncService
import com.example.data.remote.GeminiCoachService
import com.example.data.repository.TransformationRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class AppScreen {
    HOME,
    WORKOUTS,
    NUTRITION,
    PROGRESS,
    COACH,
    PROFILE_SETTINGS,
    ACTIVE_WORKOUT,
    ONBOARDING
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Project12Database.getInstance(application)
    private val firestoreSync = FirestoreSyncService(application)
    private val coachService = GeminiCoachService()
    val repository = TransformationRepository(application, db, firestoreSync, coachService)

    // Current Navigation Screen
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Active Date (YYYY-MM-DD)
    private val _selectedDate = MutableStateFlow(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Profile State
    val userProfile: StateFlow<UserProfile?> = repository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Rest Timer State
    private val _restSecondsRemaining = MutableStateFlow(0)
    val restSecondsRemaining: StateFlow<Int> = _restSecondsRemaining.asStateFlow()
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()
    private var timerJob: Job? = null

    // Workouts
    val workoutTemplates: StateFlow<List<WorkoutTemplate>> = repository.observeWorkoutTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTemplateId = MutableStateFlow("mon_push")
    val selectedTemplateId: StateFlow<String> = _selectedTemplateId.asStateFlow()

    val templateExercises: StateFlow<List<ExerciseTemplate>> = _selectedTemplateId
        .flatMapLatest { id -> repository.observeExercisesForTemplate(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySession: StateFlow<WorkoutSession?> = _selectedDate
        .flatMapLatest { date -> repository.observeSessionForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayExerciseLogs: StateFlow<List<ExerciseLog>> = _selectedDate
        .flatMapLatest { date -> repository.observeExerciseLogsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises: StateFlow<List<Exercise>> = repository.observeAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkoutSessionsWithLogs: StateFlow<List<WorkoutSessionWithLogs>> = repository.observeAllSessionsWithLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeWorkoutLogsForExercise(exerciseId: String): Flow<List<WorkoutLog>> =
        repository.observeWorkoutLogsForExercise(exerciseId)

    fun observePersonalRecord(exerciseId: String): Flow<Double?> =
        repository.observePersonalRecord(exerciseId)

    fun observeWeeklyVolume(weekNumber: Int): Flow<Double?> =
        repository.observeWeeklyVolume(weekNumber)

    // Nutrition
    val foodSearchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<FoodItem>> = foodSearchQuery
        .debounce(200)
        .flatMapLatest { query ->
            if (query.isBlank()) repository.observeAllFoods()
            else repository.searchFoods(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayMeals: StateFlow<List<MealEntry>> = _selectedDate
        .flatMapLatest { date -> repository.observeMealsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cardio
    val todayCardio: StateFlow<List<CardioEntry>> = _selectedDate
        .flatMapLatest { date -> repository.observeCardioForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCardio: StateFlow<List<CardioEntry>> = repository.observeAllCardio()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Measurements
    val allMeasurements: StateFlow<List<BodyMeasurementEntry>> = repository.observeAllMeasurements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestMeasurement: StateFlow<BodyMeasurementEntry?> = repository.observeLatestMeasurement()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Habits (Water)
    val todayHabit: StateFlow<DailyHabit?> = _selectedDate
        .flatMapLatest { date -> repository.observeHabitForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Photos
    val allPhotos: StateFlow<List<PhotoEntry>> = repository.observeAllPhotos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Coach Chat
    val chatMessages: StateFlow<List<ChatMessage>> = repository.observeChatMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isCoachThinking = MutableStateFlow(false)
    val isCoachThinking: StateFlow<Boolean> = _isCoachThinking.asStateFlow()

    init {
        // Automatically check if onboarding needs to be shown
        viewModelScope.launch {
            val profile = repository.getProfile()
            if (profile == null || !profile.completedOnboarding) {
                _currentScreen.value = AppScreen.ONBOARDING
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun selectWorkoutTemplate(templateId: String) {
        _selectedTemplateId.value = templateId
    }

    // ------------------------------------------------------------------------
    // REST TIMER
    // ------------------------------------------------------------------------
    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        _restSecondsRemaining.value = seconds
        _isTimerRunning.value = true

        timerJob = viewModelScope.launch {
            while (_restSecondsRemaining.value > 0) {
                delay(1000)
                _restSecondsRemaining.value -= 1
            }
            _isTimerRunning.value = false
            vibrateAlert()
        }
    }

    fun pauseRestTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
    }

    fun resetRestTimer() {
        timerJob?.cancel()
        _restSecondsRemaining.value = 0
        _isTimerRunning.value = false
    }

    private fun vibrateAlert() {
        try {
            val vibrator = getApplication<Application>().getSystemService(Vibrator::class.java)
            vibrator?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(500)
                }
            }
        } catch (_: Exception) {}
    }

    // ------------------------------------------------------------------------
    // WORKOUT ACTIONS
    // ------------------------------------------------------------------------
    fun saveExerciseSet(
        exerciseId: String,
        exerciseName: String,
        setNumber: Int,
        reps: Int,
        weightKg: Double,
        isCompleted: Boolean
    ) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val existingLogs = repository.getSessionForDate(date)
            val currentLogs = repository.getExercisesForTemplate(_selectedTemplateId.value)
            
            val weekNum = repository.calculateCurrentWeek(userProfile.value?.startDate ?: LocalDate.now().toString())
            val log = ExerciseLog(
                logId = "log_${date}_${exerciseId}",
                sessionId = "session_${date}",
                userId = userProfile.value?.userId ?: "user",
                date = date,
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                set1Reps = reps,
                set1Weight = weightKg,
                isCompleted = isCompleted
            )
            repository.saveExerciseLog(log)

            val workoutLog = WorkoutLog(
                logId = "wlog_${date}_${exerciseId}_set${setNumber}",
                sessionId = "session_${date}",
                userId = userProfile.value?.userId ?: "user",
                date = date,
                weekNumber = weekNum,
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                set1Reps = reps,
                set1Weight = weightKg,
                set1Completed = isCompleted,
                isCompleted = isCompleted,
                maxWeight = weightKg,
                totalVolume = reps * weightKg
            )
            repository.saveWorkoutLog(workoutLog)
        }
    }

    fun finishWorkout(
        templateTitle: String,
        durationMinutes: Int,
        perceivedEffort: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val profile = userProfile.value
            val start = profile?.startDate ?: LocalDate.now().toString()
            val weekNum = repository.calculateCurrentWeek(start)

            val session = WorkoutSession(
                sessionId = "session_${date}",
                userId = profile?.userId ?: "user",
                date = date,
                weekNumber = weekNum,
                dayOfWeek = LocalDate.parse(date).dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                workoutTitle = templateTitle,
                targetMuscleGroups = "Full Session Completed",
                completedExercisesCount = 5,
                totalExercisesCount = 5,
                durationMinutes = durationMinutes,
                isCompleted = true,
                perceivedEffort = perceivedEffort,
                notes = notes
            )
            repository.saveWorkoutSession(session)
            _currentScreen.value = AppScreen.HOME
        }
    }

    // ------------------------------------------------------------------------
    // NUTRITION ACTIONS
    // ------------------------------------------------------------------------
    fun addMeal(
        food: FoodItem,
        portionMultiplier: Double,
        mealType: String
    ) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val entry = MealEntry(
                entryId = UUID.randomUUID().toString(),
                userId = userProfile.value?.userId ?: "user",
                date = date,
                mealType = mealType,
                foodName = food.name,
                portion = portionMultiplier,
                unit = food.servingUnit,
                calories = food.calories * portionMultiplier,
                protein = food.protein * portionMultiplier,
                carbs = food.carbs * portionMultiplier,
                fat = food.fat * portionMultiplier,
                fibre = food.fibre * portionMultiplier
            )
            repository.addMealEntry(entry)
        }
    }

    fun deleteMeal(meal: MealEntry) {
        viewModelScope.launch {
            repository.deleteMealEntry(meal)
        }
    }

    fun addCustomFoodItem(
        name: String,
        category: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        fibre: Double,
        isVegetarian: Boolean
    ) {
        viewModelScope.launch {
            val food = FoodItem(
                foodId = "custom_${System.currentTimeMillis()}",
                name = name,
                category = category,
                servingSize = 1.0,
                servingUnit = "serving",
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                fibre = fibre,
                isVegetarian = isVegetarian,
                isCustom = true
            )
            repository.addCustomFood(food)
        }
    }

    // ------------------------------------------------------------------------
    // CARDIO ACTIONS
    // ------------------------------------------------------------------------
    fun logCardio(
        activityType: String,
        durationMinutes: Int,
        distanceKm: Double,
        steps: Int,
        intensity: String,
        notes: String
    ) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val calEst = durationMinutes * when (intensity) {
                "High" -> 10.0
                "Moderate" -> 6.5
                else -> 4.5
            }
            val entry = CardioEntry(
                cardioId = UUID.randomUUID().toString(),
                userId = userProfile.value?.userId ?: "user",
                date = date,
                activityType = activityType,
                durationMinutes = durationMinutes,
                distanceKm = distanceKm,
                steps = steps,
                intensity = intensity,
                caloriesBurned = calEst,
                notes = notes
            )
            repository.addCardioEntry(entry)
        }
    }

    // ------------------------------------------------------------------------
    // WATER & HABITS
    // ------------------------------------------------------------------------
    fun addWaterServing(amountMl: Int = 250) {
        viewModelScope.launch {
            repository.addWater(_selectedDate.value, amountMl)
        }
    }

    // ------------------------------------------------------------------------
    // BODY MEASUREMENTS
    // ------------------------------------------------------------------------
    fun recordMeasurement(
        weightKg: Double,
        waistCm: Double,
        chestCm: Double,
        hipsCm: Double,
        armCm: Double,
        thighCm: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val entry = BodyMeasurementEntry(
                measurementId = "m_${date}",
                userId = userProfile.value?.userId ?: "user",
                date = date,
                weightKg = weightKg,
                waistCm = waistCm,
                chestCm = chestCm,
                hipsCm = hipsCm,
                armCm = armCm,
                thighCm = thighCm,
                notes = notes
            )
            repository.addMeasurement(entry)
        }
    }

    // ------------------------------------------------------------------------
    // PHOTOS (PRIVATE SANDBOX)
    // ------------------------------------------------------------------------
    fun savePhoto(uri: Uri, poseType: String, notes: String) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val start = userProfile.value?.startDate ?: LocalDate.now().toString()
            val week = repository.calculateCurrentWeek(start)
            repository.savePhotoFromUri(uri, date, poseType, week, notes)
        }
    }

    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            repository.deletePhoto(photoId)
        }
    }

    // ------------------------------------------------------------------------
    // AI FITNESS COACH
    // ------------------------------------------------------------------------
    fun askCoach(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _isCoachThinking.value = true
            val profile = userProfile.value
            val weekNum = repository.calculateCurrentWeek(profile?.startDate ?: LocalDate.now().toString())
            val meals = todayMeals.value
            val consumedCals = meals.sumOf { it.calories }.toInt()
            val consumedProtein = meals.sumOf { it.protein }.toInt()

            val contextInfo = """
                User: ${profile?.name ?: "Athlete"}
                Current Week: Week $weekNum of 12
                Goal: ${profile?.primaryGoal ?: "fat loss"}
                Daily Calorie Target: ${profile?.dailyCaloriesTarget ?: 2100} kcal (Consumed so far: $consumedCals kcal)
                Daily Protein Target: ${profile?.dailyProteinTarget ?: 150} g (Consumed so far: $consumedProtein g)
                Diet Preference: ${profile?.dietaryPreference ?: "All"}
                Equipment: ${profile?.availableEquipment ?: "Dumbbells"}
            """.trimIndent()

            repository.sendMessageToCoach(question, contextInfo)
            _isCoachThinking.value = false
        }
    }

    // ------------------------------------------------------------------------
    // ONBOARDING SUBMISSION
    // ------------------------------------------------------------------------
    fun completeOnboarding(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile.copy(completedOnboarding = true))
            _currentScreen.value = AppScreen.HOME
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }
}
