package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val todayMeals by viewModel.todayMeals.collectAsState()
    val todaySession by viewModel.todaySession.collectAsState()
    val todayCardio by viewModel.todayCardio.collectAsState()
    val todayHabit by viewModel.todayHabit.collectAsState()
    val latestMeasurement by viewModel.latestMeasurement.collectAsState()
    val allPhotos by viewModel.allPhotos.collectAsState()

    val currentWeek = remember(profile?.startDate) {
        viewModel.repository.calculateCurrentWeek(profile?.startDate ?: LocalDate.now().toString())
    }

    // Macro aggregates
    val consumedCalories = todayMeals.sumOf { it.calories }.toInt()
    val targetCalories = profile?.dailyCaloriesTarget ?: 2100
    val consumedProtein = todayMeals.sumOf { it.protein }.toInt()
    val targetProtein = profile?.dailyProteinTarget ?: 150

    val totalCardioMinutes = todayCardio.sumOf { it.durationMinutes }
    val targetCardioMinutes = 30

    val waterMl = todayHabit?.waterMl ?: 0
    val targetWaterMl = profile?.dailyWaterTarget ?: 3000

    val photoStreak = allPhotos.map { it.date }.distinct().size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProjectBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Greeting & Week Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back, ${profile?.name?.split(" ")?.firstOrNull() ?: "Athlete"}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            // Week Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonLimeContainer)
                    .border(1.dp, NeonLime, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "WEEK $currentWeek OF 12",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = NeonLime
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 12-Week Transformation Progress Bar
        val progressRatio = (currentWeek.toFloat() / 12f).coerceIn(0f, 1f)
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "12-Week Transformation", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                Text(text = "${(progressRatio * 100).toInt()}% Elapsed", style = MaterialTheme.typography.labelSmall.copy(color = NeonLime))
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NeonLime,
                trackColor = ProjectBorder
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -------------------------------------------------------------
        // TODAY'S WORKOUT CARD
        // -------------------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate(AppScreen.WORKOUTS) }
                .testTag("today_workout_card"),
            colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonLimeContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = NeonLime)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Today's Workout",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = todaySession?.workoutTitle ?: "Scheduled Strength Session",
                                style = MaterialTheme.typography.bodySmall.copy(color = NeonLime)
                            )
                        }
                    }

                    if (todaySession?.isCompleted == true) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonLime)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "COMPLETED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val completionText = if (todaySession?.isCompleted == true) {
                    "Full workout logged and recorded"
                } else {
                    "5 targeted compound exercises planned"
                }

                Text(
                    text = completionText,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onNavigate(AppScreen.WORKOUTS) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("workout_cta_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (todaySession?.isCompleted == true) ProjectSurface else NeonLime,
                        contentColor = if (todaySession?.isCompleted == true) NeonLime else Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (todaySession?.isCompleted == true) androidx.compose.foundation.BorderStroke(1.dp, NeonLime) else null
                ) {
                    Text(
                        text = if (todaySession?.isCompleted == true) "Review Workout Logs" else "Start Today's Workout",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -------------------------------------------------------------
        // NUTRITION & CARDIO METRICS ROW
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Nutrition Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(AppScreen.NUTRITION) },
                colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Nutrition", style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary))
                        Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$consumedCalories",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                    )
                    Text(text = "of $targetCalories kcal", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (consumedCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = AccentOrange,
                        trackColor = ProjectBorder
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Protein: $consumedProtein / ${targetProtein}g",
                        style = MaterialTheme.typography.labelSmall.copy(color = NeonLime, fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Cardio Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(AppScreen.PROGRESS) },
                colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Cardio", style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary))
                        Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$totalCardioMinutes",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                    )
                    Text(text = "of $targetCardioMinutes mins goal", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (totalCardioMinutes.toFloat() / targetCardioMinutes.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = AccentBlue,
                        trackColor = ProjectBorder
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (totalCardioMinutes >= targetCardioMinutes) "Daily Goal Reached!" else "${targetCardioMinutes - totalCardioMinutes} mins remaining",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // -------------------------------------------------------------
        // WATER INTAKE & WEIGHT ROW
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Water Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Water Intake", style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary))
                        Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${waterMl}ml",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(text = "of ${targetWaterMl}ml", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Spacer(modifier = Modifier.height(10.dp))
                    FilledTonalButton(
                        onClick = { viewModel.addWaterServing(250) },
                        modifier = Modifier.fillMaxWidth().height(36.dp).testTag("quick_add_water_button"),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = AccentBlue.copy(alpha = 0.2f), contentColor = AccentBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "+250 ml", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Weight & Photo Streak
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(AppScreen.PROGRESS) },
                colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Transformation", style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary))
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = NeonLime, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val weightDisplay = latestMeasurement?.weightKg ?: profile?.currentWeightKg ?: 0.0
                    Text(
                        text = "$weightDisplay kg",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    val startWeight = profile?.currentWeightKg ?: weightDisplay
                    val delta = weightDisplay - startWeight
                    Text(
                        text = if (delta <= 0) "${"%.1f".format(delta)} kg since start" else "+${"%.1f".format(delta)} kg since start",
                        style = MaterialTheme.typography.labelSmall.copy(color = if (delta <= 0) NeonLime else AccentOrange)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = NeonLime, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$photoStreak Photo Days", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -------------------------------------------------------------
        // QUICK COACH CONSULTATION TEASER
        // -------------------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate(AppScreen.COACH) },
            colors = CardDefaults.cardColors(containerColor = NeonLimeContainer),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonLime.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NeonLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Personal Transformation Coach",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = "Ask for meal ideas, exercise swaps, or weekly review",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = NeonLime)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
