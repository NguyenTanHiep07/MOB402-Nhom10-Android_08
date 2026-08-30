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
    surfaceContainerLow = UthSurfaceContainerLow,
    surfaceContainerHighest = UthSurfaceContainerHighest,
    outline = UthOutline,
    outlineVariant = UthOutlineVariant,
    error = UthError,
    errorContainer = UthErrorContainer
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5BE0AF),
    onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF075B42),
    onPrimaryContainer = Color(0xFFBDF4DE),
    secondary = Color(0xFFFFB77E),
    onSecondary = Color(0xFF552000),
    secondaryContainer = Color(0xFF743500),
    onSecondaryContainer = Color(0xFFFFDCC2),
    background = Color(0xFF0E1713),
    onBackground = Color(0xFFE2EEE8),
    surface = Color(0xFF15211C),
    onSurface = Color(0xFFE2EEE8),
    onSurfaceVariant = Color(0xFFB5C6BE),
    outline = Color(0xFF82958C),
    outlineVariant = Color(0xFF35483F),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A)
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
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GoDropTypography,
        shapes = GoDropShapes,
        content = content
    )
}
