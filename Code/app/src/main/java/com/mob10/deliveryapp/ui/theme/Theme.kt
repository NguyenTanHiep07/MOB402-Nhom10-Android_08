package com.mob10.deliveryapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

import androidx.compose.ui.graphics.Color
import android.app.Activity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = UthPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = UthPrimaryContainer,
    onPrimaryContainer = UthPrimaryDark,
    secondary = UthSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = UthSecondaryContainer,
    onSecondaryContainer = UthOnSurface,
    background = UthBackground,
    onBackground = UthOnSurface,
    surface = UthSurface,
    onSurface = UthOnSurface,
    onSurfaceVariant = UthOnSurfaceVariant,
    outline = UthOutline,
    outlineVariant = UthOutlineVariant,
    error = UthError
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB7B9FF),
    onPrimary = Color(0xFF1F1B62),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E0FF),
    secondary = Color(0xFFFFB787),
    onSecondary = Color(0xFF552000),
    secondaryContainer = Color(0xFF7A3500),
    onSecondaryContainer = Color(0xFFFFDBBF),
    background = Color(0xFF10131C),
    onBackground = Color(0xFFE8EAF2),
    surface = Color(0xFF171A24),
    onSurface = Color(0xFFE8EAF2),
    onSurfaceVariant = Color(0xFFB9BBC8),
    outline = Color(0xFF8D909F),
    outlineVariant = Color(0xFF414452),
    error = Color(0xFFFFB4AB)

)

@Composable
fun Android08Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,

        content = content
    )
}
