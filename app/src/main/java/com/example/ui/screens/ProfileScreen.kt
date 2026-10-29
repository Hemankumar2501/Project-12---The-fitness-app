package com.example.ui.screens

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AppViewModel,
    onSignOut: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showEditTargetsDialog by remember { mutableStateOf(false) }
    var editCaloriesText by remember { mutableStateOf(profile?.dailyCaloriesTarget?.toString() ?: "2100") }
    var editProteinText by remember { mutableStateOf(profile?.dailyProteinTarget?.toString() ?: "150") }
    var editWaterText by remember { mutableStateOf(profile?.dailyWaterTarget?.toString() ?: "3000") }

    Scaffold(
        containerColor = ProjectBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile & Settings",
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // User Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(NeonLimeContainer)
                    .border(2.dp, NeonLime, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile?.name?.firstOrNull()?.toString()?.uppercase() ?: "P",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = NeonLime
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = profile?.name ?: "Athlete",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Text(
                text = "${profile?.primaryGoal?.replace("_", " ")?.uppercase()} • WEEK 12 PROGRAM",
                style = MaterialTheme.typography.labelSmall.copy(color = NeonLime, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Current Targets Card
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
                        Text(
                            text = "Configured Daily Targets",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        IconButton(onClick = {
                            editCaloriesText = profile?.dailyCaloriesTarget?.toString() ?: "2100"
                            editProteinText = profile?.dailyProteinTarget?.toString() ?: "150"
                            editWaterText = profile?.dailyWaterTarget?.toString() ?: "3000"
                            showEditTargetsDialog = true
                        }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Targets", tint = NeonLime, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ProfileMetricRow(label = "Daily Calories", value = "${profile?.dailyCaloriesTarget ?: 2100} kcal")
                    ProfileMetricRow(label = "Daily Protein", value = "${profile?.dailyProteinTarget ?: 150} g")
                    ProfileMetricRow(label = "Daily Water Goal", value = "${(profile?.dailyWaterTarget ?: 3000) / 1000} L (${profile?.dailyWaterTarget ?: 3000} ml)")
                    ProfileMetricRow(label = "Program Start Date", value = profile?.startDate ?: "2026-10-27")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preferences Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Equipment & Preferences",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ProfileMetricRow(label = "Equipment", value = profile?.availableEquipment ?: "Dumbbells & Bands")
                    ProfileMetricRow(label = "Diet Style", value = profile?.dietaryPreference?.replaceFirstChar { it.uppercase() } ?: "All")
                    ProfileMetricRow(label = "Experience", value = profile?.fitnessExperience?.replaceFirstChar { it.uppercase() } ?: "Intermediate")
                    ProfileMetricRow(label = "Target Weight", value = "${profile?.targetWeightKg ?: 70.0} kg")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Out Button
            OutlinedButton(
                onClick = {
                    Firebase.auth.signOut()
                    scope.launch {
                        try {
                            val credentialManager = CredentialManager.create(context)
                            credentialManager.clearCredentialState(ClearCredentialStateRequest())
                        } catch (_: Exception) {}
                        onSignOut()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("sign_out_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = AccentRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showEditTargetsDialog) {
        AlertDialog(
            onDismissRequest = { showEditTargetsDialog = false },
            containerColor = ProjectSurfaceElevated,
            title = { Text("Edit Daily Targets", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editCaloriesText,
                        onValueChange = { editCaloriesText = it },
                        label = { Text("Calories (kcal)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = editProteinText,
                        onValueChange = { editProteinText = it },
                        label = { Text("Protein (grams)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = editWaterText,
                        onValueChange = { editWaterText = it },
                        label = { Text("Water (ml)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        profile?.let { p ->
                            val updated = p.copy(
                                dailyCaloriesTarget = editCaloriesText.toIntOrNull() ?: p.dailyCaloriesTarget,
                                dailyProteinTarget = editProteinText.toIntOrNull() ?: p.dailyProteinTarget,
                                dailyWaterTarget = editWaterText.toIntOrNull() ?: p.dailyWaterTarget
                            )
                            viewModel.updateProfile(updated)
                        }
                        showEditTargetsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTargetsDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun ProfileMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold))
    }
}
