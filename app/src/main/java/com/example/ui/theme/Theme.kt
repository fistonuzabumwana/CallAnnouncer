package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Indigo300,
    onPrimary = Indigo900,
    primaryContainer = Indigo800,
    onPrimaryContainer = Indigo100,
    secondary = Ink300,
    onSecondary = Ink900,
    secondaryContainer = Ink700,
    onSecondaryContainer = Ink100,
    tertiary = Indigo200,
    background = Ink950,
    onBackground = Ink100,
    surface = Ink950,
    onSurface = Ink100,
    surfaceVariant = Ink800,
    onSurfaceVariant = Ink400,
    surfaceContainerLowest = Ink950,
    surfaceContainerLow = Ink900,
    surfaceContainer = Ink850,
    surfaceContainerHigh = Ink800,
    surfaceContainerHighest = Ink700,
    outline = Ink600,
    outlineVariant = Ink700,
    error = AccentRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    primaryContainer = Indigo100,
    onPrimaryContainer = Indigo900,
    secondary = Ink600,
    onSecondary = Color.White,
    secondaryContainer = Ink100,
    onSecondaryContainer = Ink900,
    tertiary = Indigo500,
    background = Ink50,
    onBackground = Ink900,
    surface = Ink50,
    onSurface = Ink900,
    surfaceVariant = Ink100,
    onSurfaceVariant = Color(0xFF5B6174),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Paper,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Ink100,
    outline = Ink300,
    outlineVariant = Ink100,
    error = AccentRed,
    onError = Color.White
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * The brand palette is used on every phone (no wallpaper-based dynamic color),
 * so the app looks the same everywhere and status colors stay readable.
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
