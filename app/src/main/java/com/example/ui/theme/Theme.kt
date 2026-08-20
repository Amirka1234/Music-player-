package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ImmersiveLavenderAccent,
    onPrimary = ImmersivePurpleDeep,
    primaryContainer = ImmersiveLavenderLight,
    onPrimaryContainer = ImmersivePurpleDark,
    secondary = ImmersiveLavenderLight,
    onSecondary = ImmersivePurpleDark,
    secondaryContainer = ImmersivePillInactive,
    onSecondaryContainer = ImmersiveLavenderLight,
    tertiary = AccentCyan,
    background = ImmersiveDarkBg,
    onBackground = ImmersiveTextPrimary,
    surface = ImmersiveDarkBg,
    onSurface = ImmersiveTextPrimary,
    surfaceVariant = ImmersiveSurfaceDark,
    onSurfaceVariant = ImmersiveTextSecondary,
    outline = ImmersiveCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ImmersivePurpleDeep,
    onPrimary = Color.White,
    primaryContainer = ImmersiveLavenderAccent,
    onPrimaryContainer = ImmersivePurpleDeep,
    secondary = ImmersivePillInactive,
    onSecondary = Color.White,
    secondaryContainer = ImmersiveLavenderLight,
    onSecondaryContainer = ImmersivePurpleDark,
    tertiary = AccentPurple,
    background = ImmersiveLightBg,
    onBackground = ImmersiveTextPrimaryLight,
    surface = ImmersiveSurfaceLight,
    onSurface = ImmersiveTextPrimaryLight,
    surfaceVariant = ImmersiveCardLight,
    onSurfaceVariant = ImmersiveTextSecondaryLight,
    outline = ImmersiveCardBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to true for Spotify dark aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
