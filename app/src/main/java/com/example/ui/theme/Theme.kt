package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AnimeColorScheme = darkColorScheme(
    primary = AnimeMagenta,
    secondary = AnimeCyan,
    tertiary = AnimeYellow,
    background = AnimeDarkBg,
    surface = AnimeSurface,
    surfaceVariant = AnimeSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = AnimeDarkBg,
    onTertiary = AnimeDarkBg,
    onBackground = AnimeTextPrimary,
    onSurface = AnimeTextPrimary,
    onSurfaceVariant = AnimeTextSecondary,
    outline = AnimeBorder,
    error = Color(0xFFFF5252)
)

private val LightAnimeColorScheme = lightColorScheme(
    primary = AnimeMagenta,
    secondary = AnimeCyan,
    tertiary = AnimeYellow,
    background = Color(0xFFFAF8FE),
    surface = Color(0xFFF0EBF9),
    surfaceVariant = Color(0xFFE6DCF5),
    onPrimary = Color.White,
    onSecondary = Color(0xFF160F2B),
    onTertiary = Color.Black,
    onBackground = Color(0xFF160F2B),
    onSurface = Color(0xFF160F2B),
    onSurfaceVariant = Color(0xFF5B4F73),
    outline = Color(0xFFD2C7E8),
    error = Color(0xFFD32F2F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default for premium neon look
    dynamicColor: Boolean = false, // Set false to ensure our beautiful anime styling is loaded
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        AnimeColorScheme
    } else {
        // We can fallback to the dark theme anyway since wallpaper apps are best viewed in dark space!
        AnimeColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
