package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BodyMeasurementEntry
import com.example.data.model.Exercise
import com.example.data.model.PhotoEntry
import com.example.data.model.WorkoutLog
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: AppViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Photo Journal", "Measurements", "12-Week Analytics")

    Scaffold(
        containerColor = ProjectBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transformation Progress",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
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
            // Segmented Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ProjectBackground,
                contentColor = NeonLime,
                indicator = { tabPositions ->
                    if (selectedTab in tabPositions.indices) {
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .height(3.dp)
                                .background(NeonLime, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        )
                    }
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) NeonLime else TextSecondary
                                )
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> PhotosJournalTab(viewModel = viewModel)
                1 -> MeasurementsTab(viewModel = viewModel)
                2 -> AnalyticsTab(viewModel = viewModel)
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 1. PHOTOS & COMPARISON TAB
// ----------------------------------------------------------------------------
@Composable
private fun PhotosJournalTab(viewModel: AppViewModel) {
    val photos by viewModel.allPhotos.collectAsState()
    var selectedPose by remember { mutableStateOf("front") }
    var photoNotes by remember { mutableStateOf("") }
    var comparisonPhotoA by remember { mutableStateOf<PhotoEntry?>(null) }
    var comparisonPhotoB by remember { mutableStateOf<PhotoEntry?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.savePhoto(it, selectedPose, photoNotes)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Upload Action Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Daily Progress Photo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Keep lighting, distance, and pose consistent. Stored privately inside your device sandbox.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("front", "side", "back").forEach { pose ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedPose == pose) NeonLime else ProjectSurface)
                                    .border(1.dp, if (selectedPose == pose) NeonLime else ProjectBorder, RoundedCornerShape(10.dp))
                                    .clickable { selectedPose = pose },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pose.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (selectedPose == pose) Color.Black else TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("upload_progress_photo_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Today's Photo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Side-by-Side Comparison Section
        if (photos.size >= 2) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ProjectSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Side-by-Side Transformation Comparison",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val photoA = comparisonPhotoA ?: photos.lastOrNull()
                        val photoB = comparisonPhotoB ?: photos.firstOrNull()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Photo A
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Start (${photoA?.date ?: "Day 1"})",
                                    style = MaterialTheme.typography.labelSmall.copy(color = NeonLime, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.75f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ProjectSurfaceElevated)
                                ) {
                                    if (photoA != null && File(photoA.filePath).exists()) {
                                        AsyncImage(
                                            model = File(photoA.filePath),
                                            contentDescription = "Before Photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = TextMuted, modifier = Modifier.align(Alignment.Center))
                                    }
                                }
                            }

                            // Photo B
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Current (${photoB?.date ?: "Today"})",
                                    style = MaterialTheme.typography.labelSmall.copy(color = NeonLime, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.75f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ProjectSurfaceElevated)
                                ) {
                                    if (photoB != null && File(photoB.filePath).exists()) {
                                        AsyncImage(
                                            model = File(photoB.filePath),
                                            contentDescription = "After Photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = TextMuted, modifier = Modifier.align(Alignment.Center))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Timeline of saved photos
        item {
            Text(
                text = "Saved Photo Journal (${photos.size} entries)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
        }

        if (photos.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No photos uploaded yet.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Text("Log your Day 1 baseline photo to start your streak!", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            items(photos) { photo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ProjectSurface)
                        ) {
                            if (File(photo.filePath).exists()) {
                                AsyncImage(
                                    model = File(photo.filePath),
                                    contentDescription = "Progress Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = photo.date,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                            Text(
                                text = "Week ${photo.weekNumber} • ${photo.poseType.uppercase()} POSE",
                                style = MaterialTheme.typography.bodySmall.copy(color = NeonLime, fontWeight = FontWeight.SemiBold)
                            )
                            if (photo.notes.isNotBlank()) {
                                Text(
                                    text = photo.notes,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.deletePhoto(photo.photoId) }) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = AccentRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 2. BODY MEASUREMENTS TAB
// ----------------------------------------------------------------------------
@Composable
private fun MeasurementsTab(viewModel: AppViewModel) {
    val measurements by viewModel.allMeasurements.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var weightText by remember { mutableStateOf("") }
    var waistText by remember { mutableStateOf("") }
    var chestText by remember { mutableStateOf("") }
    var hipsText by remember { mutableStateOf("") }
    var armText by remember { mutableStateOf("") }
    var thighText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    val startWeight = profile?.currentWeightKg ?: 78.0
    val latestWeight = measurements.lastOrNull()?.weightKg ?: startWeight
    val weightDelta = latestWeight - startWeight

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Delta Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "CURRENT WEIGHT", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, letterSpacing = 1.sp))
                        Text(text = "$latestWeight kg", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = TextPrimary))
                        Text(text = "Start: $startWeight kg", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (weightDelta <= 0) NeonLimeContainer else AccentOrange.copy(alpha = 0.15f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (weightDelta <= 0) "${"%.1f".format(weightDelta)} kg" else "+${"%.1f".format(weightDelta)} kg",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (weightDelta <= 0) NeonLime else AccentOrange,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Weight Trend Line Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ProjectSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Weight Trend (kg)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                    Spacer(modifier = Modifier.height(12.dp))
                    SimpleLineChart(
                        dataPoints = if (measurements.isNotEmpty()) measurements.map { it.weightKg.toFloat() } else listOf(startWeight.toFloat(), startWeight.toFloat()),
                        lineColor = NeonLime
                    )
                }
            }
        }

        // Measurement Entry Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Record New Measurement", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                        OutlinedTextField(
                            value = waistText,
                            onValueChange = { waistText = it },
                            label = { Text("Waist (cm)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = chestText,
                            onValueChange = { chestText = it },
                            label = { Text("Chest (cm)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                        OutlinedTextField(
                            value = hipsText,
                            onValueChange = { hipsText = it },
                            label = { Text("Hips (cm)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = armText,
                            onValueChange = { armText = it },
                            label = { Text("Arm (cm)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                        OutlinedTextField(
                            value = thighText,
                            onValueChange = { thighText = it },
                            label = { Text("Thigh (cm)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val w = weightText.toDoubleOrNull() ?: latestWeight
                            val waist = waistText.toDoubleOrNull() ?: 0.0
                            val chest = chestText.toDoubleOrNull() ?: 0.0
                            val hips = hipsText.toDoubleOrNull() ?: 0.0
                            val arm = armText.toDoubleOrNull() ?: 0.0
                            val thigh = thighText.toDoubleOrNull() ?: 0.0

                            viewModel.recordMeasurement(w, waist, chest, hips, arm, thigh, notesText)
                            weightText = ""
                            waistText = ""
                            chestText = ""
                            hipsText = ""
                            armText = ""
                            thighText = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_measurements_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Measurement", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 3. 12-WEEK ANALYTICS & REVIEW TAB
// ----------------------------------------------------------------------------
@Composable
private fun AnalyticsTab(viewModel: AppViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val allCardio by viewModel.allCardio.collectAsState()
    val allPhotos by viewModel.allPhotos.collectAsState()
    val measurements by viewModel.allMeasurements.collectAsState()

    val currentWeek = remember(profile?.startDate) {
        viewModel.repository.calculateCurrentWeek(profile?.startDate ?: LocalDate.now().toString())
    }

    var reportGenerated by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "12-Week Transformation Milestones",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    MilestoneRow(week = 1, title = "Starting Point & Baseline", status = if (currentWeek >= 1) "Completed" else "Upcoming", isCompleted = currentWeek >= 1)
                    MilestoneRow(week = 2, title = "Building Routine & Consistency", status = if (currentWeek >= 2) "Active" else "Upcoming", isCompleted = currentWeek >= 2)
                    MilestoneRow(week = 4, title = "First Review & Measurement Audit", status = if (currentWeek >= 4) "Active" else "Upcoming", isCompleted = currentWeek >= 4)
                    MilestoneRow(week = 8, title = "Midpoint Progressive Overload", status = if (currentWeek >= 8) "Active" else "Upcoming", isCompleted = currentWeek >= 8)
                    MilestoneRow(week = 12, title = "Final Transformation Review", status = if (currentWeek >= 12) "Unlocked" else "Week 12 Goal", isCompleted = currentWeek >= 12)
                }
            }
        }

        item {
            ExerciseProgressOverloadCard(viewModel = viewModel, currentWeek = currentWeek)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ProjectSurface),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Summary Statistics", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem(label = "Total Cardio Mins", value = "${allCardio.sumOf { it.durationMinutes }}")
                        StatItem(label = "Photo Entries", value = "${allPhotos.size}")
                        StatItem(label = "Weight Logs", value = "${measurements.size}")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { reportGenerated = true },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLimeContainer, contentColor = NeonLime),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonLime),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Summarize, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate 12-Week Progress Report", fontWeight = FontWeight.Bold)
                    }

                    if (reportGenerated) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "PROJECT 12 TRANSFORMATION REPORT\n" +
                                        "Athlete: ${profile?.name ?: "User"}\n" +
                                        "Start Date: ${profile?.startDate}\n" +
                                        "Current Week: $currentWeek of 12\n" +
                                        "Weight Delta: ${"%.1f".format((measurements.lastOrNull()?.weightKg ?: 78.0) - (profile?.currentWeightKg ?: 78.0))} kg\n" +
                                        "Cardio Sessions: ${allCardio.size} (${allCardio.sumOf { it.durationMinutes }} total minutes)\n" +
                                        "Progress Photos Logged: ${allPhotos.size} photos\n" +
                                        "Status: On track with 12-week progressive overload plan.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 11.sp),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneRow(week: Int, title: String, status: String, isCompleted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isCompleted) NeonLime else ProjectSurface)
                .border(1.dp, if (isCompleted) NeonLime else ProjectBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            } else {
                Text(text = "W$week", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
        Text(text = status, style = MaterialTheme.typography.labelSmall.copy(color = if (isCompleted) NeonLime else TextSecondary))
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Black))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp))
    }
}

@Composable
private fun SimpleLineChart(dataPoints: List<Float>, lineColor: Color) {
    val minVal = (dataPoints.minOrNull() ?: 0f) - 1f
    val maxVal = (dataPoints.maxOrNull() ?: 100f) + 1f
    val range = (maxVal - minVal).coerceAtLeast(1f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        if (dataPoints.size < 2) {
            drawLine(
                color = lineColor,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 3f
            )
            return@Canvas
        }

        val stepX = size.width / (dataPoints.size - 1)
        val path = Path()

        dataPoints.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedY = 1f - ((value - minVal) / range)
            val y = normalizedY * size.height

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Draw point
            drawCircle(
                color = lineColor,
                radius = 5f,
                center = Offset(x, y)
            )
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4f)
        )
    }
}

@Composable
private fun ExerciseProgressOverloadCard(
    viewModel: AppViewModel,
    currentWeek: Int
) {
    val keyExercises = listOf(
        "ex_push_1" to "Floor Press",
        "ex_leg_1" to "Goblet Squats",
        "ex_pull_1" to "One-Arm Row",
        "ex_push_3" to "Shoulder Press",
        "ex_leg_2" to "Romanian Deadlift"
    )
    var selectedExerciseId by remember { mutableStateOf("ex_push_1") }
    val selectedExerciseName = keyExercises.firstOrNull { it.first == selectedExerciseId }?.second ?: "Exercise"

    val pr by viewModel.observePersonalRecord(selectedExerciseId).collectAsState(initial = null)
    val exerciseLogs by viewModel.observeWorkoutLogsForExercise(selectedExerciseId).collectAsState(initial = emptyList())
    val weeklyVolume by viewModel.observeWeeklyVolume(currentWeek).collectAsState(initial = null)

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Column {
                    Text(
                        text = "12-Week Progressive Overload",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = "Weight & volume tracking by exercise",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonLimeContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "PR: ${pr?.let { "%.1f kg".format(it) } ?: "--"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = NeonLime)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Exercise Selection Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(keyExercises) { (id, name) ->
                    val isSelected = selectedExerciseId == id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NeonLime else ProjectSurface)
                            .border(1.dp, if (isSelected) NeonLime else ProjectBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedExerciseId = id }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else TextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (exerciseLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ProjectSurface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No $selectedExerciseName sets logged yet.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Text(
                            text = "Log your workout to track overload across 12 weeks!",
                            style = MaterialTheme.typography.labelSmall.copy(color = NeonLime, fontSize = 10.sp)
                        )
                    }
                }
            } else {
                val weightPoints = exerciseLogs.map { it.maxWeight.toFloat() }
                SimpleLineChart(dataPoints = weightPoints, lineColor = NeonLime)

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Sets: ${exerciseLogs.size}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = "Max Volume: ${"%.0f".format(exerciseLogs.maxOfOrNull { it.totalVolume } ?: 0.0)} kg",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
            }

            if (weeklyVolume != null && weeklyVolume!! > 0.0) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Week $currentWeek Total Volume: ${"%.0f".format(weeklyVolume)} kg lifted",
                    style = MaterialTheme.typography.labelSmall.copy(color = NeonLime, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

