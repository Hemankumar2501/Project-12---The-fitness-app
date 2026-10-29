package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.Project12Database
import com.example.data.local.WorkoutDao
import com.example.data.model.Exercise
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WorkoutDaoTest {

    private lateinit var database: Project12Database
    private lateinit var workoutDao: WorkoutDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, Project12Database::class.java)
            .allowMainThreadQueries()
            .build()
        workoutDao = database.workoutDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndQueryExercises() = runBlocking {
        val exercises = listOf(
            Exercise(
                exerciseId = "ex_floor_press",
                name = "Dumbbell Floor Press",
                muscleGroup = "Chest",
                category = "Strength",
                equipmentNeeded = "Dumbbells",
                defaultSets = 4,
                defaultReps = "8-12 reps",
                instructions = "Press dumbbells while lying flat on floor",
                formCues = "Retract scapula, control descent"
            ),
            Exercise(
                exerciseId = "ex_squat",
                name = "Goblet Squats",
                muscleGroup = "Legs",
                category = "Strength",
                equipmentNeeded = "Dumbbells",
                defaultSets = 4,
                defaultReps = "10-15 reps"
            )
        )

        workoutDao.insertExercises(exercises)

        val all = workoutDao.observeAllExercises().first()
        assertEquals(2, all.size)

        val chestExercises = workoutDao.observeExercisesByMuscleGroup("Chest").first()
        assertEquals(1, chestExercises.size)
        assertEquals("Dumbbell Floor Press", chestExercises[0].name)
    }

    @Test
    fun testWorkoutSessionAndRelationalLogs() = runBlocking {
        val session = WorkoutSession(
            sessionId = "session_2026_10_27",
            userId = "test_user_1",
            date = "2026-10-27",
            weekNumber = 1,
            dayOfWeek = "Tuesday",
            workoutTitle = "Push Strength & Hypertrophy",
            isCompleted = true,
            durationMinutes = 48,
            perceivedEffort = 8
        )
        workoutDao.insertSession(session)

        val log1 = WorkoutLog(
            logId = "log_1",
            sessionId = "session_2026_10_27",
            userId = "test_user_1",
            date = "2026-10-27",
            weekNumber = 1,
            exerciseId = "ex_floor_press",
            exerciseName = "Dumbbell Floor Press",
            set1Reps = 10,
            set1Weight = 20.0,
            set1Completed = true,
            maxWeight = 20.0,
            totalVolume = 200.0,
            isCompleted = true
        )

        val log2 = WorkoutLog(
            logId = "log_2",
            sessionId = "session_2026_10_27",
            userId = "test_user_1",
            date = "2026-10-27",
            weekNumber = 1,
            exerciseId = "ex_floor_press",
            exerciseName = "Dumbbell Floor Press",
            set1Reps = 8,
            set1Weight = 22.5,
            set1Completed = true,
            maxWeight = 22.5,
            totalVolume = 180.0,
            isCompleted = true
        )

        workoutDao.insertWorkoutLogs(listOf(log1, log2))

        // Verify relational query
        val sessionWithLogs = workoutDao.observeSessionWithLogs("session_2026_10_27").first()
        assertNotNull(sessionWithLogs)
        assertEquals("Push Strength & Hypertrophy", sessionWithLogs?.session?.workoutTitle)
        assertEquals(2, sessionWithLogs?.logs?.size)

        // Verify PR query
        val pr = workoutDao.observePersonalRecord("ex_floor_press").first()
        assertNotNull(pr)
        assertEquals(22.5, pr!!, 0.001)

        // Verify Weekly Volume calculation
        val weeklyVolume = workoutDao.observeWeeklyVolume(1).first()
        assertNotNull(weeklyVolume)
        assertEquals(380.0, weeklyVolume!!, 0.001)
    }

    @Test
    fun testProgressiveOverloadAcrossWeeks() = runBlocking {
        // Week 1 session
        workoutDao.insertSession(
            WorkoutSession(
                sessionId = "sess_w1",
                userId = "athlete",
                date = "2026-10-01",
                weekNumber = 1,
                workoutTitle = "Leg Day"
            )
        )
        workoutDao.insertWorkoutLog(
            WorkoutLog(
                logId = "w1_log",
                sessionId = "sess_w1",
                userId = "athlete",
                date = "2026-10-01",
                weekNumber = 1,
                exerciseId = "ex_squat",
                exerciseName = "Goblet Squats",
                maxWeight = 16.0,
                totalVolume = 160.0
            )
        )

        // Week 2 session (overload: higher weight)
        workoutDao.insertSession(
            WorkoutSession(
                sessionId = "sess_w2",
                userId = "athlete",
                date = "2026-10-08",
                weekNumber = 2,
                workoutTitle = "Leg Day"
            )
        )
        workoutDao.insertWorkoutLog(
            WorkoutLog(
                logId = "w2_log",
                sessionId = "sess_w2",
                userId = "athlete",
                date = "2026-10-08",
                weekNumber = 2,
                exerciseId = "ex_squat",
                exerciseName = "Goblet Squats",
                maxWeight = 20.0,
                totalVolume = 200.0
            )
        )

        // Overload PR should be 20.0
        val pr = workoutDao.observePersonalRecord("ex_squat").first()
        assertEquals(20.0, pr!!, 0.001)

        // Exercise logs list should contain both weeks in chronological order
        val logs = workoutDao.observeWorkoutLogsForExercise("ex_squat").first()
        assertEquals(2, logs.size)
        assertEquals(16.0, logs[0].maxWeight, 0.001)
        assertEquals(20.0, logs[1].maxWeight, 0.001)
        assertTrue(logs[1].maxWeight > logs[0].maxWeight)
    }
}
