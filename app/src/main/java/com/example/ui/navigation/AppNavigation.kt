package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.onboarding.OnboardingWizard
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AppViewModel

data class BottomNavItem(
    val screen: AppScreen,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun AppNavigation(
    viewModel: AppViewModel,
    onSignOut: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    val navItems = listOf(
        BottomNavItem(AppScreen.HOME, "Home", Icons.Default.Home, "nav_home"),
        BottomNavItem(AppScreen.WORKOUTS, "Workouts", Icons.Default.FitnessCenter, "nav_workouts"),
        BottomNavItem(AppScreen.NUTRITION, "Nutrition", Icons.Default.Restaurant, "nav_nutrition"),
        BottomNavItem(AppScreen.PROGRESS, "Progress", Icons.Default.AutoGraph, "nav_progress"),
        BottomNavItem(AppScreen.COACH, "Coach", Icons.Default.Psychology, "nav_coach")
    )

    // Handle back button for secondary screens
    if (currentScreen != AppScreen.HOME && currentScreen != AppScreen.ONBOARDING) {
        BackHandler {
            viewModel.navigateTo(AppScreen.HOME)
        }
    }

    if (currentScreen == AppScreen.ONBOARDING) {
        OnboardingWizard(
            initialUserId = profile?.userId ?: "user",
            onComplete = { newProfile ->
                viewModel.completeOnboarding(newProfile)
            }
        )
        return
    }

    Scaffold(
        containerColor = ProjectBackground,
        bottomBar = {
            NavigationBar(
                containerColor = ProjectSurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .border(
                        1.dp,
                        ProjectBorder,
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
            ) {
                navItems.forEach { item ->
                    val isSelected = currentScreen == item.screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(item.screen) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NeonLime,
                            indicatorColor = NeonLime,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(viewModel = viewModel, onNavigate = { viewModel.navigateTo(it) })
                AppScreen.WORKOUTS -> WorkoutsScreen(viewModel = viewModel)
                AppScreen.NUTRITION -> NutritionScreen(viewModel = viewModel)
                AppScreen.PROGRESS -> ProgressScreen(viewModel = viewModel)
                AppScreen.COACH -> CoachScreen(viewModel = viewModel)
                AppScreen.PROFILE_SETTINGS -> ProfileScreen(viewModel = viewModel, onSignOut = onSignOut)
                else -> HomeScreen(viewModel = viewModel, onNavigate = { viewModel.navigateTo(it) })
            }
        }
    }
}
