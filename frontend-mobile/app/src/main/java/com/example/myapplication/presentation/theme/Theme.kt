package com.example.myapplication.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Our color scheme using AppColors
private val LightColorScheme = lightColorScheme(
    primary = AppColors.blue500,
    onPrimary = AppColors.white,
    primaryContainer = AppColors.blue100,
    onPrimaryContainer = AppColors.blue900,
    secondary = AppColors.teal500,
    onSecondary = AppColors.white,
    secondaryContainer = AppColors.teal100,
    onSecondaryContainer = AppColors.teal700,
    tertiary = AppColors.blue700,
    background = AppColors.gray50,
    surface = AppColors.white,
    onSurface = AppColors.gray800,
    surfaceVariant = AppColors.gray100,
    outline = AppColors.gray200,
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.blue300,
    onPrimary = AppColors.white,
    primaryContainer = AppColors.blue900,
    onPrimaryContainer = AppColors.blue100,
    secondary = AppColors.teal300,
    onSecondary = AppColors.white,
    secondaryContainer = AppColors.teal900,
    onSecondaryContainer = AppColors.teal100,
    tertiary = AppColors.blue500,
    background = AppColors.gray800,
    surface = AppColors.gray700,
    onSurface = AppColors.white,
    surfaceVariant = AppColors.gray700,
    outline = AppColors.gray500,
)

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
} 