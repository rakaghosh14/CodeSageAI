package com.example.codesageai.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryNeonBlue,
    secondary = SecondaryCyan,
    tertiary = AccentPurple,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderSlate,
    error = ErrorRed
)

// Fallback light theme for robustness
private val LightColorScheme = lightColorScheme(
    primary = SecondaryCyan,
    secondary = PrimaryNeonBlue,
    tertiary = AccentPurple,
    background = TextPrimary,
    surface = TextPrimary,
    onBackground = DarkBackground,
    onSurface = DarkBackground
)

@Composable
fun CodeSageAITheme(
    darkTheme: Boolean = true, // Force Dark theme for premium neon visual identity by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
