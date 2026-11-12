package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = DarkBg,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = EmeraldLight,
    secondary = SoftBlueSecondary,
    onSecondary = DarkBg,
    secondaryContainer = SoftBlueDark,
    onSecondaryContainer = SoftBlueLight,
    tertiary = EmeraldLight,
    background = DarkBg,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkCardSurface,
    onSurfaceVariant = TextMuted,
    outline = DarkBorder,
    outlineVariant = DarkElevatedSurface,
    error = DangerCoral,
    onError = TextWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to stunning sleek dark mode
    dynamicColor: Boolean = false, // Keep intentional emerald & soft blue palette
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = DarkBg.toArgb()
                it.navigationBarColor = DarkBg.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
