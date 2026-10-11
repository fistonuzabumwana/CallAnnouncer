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
import com.example.data.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Indigo300,
    onPrimary = Indigo900,
    primaryContainer = Indigo800,
    onPrimaryContainer = Indigo100,
    secondary = Coal300,
    onSecondary = Black,
    secondaryContainer = Coal700,
    onSecondaryContainer = Coal100,
    tertiary = Indigo200,
    background = Black,
    onBackground = Coal100,
    surface = Black,
    onSurface = Coal100,
    surfaceVariant = Coal800,
    onSurfaceVariant = Coal400,
    surfaceContainerLowest = Black,
    surfaceContainerLow = Coal900,
    surfaceContainer = Coal850,
    surfaceContainerHigh = Coal800,
    surfaceContainerHighest = Coal700,
    outline = Coal600,
    outlineVariant = Coal700,
    inverseSurface = Coal100,
    inverseOnSurface = Black,
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
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

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
