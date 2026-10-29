package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Project12ColorScheme = darkColorScheme(
    primary = NeonLime,
    onPrimary = Color(0xFF051203),
    primaryContainer = NeonLimeContainer,
    onPrimaryContainer = NeonLime,
    secondary = AccentBlue,
    onSecondary = Color.White,
    secondaryContainer = ProjectSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentOrange,
    onTertiary = Color.Black,
    background = ProjectBackground,
    onBackground = TextPrimary,
    surface = ProjectSurface,
    onSurface = TextPrimary,
    surfaceVariant = ProjectSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = ProjectBorder,
    outlineVariant = Color(0xFF1B2836),
    error = AccentRed,
    onError = Color.White
)

@Composable
fun Project12Theme(
    darkTheme: Boolean = true, // Project 12 is purposefully deep-dark luxury athletic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = Project12ColorScheme,
        typography = Typography,
        content = content
    )
}
