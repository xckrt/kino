package com.example.kino.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
private val DarkColorScheme = darkColorScheme(
    primary = GoldNeon,
    onPrimary = Black,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = White,
    onSurface = White,
    surfaceVariant = Color(0xFF2C2C2C),
    error = Color(0xFFCF6679)
)
private val LightColorScheme = lightColorScheme(
    primary = GoldDeep,
    onPrimary = White,
    background = LightBackground,
    surface = LightSurface,
    onBackground = Black,
    onSurface = Black,
    surfaceVariant = Color(0xFFE0E0E0),
    error = Color(0xFFB00020)
)

@Composable
fun KinoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val targetColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val primary by animateColorAsState(targetColorScheme.primary, tween(600), label = "primary")
    val background by animateColorAsState(targetColorScheme.background, tween(600), label = "background")
    val surface by animateColorAsState(targetColorScheme.surface, tween(600), label = "surface")
    val onPrimary by animateColorAsState(targetColorScheme.onPrimary, tween(600), label = "onPrimary")
    val onBackground by animateColorAsState(targetColorScheme.onBackground, tween(600), label = "onBackground")
    val onSurface by animateColorAsState(targetColorScheme.onSurface, tween(600), label = "onSurface")
    val surfaceVariant by animateColorAsState(targetColorScheme.surfaceVariant, tween(600), label = "surfaceVariant")

    val animatedColorScheme = targetColorScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        background = background,
        surface = surface,
        onBackground = onBackground,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = animatedColorScheme,
        content = content
    )
}