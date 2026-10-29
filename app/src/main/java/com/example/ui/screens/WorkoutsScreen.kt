package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.model.ExerciseTemplate
import com.example.data.model.WorkoutTemplate
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    viewModel: AppViewModel
) {
    val templates by viewModel.workoutTemplates.collectAsState()
    val selectedTemplateId by viewModel.selectedTemplateId.collectAsState()
    val exercises by viewModel.templateExercises.collectAsState()
    val restSeconds by viewModel.restSecondsRemaining.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()

    var isInActiveWorkoutMode by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var perceivedEffort by remember { mutableFloatStateOf(7f) }
    var sessionNotes by remember { mutableStateOf("") }

    val activeTemplate = templates.find { it.templateId == selectedTemplateId }
        ?: templates.firstOrNull()

    // Exercise completion tracking states (exerciseId -> Boolean)
    val completedExercises = remember { mutableStateMapOf<String, Boolean>() }
    // Exercise sets tracking (exerciseId to map of setNumber -> Pair<reps, weight>)
    val recordedSets = remember { mutableStateMapOf<String, MutableMap<Int, Pair<Int, Double>>>() }

    Scaffold(
        containerColor = ProjectBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isInActiveWorkoutMode) "Active Workout" else "12-Week Training Plan",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                },
                actions = {
                    if (isInActiveWorkoutMode) {
                        Button(
                            onClick = { showFinishDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonLime,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(end = 8.dp).testTag("finish_workout_top_button")
                        ) {
                            Text("Finish", fontWeight = FontWeight.Bold)
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
        ) {
            // Day selector tabs
            if (!isInActiveWorkoutMode) {
                ScrollableTabRow(
                    selectedTabIndex = templates.indexOfFirst { it.templateId == selectedTemplateId }.coerceAtLeast(0),
                    containerColor = ProjectBackground,
                    contentColor = NeonLime,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        val index = templates.indexOfFirst { it.templateId == selectedTemplateId }.coerceAtLeast(0)
                        if (index in tabPositions.indices) {
                            Box(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[index])
                                    .height(3.dp)
                                    .background(NeonLime, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            )
                        }
                    }
                ) {
                    templates.forEach { template ->
                        val selected = template.templateId == selectedTemplateId
                        Tab(
                            selected = selected,
                            onClick = { viewModel.selectWorkoutTemplate(template.templateId) },
                            text = {
                                Text(
                                    text = template.dayOfWeek.take(3).uppercase(),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) NeonLime else TextSecondary
                                )
                            }
                        )
                    }
                }
            }

            // Workout Overview Header Card
            activeTemplate?.let { template ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = template.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = NeonLime,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${template.estimatedMinutes} mins",
                                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = template.focus,
                            style = MaterialTheme.typography.bodyMedium.copy(color = NeonLime)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Targets: ${template.targetMuscles}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )

                        if (!isInActiveWorkoutMode && template.dayOfWeek != "Sunday") {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { isInActiveWorkoutMode = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_workout_session_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonLime,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Interactive Session", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Rest Timer Floating Banner (when in active mode or running)
            if (isInActiveWorkoutMode || restSeconds > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = NeonLimeContainer),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonLime.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = NeonLime,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (restSeconds > 0) "Rest: ${restSeconds}s remaining" else "Rest Timer",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (restSeconds > 0) NeonLime else TextPrimary
                                    )
                                )
                                Text(
                                    text = if (isTimerRunning) "Timer is ticking down..." else "Select recovery interval",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )
                            }
                        }

                        // Presets
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(45, 60, 90).forEach { sec ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (restSeconds == sec) NeonLime else ProjectSurface)
                                        .border(1.dp, NeonLime, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.startRestTimer(sec) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${sec}s",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (restSeconds == sec) Color.Black else TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            if (restSeconds > 0) {
                                IconButton(
                                    onClick = { viewModel.resetRestTimer() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Reset", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Exercise List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(exercises) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        isInActiveMode = isInActiveWorkoutMode,
                        isMarkedComplete = completedExercises[exercise.exerciseId] == true,
                        onToggleComplete = { complete ->
                            completedExercises[exercise.exerciseId] = complete
                            if (complete) {
                                viewModel.startRestTimer(exercise.restSeconds)
                            }
                        },
                        onSaveSet = { setNum, reps, weight ->
                            val sets = recordedSets.getOrPut(exercise.exerciseId) { mutableMapOf() }
                            sets[setNum] = Pair(reps, weight)
                            viewModel.saveExerciseSet(
                                exerciseId = exercise.exerciseId,
                                exerciseName = exercise.name,
                                setNumber = setNum,
                                reps = reps,
                                weightKg = weight,
                                isCompleted = true
                            )
                        }
                    )
                }
            }
        }
    }

    // Finish Workout Dialog
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            containerColor = ProjectSurfaceElevated,
            title = {
                Text(
                    text = "Finish Session & Log Progress",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Great job completing your session! Rate your perceived effort (1-10):",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Effort Rating: ${perceivedEffort.toInt()} / 10",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = NeonLime,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Slider(
                        value = perceivedEffort,
                        onValueChange = { perceivedEffort = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonLime,
                            activeTrackColor = NeonLime,
                            inactiveTrackColor = ProjectBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = sessionNotes,
                        onValueChange = { sessionNotes = it },
                        label = { Text("Session Notes (optional)") },
                        placeholder = { Text("Felt strong on floor press...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = ProjectBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.finishWorkout(
                            templateTitle = activeTemplate?.title ?: "Workout",
                            durationMinutes = activeTemplate?.estimatedMinutes ?: 45,
                            perceivedEffort = perceivedEffort.toInt(),
                            notes = sessionNotes
                        )
                        showFinishDialog = false
                        isInActiveWorkoutMode = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black)
                ) {
                    Text("Save Workout Log", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ExerciseCard(
    exercise: ExerciseTemplate,
    isInActiveMode: Boolean,
    isMarkedComplete: Boolean,
    onToggleComplete: (Boolean) -> Unit,
    onSaveSet: (Int, Int, Double) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isMarkedComplete) NeonLimeContainer.copy(alpha = 0.5f) else ProjectSurfaceElevated
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isMarkedComplete) NeonLime else ProjectBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isMarkedComplete) NeonLime else TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${exercise.targetSets} sets × ${exercise.targetReps} • Rest ${exercise.restSeconds}s",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                if (isInActiveMode) {
                    IconButton(
                        onClick = { onToggleComplete(!isMarkedComplete) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isMarkedComplete) NeonLime else ProjectSurface)
                            .border(1.dp, if (isMarkedComplete) NeonLime else ProjectBorder, CircleShape)
                            .testTag("mark_exercise_complete_${exercise.exerciseId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Complete Exercise",
                            tint = if (isMarkedComplete) Color.Black else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Set inputs when in active mode
            if (isInActiveMode) {
                Spacer(modifier = Modifier.height(12.dp))
                for (s in 1..exercise.targetSets) {
                    SetInputRow(setNumber = s, onSave = { reps, weight -> onSaveSet(s, reps, weight) })
                    if (s < exercise.targetSets) Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Expandable form cues
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Hide Form Cues & Alternative" else "Technique Tips & Substitutions",
                    style = MaterialTheme.typography.labelSmall.copy(color = NeonLime)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = NeonLime,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Instructions: ${exercise.instructions}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                )
                if (exercise.substituteAlternative.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Substitution: ${exercise.substituteAlternative}",
                        style = MaterialTheme.typography.bodySmall.copy(color = AccentOrange, fontSize = 12.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SetInputRow(
    setNumber: Int,
    onSave: (Int, Double) -> Unit
) {
    var repsText by remember { mutableStateOf("10") }
    var weightText by remember { mutableStateOf("12.5") }
    var isSetLogged by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ProjectSurface)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Set $setNumber",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSetLogged) NeonLime else TextPrimary
            ),
            modifier = Modifier.width(50.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("kg", fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.width(68.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonLime,
                    unfocusedBorderColor = ProjectBorder
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = repsText,
                onValueChange = { repsText = it },
                label = { Text("reps", fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.width(68.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonLime,
                    unfocusedBorderColor = ProjectBorder
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = {
                    isSetLogged = !isSetLogged
                    val reps = repsText.toIntOrNull() ?: 10
                    val weight = weightText.toDoubleOrNull() ?: 0.0
                    onSave(reps, weight)
                },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isSetLogged) NeonLime else ProjectBorder)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Log Set",
                    tint = if (isSetLogged) Color.Black else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
