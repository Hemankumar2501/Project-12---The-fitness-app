package com.example.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWizard(
    initialUserId: String = "athlete_user",
    onComplete: (UserProfile) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    // Form inputs
    var name by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("28") }
    var heightText by remember { mutableStateOf("175") }
    var weightText by remember { mutableStateOf("78") }
    var sex by remember { mutableStateOf("male") }

    var primaryGoal by remember { mutableStateOf("fat_loss") }
    var targetWeightText by remember { mutableStateOf("70") }
    var fitnessExperience by remember { mutableStateOf("intermediate") }

    var availableEquipment by remember { mutableStateOf("Dumbbells, Resistance Bands, Bodyweight") }
    var workoutTimeAvailability by remember { mutableStateOf("45 minutes") }
    var startDate by remember { mutableStateOf(LocalDate.now().toString()) }

    var dietaryPreference by remember { mutableStateOf("all") }
    var allergies by remember { mutableStateOf("None") }
    var activityLevel by remember { mutableStateOf("moderately_active") }

    // Computed targets
    val age = ageText.toIntOrNull() ?: 28
    val height = heightText.toDoubleOrNull() ?: 175.0
    val weight = weightText.toDoubleOrNull() ?: 78.0
    val targetWeight = targetWeightText.toDoubleOrNull() ?: 70.0

    val bmr = if (sex == "female") {
        (10.0 * weight) + (6.25 * height) - (5.0 * age) - 161.0
    } else {
        (10.0 * weight) + (6.25 * height) - (5.0 * age) + 5.0
    }
    val tdee = bmr * 1.5
    val calorieTarget = when (primaryGoal) {
        "fat_loss" -> (tdee - 450).toInt().coerceAtLeast(1400)
        "muscle_gain" -> (tdee + 300).toInt()
        else -> (tdee - 150).toInt()
    }
    val proteinTarget = (weight * 2.0).toInt().coerceIn(90, 220)

    Scaffold(
        containerColor = ProjectBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Step $step of 4",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = NeonLime,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    if (step > 1) {
                        IconButton(onClick = { step -= 1 }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProjectBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 1..4) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (i <= step) NeonLime else ProjectBorder)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (step) {
                1 -> Step1PersonalStats(
                    name = name, onNameChange = { name = it },
                    age = ageText, onAgeChange = { ageText = it },
                    height = heightText, onHeightChange = { heightText = it },
                    weight = weightText, onWeightChange = { weightText = it },
                    sex = sex, onSexChange = { sex = it }
                )
                2 -> Step2Goals(
                    primaryGoal = primaryGoal, onGoalChange = { primaryGoal = it },
                    targetWeight = targetWeightText, onTargetWeightChange = { targetWeightText = it },
                    experience = fitnessExperience, onExperienceChange = { fitnessExperience = it }
                )
                3 -> Step3Training(
                    equipment = availableEquipment, onEquipmentChange = { availableEquipment = it },
                    time = workoutTimeAvailability, onTimeChange = { workoutTimeAvailability = it },
                    startDate = startDate, onStartDateChange = { startDate = it }
                )
                4 -> Step4Nutrition(
                    diet = dietaryPreference, onDietChange = { dietaryPreference = it },
                    allergies = allergies, onAllergiesChange = { allergies = it },
                    calorieTarget = calorieTarget,
                    proteinTarget = proteinTarget,
                    waterTarget = 3000
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Action Button
            Button(
                onClick = {
                    if (step < 4) {
                        step += 1
                    } else {
                        val profile = UserProfile(
                            userId = initialUserId,
                            name = name.ifBlank { "Athlete" },
                            age = age,
                            heightCm = height,
                            currentWeightKg = weight,
                            targetWeightKg = targetWeight,
                            sex = sex,
                            primaryGoal = primaryGoal,
                            fitnessExperience = fitnessExperience,
                            availableEquipment = availableEquipment,
                            workoutTimeAvailability = workoutTimeAvailability,
                            dietaryPreference = dietaryPreference,
                            allergies = allergies,
                            activityLevel = activityLevel,
                            timezone = "Asia/Kolkata",
                            startDate = startDate,
                            dailyCaloriesTarget = calorieTarget,
                            dailyProteinTarget = proteinTarget,
                            dailyWaterTarget = 3000,
                            completedOnboarding = true
                        )
                        onComplete(profile)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("onboarding_continue_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonLime,
                    contentColor = Color(0xFF051203)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (step < 4) "Continue" else "Start 12-Week Transformation",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (step < 4) Icons.Default.ArrowForward else Icons.Default.Check,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Step1PersonalStats(
    name: String, onNameChange: (String) -> Unit,
    age: String, onAgeChange: (String) -> Unit,
    height: String, onHeightChange: (String) -> Unit,
    weight: String, onWeightChange: (String) -> Unit,
    sex: String, onSexChange: (String) -> Unit
) {
    Text(
        text = "Your Baseline Profile",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
    )
    Text(
        text = "Enter your current metrics so we can accurately calibrate your 12-week calorie and macro targets.",
        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Spacer(modifier = Modifier.height(20.dp))

    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Your Name") },
        placeholder = { Text("e.g. Alex Kumar") },
        modifier = Modifier.fillMaxWidth().testTag("input_name"),
        colors = inputColors()
    )

    Spacer(modifier = Modifier.height(14.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = age,
            onValueChange = onAgeChange,
            label = { Text("Age (years)") },
            modifier = Modifier.weight(1f).testTag("input_age"),
            colors = inputColors()
        )
        OutlinedTextField(
            value = height,
            onValueChange = onHeightChange,
            label = { Text("Height (cm)") },
            modifier = Modifier.weight(1f).testTag("input_height"),
            colors = inputColors()
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    OutlinedTextField(
        value = weight,
        onValueChange = onWeightChange,
        label = { Text("Current Weight (kg)") },
        modifier = Modifier.fillMaxWidth().testTag("input_weight"),
        colors = inputColors()
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "Biological Sex", style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary))
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SelectableChip(label = "Male", selected = sex == "male", onClick = { onSexChange("male") }, modifier = Modifier.weight(1f))
        SelectableChip(label = "Female", selected = sex == "female", onClick = { onSexChange("female") }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun Step2Goals(
    primaryGoal: String, onGoalChange: (String) -> Unit,
    targetWeight: String, onTargetWeightChange: (String) -> Unit,
    experience: String, onExperienceChange: (String) -> Unit
) {
    Text(
        text = "Your Transformation Goal",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
    )
    Text(
        text = "Choose your primary focus for the next 12 weeks.",
        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GoalCard(
            title = "Fat Loss & Conditioning",
            description = "Shed body fat while preserving lean muscle with a moderate deficit and high protein.",
            selected = primaryGoal == "fat_loss",
            onClick = { onGoalChange("fat_loss") }
        )
        GoalCard(
            title = "Muscle Gain & Strength",
            description = "Build muscle with progressive overload strength training and caloric surplus.",
            selected = primaryGoal == "muscle_gain",
            onClick = { onGoalChange("muscle_gain") }
        )
        GoalCard(
            title = "Body Recomposition",
            description = "Simultaneously lose fat and build strength at maintenance calories.",
            selected = primaryGoal == "body_recomposition",
            onClick = { onGoalChange("body_recomposition") }
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    OutlinedTextField(
        value = targetWeight,
        onValueChange = onTargetWeightChange,
        label = { Text("Target Weight (kg)") },
        modifier = Modifier.fillMaxWidth(),
        colors = inputColors()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(text = "Fitness Experience", style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary))
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SelectableChip(label = "Beginner", selected = experience == "beginner", onClick = { onExperienceChange("beginner") }, modifier = Modifier.weight(1f))
        SelectableChip(label = "Intermediate", selected = experience == "intermediate", onClick = { onExperienceChange("intermediate") }, modifier = Modifier.weight(1f))
        SelectableChip(label = "Advanced", selected = experience == "advanced", onClick = { onExperienceChange("advanced") }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun Step3Training(
    equipment: String, onEquipmentChange: (String) -> Unit,
    time: String, onTimeChange: (String) -> Unit,
    startDate: String, onStartDateChange: (String) -> Unit
) {
    Text(
        text = "Training Setup",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
    )
    Text(
        text = "Project 12 is engineered for dumbbells, bands, and bodyweight exercises at home.",
        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = equipment,
        onValueChange = onEquipmentChange,
        label = { Text("Available Equipment") },
        modifier = Modifier.fillMaxWidth(),
        colors = inputColors()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(text = "Workout Time Availability", style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary))
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SelectableChip(label = "30 mins", selected = time == "30 minutes", onClick = { onTimeChange("30 minutes") }, modifier = Modifier.weight(1f))
        SelectableChip(label = "45 mins", selected = time == "45 minutes", onClick = { onTimeChange("45 minutes") }, modifier = Modifier.weight(1f))
        SelectableChip(label = "60 mins", selected = time == "60 minutes", onClick = { onTimeChange("60 minutes") }, modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(20.dp))

    OutlinedTextField(
        value = startDate,
        onValueChange = onStartDateChange,
        label = { Text("Program Start Date (YYYY-MM-DD)") },
        modifier = Modifier.fillMaxWidth(),
        colors = inputColors()
    )
}

@Composable
private fun Step4Nutrition(
    diet: String, onDietChange: (String) -> Unit,
    allergies: String, onAllergiesChange: (String) -> Unit,
    calorieTarget: Int,
    proteinTarget: Int,
    waterTarget: Int
) {
    Text(
        text = "Nutrition & Target Calories",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
    )
    Text(
        text = "We built an extensive everyday Indian & South Indian food library into Project 12.",
        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(text = "Dietary Preference", style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary))
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SelectableChip(label = "Non-Veg", selected = diet == "all", onClick = { onDietChange("all") }, modifier = Modifier.weight(1f))
        SelectableChip(label = "Eggetarian", selected = diet == "eggetarian", onClick = { onDietChange("eggetarian") }, modifier = Modifier.weight(1f))
        SelectableChip(label = "Vegetarian", selected = diet == "vegetarian", onClick = { onDietChange("vegetarian") }, modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = allergies,
        onValueChange = onAllergiesChange,
        label = { Text("Food Allergies / Dislikes") },
        modifier = Modifier.fillMaxWidth(),
        colors = inputColors()
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Caloric Targets Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonLime.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "YOUR INITIAL ESTIMATED TARGETS",
                style = MaterialTheme.typography.labelSmall.copy(color = NeonLime, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TargetMetric(value = "$calorieTarget", unit = "kcal/day", label = "Calories")
                TargetMetric(value = "${proteinTarget}g", unit = "per day", label = "Protein")
                TargetMetric(value = "${waterTarget / 1000}L", unit = "daily", label = "Water")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Note: These are scientific starting targets based on your metrics, which you can adjust at any time in Profile Settings.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
            )
        }
    }
}

@Composable
private fun TargetMetric(value: String, unit: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Black))
        Text(text = unit, style = MaterialTheme.typography.bodySmall.copy(color = NeonLime, fontSize = 11.sp))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
    }
}

@Composable
private fun GoalCard(title: String, description: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) NeonLimeContainer else ProjectSurfaceElevated
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) NeonLime else ProjectBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (selected) NeonLime else TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(NeonLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SelectableChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) NeonLime else ProjectSurfaceElevated)
            .border(1.dp, if (selected) NeonLime else ProjectBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.Black else TextPrimary
            )
        )
    }
}

@Composable
private fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = NeonLime,
    unfocusedBorderColor = ProjectBorder,
    focusedLabelColor = NeonLime,
    unfocusedLabelColor = TextSecondary,
    focusedContainerColor = ProjectSurface,
    unfocusedContainerColor = ProjectSurface
)
