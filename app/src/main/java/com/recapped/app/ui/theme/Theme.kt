package com.recapped.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity

private val DarkColors = darkColorScheme(
    primary = RecappedColors.BrandOrange,
    onPrimary = RecappedColors.OnSurface,
    secondary = RecappedColors.BrandRed,
    onSecondary = RecappedColors.OnSurface,
    tertiary = RecappedColors.BrandGold,
    background = RecappedColors.Background,
    onBackground = RecappedColors.OnSurface,
    surface = RecappedColors.Background,
    onSurface = RecappedColors.OnSurface,
    error = RecappedColors.Error,
    onError = RecappedColors.OnSurface
)

@Composable
fun RecappedTheme(
    darkTheme: Boolean = true, // Recapped es siempre dark
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = DarkColors,
        typography = RecappedTypography,
        content = content
    )
}
